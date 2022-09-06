package de.fhac.mazenet.server.networking;

import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public class TCPConnectionCreationTask implements Callable<Socket> {
    private Socket incomingSocket;
    private ServerSocket serverSocket;
    private CyclicBarrier barrier;

    public TCPConnectionCreationTask(ServerSocket serverSocket, CyclicBarrier barrier) {
        this.serverSocket = serverSocket;
        this.barrier = barrier;
    }

    @Override
    public Socket call() {
        incomingSocket = null;
        try {
            incomingSocket = serverSocket.accept();
        } catch (IOException e) {
            Debug.print(Messages.getString("Game.errorWhileConnecting") + " (Port:" + serverSocket.getLocalPort() + ")", DebugLevel.DEFAULT);
            System.err.println(e.getMessage());
        }
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        if (incomingSocket == null)
            Debug.print(Messages.getString("TCPConnectionCreationTask.SocketNull"), DebugLevel.DEFAULT);
        return incomingSocket;
    }
}
