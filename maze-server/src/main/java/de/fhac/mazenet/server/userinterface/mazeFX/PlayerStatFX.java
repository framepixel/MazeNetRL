package de.fhac.mazenet.server.userinterface.mazeFX;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.PositionData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Created by richard on 08.06.16.
 */
public class PlayerStatFX {

    public final int playerId;
    public final PlayerStat controller;
    public final Node rootNode;
    private int treasureFound;
    private int cachedTreasuresRemaining;
    private Player player;

    public PlayerStatFX(int playerId) throws IOException {
        this.playerId = playerId;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/layouts/PlayerStat.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("locale"));
        rootNode = fxmlLoader.load();
        controller = fxmlLoader.getController();
        // da die Nummer der zu suchenden Schätze
        // nicht bekannt ist wird zuerst auf 0 erhöht
        treasureFound = -1;
    }

    public void update(Player p) {
        if (!(cachedTreasuresRemaining == p.treasuresToGo())) {
            controller.setNumFound(++treasureFound);
        }
        cachedTreasuresRemaining = p.treasuresToGo();
        controller.setTeamId(playerId);
        controller.setPlayerName(p.getName());
        controller.setNumRemaining(p.treasuresToGo());
        controller.setTreasureImage(p.getCurrentTreasure().value());
        this.player = p;
    }

    public void setWinner() {
        controller.setNumFound(++treasureFound);
        controller.setNumRemaining(0);
        controller.setWinner();
    }

    public void active(boolean act) {
        controller.setActive(act);
    }


    public Player getPlayer() {
        return player;
    }

}
