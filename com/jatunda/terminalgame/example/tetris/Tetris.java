package com.jatunda.terminalgame.example.tetris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.jatunda.terminalgame.core.KeyCode;
import com.jatunda.terminalgame.core.KeyEvent;
import com.jatunda.terminalgame.core.TerminalGame;

public class Tetris extends TerminalGame {

    private static final int GRID_ROWS = 20;
    private static final int GRID_COLS = 10;
    private static final int START_COL = (GRID_COLS + 1) / 2 - 1;
    private static final Location START_LOCATION = new Location(1, START_COL);
    private static final int LINES_PER_LEVEL = 10;
    private static final String BORDER_CHAR = "⬜";
    private static final String EMPTY_CHAR = "  ";// "⬛";

    private TetrisGrid grid;
    private Tetromino activeTetromino;
    private Deque<BlockType> nextPieces = new LinkedList<BlockType>();
    private Tetromino heldTetromino = null;
    private boolean hasUsedSwapOnThisPiece = false;
    private long timeOfLastGravity = 0;
    private int totalLinesCleared = 0;
    private int score = 0;
    private boolean gameOver = false;

    private String debugOutput = "";

    public static void main(String[] args) {
        Tetris game = new Tetris();
        game.play();
    }

    public Tetris() {
        grid = new TetrisGrid(GRID_ROWS, GRID_COLS);
    }

    public void onStart() {
        timeOfLastGravity = System.nanoTime();
        fillGrabBag();
        spawnNewPiece();
    }

    public void onUpdate() {
        // gravity stuff
        long currTime = System.nanoTime();
        long timePerGravity = (long) (getTimePerGravity(getLevel()) * 1000000000);
        while (timeOfLastGravity + timePerGravity < currTime) {
            boolean moved = tryMove(Direction.DOWN);
            if (!moved) {
                // tried to move down but got blocked
                // this means we spawned a new piece
                timeOfLastGravity = currTime;
                break;
            } else {
                // moved
                timeOfLastGravity += timePerGravity;
            }
        }
    }

