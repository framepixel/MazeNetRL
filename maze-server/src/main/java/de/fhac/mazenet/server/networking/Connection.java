/*
 * Connection regelt die serverseitige Protokollarbeit
 */
package de.fhac.mazenet.server.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Deque;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLSocket;
import javax.xml.bind.UnmarshalException;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Game;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.Errortype;
import de.fhac.mazenet.server.generated.MazeCom;
import de.fhac.mazenet.server.generated.MazeComMessagetype;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.timeouts.TimeOutManager;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;

public class Connection {

    private Socket socket;
    private Future<Client> clientFuture;
    private XmlInputStream inFromClient;
    private XmlOutputStream outToClient;
    private TimeOutManager timeOutManager;
    private Game currentGame;
    private int id;
    private final ExecutorService executor;

    /**
     * Speicherung des Sockets und oeffnen der Streams
     *
     * @param socket Socket der Verbindung
     */
    public Connection(Socket socket, Game game) {
        this.socket = socket;
        this.currentGame = game;
        try {
            this.inFromClient = new XmlInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            System.err.println(Messages.getString("Connection.couldNotOpenInputStream"));
        }
        try {
            this.outToClient = new XmlOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            System.err.println(Messages.getString("Connection.couldNotOpenOutputStream"));
        }
        if (currentGame != null) {
            this.timeOutManager = currentGame.getTimeOutManager();
        } else {
            this.timeOutManager = null;
        }
        executor = Executors.newFixedThreadPool(4);
    }

    public boolean wantMutualSSL() {
        try {
            if (socket instanceof SSLSocket) {
                SSLSocket sslSocket = (SSLSocket) socket;
                sslSocket.setNeedClientAuth(true);
                sslSocket.startHandshake();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Debug.print(Messages.getString("Connection.sslClientAuthfailed"), DebugLevel.DEFAULT);
            return false;
        }
    }

    public InetAddress getIPAdress() {
        return socket.getInetAddress();
    }

    public Client getClient() throws ExecutionException, InterruptedException {
        if (!clientFuture.isDone()) return null;
        return clientFuture.get();
    }

    // TODO Muss nach Login unbedingt gesetzt werden!!!!!!!
    // SEHR UNSCHOEN!!!!
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Allgemeines Senden einer fertigen MazeCom-Instanz
     */
    public void sendMessage(MazeCom mazeCom, boolean withTimer) {
        // Timer starten, der beim lesen beendet wird
        // Ablauf Timer = Problem User
        if (withTimer)
            this.timeOutManager.startSendMessageTimeOut(this.id, this);
        try {
            mazeCom.setId(this.id);
            this.outToClient.write(mazeCom);
        } catch (IOException e) {
            Debug.print(Messages.getString("Connection.playerExitedUnexpected"), DebugLevel.DEFAULT);
            // entfernen des Spielers
            Server.getInstance().removeConnection(this);
        }
    }

    /**
     * Conveniance, ruft sendMessage(message,false) auf
     *
     * @param message Nachricht die übermittelt werden soll
     */
    public void sendMessage(MazeCom message) {
        sendMessage(message, false);
    }


    /**
     * Allgemeines empfangen einer MazeCom-Instanz
     *
     * @return eingelesenes MazeCom
     */
    public MazeCom receiveMessage() {
        MazeCom result = null;
        try {
            result = this.inFromClient.readMazeCom();
        } catch (UnmarshalException e) {
            Throwable xmle = e.getLinkedException();
            Debug.print(Messages.getString("Connection.XmlError") + xmle.getMessage(), DebugLevel.DEFAULT);
        } catch (IOException e) {
            Debug.print(Messages.getString("Connection.playerExitedUnexpected"), DebugLevel.DEFAULT);
            // entfernen des Spielers
            Server.getInstance().removeConnection(this);
        } catch (OutOfMemoryError e) {
            Debug.print(Messages.getString("Connection.MemoryLeak"), DebugLevel.DEFAULT);
            // TODO Wenn zulange Nachrichten geschickt werden
            Server.getInstance().removeConnection(this);
        } catch (IllegalArgumentException e) {
            Debug.print(Messages.getString("Connection.NegativeMessageSize"), DebugLevel.DEFAULT);
            // TODO Wenn negative Zahl als Länge geschickt wird
            Server.getInstance().removeConnection(this);
        }
        if (this.timeOutManager != null)
            this.timeOutManager.stopSendMessageTimeOut(this.id);
        return result;
    }

    /**
     * Allgemeines Erwarten eines Login
     *
     * @return Neuer Client, bei einem Fehler jedoch null
     */
    public Future<Client> login(Deque<Integer> availablePlayerIDs) {
        // ein Thread fuer einen Login. Weitere Logins in rufendender Schleife
        clientFuture = executor.submit(new LoginTask(this, availablePlayerIDs, currentGame));
        return clientFuture;
    }

    /**
     * Anfrage eines Zuges beim Spieler
     *
     * @param board aktuelles Spielbrett
     * @return Valieder Zug des Spielers oder NULL
     */
    public MoveMessageData awaitMove(HashMap<Integer, Player> players, Board board, int tries) {
        if (players.get(id) != null && tries < Settings.MOVETRIES) {
            sendMessage(MazeComMessageFactory.createAwaitMoveMessage(players, id, board),
                    true);
            MazeCom result = this.receiveMessage();
            if (result != null && result.getMessagetype() == MazeComMessagetype.MOVE) {
                if (currentGame.getBoard().validateTransition(result.getMoveMessage(), id)) {
                    sendMessage(MazeComMessageFactory.createAcceptMessage(id, Errortype.NOERROR), false);
                    return result.getMoveMessage();
                }
                // nicht regelkonform
                sendMessage(MazeComMessageFactory.createAcceptMessage(id, Errortype.ILLEGAL_MOVE), false);
                return awaitMove(players, board, ++tries);
            }
            // XML nicht verwertbar
            sendMessage(MazeComMessageFactory.createAcceptMessage(id, Errortype.AWAIT_MOVE), false);
            return awaitMove(players, board, ++tries);

        } else {
            disconnect(Errortype.TOO_MANY_TRIES);
            return null;
        }
    }

    // /**
    //  * sendet dem Spieler den Namen des Gewinners sowie dessen ID und das
    //  * Schlussbrett
    //  *
    //  * @param winnerId ID des Gewinners
    //  * @param name Name des Gewinners
    //  * @param board finales Board
    //  */
    // public void sendWin(int winnerId, String name, Board board, List<StatisticData> stats) {
    //     this.sendMessage(MazeComMessageFactory.createWinMessage(this.id, winnerId, name, board,stats), false);
    // }

    /**
     * Senden, dass Spieler diconnected wurde
     */
    public void disconnect(Errortype errortype) {
        try {
            Future<Client> clientFuture = this.clientFuture;
            String name;
            if (clientFuture.isDone())
                name = clientFuture.get().getName();
            else
                name = "<not logged in>";
            this.sendMessage(
                    MazeComMessageFactory.createDisconnectMessage(this.id, name, errortype),
                    false);

        } catch (InterruptedException |
                ExecutionException e) {
            Debug.print(Messages.getString("Connection.LoginitusInterruptus"), DebugLevel.DEFAULT);
        }
        terminateConnection();
    }

    private void terminateConnection() {
        try {
            this.inFromClient.close();
            this.outToClient.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.getInstance().removeConnection(this);
        Debug.print(this.getIPAdress() + " wurde entfernt", DebugLevel.DEFAULT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return id == that.id &&
                Objects.equals(socket, that.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, id);
    }


}
