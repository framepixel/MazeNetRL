package de.fhac.mazenet.server.userinterface.CLIUI;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.game.Position;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.tools.Algorithmics;
import de.fhac.mazenet.server.userinterface.UI;

import java.util.List;

/**
 * Featurepreview für eine CommandlineUI
 * <p>
 * Hauptsächlich als Vorbereitung für UnitTests/Automatisierte Tests
 * <p>
 * Alphastatus!!! Erwarte Fehler.
 */
public class CommandLineUI implements UI {

    private static CommandLineUI instance;
    private Board recentBoard;
    private int currentPlayerId;
    private UserPrompt prompt;

    /**
     * privater Constructor wegen Singleton
     */
    private CommandLineUI() {
        prompt = new UserPrompt(this);
    }

    public static UI getInstance() {
        if (instance == null)
            instance = new CommandLineUI();
        return instance;
    }

    @Override
    public void displayMove(MoveMessageData moveMessage, Board boardAfterMove, long moveDelay, long shiftDelay, boolean treasureReached) {
        try {
            System.out.print("Schiebt in " + new Position(moveMessage.getShiftPosition()) + "...");
            Thread.sleep(shiftDelay);
            System.out.println("fertig");
            recentBoard.proceedShift(moveMessage);
            List<Position> path = Algorithmics.findPath(recentBoard, recentBoard.findPlayer(currentPlayerId), new Position(moveMessage.getNewPinPos()));
            for (Position p : path) {
                System.out.print(p + " -> ");
                Thread.sleep(moveDelay);
            }
            if (treasureReached) {
                System.out.println("Schatz gefunden");
            } else {
                System.out.println("keinen Schatz gefunden");
            }
            recentBoard = (Board) boardAfterMove.clone();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayerStatistics(List<Player> statistics, Integer currentPlayerID) {
        this.currentPlayerId = currentPlayerID;
        for (Client client : statistics) {
            System.out.println(client);
        }
    }

    @Override
    public void init(Board board) {
        recentBoard = (Board) board.clone();
        new Thread(prompt).start();
        //System.out.println("waitForConnections");
    }

/*    @Override
    public void setGame(Game game) {
        this.game = game;
        System.out.println("setGame");
        UserPrompt userPrompt = new UserPrompt(instance);
        new Thread(userPrompt).start();
    }*/

    @Override
    public void gameEnded(Client winner) {
        if (winner != null) {
            System.out.println("Gewinner ist " + winner.getName() + " (ID:" + winner.getId() + ")");
        } else {
            System.out.println("Spiel wurde abgebrochen");
        }
    }

    @Override
    public String toString() {
        return "CLIUI";
    }

    public void startGame() {
        Server.getInstance().parsArgs();
        Server.getInstance().startGame();
    }

    public void stopGame() {
        Server.getInstance().startGame();
    }

}
