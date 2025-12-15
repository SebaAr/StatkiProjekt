package pl.arczewski.zubrzycki.statki.network;

import pl.arczewski.zubrzycki.statki.protocol.GameEvent;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class GameServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private GameNetworkListener listener;

    public void setListener(GameNetworkListener listener) {
        this.listener = listener;
    }

    /**
     * Start serwera i czekanie na pierwszego klienta.
     * Blokujące – dlatego odpalaj to w osobnym wątku
     * albo z osobnego okna konfiguracji.
     */
    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();


        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(clientSocket.getInputStream());

        startReadLoop();
    }

    public synchronized void send(GameEvent event) throws Exception {
        if (out == null) return;
        out.writeObject(event);
        out.flush();
    }

    public void close() {
        try { if (clientSocket != null) clientSocket.close(); } catch (Exception ignored) {}
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
    }

    // ===== PRYWATNE =====

    private void startReadLoop() {
        Thread t = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    Object obj = in.readObject();
                    if (!(obj instanceof GameEvent ev)) continue;


                }
            } catch (Exception e) {

            }
        }, "GameServer-Receiver");

        t.setDaemon(true);
        t.start();
    }


}