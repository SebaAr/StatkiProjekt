package pl.arczewski.zubrzycki.statki.model;

/**
 * Reprezentacja statku:
 * - nazwa
 * - rozmiar
 * - liczba trafień
 * - orientacja
 * - pozycja (x,y)
 * - flaga FAKE dla boosta TROLL
 */
public class Ship {

    private final String name;
    private final int size;
    private int hits = 0;
    private boolean horizontal = true;

    // pozycja startowa statku na planszy
    private int x;
    private int y;

    // czy to fałszywy statek (boost TROLL)
    private boolean fake = false;

    // konstruktor klasyczny
    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    // -------- GET / SET podstawowe --------

    public String getName() { return name; }

    public int getSize() { return size; }

    public boolean isHorizontal() { return horizontal; }

    // -------- Pozycja --------

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int x, int y, boolean horizontal) {
        this.x = x;
        this.y = y;
        this.horizontal = horizontal;
    }

    // -------- Trafienia --------

    public void hit() { hits++; }

    public boolean isSunk() { return hits >= size; }

    // -------- Fake Ship (TROLL) --------

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }
}
