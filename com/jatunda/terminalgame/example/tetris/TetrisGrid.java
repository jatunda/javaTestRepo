package com.jatunda.terminalgame.example.tetris;

import java.util.List;

public class TetrisGrid {
    private final BlockType[][] grid;

    public TetrisGrid(int rows, int cols) {
        grid = new BlockType[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = BlockType.NONE;
    }

    public int getRows() { return grid.length; }
    public int getCols() { return grid[0].length; }

    public BlockType get(int row, int col) {
        return grid[row][col];
    }

    public void set(int row, int col, BlockType type) {
        grid[row][col] = type;
    }

    public void setLocations(List<Location> locations, BlockType type) {
        for (Location loc : locations)
            grid[loc.row][loc.col] = type;
    }

    public boolean isInBounds(Location loc) {
        return loc.row >= 0 && loc.row < grid.length && loc.col >= 0 && loc.col < grid[0].length;
    }

    public boolean isEmpty(Location loc) {
        return isInBounds(loc) && grid[loc.row][loc.col] == BlockType.NONE;
    }

    public int clearFullLines() {
        int linesCleared = 0;
        for (int r = 0; r < grid.length; r++) {
            boolean full = true;
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == BlockType.NONE) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
                // Move all previous lines down
                for (int innerR = r; innerR > 0; innerR--)
                    grid[innerR] = grid[innerR - 1];
                // Fill top line with empty
                grid[0] = new BlockType[grid[0].length];
                for (int i = 0; i < grid[0].length; i++)
                    grid[0][i] = BlockType.NONE;
                r--; // recheck this row
            }
        }
        return linesCleared;
    }

    public BlockType[][] getGrid() {
        return grid;
    }
}