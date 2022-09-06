package de.fhac.mazenet.server.manager;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.generated.ControlServerData;
import de.fhac.mazenet.server.generated.MazeCom;
import de.fhac.mazenet.server.networking.Connection;

/**
 * Wartet aus Gamecontroll Messages
 */

public class ManagerListener extends Thread {

    private Connection connection;
    private boolean connected;

    public ManagerListener(Connection connection) {
        super();
        this.connection = connection;
        this.connected = true;

    }

    @Override
    public void run() {
        while (connected) {
            System.out.println("... wait for message from Manager");
            MazeCom message = connection.receiveMessage();
            if (message != null) {
                System.out.println("Manager hat " + message.getMessagetype() + " empfangen");
                switch (message.getMessagetype()) {
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
                        break;
                    case CONTROLSERVER:
                        contorlServer(message.getControlServerMessage());
                        break;
                    case ACCEPT:
                        break;
                    case WIN:
                        break;
                    case DISCONNECT:
                        connected = false;
                        break;
                }
            } else {
                //something went wrong
                connected = false;
                Server.getInstance().removeConnection(connection);
            }
        }

    }

    private void contorlServer(ControlServerData controlServerMessage) {
        if (controlServerMessage.getPlayerCount() != null) {
            Settings.NUMBER_OF_PLAYERS = controlServerMessage.getPlayerCount();
        }

        if (controlServerMessage.getCommand().equals("START")) {
            Server.getInstance().startGame();
        } else if (controlServerMessage.getCommand().equals("STOP")) {
            Server.getInstance().stopGame();
        }
    }


}
