package de.fhac.mazenet.server;

import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Game;
import de.fhac.mazenet.server.generated.ClientRole;
import de.fhac.mazenet.server.generated.MazeCom;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.networking.Connection;
import de.fhac.mazenet.server.networking.TCPConnectionCreationTask;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;
import de.fhac.mazenet.server.userinterface.GameController;
import de.fhac.mazenet.server.userinterface.UI;
import org.apache.commons.cli.*;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Server implements GameController {

    private static Game currentGame;
    private static Server instance;
    private ServerSocket serverSocket;
    private ServerSocket sslServerSocket;
    private UI userinterface;
    private boolean ssl;
    private String[] args;
    private List<Future<Client>> connectedClients;
    private int availableSpectatorId;
    private Deque<Integer> availablePlayerIds;
    private ArrayList<String> connectedIPs;
    private Client manager;
    private String configPath;

    private Server() {
        connectedClients = Collections.synchronizedList(new ArrayList<>());
        availableSpectatorId = 5;
        configPath = System.getProperty("user.home") + "/.mazenet.prop";
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.args = args;
        server.parsArgs();
        server.userinterface = Settings.USERINTERFACE;
        Locale.setDefault(Settings.LOCALE);
        server.userinterface.init(new Board());
        server.waitForConnections();
    }

    public void parsArgs() {
        Options availableOptions = new Options();
        availableOptions.addOption("c", true, "path to property file for configuration");
        availableOptions.addOption("h", false, "displays this help message");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(availableOptions, args);
            if (cmd.hasOption("h")) {
                printCMDHelp(0, availableOptions);
            }
            var config = cmd.getOptionValue("c");
            if (config != null) {
                configPath = config;
            }
            // Wenn mit null aufgerufen, werden Standardwerte benutzt
            Settings.reload(configPath);
        } catch (ParseException e) {
            printCMDHelp(1, availableOptions);
        }
    }

    private void printCMDHelp(int exitCode, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar maze-server.jar [options]\nAvailable Options:", options);
        System.exit(exitCode);
    }

    private void printServerAddresses(ServerSocket serverSocket, boolean ssl) {
        int serverPort = serverSocket.getLocalPort();
        // Server verwenden die Adresse 0.0.0.0 oft als Platzhalter für alle IP-Adressen des
        // lokalen Computers. Wenn der Server zwei IP-Adressen hat, z. B. 192.168.5.10
        // und 10.17.0.12, und der Server auf diese Adresse reagiert, ist er auf beiden
        // IP-Adressen erreichbar.
        // https://de.wikipedia.org/wiki/0.0.0.0
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface n = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAdresses = n.getInetAddresses();
            while (inetAdresses.hasMoreElements()) {
                InetAddress inetAddress = inetAdresses.nextElement();
                // nur die IPv4-Adressen ausgeben
                if (inetAddress instanceof Inet4Address)
                    Debug.print(String.format(Messages.getString("Server.listening"), inetAddress.getHostAddress(), serverPort, (!ssl) ? "no " : ""), DebugLevel.DEFAULT);
            }
        }
    }

    /**
     * Auf TCP Verbindungen warten und den Spielern die Verbindung ermoeglichen
     * Speichert alle eincommenden Verbindungen in connectedClients
     * Spieler werden in prepareGame gefiltert
     */
    public void waitForConnections() {
        Debug.addDebugger(System.out, Settings.DEBUGLEVEL);
        Debug.print(Messages.getString("Game.initFkt"), DebugLevel.VERBOSE);
        // Socketinitialisierung aus dem Constructor in waitForConnections verschoben. Sonst
        // Errors wegen Thread.
        // waitForConnections wird von run (also vom Thread) aufgerufen, im Gegesatz zum
        // Constructor
        try {
            // unverschluesselt
            serverSocket = new ServerSocket(Settings.PORT);
            ssl = false;
            if (!Settings.SSL_CERT_STORE.equals("")) {
                // verschluesselt
                // Setup SSL
                if (new File(Settings.SSL_CERT_STORE).exists()) {
                    System.setProperty("javax.net.ssl.keyStorePassword", Settings.SSL_CERT_STORE_PASSWD);
                    System.setProperty("javax.net.ssl.keyStore", Settings.SSL_CERT_STORE);
                    //TODO: In Zukunft trennen, noch keine realistische Bedrohung
                    System.setProperty("javax.net.ssl.trustStorePassword", Settings.SSL_CERT_STORE_PASSWD);
                    System.setProperty("javax.net.ssl.trustStore", Settings.SSL_CERT_STORE);
                    sslServerSocket = SSLServerSocketFactory.getDefault().createServerSocket(Settings.SSL_PORT);
                    ssl = true;
                } else {
                    Debug.print(Messages.getString("Game.certStoreNotFound"), DebugLevel.DEFAULT);
                }
            } else {
                Debug.print(Messages.getString("Game.noSSL"), DebugLevel.DEFAULT);
            }
        } catch (IOException e) {
            Debug.print(Messages.getString("Game.portUsed"), DebugLevel.DEFAULT);
        }
        connectedIPs = new ArrayList<>();

        // Tasks vorbereiten fuer SSL und unverschluesselt
        final CyclicBarrier barrier = new CyclicBarrier(2);
        Socket mazeClientSocket = null;
        TCPConnectionCreationTask waitForConnectionTask = new TCPConnectionCreationTask(serverSocket, barrier);
        TCPConnectionCreationTask waitForSSLConnectionTask = new TCPConnectionCreationTask(sslServerSocket, barrier);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<Socket> noSSLSocketFuture = null;
        Future<Socket> sslSocketFuture = null;

        printServerAddresses(serverSocket, false);
        if (ssl) printServerAddresses(sslServerSocket, true);
        while (true) {
            try {
                cleanUpConnections();
                mazeClientSocket = null;
                Debug.print(Messages.getString("Game.waitingForConnections"), DebugLevel.DEFAULT);
                // Neustart des benutzten serverSockets
                //barrier.reset(); rausnehmen wegen Exception?
                if (noSSLSocketFuture == null || noSSLSocketFuture.isDone()) {
                    noSSLSocketFuture = pool.submit(waitForConnectionTask);
                }
                if (ssl && (sslSocketFuture == null || sslSocketFuture.isDone())) {
                    sslSocketFuture = pool.submit(waitForSSLConnectionTask);
                }
                // Warten bis Verbindung kommt
                barrier.await();
                cleanUpConnections();
                // An dieser Stelle existiert (oder existiert in unmittelbarer Zukunft) ein neuer Socket, weil ein Client verbunden wurde
                // Nach der ersten Verbindung soll der Timeout gestartet werden
                if (currentGame != null && currentGame.getGameStatus() == Game.Status.WAITFORPLAYER) {
                    // TODO: Check: wenn sich kein Spieler verbindet, sollte ewig gewartet werden
                    // Es wird noch nicht geprüft ob es sich bei der eingehenden Verbindung um einen Spieler handelt
                    currentGame.getTimeOutManager().startLoginTimeOut();
                }
                try {
                    // Fuer megaeklige Racekondition
                    // Falls TCPConnectionCreationTask.call:barrier.await() langsamer ist als hier nach der Barrier von oben
                    boolean fuckRaceConditions = true;
                    while (fuckRaceConditions) {
                        // Abrufen des Sockets für die neue Verbindung
                        if (noSSLSocketFuture.isDone()) {
                            fuckRaceConditions = false;
                            mazeClientSocket = noSSLSocketFuture.get();
                        }
                        // SSLStatus.isDone() liefert bei nicht konfiguriertem SSL
                        // auch true zurück, deshalb Flag ssl
                        if (ssl && sslSocketFuture.isDone()) {
                            fuckRaceConditions = false;
                            mazeClientSocket = sslSocketFuture.get();
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (mazeClientSocket != null) {
                    // Nur ein Verbindung pro IP erlauben (Ausnahme localhost)
                    InetAddress inetAddress = mazeClientSocket.getInetAddress();
                    String ip = inetAddress.getHostAddress();
                    if (!connectedIPs.contains(ip)) {
                        Connection connection = new Connection(mazeClientSocket, currentGame);

                        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        // Verbindung aufgebaut, zuerst login durchführen
                        // asynchron
                        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        connectedClients.add(connection.login(availablePlayerIds));
                    } else {
                        Debug.print(String.format(Messages.getString("Game.HostAlreadyConnected"), ip),
                                DebugLevel.DEFAULT);
                    }
                } else
                    System.out.println("mazeClienSocket==null");
            } catch (InterruptedException e) {
                Debug.print(Messages.getString("Game.playerWaitingTimedOut"), DebugLevel.DEFAULT);
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

    }

    public void cleanUpConnections() {
        this.removeConnection(null);
    }

    public void addConnectedIP(String ip) {
        connectedIPs.add(ip);
    }

    @Override
    public void startGame() {
        currentGame = new Game();
        currentGame.setUserinterface(this.userinterface);
        currentGame.start();
    }

    @Override
    public void stopGame() {
        if (currentGame != null) {
            currentGame.stopGame();
        }
        currentGame = null;
    }

    public void removeConnection(Connection toRemove) {
        try {
            boolean removed;
            // Nicht waehrend Iteration Elemente rausloeschen!
            // Finden des zu loeschenden Clients
            List<Future<Client>> clientsToBeRemoved = connectedClients.stream()
                    .filter(Future::isDone)
                    .filter((client) -> {
                        try {
                            return (client.get().getId() == -1) || (client.get().getConnectionToClient().equals(toRemove));
                        } catch (InterruptedException | ExecutionException e) {
                            //remove interupted Logins to
                            return true;
                        }
                    }).collect(Collectors.toList());
     
            // Tatsaechliches Loeschen des Clients
            for (Future<Client> clientToBeRemoved : clientsToBeRemoved) {
                removed = connectedClients.remove(clientToBeRemoved);
                if (removed) {
                    if (clientToBeRemoved.isDone()) {
                        Client client = clientToBeRemoved.get();
                        if (client != null) {
                            if (client.getRole() == ClientRole.PLAYER) {
                                connectedIPs.remove(client.getConnectionToClient().getIPAdress().getHostAddress());
                                currentGame.removePlayer(client.getId());
                            }
                            if (client.getRole() == ClientRole.MANAGER)
                                this.setManager(null);
                        }
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        System.out.println("closeServerSocket called");
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (sslServerSocket != null) {
                sslServerSocket.close();
            }
            if (sslServerSocket == null && serverSocket == null)
                Debug.print(Messages.getString("Game.serverSocketNull"), DebugLevel.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNextSpectatorId() {
        return availableSpectatorId++;
    }

    public List<Future<Client>> getConnectedClients() {
        return connectedClients;
    }

    public void resetPlayerIds() {
        List<Integer> availablePlayerIdsInitList = new ArrayList<>();
        for (int i = 1; i <= Settings.NUMBER_OF_PLAYERS; i++) {
            availablePlayerIdsInitList.add(i);
        }
        if (!Settings.TESTBOARD) {
            Collections.shuffle(availablePlayerIdsInitList);
        }
        availablePlayerIds = new ConcurrentLinkedDeque<>(availablePlayerIdsInitList);
    }

    public void setManager(Client manager) {
        this.manager = manager;
    }

    public void sendToManager(MazeCom message) {
        if (manager != null) {
            manager.getConnectionToClient().sendMessage(message);
        }
    }

    public void sendToAllConnected(MazeCom message) {
        cleanUpConnections();
        for (Future<Client> connectedClient : this.connectedClients) {
            // Nur zu verbundenen Spielern senden
            if (connectedClient.isDone()) {
                Client client = null;
                try {
                    client = connectedClient.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (client != null) {
                    client.getConnectionToClient().sendMessage(message, false);
                }
            }
        }
    }

    public String getConfigPath() {
        return configPath;
    }
}
