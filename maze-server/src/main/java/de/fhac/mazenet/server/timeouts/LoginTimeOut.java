package de.fhac.mazenet.server.timeouts;

import de.fhac.mazenet.server.game.Game;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;

import java.util.TimerTask;

public class LoginTimeOut extends TimerTask {

    private Game currentGame;

    public LoginTimeOut(Game currentGame) {
        super();
        this.currentGame = currentGame;
    }

    @Override
    public void run() {
        Debug.print(Messages.getString("LoginThread.Timeout"), DebugLevel.DEFAULT);
        currentGame.stopWaitingForPlayers();
    }

}
