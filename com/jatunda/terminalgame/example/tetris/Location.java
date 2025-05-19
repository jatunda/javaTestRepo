package com.jatunda.terminalgame.example.tetris;

import java.util.Objects;

public class Location {
    public final int row;
    public final int col;
    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof Location)) return false;
        Location o = (Location) other;
        return row == o.row && col == o.col;
    }
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    @Override
    public String toString() {
        return "<row:" + row + ", col:" + col + ">";
    }
}