package com.jatunda.terminalgame.example.tetris;

import java.util.ArrayList;
import java.util.List;

public class Tetromino {
    public final BlockType blockType;
    public final Location pivot;
    public final int rotation; // 0-3

    public Tetromino(BlockType blockType, Location pivot, int rotation) {
        this.blockType = blockType;
        this.pivot = pivot;
        this.rotation = rotation;
    }

    public boolean contains(Location loc) {
        return getLocations().contains(loc);
    }

    public List<Location> getLocations() {
        List<Location> output = new ArrayList<>(4);
        int row = pivot.row;
        int col = pivot.col;
        switch (blockType) {
            case I:
                for (int i = -1; i <= 2; i++) {
                    output.add(new Location(row, col + i));
                }
                break;
            case O:
                output.add(new Location(row, col));
                output.add(new Location(row - 1, col));
                output.add(new Location(row, col + 1));
                output.add(new Location(row - 1, col + 1));
                break;
            case T:
                output.add(new Location(row, col));
                output.add(new Location(row, col + 1));
                output.add(new Location(row, col - 1));
                output.add(new Location(row - 1, col));
                break;
            case J:
                output.add(new Location(row, col));
                output.add(new Location(row, col + 1));
                output.add(new Location(row, col - 1));
                output.add(new Location(row - 1, col - 1));
                break;
            case L:
                output.add(new Location(row, col));
                output.add(new Location(row, col + 1));
                output.add(new Location(row, col - 1));
                output.add(new Location(row - 1, col + 1));
                break;
            case Z:
                output.add(new Location(row, col));
                output.add(new Location(row, col + 1));
                output.add(new Location(row - 1, col));
                output.add(new Location(row - 1, col - 1));
                break;
            case S:
                output.add(new Location(row, col));
                output.add(new Location(row, col - 1));
                output.add(new Location(row - 1, col));
                output.add(new Location(row - 1, col + 1));
                break;
            default:
                break;
        } 

        if(rotation == 3) {
            return rotateLocations(output, pivot, blockType == BlockType.I, RotationDirection.COUNTERCLOCKWISE);
        }
        
        for (int i = 0; i < rotation; i++) {
            output = rotateLocations(output, pivot, blockType == BlockType.I, RotationDirection.CLOCKWISE);
        }
        return output;
    }

    public Tetromino moved(Direction dir, int distance) {
        int newRow = pivot.row;
        int newCol = pivot.col;
        switch (dir) {
            case UP:
                newRow -= distance;
                break;
            case DOWN:
                newRow += distance;
                break;
            case LEFT:
                newCol -= distance;
                break;
            case RIGHT:
                newCol += distance;
                break;
        }
        return new Tetromino(blockType, new Location(newRow, newCol), rotation);
    }

    public Tetromino rotated(RotationDirection dir) {
        return new Tetromino(blockType, pivot, rotate(rotation, dir));
    }

    public static int rotate(int rotation, RotationDirection rotationDirection) {
        int newRotation = rotation + (rotationDirection == RotationDirection.CLOCKWISE ? 1 : -1);
        newRotation = (newRotation + 4) % 4;
        return newRotation;
    }

    public List<Tetromino> getWallKicks(RotationDirection rotationDirection) {
        List<Tetromino> wallKicks = new ArrayList<>();
        List<Location> wallKickOffsets = getWallKickOffsets(blockType, rotation, rotationDirection);
        for (Location offset : wallKickOffsets) {
            wallKicks.add(
                new Tetromino(
                    blockType, 
                    new Location(pivot.row + offset.row, pivot.col + offset.col), 
                    rotate(rotation, rotationDirection)));
        }
        return wallKicks;
    }

