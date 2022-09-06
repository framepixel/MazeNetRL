package de.fhac.mazenet.server.networking;

import de.fhac.mazenet.server.generated.ClientRole;
import de.fhac.mazenet.server.generated.Errortype;

import java.util.Objects;

public class Client {
    final int id;
    private String name;
    private ClientRole role;
    private Connection connectionToClient;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id &&
                Objects.equals(connectionToClient, client.connectionToClient);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, connectionToClient);
    }

    /**
     * Client darf nicht selber generiert werden, sondern nur vom Login erzeugt!
     */
    public Client(int id, String name, ClientRole role, Connection connection) {
        this.id = id;
        this.name = name;
        this.connectionToClient = connection;
        this.role = role;
    }

    public ClientRole getRole() {
        return role;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Connection getConnectionToClient() {
        return connectionToClient;
    }

    public void disconnect(Errortype error) {
        connectionToClient.disconnect(error);
    }

}
