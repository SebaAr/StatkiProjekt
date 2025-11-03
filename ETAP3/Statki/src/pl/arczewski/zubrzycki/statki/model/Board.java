package pl.arczewski.zubrzycki.statki.model;


public class Board {
    private final int SIZE = 10;
    private char[][] grid;

    public Board() {
        grid = new char[SIZE][SIZE];
        clearBoard();
    }

    public void clearBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = '~'; // symbol wody
            }
        }
    }

    public char[][] getGrid() {
        return grid;
    }
}
