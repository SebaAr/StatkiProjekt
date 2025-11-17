package pl.arczewski.zubrzycki.statki.model;

/**
 * Plansza 10x10. Indeksacja: x [0..9] kolumny, y [0..9] wiersze.
 * Zawiera metody do ustawiania statków z odstępem, strzałów i sprawdzenia czy wszystkie statki zatopione.
 */
public class Board {

    private final Cell[][] grid = new Cell[10][10];

    public Board() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                grid[y][x] = new Cell();
            }
        }
    }

    public Cell getCell(int x, int y) {
        return grid[y][x];
    }

    /**
     * Sprawdza czy statek o danym rozmiarze można ustawić w (startX,startY) w orientacji horizontal,
     * z zachowaniem jednopólnego odstępu od innych statków.
     */
    public boolean canPlaceShipWithSpacing(int startX, int startY, int size, boolean horizontal) {
        if (horizontal) {
            if (startX + size > 10) return false;
        } else {
            if (startY + size > 10) return false;
        }

        for (int i = -1; i <= size; i++) {
            for (int j = -1; j <= 1; j++) {
                int xi = horizontal ? startX + i : startX + j;
                int yi = horizontal ? startY + j : startY + i;
                if (xi >= 0 && xi < 10 && yi >= 0 && yi < 10) {
                    if (grid[yi][xi].hasShip()) return false;
                }
            }
        }
        return true;
    }

    /**
     * Ustawia obiekt Ship (już stworzony) — zwraca true jeśli ustawienie powiodło się.
     * Metoda nie tworzy obiektu Ship — to pozwala zachować informacje o nazwie/rozmiarze/horizontal.
     */
    public boolean placeShip(Ship ship, int startX, int startY, boolean horizontal) {
        int size = ship.getSize();
        if (!canPlaceShipWithSpacing(startX, startY, size, horizontal)) return false;

        if (horizontal) {
            for (int i = 0; i < size; i++) {
                grid[startY][startX + i].setShip(ship);
            }
        } else {
            for (int i = 0; i < size; i++) {
                grid[startY + i][startX].setShip(ship);
            }
        }
        ship.setHorizontal(horizontal);
        return true;
    }

    /**
     * Strzał w pole (x,y). Zwraca true jeśli trafienie.
     * Jeśli pole już było trafione, zwraca false (nie "trafienie").
     */
    public boolean shoot(int x, int y) {
        Cell c = getCell(x, y);
        if (c.isShot()) return false;
        c.setShot(true);
        if (c.hasShip()) {
            c.getShip().hit();
            return true;
        }
        return false;
    }

    /**
     * Sprawdza, czy wszystkie umieszczone statki zostały zatopione.
     */
    public boolean allShipsSunk() {
        // Wykrywamy istniejące statki po komórkach: jeżeli istnieje pole z ship i ship.isSunk() == false -> false
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Cell c = grid[y][x];
                if (c.hasShip() && !c.getShip().isSunk()) return false;
            }
        }
        return true;
    }
}
