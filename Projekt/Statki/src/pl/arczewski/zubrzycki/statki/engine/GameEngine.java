package pl.arczewski.zubrzycki.statki.engine;

import pl.arczewski.zubrzycki.statki.model.Player;
import pl.arczewski.zubrzycki.statki.model.Board;

/**
 * Prosty silnik gry: trzyma GameState (graczy i turę) i obsługuje strzały.
 */
public class GameEngine {

    private final GameState state;

    public GameEngine(Player p1, Player p2) {
        this.state = new GameState(p1, p2);
    }

    public GameState getState() { return state; }

    /**
     * Strzał aktualnego gracza w pole (x,y) przeciwnika.
     * Zwraca true jeśli trafienie (aktualny gracz strzela dalej).
     * Jeśli pudło → tura zostaje zmieniona (swapTurn).
     */
    public boolean shoot(int x, int y) {
        Player opponent = state.getOtherPlayer();
        Board b = opponent.getBoard();
        boolean hit = b.shoot(x, y);
        if (!hit) state.swapTurn();
        return hit;
    }

    public boolean isGameFinished() {
        return state.getCurrentPlayer().getBoard().allShipsSunk() ||
                state.getOtherPlayer().getBoard().allShipsSunk();
    }

    public Player getWinner() {
        if (state.getCurrentPlayer().getBoard().allShipsSunk()) return state.getOtherPlayer();
        if (state.getOtherPlayer().getBoard().allShipsSunk()) return state.getCurrentPlayer();
        return null;
    }
}
