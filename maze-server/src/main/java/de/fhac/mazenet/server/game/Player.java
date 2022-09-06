package de.fhac.mazenet.server.game;

import de.fhac.mazenet.server.generated.ClientRole;
import de.fhac.mazenet.server.generated.Treasure;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.networking.Connection;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Stack;

public class Player extends Client {

    private Treasure currentTreasure;
    private Stack<Treasure> treasures;
    private Statistic stats;

    /**
     * Client darf nicht selber generiert werden, sondern nur vom Login erzeugt!
     */
    public Player(int id, String name, Connection connection) {
        super(id, name, ClientRole.PLAYER, connection);
        currentTreasure = null;
        treasures = new Stack<>();
        // Hinzufuegen des Starts als letzter zu holender Schatz
        // z.B.: Treasure.START_01
        treasures.push(Treasure.fromValue("Start0" + this.getId()));
        stats = new Statistic();
    }

    public Treasure getCurrentTreasure() {
        return currentTreasure;
    }

    /**
     * Diese Methode wird aufgerufen wenn der Spieler einen Schatz gefunden hat.
     * Dadurch wird der aktuelle Schatz durch den nächsten zu suchenden Schatz
     * ersetzt. Der Rückgabewert ist die Anzahl der noch zu suchenden Schätze
     *
     * @return Schaetze die der Spieler noch finden muss einschließlich Startfeld
     */
    public int foundTreasure() {
        try {
            currentTreasure = treasures.pop();
        } catch (EmptyStackException e) {
            return 0;
        }
        return treasures.size() + 1;

    }

    public void updateStats(boolean foundTreasure) {
        stats.moveDone(foundTreasure);
    }

    public Statistic getStats() {
        return stats;
    }

    public int treasuresToGo() {
        return treasures.size() + ((currentTreasure != null) ? 1 : 0);
    }

    public void setTreasure(Collection<? extends Treasure> treasures) {
        this.treasures.addAll(treasures);
        this.currentTreasure = this.treasures.pop();
        // this.stats.setTreasureCountStart(treasuresToGo());
        this.stats.setTreasureCountStart(0);
    }

    public Stack<Treasure> getTreasures() {
        return treasures;
    }

    @Override
    public String toString() {
        return getName() + " (" + getId() + ")" + " search for " + currentTreasure + " (" + stats + ")";
    }

}
