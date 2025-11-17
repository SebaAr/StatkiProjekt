package pl.arczewski.zubrzycki.statki.engine;

import pl.arczewski.zubrzycki.statki.model.Player;

public class GameState {

    private final Player player1;
    private final Player player2;
    private PlayerTurn turn;

    public GameState(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.turn = PlayerTurn.PLAYER1;
    }

    public Player getCurrentPlayer() {
        return turn == PlayerTurn.PLAYER1 ? player1 : player2;
    }

    public Player getOtherPlayer() {
        return turn == PlayerTurn.PLAYER1 ? player2 : player1;
    }

    public void swapTurn() {
        turn = (turn == PlayerTurn.PLAYER1) ? PlayerTurn.PLAYER2 : PlayerTurn.PLAYER1;
    }
}
