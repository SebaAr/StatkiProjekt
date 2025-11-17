package pl.arczewski.zubrzycki.statki.model;

/**
 * Reprezentacja statku: nazwa + rozmiar + liczba trafień + orientacja.
 * Konstruktor domyślny używa (name, size). Dodano też konstruktor pomocniczy z ShipType.
 */
public class Ship {

    private final String name;
    private final int size;
    private int hits = 0;
    private boolean horizontal = true;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
    }

    // pomocniczy konstruktor (jeśli gdzieś w projekcie używasz ShipType)
    public Ship(ShipType type) {
        this.name = type.name();
        this.size = type.getSize();
    }

    public String getName() { return name; }
    public int getSize() { return size; }

    public boolean isHorizontal() { return horizontal; }
    public void setHorizontal(boolean horizontal) { this.horizontal = horizontal; }

    public void hit() { hits++; }
    public int getHits() { return hits; }
    public boolean isSunk() { return hits >= size; }
}
