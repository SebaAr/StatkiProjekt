package pl.arczewski.zubrzycki.statki.protocol;

import java.io.Serializable;

public interface GameEvent extends Serializable {
    String getType();
}
