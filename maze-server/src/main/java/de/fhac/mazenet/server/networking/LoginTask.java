package de.fhac.mazenet.server.networking;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Game;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.ClientRole;
import de.fhac.mazenet.server.generated.Errortype;
import de.fhac.mazenet.server.generated.MazeCom;
import de.fhac.mazenet.server.generated.MazeComMessagetype;
import de.fhac.mazenet.server.manager.Manager;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;

import java.util.Deque;
import java.util.concurrent.Callable;

public class LoginTask implements Callable<Client> {

    private Connection connection;
    private Client client;
    private int id;
    private Deque<Integer> availablePlayers;
    private Game currentGame;

    public LoginTask(Connection connection, Deque<Integer> availablePlayers, Game currentGame) {
        this.id = -1;
        this.connection = connection;
        this.availablePlayers = availablePlayers;
        this.currentGame = currentGame;
    }

    /**
     * ersetze alle Zeichen ausser den erlaubten:
     * \w A word character: [a-zA-Z_0-9]
     * \p{Punct} Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
     * ausserdem äöüßÄÖÜ und Leerzeichen
     *
     * @param name Name des Clients
     * @return bereinigter Clientname
     */
    private String cleanUpName(String name) {
        String nameBuffer = name.replaceAll("[^\\w äüöÜÖÄß\\p{Punct}]", "");
        return nameBuffer.substring(0, Math.min(Settings.MAX_NAME_LENGTH, nameBuffer.length()));
    }

    @Override
    public Client call() {
        MazeCom loginMessage = this.connection.receiveMessage();
        int failCounter = 0;
        while (failCounter < Settings.LOGINTRIES) {
            // Test ob es sich um eine LoginNachricht handelt
            if (loginMessage != null && loginMessage.getMessagetype() == MazeComMessagetype.LOGIN) {
                // default role auf player setzen (waere schoener im xsd geloest)
                if (loginMessage.getLoginMessage().getRole() == null) {
                    loginMessage.getLoginMessage().setRole(ClientRole.PLAYER);
                }
                //check witch role:
                switch (loginMessage.getLoginMessage().getRole()) {

                    case MANAGER:
                        System.out.print("VisualManager tries to log in...");
                        if (connection.wantMutualSSL()) {
                            System.out.println("successful");
                            this.id = 99;
                            Manager manager = new Manager(this.id, cleanUpName(loginMessage.getLoginMessage().getName()), connection);
                            this.client = manager;
                            Server.getInstance().setManager(this.client);
                            manager.startListening();
                        } else {
                            System.out.println("not succesful");
                            connection.disconnect(Errortype.ERROR);
                            return null;
                        }
                        break;
                    case SPECTATOR:
                        System.out.println("Spectator logged in");
                        this.id = Server.getInstance().getNextSpectatorId();
                        this.client = new Client(this.id, cleanUpName(loginMessage.getLoginMessage().getName()), ClientRole.SPECTATOR, connection);
                        break;
                    case PLAYER:
                        System.out.print("player tries to login...");
                        System.out.println();
                        if (currentGame != null && currentGame.getGameStatus() == Game.Status.WAITFORPLAYER) {
                            System.out.println("successful");
                            this.id = this.availablePlayers.pop();

                            String awaitedPlayer = " (" + (Settings.NUMBER_OF_PLAYERS - availablePlayers.size())
                                    + "/" + Settings.NUMBER_OF_PLAYERS + ")";
                            System.out.println("Spieler " + awaitedPlayer + " verbunden");

                            if(!connection.getIPAdress().isLoopbackAddress()){
                                Server.getInstance().addConnectedIP(connection.getIPAdress().getHostAddress());
                            }

                            this.client = new Player(this.id, cleanUpName(loginMessage.getLoginMessage().getName()), connection);
                            if (availablePlayers.size() == 0) {
                                System.out.println("Alle Spieler verbunden");
                                currentGame.stopWaitingForPlayers();
                            }
                        } else {
                            //TODO: Spezifischer (xsd-Anpassung)
                            connection.disconnect(Errortype.ERROR);
                            System.out.println("not succesful");
                            return new Client(-1, "notLoggedIn", ClientRole.SPECTATOR, this.connection);
                        }
                        // Spieler darf sich nicht einloggen, weil nicht gestartet oder schon laeuft...
                        // Nachrichtenrueckgabe?
                        break;
                }
                if (this.id != -1) {
                    connection.setId(this.id);
                    this.connection.sendMessage(MazeComMessageFactory.createLoginReplyMessage(this.id), false);
                    Debug.print(
                            String.format(Messages.getString("LoginThread.successful"), client.getId(), client.getName()),
                            DebugLevel.DEFAULT);
                    return this.client;// Verlassen des Threads
                }
            }
            // Sende Fehler
            this.connection.sendMessage(MazeComMessageFactory.createAcceptMessage(-1, Errortype.AWAIT_LOGIN), true);
            failCounter++;
            // nach einem Fehler auf den naechsten Versuch warten
            loginMessage = this.connection.receiveMessage();
        }
        // Verlassen mit schwerem Fehlerfall
        // ID wird wieder freigegeben
        Debug.print(String.format(Messages.getString("LoginThread.failed"), this.id), DebugLevel.DEFAULT);
        //availablePlayers.push(this.id);
        this.connection.disconnect(Errortype.TOO_MANY_TRIES);
        return new Client(-1, "notLoggedIn", ClientRole.SPECTATOR, this.connection);
    }
}
