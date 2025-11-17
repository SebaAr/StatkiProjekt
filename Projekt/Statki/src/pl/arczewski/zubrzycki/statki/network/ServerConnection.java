package pl.arczewski.zubrzycki.statki.network;



import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {

    public Socket waitForClient(int port) throws Exception {
        ServerSocket server = new ServerSocket(port);
        return server.accept();
    }
}
