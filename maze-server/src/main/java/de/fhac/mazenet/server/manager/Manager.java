package de.fhac.mazenet.server.manager;

import de.fhac.mazenet.server.generated.ClientRole;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.networking.Connection;

public class Manager extends Client {

    private ManagerListener listener;

    public Manager(int id, String name, Connection connection) {
        super(id, name, ClientRole.MANAGER, connection);
        listener = new ManagerListener(connection);
    }

    public void startListening() {
        listener.start();
    }

}
