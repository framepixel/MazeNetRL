package de.fhac.mazenet.server.game;

import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.generated.*;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board extends BoardData {

    //FIXME: der aktuelle Schatz gehoert nicht zum Board und wandert in Zukunft hier raus (siehe Issue)
    private Treasure currentTreasure;

    /**
     * Erzeugt ein Board von einem BoardData. Dabei wird currentTreasure von
     * Board <b>nicht</b> gesetzt, da es keine Objektvariable von BoardData ist
     *
     * @param boardData Instanz von BoardData, aus der eine Instanz von Board
     *                  generiert wird
     */
    public Board(BoardData boardData) {
        super();
        PositionData forbiddenPositionData = boardData.getForbidden();
        forbidden = (forbiddenPositionData != null) ? new Position(forbiddenPositionData) : null;
        getFoundTreasures().addAll(boardData.getFoundTreasures());
        shiftCard = new Card(boardData.getShiftCard());
        this.getRow();
        for (int i = 0; i < 7; i++) {
            this.getRow().add(i, new Row());
            // Initialisierung in getCol()
            this.getRow().get(i).getCol();
            for (int j = 0; j < 7; j++) {
                // new Card, damit keine Referenzen, sondern richte Kopien
                // erstellt werden
                this.getRow().get(i).getCol().add(j, new Card(boardData.getRow().get(i).getCol().get(j)));
            }
        }
        // es darf keine boardinitialisierung mehr durchgefuehrt werden, da
        // diese die Kopie ueberschreiben wuerde
    }

    public Board() {
        super();
        forbidden = null;
        this.getRow();
        // Erst werden alle Karten mit einer Standardkarte belegt
        for (int i = 0; i < 7; i++) {
            this.getRow().add(i, new Row());
            this.getRow().get(i).getCol();
            for (int j = 0; j < 7; j++) {
                this.getRow().get(i).getCol().add(j, new Card(Card.CardShape.I, Card.Orientation.D0, null));
            }
        }
        this.getFoundTreasures().clear();
        // Dann wird das Spielfeld regelkonform aufgebaut
        generateInitialBoard();
    }

    private void generateInitialBoard() {
        // fixedCards:
        // Die festen, unveraenderbaren Karten auf dem Spielbrett
        setCard(0, 0, new Card(Card.CardShape.L, Card.Orientation.D90, null));
        setCard(0, 2, new Card(Card.CardShape.T, Card.Orientation.D0, Treasure.SYM_13));
        setCard(0, 4, new Card(Card.CardShape.T, Card.Orientation.D0, Treasure.SYM_14));
        setCard(0, 6, new Card(Card.CardShape.L, Card.Orientation.D180, null));
        setCard(2, 0, new Card(Card.CardShape.T, Card.Orientation.D270, Treasure.SYM_15));
        setCard(2, 2, new Card(Card.CardShape.T, Card.Orientation.D270, Treasure.SYM_16));
        setCard(2, 4, new Card(Card.CardShape.T, Card.Orientation.D0, Treasure.SYM_17));
        setCard(2, 6, new Card(Card.CardShape.T, Card.Orientation.D90, Treasure.SYM_18));
        setCard(4, 0, new Card(Card.CardShape.T, Card.Orientation.D270, Treasure.SYM_19));
        setCard(4, 2, new Card(Card.CardShape.T, Card.Orientation.D180, Treasure.SYM_20));
        setCard(4, 4, new Card(Card.CardShape.T, Card.Orientation.D90, Treasure.SYM_21));
        setCard(4, 6, new Card(Card.CardShape.T, Card.Orientation.D90, Treasure.SYM_22));
        setCard(6, 0, new Card(Card.CardShape.L, Card.Orientation.D0, null));
        setCard(6, 2, new Card(Card.CardShape.T, Card.Orientation.D180, Treasure.SYM_23));
        setCard(6, 4, new Card(Card.CardShape.T, Card.Orientation.D180, Treasure.SYM_24));
        setCard(6, 6, new Card(Card.CardShape.L, Card.Orientation.D270, null));

        // die freien verschiebbaren Teile auf dem Spielbrett
        ArrayList<Card> freeCards = new ArrayList<>();
        Random random = new Random();
        if (Settings.TESTBOARD) {
            random.setSeed(Settings.TESTBOARD_SEED);
        }
        // 15 mal L-shape (6 (sym) + 9 (ohne))
        freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_01));
        freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_02));
        freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_03));
        freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_04));
        freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_05));
        freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_06));

        for (int i = 0; i < 9; i++) {
            freeCards.add(new Card(Card.CardShape.L, Card.Orientation.fromValue(random.nextInt(4) * 90), null));
        }

        // 13 mal I-shape
        for (int i = 0; i < 13; i++) {
            freeCards.add(new Card(Card.CardShape.I, Card.Orientation.fromValue(random.nextInt(4) * 90), null));
        }

        // 6 mal T-shape
        freeCards.add(new Card(Card.CardShape.T, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_07));
        freeCards.add(new Card(Card.CardShape.T, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_08));
        freeCards.add(new Card(Card.CardShape.T, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_09));
        freeCards.add(new Card(Card.CardShape.T, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_10));
        freeCards.add(new Card(Card.CardShape.T, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_11));
        freeCards.add(new Card(Card.CardShape.T, Card.Orientation.fromValue(random.nextInt(4) * 90), Treasure.SYM_12));

        if (!Settings.TESTBOARD)
            Collections.shuffle(freeCards);

        for (int i = 1; i < 7; i += 2) {
            for (int j = 0; j < 7; j += 1) {
                Card card = freeCards.get(random.nextInt(freeCards.size()));
                setCard(i, j, card);
                freeCards.remove(card);
            }
        }
        for (int i = 1; i < 7; i += 2) {
            for (int j = 0; j < 7; j += 2) {
                Card card = freeCards.get(random.nextInt(freeCards.size()));
                setCard(j, i, card);
                freeCards.remove(card);
            }
        }
        this.setShiftCard(freeCards.get(0));

        switch (Settings.NUMBER_OF_PLAYERS) {
            case 4:
                getCard(6, 6).getPin().getPlayerID().add(4);
            case 3:
                getCard(6, 0).getPin().getPlayerID().add(3);
            case 2:
                getCard(0, 6).getPin().getPlayerID().add(2);
            case 1:
                getCard(0, 0).getPin().getPlayerID().add(1);
        }

        // Start als Schatz hinterlegen
        getCard(0, 0).setTreasure(Treasure.START_01);
        getCard(0, 6).setTreasure(Treasure.START_02);
        getCard(6, 0).setTreasure(Treasure.START_03);
        getCard(6, 6).setTreasure(Treasure.START_04);

        Debug.print(this.toString(), DebugLevel.DEBUG);
    }

    // Ausgabe des Spielbretts als AsciiArt
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Board [currentTreasure=").append(currentTreasure).append("]\n");
        stringBuilder.append(Messages.getString("Board.Board"));
        stringBuilder.append(" ------ ------ ------ ------ ------ ------ ------ \n");
        for (int i = 0; i < getRow().size(); i++) {
            StringBuilder line1 = new StringBuilder("|");
            StringBuilder line2 = new StringBuilder("|");
            StringBuilder line3 = new StringBuilder("|");
            StringBuilder line4 = new StringBuilder("|");
            StringBuilder line5 = new StringBuilder("|");
            StringBuilder line6 = new StringBuilder("|");
            for (int j = 0; j < getRow().get(i).getCol().size(); j++) {
                Card card = new Card(getCard(i, j));
                if (card.getOpenings().isTop()) {
                    line1.append("##  ##|");
                    line2.append("##  ##|");
                } else {
                    line1.append("######|");
                    line2.append("######|");
                }
                if (card.getOpenings().isLeft()) {
                    line3.append("  ");
                    line4.append("  ");
                } else {
                    line3.append("##");
                    line4.append("##");
                }
                if (card.getPin().getPlayerID().size() != 0) {
                    line3.append("S");
                } else {
                    line3.append(" ");
                }
                if (card.getTreasure() != null) {
                    String name = card.getTreasure().name();
                    switch (name.charAt(1)) {
                        case 'Y':
                            // Symbol
                            line3.append("T");
                            break;
                        case 'T':
                            // Startpunkt
                            line3.append("S");
                            break;
                    }
                    line4.append(name.substring(name.length() - 2));
                } else {
                    line3.append(" ");
                    line4.append("  ");
                }
                if (card.getOpenings().isRight()) {
                    line3.append("  |");
                    line4.append("  |");
                } else {
                    line3.append("##|");
                    line4.append("##|");
                }
                if (card.getOpenings().isBottom()) {
                    line5.append("##  ##|");
                    line6.append("##  ##|");
                } else {
                    line5.append("######|");
                    line6.append("######|");
                }
            }
            stringBuilder.append(line1.append("\n"));
            stringBuilder.append(line2.append("\n"));
            stringBuilder.append(line3.append("\n"));
            stringBuilder.append(line4.append("\n"));
            stringBuilder.append(line5.append("\n"));
            stringBuilder.append(line6.append("\n"));
            stringBuilder.append(" ------ ------ ------ ------ ------ ------ ------ \n");
        }
        return stringBuilder.toString();
    }

    public void setCard(int row, int col, CardData card) {
        // Muss ueberschrieben werden, daher zuerst entfernen und dann...
        this.getRow().get(row).getCol().remove(col); //Index, deshalb kein new Integer
        // ...hinzufuegen
        // new Card, da sonst in Spezialfaellen Duplikate auf dem Board erscheinen
        this.getRow().get(row).getCol().add(col, new Card(card));
    }

    public CardData getCard(int row, int col) {
        return this.getRow().get(row).getCol().get(col);
    }

    // Fuehrt nur das Hereinschieben der Karte aus!!!
    public void proceedShift(MoveMessageData move) {
        Debug.print(Messages.getString("Board.proceedShiftFkt"), DebugLevel.VERBOSE);
        Position shiftPosition = new Position(move.getShiftPosition());
        if (shiftPosition.getCol() % 6 == 0) { // Col=6 oder 0
            if (shiftPosition.getRow() % 2 == 1) {
                // horizontal schieben
                int row = shiftPosition.getRow();
                int start = (shiftPosition.getCol() + 6) % 12; // Karte die rausgenommen
                // wird
                setShiftCard(getCard(row, start));

                if (start == 6) {
                    for (int i = 6; i > 0; --i) {
                        setCard(row, i, new Card(getCard(row, i - 1)));
                    }
                } else {// Start==0
                    for (int i = 0; i < 6; ++i) {
                        setCard(row, i, new Card(getCard(row, i + 1)));
                    }
                }
            }
        } else if (shiftPosition.getRow() % 6 == 0) {
            if (shiftPosition.getCol() % 2 == 1) {
                // vertikal schieben
                int col = shiftPosition.getCol();
                int start = (shiftPosition.getRow() + 6) % 12; // Karte die rausgenommen
                // wird
                setShiftCard(getCard(start, col));
                if (start == 6) {
                    for (int i = 6; i > 0; --i) {
                        setCard(i, col, new Card(getCard(i - 1, col)));
                    }
                } else {// Start==0
                    for (int i = 0; i < 6; ++i) {
                        setCard(i, col, new Card(getCard(i + 1, col)));
                    }
                }

            }
        }
        forbidden = shiftPosition.getOpposite();
        Card shiftCard = new Card(move.getShiftCard());
        // Wenn Spielfigur auf neuer shiftcard steht,
        // muss dieser wieder aufs Brett gesetzt werden
        // Dazu wird Sie auf die gerade hereingeschoben
        // Karte gesetzt
        if (!this.shiftCard.getPin().getPlayerID().isEmpty()) {
            // Figur zwischenspeichern
            CardData.Pin temp = this.shiftCard.getPin();
            // Figur auf SchiebeKarte löschen
            this.shiftCard.setPin(new CardData.Pin());
            // Zwischengespeicherte Figut auf
            // neuer Karte plazieren
            shiftCard.setPin(temp);
        }
        setCard(shiftPosition.getRow(), shiftPosition.getCol(), shiftCard);
    }

    /**
     * gibt zurueck ob mit dem Zug der aktuelle Schatz erreicht wurde
     *
     * @param move
     * @param currentPlayer
     * @return
     */
    public boolean proceedTurn(MoveMessageData move, Integer currentPlayer) {
        Debug.print(Messages.getString("Board.proceedTurnFkt"), DebugLevel.VERBOSE);
        // XXX ACHTUNG wird nicht mehr auf Richtigkeit überprüft!!!
        this.proceedShift(move);
        Position target = new Position(move.getNewPinPos());
        Debug.print(String.format(Messages.getString("Board.playerMovesTo"), target.getRow(), target.getCol()),
                DebugLevel.DEFAULT);
        movePlayer(findPlayer(currentPlayer), target, currentPlayer);
        Card c = new Card(getCard(target.getRow(), target.getCol()));
        return (c.getTreasure() == currentTreasure);
    }

    protected void movePlayer(PositionData oldPosition, PositionData newPosition, Integer playerID) {
        Debug.print(Messages.getString("Board.movePlayerFkt"), DebugLevel.VERBOSE);
        this.getCard(oldPosition.getRow(), oldPosition.getCol()).getPin().getPlayerID().remove(playerID);
        /*Card c=new Card(this.getCard(newPosition.getRow(), newPosition.getCol()));
        c.getPin().getPlayerID().add(playerID);
        this.setCard(newPosition.getRow(),newPosition.getCol(),c);*/
        this.getCard(newPosition.getRow(), newPosition.getCol()).getPin().getPlayerID().add(playerID);
    }

    public Board fakeShift(MoveMessageData move) {
        Debug.print(Messages.getString("Board.fakeShiftFkt"), DebugLevel.VERBOSE);
        Board fake = (Board) this.clone();
        fake.proceedShift(move);
        return fake;
    }

    @Override
    public Object clone() {
        Board clone = new Board(this);
        clone.currentTreasure = this.currentTreasure;
        return clone;
    }

    public boolean validateTransition(MoveMessageData move, Integer playerID) {
        Debug.print(Messages.getString("Board.validateTransitionFkt"), DebugLevel.VERBOSE);
        PositionData movePosition = move.getShiftPosition();
        CardData moveShiftCard = move.getShiftCard();
        if (movePosition == null || moveShiftCard == null)
            return false;
        Position sm = new Position(movePosition);
        // Ueberpruefen ob das Reinschieben der Karte gueltig ist
        if (!sm.isLooseShiftPosition() || sm.equals(forbidden)) {
            System.err.println(String.format(Messages.getString("Board.forbiddenPostitionShiftCard"),
                    movePosition.getRow(), movePosition.getCol()));
            return false;
        }
        Card sc = new Card(moveShiftCard);
        if (!sc.equals(shiftCard)) {
            System.err.println(Messages.getString("Board.shiftCardIllegallyChanged"));
            return false;
        }
        // Ueberpruefen ob der Spielzug gueltig ist
        Board fakeBoard = this.fakeShift(move);
        Position playerPosition = new Position(fakeBoard.findPlayer(playerID));
        Debug.print(
                String.format(Messages.getString("Board.playerWantsToMoveFromTo"), playerPosition.getRow(),
                        playerPosition.getCol(), move.getNewPinPos().getRow(), move.getNewPinPos().getCol()),
                DebugLevel.VERBOSE);
        Debug.print(Messages.getString("Board.boardAfterShifting"), DebugLevel.DEBUG);
        Debug.print(fakeBoard.toString(), DebugLevel.DEBUG);
        if (fakeBoard.pathPossible(playerPosition, move.getNewPinPos())) {
            Debug.print(Messages.getString("Board.legalMove"), DebugLevel.VERBOSE);
            return true;
        }
        Debug.print(String.format(Messages.getString("Board.positionNotReachable"), move.getNewPinPos().getRow(),
                move.getNewPinPos().getCol()), DebugLevel.DEFAULT);
        return false;
    }

    public boolean pathPossible(PositionData oldPositionData, PositionData newPositionData) {
        Debug.print(Messages.getString("Board.pathPossibleFkt"), DebugLevel.VERBOSE);
        if (oldPositionData == null || newPositionData == null)
            return false;
        Position oldPosition = new Position(oldPositionData);
        Position newPosition = new Position(newPositionData);
        return getAllReachablePositions(oldPosition).contains(newPosition);
    }

    public List<Position> getAllReachablePositions(PositionData position) {
        Debug.print(Messages.getString("Board.getAllReachablePositionsFkt"), DebugLevel.VERBOSE);
        List<Position> reachablePositions = new ArrayList<>();
        PathInfo[][] reachable = getAllReachablePositionsMatrix(position);
        for (int i = 0; i < reachable.length; i++) {
            for (int j = 0; j < reachable[0].length; j++) {
                if (reachable[i][j].getStepsFromSource() > -1) {
                    reachablePositions.add(new Position(i, j));
                }
            }
        }
        return reachablePositions;
    }

    public PathInfo[][] getAllReachablePositionsMatrix(PositionData position) {
        PathInfo[][] reachable = new PathInfo[7][7];
        for (int i = 0; i < reachable.length; i++) {
            for (int j = 0; j < reachable[i].length; j++) {
                reachable[i][j] = new PathInfo();
            }
        }
        return getAllReachablePositionsMatrix(new Position(position), reachable, 0, null);
    }

    private PathInfo[][] getAllReachablePositionsMatrix(Position position, PathInfo[][] rechable, int step,
                                                        Position cameFrom) {
        rechable[position.getRow()][position.getCol()].setStepsFromSource(step);
        rechable[position.getRow()][position.getCol()].setCameFrom(cameFrom);
        final List<Position> directReachablePositions = getDirectReachablePositions(position);
        for (Position p1 : directReachablePositions) {
            if (rechable[p1.getRow()][p1.getCol()].getStepsFromSource() < 0
                    || rechable[p1.getRow()][p1.getCol()].getStepsFromSource() > step + 1) {
                getAllReachablePositionsMatrix(p1, rechable, step + 1, position);
            }
        }
        return rechable;
    }

    private List<Position> getDirectReachablePositions(PositionData position) {
        List<Position> positions = new ArrayList<>();
        CardData k = this.getCard(position.getRow(), position.getCol());
        CardData.Openings openings = k.getOpenings();
        if (openings.isLeft()) {
            if (position.getCol() - 1 >= 0
                    && getCard(position.getRow(), position.getCol() - 1).getOpenings().isRight()) {
                positions.add(new Position(position.getRow(), position.getCol() - 1));
            }
        }
        if (openings.isTop()) {
            if (position.getRow() - 1 >= 0
                    && getCard(position.getRow() - 1, position.getCol()).getOpenings().isBottom()) {
                positions.add(new Position(position.getRow() - 1, position.getCol()));
            }
        }
        if (openings.isRight()) {
            if (position.getCol() + 1 <= 6
                    && getCard(position.getRow(), position.getCol() + 1).getOpenings().isLeft()) {
                positions.add(new Position(position.getRow(), position.getCol() + 1));
            }
        }
        if (openings.isBottom()) {
            if (position.getRow() + 1 <= 6 && getCard(position.getRow() + 1, position.getCol()).getOpenings().isTop()) {
                positions.add(new Position(position.getRow() + 1, position.getCol()));
            }
        }
        return positions;
    }

    public Position findPlayer(int playerID) {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                CardData.Pin pinsOnCard = getCard(i, j).getPin();
                for (int pin : pinsOnCard.getPlayerID()) {
                    if (pin == playerID) {
                        Position position = new Position(i, j);
                        return position;
                    }
                }
            }
        }
        // Pin nicht gefunden.
        // Darf nicht vorkommen
        return null;
    }

    /**
     * Convenience-Methode um einen Schatz zu finden
     *
     * @param treasureData zu suchender Schatz
     * @return Position des Schatz' oder null, wenn Schatz auf Schiebekarte liegt
     */
    public PositionData findTreasure(Treasure treasureData) {
        if (treasureData == null) throw new IllegalArgumentException(Messages.getString("Board.treasureIsNull"));
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Treasure treasure = getCard(i, j).getTreasure();
                if (treasure == treasureData) {
                    PositionData position = new PositionData();
                    position.setCol(j);
                    position.setRow(i);
                    return position;
                }
            }
        }
        // Schatz nicht gefunden, kann nur bedeuten, dass Schatz sich auf
        // Schiebekarte befindet
        return null;
    }

    public Treasure getTreasure() {
        return currentTreasure;
    }

    public void setTreasure(Treasure t) {
        currentTreasure = t;
    }
}
