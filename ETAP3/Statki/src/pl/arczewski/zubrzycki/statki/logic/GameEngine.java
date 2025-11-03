package pl.arczewski.zubrzycki.statki.logic;


import pl.arczewski.zubrzycki.statki.model.Board;
import pl.arczewski.zubrzycki.statki.model.Player;

public class GameEngine {
    private Board playerBoard;
    private Board opponentBoard;
    private Player player;

    public GameEngine(Player player) {
        this.player = player;
        this.playerBoard = new Board();
        this.opponentBoard = new Board();
    }

    public void startGame() {
        System.out.println("Rozpoczęto grę dla gracza: " + player.getName());
    }
}
