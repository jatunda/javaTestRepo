import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerminalGame {

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
    }

    private enum RotationDirection {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private class Location {
        public final int row;
        public final int col;
        public Location(int row, int col) {
            this.row = row;
            this.col = col;
        } 

        @Override
        public boolean equals(Object other) {
            if(other == this) { return true; }
            if(other == null) { return false; }
            if(!(other instanceof Location)) { return false; }
            Location otherLocation = (Location) other;
            return row == otherLocation.row && col == otherLocation.col;
        }

        @Override 
        public String toString() {
            return "<row:" + row + ", col:" + col + ">";
        }
    }

    private char[][] grid;
    private final String BORDER_CHAR = "⬜";
    private final String EMPTY_CHAR = "  ";//"⬛";
    private int cursorRow = 0;
    private int cursorCol = 0;
    private char blockType = 'O'; // I,O,T,S,Z,J,L
    private int cursorRotation = 0; // will be incremented/decremented based on blockType

    private long timeOfLastGravity=0; 
    private int totalLinesCleared = 0;

    private int score = 0;
    private char mostRecentKeypress;
    private String debugOutput = "";
    private ArrayList<Character> nextPieces = new ArrayList<Character>(7);

    private boolean gameOver = false; 

    public TerminalGame(){

        grid = new char[20][10];
        for(int r = 0; r < grid.length; r++) {
            for(int c = 0; c < grid[0].length; c++) {
                grid[r][c] = '-';
            }
        }

        timeOfLastGravity = System.nanoTime();
        spawnNewPiece();
    }


    private void spawnNewPiece() {

        // set block type
        if(nextPieces.isEmpty()) {
            nextPieces.add('T');
            nextPieces.add('O');
            nextPieces.add('I');
            nextPieces.add('J');
            nextPieces.add('L');
            nextPieces.add('S');
            nextPieces.add('Z');
            Collections.shuffle(nextPieces);
        }
        blockType = nextPieces.removeFirst();
        if(nextPieces.isEmpty()) {
            nextPieces.add('T');
            nextPieces.add('O');
            nextPieces.add('I');
            nextPieces.add('J');
            nextPieces.add('L');
            nextPieces.add('S');
            nextPieces.add('Z');
            Collections.shuffle(nextPieces);
        }

        // set cursor location 
        cursorRow = 1;
        cursorCol = 4;
        if(blockType == 'I'){
            cursorRow--;
        } 

        // set rotation 
        cursorRotation = 0;

        // dying
        for(Location loc: getActiveTetromino()) {
            if(grid[loc.row][loc.col] != '-') {
                gameOver = true;
                TerminalGameManager.shutdown();
            }
        }

        // put into grid
        for(Location loc : getActiveTetromino()) {
            grid[loc.row][loc.col] = blockType;
        }
    }

    private static double getTimePerGravity(int level) {
        return Math.pow(0.8 - ((level-1)*0.007), level-1);
    }

    private List<Location> generateTetromino(Location pivot, char blockType, int rotation) {
        List<Location> output = new ArrayList<>(4);
        if(blockType == 'I')
        {
            for(int i = -1; i <= 2; i++) { // default horizontal mode
                output.add(new Location(pivot.row, pivot.col+i));
            } 
        }
        else if (blockType == 'O') 
        {
            output.add(new Location(pivot.row,pivot.col));
            output.add(new Location(pivot.row-1,pivot.col));
            output.add(new Location(pivot.row,pivot.col+1));
            output.add(new Location(pivot.row-1,pivot.col+1));
        }
        else if (blockType == 'T') 
        {
            output.add(new Location(pivot.row,pivot.col));
            output.add(new Location(pivot.row,pivot.col+1));
            output.add(new Location(pivot.row,pivot.col-1));
            output.add(new Location(pivot.row-1,pivot.col));
        }
        else if (blockType == 'J') 
        {
            output.add(new Location(pivot.row,pivot.col));
            output.add(new Location(pivot.row,pivot.col+1));
            output.add(new Location(pivot.row,pivot.col-1));
            output.add(new Location(pivot.row-1,pivot.col-1));
        }
        else if (blockType == 'L') 
        {
            output.add(new Location(pivot.row,pivot.col));
            output.add(new Location(pivot.row,pivot.col+1));
            output.add(new Location(pivot.row,pivot.col-1));
            output.add(new Location(pivot.row-1,pivot.col+1));
        }
        else if (blockType == 'Z') 
        {
            output.add(new Location(pivot.row,pivot.col));
            output.add(new Location(pivot.row,pivot.col+1));
            output.add(new Location(pivot.row-1,pivot.col));
            output.add(new Location(pivot.row-1,pivot.col-1));
        }
        else if (blockType == 'S') 
        {
            output.add(new Location(pivot.row,pivot.col));
            output.add(new Location(pivot.row,pivot.col-1));
            output.add(new Location(pivot.row-1,pivot.col));
            output.add(new Location(pivot.row-1,pivot.col+1));
        }

        for(int i = 0; i < rotation; i++) {
            output = rotateTetromino(output, pivot , RotationDirection.CLOCKWISE, blockType=='I');
        }

        return output;
    }

    private List<Location> getActiveTetromino() { // gets them in default mode
        return generateTetromino(new Location(cursorRow, cursorCol), blockType, cursorRotation);
    }

    public int getLevel() {
        return totalLinesCleared/10;
    }

    private List<Location> translateTetromino(List<Location> tetromino, Direction direction, int distance) {
        List<Location> newTetromino = new ArrayList<Location>(4);
        for(Location loc : tetromino) {
            int newCol = loc.col;
            int newRow = loc.row;
            if(direction == Direction.UP) { newRow-=distance; }
            if(direction == Direction.DOWN) { newRow+=distance; }
            if(direction == Direction.LEFT) { newCol-=distance; }
            if(direction == Direction.RIGHT) { newCol+=distance; }
            newTetromino.add(new Location(newRow, newCol));
        }
        return newTetromino;
    }

    /**
     * 
     * @param direction
     * @return true if moved successfully, false otherwise
     */
    private boolean tryMove(List<Location> tetromino, Direction direction) {
        
        // generate potential new tetromino
        List<Location> newTetromino = translateTetromino(tetromino, direction, 1);

        if(!isNewTetrominoAllowed(newTetromino))
        {
            // move failed
            if(direction == Direction.DOWN) {
                int linesCleared = clearFullLines();
                score += (((linesCleared +2)/2) * ((linesCleared+3)/2) - 1) * 100;
                totalLinesCleared += linesCleared;
                spawnNewPiece();
            }
            return false;
        }
        
        
        // if we are here, all the blocks can move

        // actually do the move
        // remove previous blocks
        setGridLocations(tetromino, '-');
        setGridLocations(newTetromino, blockType);

        if(direction == Direction.UP) { cursorRow--; }
        if(direction == Direction.DOWN) { cursorRow++; }
        if(direction == Direction.LEFT) { cursorCol--; }
        if(direction == Direction.RIGHT) { cursorCol++; }
        
        return true;
    }

    private boolean locationInBounds(Location location) {
        return location.row >= 0 
                && location.row < grid.length 
                && location.col >= 0
                && location.col < grid[0].length;
    }

    private void setGridLocations(List<Location> locations, char newValue) {
        for(Location loc : locations) {
            grid[loc.row][loc.col] = newValue;
        }
    }

    private int clearFullLines() {

        int linesCleared = 0;
        for(int r = 0; r < grid.length; r++) {
            
            // if the line is full
            boolean isLineFull = true;
            for(int c = 0; c < grid[r].length; c++) {
                if(grid[r][c] == '-') {
                    isLineFull = false;
                    break;
                }
            }

            if(isLineFull){
                linesCleared++;

                // move all previous lines down
                for(int innerR = r; innerR>0; innerR--) {
                    grid[innerR] = grid[innerR-1];
                }

                // fill top line with empty
                grid[0] = new char[10];
                for(int i = 0; i < grid[0].length; i++) {
                    grid[0][i] = '-';
                }

                // do not increment so we don't skip checking any lines
                r--;
            }
        
            
        }
        return linesCleared;
    }

    
    private boolean isNewTetrominoAllowed(List<Location> newTetromino) {
        for(Location newLoc : newTetromino) {
            // if the potential location is found in our tetromino, continue
            if(getActiveTetromino().contains(newLoc)) { continue; }

            // else if piece cannot move (aka space occupied or out of bounds)
            else if ( !locationInBounds(newLoc) || grid[newLoc.row][newLoc.col] != '-' )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @param rotationDirection only works with left/right
     * @return true if rotated successfully, false otherwise.
     */
    private boolean tryRotate(RotationDirection rotationDirection) {

        if (blockType == 'O') { return true; }

        List<Location> newTetromino;

        // get rotated version of tetromino
        Location rotationPivot = new Location(cursorRow, cursorCol);
        newTetromino = rotateTetromino(getActiveTetromino(), rotationPivot, rotationDirection, blockType == 'I');
        
        // check each spot as valid
        if(!isNewTetrominoAllowed(newTetromino)) {
            return false;
        }
        

        // if we reach this point, we have a valid rotation!

        // actually set the grid and rotation
        setGridLocations(getActiveTetromino(), '-');
        setGridLocations(newTetromino, blockType);
        if(rotationDirection == RotationDirection.CLOCKWISE) {
            cursorRotation++;
        } else {
            cursorRotation--;
        }
        cursorRotation += 4; // prevent negative mod values
        cursorRotation %= 4;
        
        return true;
    }

    private List<Location> rotateTetromino(List<Location> tetromino, Location rotationPivot, RotationDirection rotationDirection, boolean isTetrominoIPiece) {
        return tetromino.stream().map(
                location -> rotateLocation(location, rotationPivot, rotationDirection, isTetrominoIPiece)).toList();
    }

    private Location rotateLocation(Location locationToRotate, Location pivot, RotationDirection rotationDirection, boolean shouldOffsetForIPiece) {
        
        float pivotRow = pivot.row;
        float pivotCol = pivot.col;

        if(shouldOffsetForIPiece) {
            pivotRow -= 0.5;
            pivotCol += 0.5;
        }
        
        float dCol = locationToRotate.col - pivotCol; 
        float dRow = locationToRotate.row - pivotRow;

        if(shouldOffsetForIPiece) {
            if(rotationDirection == RotationDirection.CLOCKWISE) {
                return new Location((int)(cursorRow - 0.5 + dCol), (int)(cursorCol + 0.5 -dRow));
            } else {
                return new Location((int)(cursorRow - 0.5 -dCol), (int)(cursorCol + 0.5 +dRow));
            }
        }

        if(rotationDirection == RotationDirection.CLOCKWISE) {
            return new Location((int)(cursorRow + dCol), (int)(cursorCol-dRow));
        } else {
            return new Location((int)(cursorRow-dCol), (int)(cursorCol+dRow));
        }
    }

    /**
     * Default: executes 60 times a second.
     * Is immediately followed by a render()
     * To change update frequency, change UPDATES_PER_SECOND in GameManager.java
     * */
    public void onUpdate() {
        /* NOTE: to end the game, call GameManager.shutdown() */
        
        // gravity stuff
        long currTime = System.nanoTime();
        long timePerGravity = (long)(getTimePerGravity(getLevel()) * 1000000000);
        while(timeOfLastGravity + timePerGravity < currTime) {
            boolean moved = tryMove(getActiveTetromino(), Direction.DOWN);
            if(!moved) {
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




    /**
     * Runs every time user presses a key.
     * @param c character of input. 'A' and 'a' are distinct. Only allows alphanumerics, and symbols, 
     *    no modifiers (e.g. shift, ctrl) or other special keys (e.g. esc, numlock).
     *    HACK: arrow keys are up:'A', down:'B', right:'C', left:'D' 
     */
    public void onKeyPress(char c) {
        /* NOTE: to end the game, call GameManager.shutdown() */

        mostRecentKeypress = c;
        // left/right moves left and right
        // down moves down (and resets gravity timer?)
        // turn cw and ccw do those things (might do nothing if rotate is not possible)
        // spacebar drops
        switch(c){

            case 'w': //up
            case 'A':
                // no up button in this game
                break;

            case 'a': // left
            case 'D':
                tryMove(getActiveTetromino(), Direction.LEFT);
                break;

            case 's': // down
            case 'B':
                tryMove(getActiveTetromino(), Direction.DOWN);
                score+=1;
                break;

            case 'd': // right
            case 'C':
                tryMove(getActiveTetromino(), Direction.RIGHT);
                break;

            case 'j': // ccw rotation
                tryRotate(RotationDirection.COUNTERCLOCKWISE);
                break;

            case 'k': // cw rotation
                tryRotate(RotationDirection.CLOCKWISE);
                break;

            case ' ': // spacebar is the DROP
                while(tryMove(getActiveTetromino(), Direction.DOWN)) {
                    score+=2;
                }
                break;
            case 'q':
                for(int i = 0; i < grid.length; i++) {
                    for(int j = 0; j < grid[i].length; j++) {
                        grid[i][j] = '-';
                    }
                }
                break;
            default:
                break;
        }



    }

    private boolean isGhostLocation(Location location) {
        List<Location> activeTetromino = getActiveTetromino();
        for(int i = 20; i > 0; i--) {
            List<Location> translatedTeromino = translateTetromino(activeTetromino, Direction.DOWN, i);
            if(isNewTetrominoAllowed(translatedTeromino)) {
                return translatedTeromino.contains(location);
            }
        }
        // should never reach here
        return getActiveTetromino().contains(location);
    }


    /**
     * Render output to the console.
     * Runs after each onStep, and once after GameManager.shutdown()
     * Screen is cleared before each render(), so this function should draw the entire screen.
     * It is recommended that this function contain no game logic, and does not have any side effects.  
     */
    public void render(){
        // rendering NEXT 
        System.out.println("       +--NEXT--+");
        List<Location> nextTetromino = generateTetromino(new Location(1,5), nextPieces.get(0), 0);
        for(int r = 0; r < 2; r++) {
            for(int c = 0; c < 10; c++){
                if(nextTetromino.contains(new Location(r,c))) {
                    System.out.print(blockTypeToEmoji(nextPieces.get(0)));
                } else {
                    System.out.print(blockTypeToEmoji('-'));
                }
            }
            System.out.println();
        }

        String borderRow = "";
        for(int c = -1; c < grid[0].length + 1; c++) {
            borderRow += BORDER_CHAR;
        }
        System.out.println(borderRow);
        for(int r = 0; r < grid.length; r++) {
            char[] row = grid[r];
            String outputRow = BORDER_CHAR;
            for(int c = 0; c < row.length; c++) {

                char blockType = row[c];

                Location currLocation = new Location(r,c);
                if(!getActiveTetromino().contains(currLocation)) {
                    if(isGhostLocation(currLocation))
                    {
                        blockType = 'G'; // override with ghost
                    }
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
        System.out.println("most recent key: " + mostRecentKeypress);
        if(gameOver) {System.out.println("Game Over!");}
        System.out.println("debug:" + debugOutput);
        debugOutput = "";
    }

    private String blockTypeToEmoji(char blockType){
        switch (blockType) {
            case 'Z':
                return "🟥"; // Z
            case 'L':
                return "🟧"; // L
            case 'O':
                return "🟨"; // O
            case 'S':
                return "🟩"; // S
            case 'J':
                return "🟦"; // J
            case 'T':
                return "🟪"; // T
            case 'I':
                return "🧊"; // I
            case 'G':
                return "⬛"; // Ghost
            default:
                return EMPTY_CHAR;
        }
    }

    // TODO: lock delay - 0.5s before level 20... idk after 
    // TODO: ghost for where tetromino would land after a drop
    // TODO: rotation wallkicks/floorkicks
    // TODO: hold
}
