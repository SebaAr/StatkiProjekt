package pl.arczewski.zubrzycki.statki.logic;


public class Ship{
    private int length;
    private boolean sunk;

    public Ship(int length) {
        this.length = length;
        this.sunk = false;
    }

    public int getLength() {
        return length;
    }

    public boolean isSunk() {
        return sunk;
    }

    public void hit() {
        System.out.println("Trafiono statek!");
        sunk = true;
    }
}
