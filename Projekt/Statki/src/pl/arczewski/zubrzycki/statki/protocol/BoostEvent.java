package pl.arczewski.zubrzycki.statki.protocol;

import java.io.Serializable;

public class BoostEvent implements GameEvent, Serializable {

    public static final String TYPE = "BOOST";

    public enum Kind {
        RADAR,
        SWAP_SELECT,      // kliknięcie statku (na razie niewykorzystywane z sieci)
        SWAP_MOVE,        // przesunięcie statku
        TROLL_PLACE       // ustawienie trolla
    }

    public final Kind kind;

    // współrzędne (jeśli potrzebne)
    public final int x;
    public final int y;

    // rozmiar i orientacja (SWAP i TROLL)
    public final int size;
    public final boolean horizontal;

    public BoostEvent(Kind kind, int x, int y, int size, boolean horizontal) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.size = size;
        this.horizontal = horizontal;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