    private static List<Location> getWallKickOffsets(BlockType blockType, int rotation, RotationDirection dir) {
        
        List<Location> wallKicks = new ArrayList<>();
        wallKicks.add(new Location(0, 0)); 
        if (blockType == BlockType.I) {
            if(rotation == 0 && dir == RotationDirection.CLOCKWISE
                || rotation == 3 && dir == RotationDirection.COUNTERCLOCKWISE) {
                wallKicks.add(new Location(0, -2));
                wallKicks.add(new Location(0, 1));
                wallKicks.add(new Location(-1, -2));
                wallKicks.add(new Location(2, 1));
            } else if (rotation == 1 && dir == RotationDirection.COUNTERCLOCKWISE
                || rotation == 2 && dir == RotationDirection.CLOCKWISE) {
                wallKicks.add(new Location(0, 2));
                wallKicks.add(new Location(0, -1));
                wallKicks.add(new Location(1, 2));
                wallKicks.add(new Location(-2, -1));
            } else if (rotation == 1 && dir == RotationDirection.CLOCKWISE 
                || rotation == 0 && dir == RotationDirection.COUNTERCLOCKWISE) {
                wallKicks.add(new Location(0, -1));
                wallKicks.add(new Location(0, 2));
                wallKicks.add(new Location(2, -1));
                wallKicks.add(new Location(-1, 2));
            } else if (rotation == 2 && dir == RotationDirection.COUNTERCLOCKWISE 
                || rotation == 3 && dir == RotationDirection.CLOCKWISE) {
                wallKicks.add(new Location(0, 1));
                wallKicks.add(new Location(0, -2));
                wallKicks.add(new Location(-2, 1));
                wallKicks.add(new Location(1, -2));
            } else  {
                // should never reach here
                throw new IllegalStateException("Unexpected rotation value: " + rotation);
            }
        } else {
            if(rotation == 1){
                wallKicks.add(new Location(0, 1));
                wallKicks.add(new Location(-1, 1));
                wallKicks.add(new Location(2, 0));
                wallKicks.add(new Location(2, 1));
            } else if (rotation == 3){
                wallKicks.add(new Location(0, -1));
                wallKicks.add(new Location(-1, -1));
                wallKicks.add(new Location(2, 0));
                wallKicks.add(new Location(2, -1));
            } else if (rotation == 0 && dir == RotationDirection.CLOCKWISE 
            || rotation == 2 && dir == RotationDirection.COUNTERCLOCKWISE) {
                wallKicks.add(new Location(0, -1));
                wallKicks.add(new Location(-1, 1));
                wallKicks.add(new Location(-2, 0));
                wallKicks.add(new Location(-2, -1));
            } else if (rotation == 0 && dir == RotationDirection.COUNTERCLOCKWISE
            || rotation == 2 && dir == RotationDirection.CLOCKWISE) {
                wallKicks.add(new Location(0, 1));
                wallKicks.add(new Location(1, 1));
                wallKicks.add(new Location(-2, 0));
                wallKicks.add(new Location(-2, 1));
            } else {
                // should never reach here
                throw new IllegalStateException("Unexpected rotation value: " + rotation);
            }
        }

        // our grid is flipped vertically, so we need to flip the wall kicks
        for (int i = 0; i < wallKicks.size(); i++) {
            Location wallKick = wallKicks.get(i);
            wallKicks.set(i, new Location(-wallKick.row, wallKick.col));
        }
        return wallKicks;
    }

    private static List<Location> rotateLocations(List<Location> locations, Location pivot, boolean shouldOffsetPivot, RotationDirection rotationDirection) {
        return locations.stream().map(
                loc -> rotateLocation(loc, pivot, shouldOffsetPivot, rotationDirection) 
        ).toList();
    }

    private static Location rotateLocation(Location loc, Location pivot, boolean shouldOffsetPivot, RotationDirection rotationDirection) {
        float pivotRow = pivot.row;
        float pivotCol = pivot.col;
        if (shouldOffsetPivot) {
            pivotRow -= 0.5;
            pivotCol += 0.5;
        }
        float dCol = loc.col - pivotCol;
        float dRow = loc.row - pivotRow;
        if(rotationDirection == RotationDirection.COUNTERCLOCKWISE) {
            dCol = -dCol;
            dRow = -dRow;
        }
        return new Location((int) (pivotRow + dCol), (int) (pivotCol - dRow));
    }
}