package de.fhac.mazenet.server.game;

import de.fhac.mazenet.server.generated.PositionData;

import java.util.ArrayList;
import java.util.List;

public class Position extends PositionData {

    public Position() {
        super();
        row = -1;
        col = -1;
    }

    public Position(PositionData position) {
        super();
        row = position.getRow();
        col = position.getCol();
    }

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return Gibt eine Liste der moeglichen Positionen fuer die Schiebekarte
     *         zurueck. Hat jedesmal das gleiche Ergebnis
     */
    static public List<Position> getPossiblePositionsForShiftcard() {
        List<Position> positions = new ArrayList<>();
        for (int a : new int[] { 0, 6 }) {
            for (int b : new int[] { 1, 3, 5 }) {
                positions.add(new Position(a, b));
                positions.add(new Position(b, a));
            }
        }
        return positions;
    }

    /**
     * Checkt, ob an dieser Stelle ein Schieben moeglich ist
     */
    public boolean isLooseShiftPosition() {
        return (row > -1 && row < 7 && col > -1 && col < 7)
                && ((row % 6 == 0 && col % 2 == 1) || (col % 6 == 0 && row % 2 == 1));
    }

    public boolean isOppositePosition(PositionData position) {
        return this.getOpposite().equals(new Position(position));
    }

    /**
     * Gibt die gegenueberliegende Position auf dem Spielbrett wieder
     */
    public Position getOpposite() {
        if (row % 6 == 0) {
            return new Position((row + 6) % 12, col);
        } else if (col % 6 == 0) {
            return new Position(row, (col + 6) % 12);
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + col;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        Position other = new Position((PositionData) obj);
        if (col != other.col)
            return false;
        if (row != other.row)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "(" + col + "," + row + ")";
    }

}
