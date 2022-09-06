package de.fhac.mazenet.server.userinterface;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.networking.Client;

import java.util.List;

public interface UI {
    void displayMove(MoveMessageData moveMessage, Board boardAfterMove, long moveDelay,
                     long shiftDelay, boolean treasureReached);

    void updatePlayerStatistics(List<Player> statistics, Integer currentPlayerID);

    void init(Board board);

    void gameEnded(Client winner);

    @Override
    String toString();
}
