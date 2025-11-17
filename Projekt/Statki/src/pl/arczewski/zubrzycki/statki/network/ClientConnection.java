package pl.arczewski.zubrzycki.statki.network;




import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private Socket socket;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
    }
}
