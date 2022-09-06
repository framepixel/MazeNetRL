package de.fhac.mazenet.server.tools;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.PathInfo;
import de.fhac.mazenet.server.game.Position;

import java.util.LinkedList;
import java.util.List;

public class Algorithmics {
    private Algorithmics() {
    }

    public static List<Position> findPath(Board board, Position from, Position to) {
        PathInfo[][] reach = board.getAllReachablePositionsMatrix(from);
        return getPathToPosition(to, reach);
    }

    private static List<Position> getPathToPosition(Position target, final PathInfo[][] reachableMatrix) {
        List<Position> path = new LinkedList<>();
        int stepsToGo = reachableMatrix[target.getRow()][target.getCol()].getStepsFromSource();
        if (stepsToGo == 0) {
            return path;
        } else {
            List<Position> toGo = getPathToPosition(reachableMatrix[target.getRow()][target.getCol()].getCameFrom(),
                    reachableMatrix);
            for (Position p : toGo) {
                path.add(p);
            }
            path.add(target);
            return path;
        }
    }
}
