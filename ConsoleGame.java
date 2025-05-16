public class ConsoleGame {
    
    // variables for your game go here
    // YOUR CODE HERE

    // Example Code start
    int numWPresses = 0;
    int timer = 0;
    private boolean gameOver;
    // Example Code end

    public ConsoleGame(){
        // initialize your game here
        // YOUR CODE HERE

        // Example Code
        {
            gameOver = false;
        }

    }

    /**
     * Default: executes 60 times a second.
     * Is immediately followed by a render()
     * To change update frequency, change UPDATES_PER_SECOND in GameManager.java
     * */
    public void onUpdate() {
        /* NOTE: to end the game, call GameManager.shutdown() */
        // YOUR CODE HERE

        // Example code
        {
            timer++;

            if(timer >= 100 || numWPresses >= 10) {
                ConsoleGameManager.shutdown();
                gameOver = true;
            }
        }

    }


    /**
     * Runs every time user presses a key. Is immediately followed by a render().
     * @param c character of input. 'A' and 'a' are distinct. Only allows alphanumerics, and symbols, 
     *    no modifiers (e.g. shift, ctrl) or other special keys (e.g. esc, numlock).
     */
    public void onKeyPress(char c) {
        /* NOTE: to end the game, call GameManager.shutdown() */
        // YOUR CODE HERE
        
        // Example code
        {
            numWPresses++;
        }
    }


    /**
     * Render output to the console.
     * Runs after each onStep,  after each onKeyPress, and once after GameManager.shutdown()
     * It is recommended to begin each render by clearing the screen using GameManager.clearScreen()
     * Instead of System.out.println, please use GameManager.println(). See GameManager.println() for reasoning.
     * It is recommended that this function contain no game logic, and does not have any side effects.  
     */
    public void render(){
        ConsoleGameManager.clearScreen();

        // Example code
        {
            ConsoleGameManager.println("\rWs: " + numWPresses);
            ConsoleGameManager.println("\rTime: " + timer);

            if(gameOver) {
                ConsoleGameManager.println("game over");
                ConsoleGameManager.println("score: " + (numWPresses+timer));
            }
        }

    }

}
