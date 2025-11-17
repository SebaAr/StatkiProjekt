package pl.arczewski.zubrzycki.statki.engine;

import pl.arczewski.zubrzycki.statki.model.Player;
import pl.arczewski.zubrzycki.statki.model.Board;

public class GameEngine {

    public enum Phase { PLACEMENT, BATTLE }

    private final GameState state;
    private Phase phase = Phase.PLACEMENT;

    public GameEngine(Player p1, Player p2) {
        this.state = new GameState(p1, p2);
    }

    public GameState getState() { return state; }
    public Phase getPhase() { return phase; }

    public void startBattlePhase() {
        this.phase = Phase.BATTLE;
    }

    public boolean isBattlePhase() {
        return phase == Phase.BATTLE;
    }

    /**
     * Strzał – ZWRACA true jeśli trafienie, false jeśli pudło.
     * UWAGA – NIE zmienia tury!
     * Zmiana tury wykonywana jest dopiero w GameView przy "Kontynuuj".
     */
    public boolean shoot(int x, int y) {
        Player opponent = state.getOtherPlayer();
        Board board = opponent.getBoard();
        return board.shoot(x, y);
    }

    public boolean isGameFinished() {
        return state.getPlayer1().getBoard().allShipsSunk()
                || state.getPlayer2().getBoard().allShipsSunk();
    }

    public Player getWinner() {
        if (state.getPlayer1().getBoard().allShipsSunk()) return state.getPlayer2();
        if (state.getPlayer2().getBoard().allShipsSunk()) return state.getPlayer1();
        return null;
    }
}
