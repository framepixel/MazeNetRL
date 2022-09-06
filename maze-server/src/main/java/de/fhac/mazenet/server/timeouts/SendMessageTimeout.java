package de.fhac.mazenet.server.timeouts;

import de.fhac.mazenet.server.generated.Errortype;
import de.fhac.mazenet.server.networking.Connection;

import java.util.TimerTask;

public class SendMessageTimeout extends TimerTask {

    private Connection connection;

    public SendMessageTimeout(Connection con) {
        this.connection = con;
    }

    @Override
    public void run() {
        this.connection.disconnect(Errortype.TIMEOUT);
    }

}
