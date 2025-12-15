package pl.arczewski.zubrzycki.statki.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Klient lobby – proste wywołania CREATE / JOIN.
 */
public record LobbyClient(String lobbyHost, int lobbyPort) {

    public record CreateRoomResult(String roomCode) { }
    public record JoinRoomResult(String hostIp, int hostGamePort) { }

    // ==========================
    // CREATE ROOM
    // ==========================
    public CreateRoomResult createRoom(int gamePort) throws IOException {
        try (Socket socket = new Socket(lobbyHost, lobbyPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            out.println("CREATE " + gamePort);

            String resp = in.readLine();
            if (resp == null)
                throw new IOException("Brak odpowiedzi z lobby");

            if (resp.startsWith("ROOM "))
                return new CreateRoomResult(resp.substring(5).trim());

            if (resp.startsWith("ERROR "))
                throw new IOException(resp);

            throw new IOException("Nieoczekiwana odpowiedź: " + resp);
        }
    }

    // ==========================
    // JOIN ROOM
    // ==========================
    public JoinRoomResult joinRoom(String code) throws IOException {
        try (Socket socket = new Socket(lobbyHost, lobbyPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            out.println("JOIN " + code);

            String resp = in.readLine();
            if (resp == null)
                throw new IOException("Brak odpowiedzi z lobby");

            if (resp.startsWith("HOST ")) {
                String[] p = resp.split("\\s+");
                if (p.length != 3)
                    throw new IOException("Zły format HOST: " + resp);

                return new JoinRoomResult(p[1], Integer.parseInt(p[2]));
            }

            if (resp.startsWith("ERROR "))
                throw new IOException(resp);

            throw new IOException("Nieoczekiwana odpowiedź: " + resp);
        }
    }
}
