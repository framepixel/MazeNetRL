package de.fhac.mazenet.server.timeouts;

import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Game;
import de.fhac.mazenet.server.networking.Connection;

import java.util.HashMap;
import java.util.Timer;

public class TimeOutManager extends Timer {

    private LoginTimeOut loginTimeOut;
    private HashMap<Integer, SendMessageTimeout> sendMessageTimeout;
    private boolean loginTimeoutStartet;

    public TimeOutManager(Game game) {
        super("TimeOuts", true);
        this.sendMessageTimeout = new HashMap<>();
        loginTimeOut = new LoginTimeOut(game);
        loginTimeoutStartet = false;
    }

    public void startLoginTimeOut() {
        //bei  ersten Aufruf LoginTimeout starten
        if (!loginTimeoutStartet) {
            this.schedule(loginTimeOut, Settings.LOGINTIMEOUT);
            loginTimeoutStartet = true;
        }
    }

    public void stopLoginTimeOut() {
        loginTimeOut.cancel();
    }

    public void startSendMessageTimeOut(int playerId, Connection c) {
        sendMessageTimeout.put(playerId, new SendMessageTimeout(c));
        this.schedule(sendMessageTimeout.get(playerId), Settings.SENDTIMEOUT);
    }

    public void stopSendMessageTimeOut(int playerId) {
        if (sendMessageTimeout.containsKey(playerId))
            sendMessageTimeout.get(playerId).cancel();
    }
}
