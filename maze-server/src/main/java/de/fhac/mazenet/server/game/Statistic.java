package de.fhac.mazenet.server.game;

import de.fhac.mazenet.server.generated.StatisticData;

public class Statistic {
    private int foundTreasures;
    private int longestFailMoves;
    private int movesDone;
    private int currentFails;

    public Statistic() {
        this.foundTreasures = 0;
        this.longestFailMoves = 0;
        this.movesDone = 0;
        this.currentFails = 0;
    }

    public void moveDone(boolean treasureFound) {
        ++this.movesDone;
        if (!treasureFound) {
            if (++this.currentFails > this.longestFailMoves) {
                this.longestFailMoves = this.currentFails;
            }
        } else {
            ++this.foundTreasures;
            this.currentFails = 0;
        }
    }

    public StatisticData toStatisticData() {
        var statData = new StatisticData();
        statData.setFoundTreasures(getFoundTreasures());
        statData.setLongestFailMoves(getLongestFailMoves());
        statData.setMovesDone(getMovesDone());
        return statData;
    }

    public void setTreasureCountStart(int treasureCountStart) {
        this.foundTreasures = treasureCountStart;
    }

    public int getFoundTreasures() {
        return foundTreasures;
    }

    public int getLongestFailMoves() {
        return longestFailMoves;
    }

    public int getMovesDone() {
        return movesDone;
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "Schaetze Gefunden:" + foundTreasures +
                ", Laengste Moves ohne Schatz:" + longestFailMoves +
                ", Zuege gemacht:" + movesDone +
                '}';
    }
}
