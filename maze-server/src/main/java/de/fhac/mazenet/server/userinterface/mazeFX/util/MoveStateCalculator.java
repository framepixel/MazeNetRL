package de.fhac.mazenet.server.userinterface.mazeFX.util;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Position;
import de.fhac.mazenet.server.generated.CardData;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.generated.PositionData;
import de.fhac.mazenet.server.userinterface.mazeFX.data.VectorInt2;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates different states (before shift, after shift, after pin move),
 * more precisely, (hopefully) relevant data for each of this stages.
 *
 * WARNING: Does *not* check moves for correctness!
 *
 * Unashamedly copied in parts from Game.java and MazeFX.java.
 */
public class MoveStateCalculator {

    // CONSTANTS
    /** Width of the board */
    public static final int BOARD_WIDTH = 7;
    /** Height of the board */
    public static final int BOARD_HEIGHT = 7;

    // ATTRIBUTES
    /** Move message send by a client */
    private MoveMessageData moveMessage;
    /** Current board instance */
    private Board board;
    /**
     * Cache for {@link #getShiftDelta()}
     */
    private VectorInt2 shiftDelta;
    /**
     * Cache for {@link #getShiftCardStart()}
     */
    private VectorInt2 shiftCardStart;
    /**
     * Cache for {@link #getCardsToShift()}
     */
    private List<VectorInt2> cardsToShift;
    /**
     * Cache for {@link #getPushedOutPlayersPosition()}
     */
    private VectorInt2 pushedOutPlayersPosition;
    /**
     * Cache for {@link #getPushedOutPlayers()}
     */
    private List<Integer> pushedOutPlayers;
    /**
     * Cache for {@link #getNewPlayerPosition()}
     */
    private VectorInt2 newPlayerPosition;

    /**
     * Creates a new Instance which will be able to calculate data for one player's turn.
     *
     * @param moveMessage Move message send by a client.
     * @param board Current instance of the game board.
     */
    public MoveStateCalculator(MoveMessageData moveMessage, Board board) {
        this.moveMessage = moveMessage;
        this.board = board;
    }

    /**
     * Calculates a translation vector for shifting, based on the passed shift card position
     * <p>
     * WARNING: This function does *not* check the position's validity
     * and may return unusable results for invalid values.
     *
     * @param position Position of the shift card
     * @return Translation Vector for shifted cards
     */
    private static VectorInt2 getCardShiftBy(PositionData position) {
        if (position.getCol() == 0)
            return new VectorInt2(1, 0);
        if (position.getCol() == BOARD_WIDTH - 1)
            return new VectorInt2(-1, 0);

        if (position.getRow() == 0)
            return new VectorInt2(0, 1);
        if (position.getRow() == BOARD_HEIGHT - 1)
            return new VectorInt2(0, -1);

        return new VectorInt2(0, 0); // no idea!
    }

    /**
     * Checks if a position is on the board
     *
     * @param v Position as vector
     * @return true, if it is on the board, false otherwise
     * @see #isOnBoard(int, int)
     */
    public static boolean isOnBoard(VectorInt2 v) {
        return isOnBoard(v.x, v.y);
    }

    /**
     * Checks if a position is on the board
     *
     * @param x Position's X coordinate
     * @param y Position's Y coordinate
     * @return true, if it is on the board, false otherwise
     */
    public static boolean isOnBoard(int x, int y) {
        return x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT;
    }

    /**
     * Calculates a vector which represents the translation done when shifting cards.
     *
     * @return Vector which can be added to existing cards positions for shifting.
     */
    public VectorInt2 getShiftDelta(){
        if(shiftDelta == null){
            shiftDelta = getCardShiftBy(moveMessage.getShiftPosition());
        }
        return shiftDelta;
    }

    /**
     * Calculates the shift card's start position
     * (the position from which the shift is initiated, outside of the board)
     *
     * @return Vector for the shift card position
     */
    public VectorInt2 getShiftCardStart(){
        if(shiftCardStart==null) {
            VectorInt2 shiftTo = VectorInt2.copy(moveMessage.getShiftPosition());
            shiftCardStart = shiftTo.translate(getShiftDelta().invert());
        }
        return shiftCardStart;
    }

    /**
     * Calculates a list of all card coordinates (currently on the board) which will get shifted
     *
     * @return List of vectors containing shifted card coordinates
     */
    public List<VectorInt2> getCardsToShift() {
        if (cardsToShift == null) {
            VectorInt2 shiftCardStart = getShiftCardStart();
            VectorInt2 shiftDelta = getShiftDelta();
            List<VectorInt2> list = new ArrayList<>(Math.max(BOARD_WIDTH, BOARD_HEIGHT));
            while (isOnBoard(shiftCardStart = shiftCardStart.translate(shiftDelta))) {
                list.add(shiftCardStart);
            }
            cardsToShift = list;
        }
        return cardsToShift;
    }

    /**
     * Calculates the position (card coordinates) at which players will get pushed out of the board
     *
     * @return Card position as vector
     */
    public VectorInt2 getPushedOutPlayersPosition(){
        if(pushedOutPlayersPosition == null){
            Position oppositePosition = new Position(moveMessage.getShiftPosition()).getOpposite();
            pushedOutPlayersPosition = VectorInt2.copy(oppositePosition);
        }
        return pushedOutPlayersPosition;
    }

    /* *************************************************************
    ********                HELPER FUNCTIONS                ********
    ****************************************************************/

    /**
     * Calculates the IDs of all players that will be pushed out when shifting
     *
     * WARNING: do *not* use for processing in UIs, the board will already have changed!
     *
     * @return List of player IDs
     */
    public List<Integer> getPushedOutPlayers(){
        if(pushedOutPlayers == null){
            VectorInt2 position = getPushedOutPlayersPosition();
            CardData card = board.getCard(position.x, position.y);
            List<Integer> playersOnCard = card.getPin().getPlayerID();
            pushedOutPlayers = new ArrayList<>(playersOnCard);
        }
        return pushedOutPlayers;
    }

    /**
     * Calculates the new position for shifted out players
     *
     * This will be the opposite of the original position of the shifted out
     * card, or simpler: the shift card's new position.
     *
     * @return Card position as vector
     */
    public VectorInt2 getNewPlayerPosition(){
        if(newPlayerPosition == null){
            newPlayerPosition = getShiftCardStart().translate(getShiftDelta());
        }
        return newPlayerPosition;
    }

    /**
     * Calculates the position a pin has after shifting and before moving, based on the position it has before shifting.
     *
     * @param preShiftPos Client's position before using the shift card
     * @return Client's position after shifting
     */
    public VectorInt2 getPlayerPositionAfterShift(VectorInt2 preShiftPos){
        if(!getCardsToShift().contains(preShiftPos)){
            return preShiftPos;
        }
        if(preShiftPos.equals(getPushedOutPlayersPosition())){
            return getNewPlayerPosition();
        }
        return preShiftPos.translate(getShiftDelta());
    }

}
