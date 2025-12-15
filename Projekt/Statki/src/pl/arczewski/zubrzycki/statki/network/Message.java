package pl.arczewski.zubrzycki.statki.network;

import java.io.Serializable;

/**
 * Przekazywanie wiadomosci sieciowych,
 * poki co tylko struktura danych.
 */
public class Message implements Serializable {

    //TYP WIADOMOŚCI
    public enum Type {
        SHOT,           // oddano strzał
        SHOT_RESULT,    // wynik strzału
        BOOST,          // użycie boosta
        GAME_OVER,      // koniec gry
        READY           // gracz gotowy
    }

    //WSPÓLNE
    private final Type type;

    //STRZAŁ
    private final int x;
    private final int y;

    //WYNIK STRZAŁU
    private final boolean hit;

    //BOOST
    private final String boostKind;

    //KONSTRUKTORY PRYWATNE
    private Message(Type type, int x, int y, boolean hit, String boostKind) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.hit = hit;
        this.boostKind = boostKind;
    }

    //GETTERY

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isHit() {
        return hit;
    }

    public String getBoostKind() {
        return boostKind;
    }
}