    public void onKeyPress(KeyEvent keyEvent) {
        switch (keyEvent.keyCode) {
            case KeyCode.VK_LEFT: // left
            case KeyCode.VK_A:
                tryMove(Direction.LEFT);
                break;

            case KeyCode.VK_DOWN: // down
            case KeyCode.VK_S:
                if (tryMove(Direction.DOWN)) {
                    score += 1;
                }
                break;

            case KeyCode.VK_RIGHT: // right
            case KeyCode.VK_D:
                tryMove(Direction.RIGHT);
                break;

            case KeyCode.VK_J: // ccw rotation
            case KeyCode.VK_Z: // down
                tryRotate(RotationDirection.COUNTERCLOCKWISE);
                break;

            case KeyCode.VK_K: // cw rotation
            case KeyCode.VK_UP: // up
            case KeyCode.VK_W:
                tryRotate(RotationDirection.CLOCKWISE);
                break;

            case KeyCode.VK_SPACE: // spacebar is the DROP
            case KeyCode.VK_X:
                while (tryMove(Direction.DOWN)) {
                    score += 2;
                }
                break;

            case KeyCode.VK_C: // hold
                tryHold();
                break;
            case KeyCode.VK_Q: // debug clear board
                for (int i = 0; i < grid.getRows(); i++) {
                    for (int j = 0; j < grid.getCols(); j++) {
                        grid.set(i, j, BlockType.NONE);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onShutdown() {
        // draw active piece on grid, but if it's an overlap, turn that space into an ERROR
        for (Location loc : activeTetromino.getLocations()) {
            if (grid.get(loc.row, loc.col) != BlockType.NONE) {
                grid.set(loc.row, loc.col, BlockType.ERROR);
            } else {
                grid.set(loc.row, loc.col, activeTetromino.blockType);
            }
        }   
    }

    /**
     * @param direction
     * @return true if moved successfully, false otherwise
     */
    private boolean tryMove(Direction direction) {

        // generate potential new tetromino
        Tetromino newTetromino = activeTetromino.moved(direction, 1);

        if (!isNewTetrominoAllowed(newTetromino)) {
            // move failed
            if (direction == Direction.DOWN) {
                int linesCleared = grid.clearFullLines();
                score += (((linesCleared + 2) / 2) * ((linesCleared + 3) / 2) - 1) * 100;
                totalLinesCleared += linesCleared;
                spawnNewPiece();
                hasUsedSwapOnThisPiece = false;
            }
            return false;
        }

        // actually do the move
        grid.setLocations(activeTetromino.getLocations(), BlockType.NONE);
        grid.setLocations(newTetromino.getLocations(), newTetromino.blockType);

        activeTetromino = newTetromino;
        return true;
    }

    /**
     * @param rotationDirection
     * @return true if rotated successfully, false otherwise.
     */
    private boolean tryRotate(RotationDirection rotationDirection) {

        if (activeTetromino.blockType == BlockType.O) {
            return true;
        }

        Tetromino newTetromino = null;

        List<Tetromino> wallkicks = activeTetromino.getWallKicks(rotationDirection);

        for(Tetromino wallkick : wallkicks) {
            if (isNewTetrominoAllowed(wallkick)) {
                newTetromino = wallkick;
                break;
            }
        }
        if (newTetromino == null) {
            return false;
        }

        // if we reach this point, we have a valid rotation!
        grid.setLocations(activeTetromino.getLocations(), BlockType.NONE); // clear old piece
        grid.setLocations(newTetromino.getLocations(), newTetromino.blockType); // set new piece
        activeTetromino = newTetromino;

        return true;
    }

    private void tryHold() {
        if (heldTetromino == null) {
            heldTetromino = activeTetromino;
            grid.setLocations(activeTetromino.getLocations(), BlockType.NONE);
            spawnNewPiece();
        } else {
            if (hasUsedSwapOnThisPiece) {
                return;
            }

            // remove current active piece from grid
            grid.setLocations(activeTetromino.getLocations(), BlockType.NONE);

            // swap the held piece with the active piece
            Tetromino temp = activeTetromino;
            activeTetromino = new Tetromino(heldTetromino.blockType, START_LOCATION, 0);
            heldTetromino = temp;

            // put the new active piece into the grid
            grid.setLocations(activeTetromino.getLocations(), activeTetromino.blockType);

            hasUsedSwapOnThisPiece = true;
        }
    }

    private void spawnNewPiece() {

        if (nextPieces.size() < 7) {
            fillGrabBag();
        }

        activeTetromino = new Tetromino(nextPieces.removeFirst(), START_LOCATION, 0);

        // dying
        for (Location loc : activeTetromino.getLocations()) {
            if (grid.get(loc.row, loc.col) != BlockType.NONE) {
                gameOver = true;
                this.initiateShutdown();
                return;
            }
        }

        // put into grid
        for (Location loc : activeTetromino.getLocations()) {
            grid.set(loc.row, loc.col, activeTetromino.blockType);
        }
    }

    public void fillGrabBag() {
        ArrayList<BlockType> bag = new ArrayList<BlockType>();
        bag.add(BlockType.T);
        bag.add(BlockType.O);
        bag.add(BlockType.I);
        bag.add(BlockType.J);
        bag.add(BlockType.L);
        bag.add(BlockType.S);
        bag.add(BlockType.Z);
        Collections.shuffle(bag);
        nextPieces.addAll(bag);
    }

    private boolean isNewTetrominoAllowed(Tetromino newTetromino) {
        for (Location newLoc : newTetromino.getLocations()) {
            // if the potential location is found in our tetromino, continue
            if (activeTetromino.contains(newLoc)) {
                continue;
            }

            // else if piece cannot move (aka space occupied or out of bounds)
            else if (!grid.isInBounds(newLoc) || grid.get(newLoc.row, newLoc.col) != BlockType.NONE) {
                return false;
            }
        }
        return true;
    }

    private static double getTimePerGravity(int level) {
        return Math.pow(0.8 - ((level - 1) * 0.007), level - 1);
    }

    public int getLevel() {
        return totalLinesCleared / LINES_PER_LEVEL + 1;
    }

    /**
     * Render output to the console.
     * Runs after each onStep, and once after GameManager.shutdown()
     * Screen is cleared before each render(), so this function should draw the
     * entire screen.
     * It is recommended that this function contain no game logic, and does not have
     * any side effects.
     */
    @Override
    public void onRender() {
        // rendering NEXT
        System.out.println("       +--NEXT--+    +--HOLD--+");
        Tetromino nextTetromino = new Tetromino(nextPieces.peekFirst(), new Location(1, 5), 0);
        Tetromino heldTetromino = null;
        if (this.heldTetromino != null) {
            heldTetromino = new Tetromino(this.heldTetromino.blockType, new Location(1, 12), 0);
        }
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 15; c++) {
                if (nextTetromino.contains(new Location(r, c))) {
                    System.out.print(blockTypeToEmoji(nextPieces.peekFirst()));
                } else if (heldTetromino != null && heldTetromino.contains(new Location(r, c))) {
                    System.out.print(blockTypeToEmoji(heldTetromino.blockType));
                } else {
                    System.out.print(blockTypeToEmoji(BlockType.NONE));
                }
            }
            System.out.println();
        }

        String borderRow = "";
        for (int c = -1; c < grid.getCols() + 1; c++) {
            borderRow += BORDER_CHAR;
        }
        System.out.println(borderRow);
        for (int r = 0; r < grid.getRows(); r++) {
            BlockType[] row = grid.getGrid()[r];
            String outputRow = BORDER_CHAR;
            for (int c = 0; c < row.length; c++) {

                BlockType blockType = row[c];
                Location currLocation = new Location(r, c);
                if (!activeTetromino.contains(currLocation)
                        && isGhostLocation(currLocation)) {
                    blockType = BlockType.GHOST; // override with ghost
                }
                outputRow += blockTypeToEmoji(blockType);
            }
            outputRow += BORDER_CHAR;
            System.out.println(outputRow);
        }
        System.out.println(borderRow);

        System.out.println("Lines cleared: " + totalLinesCleared);
        System.out.println("Level: " + getLevel());
        System.out.println("Score: " + score);
        if (gameOver) {
            System.out.println("Game Over!");
        }
        System.out.println("debug:" + debugOutput);
        debugOutput = "";
    }

    private String blockTypeToEmoji(BlockType blockType) {
        switch (blockType) {
            case BlockType.Z:
                return "🟥"; // Z
            case BlockType.L:
                return "🟧"; // L
            case BlockType.O:
                return "🟨"; // O
            case BlockType.S:
                return "🟩"; // S
            case BlockType.J:
                return "🟦"; // J
            case BlockType.T:
                return "🟪"; // T
            case BlockType.I:
                return "🧊"; // I
            case BlockType.GHOST:
                return "⬛"; // Ghost
            case BlockType.ERROR:
                return "❌"; // Error
            default:
                return EMPTY_CHAR;
        }
    }

    private boolean isGhostLocation(Location location) {
        Tetromino bestGhost = activeTetromino;
        while (true) {
            Tetromino nextGhost = bestGhost.moved(Direction.DOWN, 1);
            if (!isNewTetrominoAllowed(nextGhost)) {
                break;
            }
            bestGhost = nextGhost;
        }
        return bestGhost.contains(location);
    }

    // TODO: lock delay - 0.5s up to level 20, and faster after that
    // A piece has 0.5 seconds after landing on the stack before it locks down; 
    // for games with Master mode, the lock down delay value will decrease per level when the gravity is 20G. 
    // With infinity, rotating or moving the piece will reset this timer. 
    // With move reset, this is limited to 15 moves/rotations
}
