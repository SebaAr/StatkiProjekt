package pl.arczewski.zubrzycki.statki.model;

public enum ShipType {
    CARRIER(5), BATTLESHIP(4), CRUISER(3), SUBMARINE(3), DESTROYER(2);

    private final int size;
    ShipType(int size) { this.size = size; }
    public int getSize() { return size; }

    public static ShipType fromSize(int size) {
        switch(size) {
            case 5: return CARRIER;
            case 4: return BATTLESHIP;
            case 3: return CRUISER;
            case 2: return DESTROYER;
            default: throw new IllegalArgumentException("Niepoprawny rozmiar statku");
        }
    }
}
