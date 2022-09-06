package de.fhac.mazenet.server.manager;

import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.*;
import de.fhac.mazenet.server.networking.Connection;
import de.fhac.mazenet.server.networking.MazeComMessageFactory;
import de.fhac.mazenet.server.userinterface.GameController;
import de.fhac.mazenet.server.userinterface.UI;
import org.apache.commons.cli.*;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VisualManager implements GameController {
    private final boolean ssl = true;
    //TODO: use parameter
    private final String host = "localhost";
    UI ui;
    public static VisualManager instance;
    private int id;
    private Connection connection;
    private boolean connected;
    private boolean gameInitilised;
    private List<Player> player;

    private VisualManager() {
        id = -1;
        Socket c;
    }

    public static void main(String[] argv){
        VisualManager visualManager = VisualManager.getInstance();
        visualManager.parsArgs(argv);
        visualManager.connect();
        visualManager.ui = Settings.USERINTERFACE;
        visualManager.id = visualManager.login("Overseer");
        System.out.println("ID erhalten: " + visualManager.id);
        visualManager.connection.setId(visualManager.id);
        visualManager.ui.init(new Board());
        while (visualManager.connected) {
            MazeCom srvMessage = visualManager.connection.receiveMessage();
            if (!(srvMessage == null)) {
                System.out.println(srvMessage.getMessagetype().value() + " received");
                switch (srvMessage.getMessagetype()) {
                    case LOGIN:
                        break;
                    case LOGINREPLY:
                        break;
                    case AWAITMOVE:
                        break;
                    case MOVE:
                        break;
                    case MOVEINFO:
                        break;
                    case GAMESTATUS:
                        GameStatusData gs = srvMessage.getGameStatusMessage();
                        if (!visualManager.gameInitilised) {
                            visualManager.parsePlayerStats(gs.getPlayerStatus());
                            visualManager.ui.init(new Board(gs.getBoardAfterMove()));
                            visualManager.ui.updatePlayerStatistics(visualManager.player, gs.getPlayerId());
                            visualManager.gameInitilised = true;
                        } else {
                            visualManager.ui.updatePlayerStatistics(visualManager.player, gs.getPlayerId());
                            visualManager.ui.displayMove(gs.getSendMove(), new Board(gs.getBoardAfterMove()), Settings.MOVEDELAY, Settings.SHIFTDELAY, gs.isTreasureReached());
                            visualManager.parsePlayerStats(gs.getPlayerStatus());
                        }
                        break;
                    case CONTROLSERVER:
                        break;
                    case ACCEPT:
                        break;
                    case WIN:
                        WinMessageData winMsg = srvMessage.getWinMessage();
                        visualManager.ui.gameEnded(new Player(winMsg.getWinner().getId(), winMsg.getWinner().getValue(), null));
                        break;
                    case DISCONNECT:
                        visualManager.connected = false;
                        break;
                }
            } else {
                System.err.println("keine Nachricht erhalten");
                // srv message null
                visualManager.connected = false;
            }

        }
        System.exit(0);
    }

    public static VisualManager getInstance() {
        if (instance == null) {
            instance = new VisualManager();
        }
        return instance;
    }

    private void connect() {
        Socket c;
        connected = true;
        try {
            if (ssl) {
                System.out.println("Starte SSL Verbindung");
                System.setProperty("javax.net.ssl.trustStore", Settings.SSL_CERT_STORE);
                System.setProperty("javax.net.ssl.trustStorePassword", Settings.SSL_CERT_STORE_PASSWD);
                SocketFactory sslSocketFactory = SSLSocketFactory.getDefault();
                c = sslSocketFactory.createSocket(host, 5432);
            } else {
                System.out.println("Starte TCP Verbindung");
                c = new Socket(host, 5123);
            }
            connection = new Connection(c, null);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            connected = false;
            System.exit(1);
        }
    }

    public void parsArgs(String[] args) {
        Options availableOptions = new Options();
        availableOptions.addOption("c", true, "path to property file for configuration");
        availableOptions.addOption("h", false, "displays this help message");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(availableOptions, args);
            if (cmd.hasOption("h")) {
                printCMDHelp(0, availableOptions);
            }
            String configPath = cmd.getOptionValue("c");
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

    private List<Player> parsePlayerStats(List<GameStatusData.PlayerStatus> playerStatus) {
        player = new ArrayList<>();
        for (GameStatusData.PlayerStatus playerStat : playerStatus) {
            Player player = new Player(playerStat.getPlayerID(), playerStat.getPlayerName(), null);
            //get treausers working....
            Stack<Treasure> treasureStack = new Stack<>();
            treasureStack.addAll(playerStat.getTreasures());
            treasureStack.add(playerStat.getCurrentTreasure());
            player.setTreasure(treasureStack);
            this.player.add(player);
        }
        return this.player;
    }

    @Override
    public void startGame() {
        MazeCom message = prepareServerControl(Settings.NUMBER_OF_PLAYERS, "START");
        connection.sendMessage(message, false);
        gameInitilised = false;
        System.out.println("Start Message send");
    }

    @Override
    public void stopGame() {
        MazeCom message = prepareServerControl(Settings.NUMBER_OF_PLAYERS, "STOP");
        connection.sendMessage(message, false);
        ui.gameEnded(null);
        System.out.println("Stop Message send");
    }

    private MazeCom prepareServerControl(int number_of_players, String type) {
        ObjectFactory of = new ObjectFactory();
        MazeCom message = of.createMazeCom();
        message.setId(this.id);
        message.setMessagetype(MazeComMessagetype.CONTROLSERVER);
        message.setControlServerMessage(of.createControlServerData());
        message.getControlServerMessage().setCommand(type);
        message.getControlServerMessage().setPlayerCount(number_of_players);
        return message;
    }

    private int login(String name) {
        System.out.println("========================================");
        System.out.println("Sende Login");
        MazeCom loginMessage = MazeComMessageFactory.createLoginMessage(name);
        loginMessage.getLoginMessage().setRole(ClientRole.MANAGER);
        connection.sendMessage(loginMessage, false);
        System.out.println("========================================");
        System.out.print("Warte auf Reply....");
        MazeCom loginAnswer = this.connection.receiveMessage();
        System.out.println("erhalten");
        System.out.println("========================================");
        if (loginAnswer.getMessagetype() == MazeComMessagetype.LOGINREPLY) {
            return loginAnswer.getLoginReplyMessage().getNewID();
        } else {
            return -1;
        }
    }

}
