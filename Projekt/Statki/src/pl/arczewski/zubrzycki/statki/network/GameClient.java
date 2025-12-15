package pl.arczewski.zubrzycki.statki.network;

import pl.arczewski.zubrzycki.statki.protocol.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Klient TCP – łączy się z hostem.
 */
public class GameClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private GameNetworkListener listener;

    public void setListener(GameNetworkListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws Exception {

        socket = new Socket(host, port);


        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        startReadLoop();
    }

    /** Wysłanie dowolnego GameEvent */
    public synchronized void send(GameEvent event) throws Exception {
        if (out == null) return;
        out.writeObject(event);
        out.flush();
    }

    public void close() {
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }


    private void startReadLoop() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    Object obj = in.readObject();
                    if (obj instanceof GameEvent ev) {

                    }
                }
            } catch (Exception ignored) {

            }
        }, "GameClient-Receiver");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }



}
