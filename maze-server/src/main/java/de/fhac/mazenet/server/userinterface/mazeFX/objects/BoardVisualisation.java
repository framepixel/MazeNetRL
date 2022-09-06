package de.fhac.mazenet.server.userinterface.mazeFX.objects;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.generated.MoveMessageData;
import javafx.scene.layout.Pane;

import java.util.concurrent.CountDownLatch;

public interface BoardVisualisation {
     void animateMove(MoveMessageData moveMessage, Board targetBoard, long moveDelay, long shiftDelay,
                             boolean treasureReached, CountDownLatch lock, Integer playerID) ;
     void initFromBoard(Board startBoard);

    void focusLost() ;

    Pane getRoot();

    void clearBoard();

}
