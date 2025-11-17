package pl.arczewski.zubrzycki.statki.model;

public class Player {

    private final String name;
    private final Board board;
    private boolean ready = false; // nowa flaga - czy gracz potwierdził gotowość

    public Player(String name) {
        this.name = name;
        this.board = new Board();
    }

    public String getName() { return name; }
    public Board getBoard() { return board; }

    // ready
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
}
