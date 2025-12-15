package pl.arczewski.zubrzycki.statki.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plansza 10x10. Indeksacja: x [0..9] kolumny, y [0..9] wiersze.
 * Zawiera metody:
 * - ustawiania statków z odstępem
 * - zdejmowania statków
 * - strzałów
 * - sprawdzania warunku zwycięstwa (FAKE statki nie liczą się)
 * - przesuwania statku (SWAP booster)
 * - ujawniania losowego pola statku (RADAR booster)
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
     * Sprawdza czy statek o danym rozmiarze można ustawić w (startX,startY)
     * w orientacji horizontal, z zachowaniem jednopólnego odstępu.
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
     * Ustawia obiekt Ship na planszy.
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

        ship.setPosition(startX, startY, horizontal);
        return true;
    }

    /**
     * Usuwa wszystkie pola należące do danego statku.
     */
    public void removeShip(Ship ship) {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Cell c = grid[y][x];
                if (c.getShip() == ship) {
                    c.setShip(null);
                }
            }
        }
    }

    /**
     * Reset planszy.
     */
    public void clear() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                grid[y][x].setShip(null);
                grid[y][x].setShot(false);
            }
        }
    }

    /**
     * Strzał w pole.
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
     * Sprawdza, czy WSZYSTKIE PRAWDZIWE statki są zatopione.
     * Statki oznaczone jako fake (TROLL) są ignorowane.
     */
    public boolean allShipsSunk() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Cell c = grid[y][x];
                if (c.hasShip()) {
                    Ship s = c.getShip();
                    if (!s.isFake() && !s.isSunk()) return false;
                }
            }
        }
        return true;
    }

    /**
     * Zwraca przypadkowe pole zawierające statek (RADAR).
     */
    public Cell revealRandomShipCell() {
        List<Cell> shipCells = new ArrayList<>();

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (grid[y][x].hasShip()) {
                    shipCells.add(grid[y][x]);
                }
            }
        }

        if (shipCells.isEmpty()) return null;

        return shipCells.get(new Random().nextInt(shipCells.size()));
    }

    /**
     * Przesuwa dany statek na nowe miejsce (boost SWAP).
     * Sprawdza poprawność, czy można wstawić bez kolizji.
     */
    public boolean moveShip(Ship ship, int newX, int newY, boolean horizontal) {
        int size = ship.getSize();

        // sprawdź czy mieści się i nie koliduje
        if (!canPlaceShipWithSpacing(newX, newY, size, horizontal))
            return false;

        // usuń stary statek
        removeShip(ship);

        // spróbuj ustawić w nowym miejscu
        boolean ok = placeShip(ship, newX, newY, horizontal);
        if (!ok) {
            // jeśli się nie udało, spróbuj odtworzyć starą pozycję (awaryjnie)
            placeShip(ship, ship.getX(), ship.getY(), ship.isHorizontal());
            return false;
        }

        return true;
    }

    /**
     * Tworzy fałszywy statek (TROLL).
     */
    public Ship placeFakeShip(int size, int x, int y, boolean horizontal) {
        if (!canPlaceShipWithSpacing(x, y, size, horizontal)) return null;

        Ship fakeShip = new Ship("TROLL", size);
        fakeShip.setFake(true);
        placeShip(fakeShip, x, y, horizontal);

        return fakeShip;
    }
}
