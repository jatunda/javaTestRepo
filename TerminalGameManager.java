import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TerminalGameManager {
    
    private static final int UPDATES_PER_SECOND = 60;

    private static TerminalGame myGame;
    private static boolean shouldShutDown = false;

    private static boolean shouldRenderAfterUpdate = true;
    private static boolean shouldRenderAfterKeyPress = false;
    private static boolean shouldRenderAfterShutdown = true;

    // TODO: add support for arrow keys 
    //      they are ANSI escape codes A,B,C,D)
    //      would require breaking changes to onkeypress parameter type

    public static void main(String[] args) throws IOException, InterruptedException {

        // set console to raw mode
        // raw mode is how we are able to get instant input for onKeyPress
        TerminalHelper.setTerminalRawMode(true);

        // initialize game
        myGame = new TerminalGame();

        // initialize rendering stuff
        TerminalHelper.clearScreen();

        // init onUpdate thread
        ScheduledExecutorService updateExecutor = Executors.newSingleThreadScheduledExecutor();
        long updateIntervalNanos = 1_000_000_000L / UPDATES_PER_SECOND;
        updateExecutor.scheduleAtFixedRate(
            () -> {
                if(!shouldShutDown) {
                    myGame.onUpdate();
                    if(shouldRenderAfterUpdate) {
                        render();
                    }
                }
            },
            0, 
            updateIntervalNanos, 
            TimeUnit.NANOSECONDS);

        // init onKeyPress thread
		Console console = System.console();
		Reader reader = console.reader();
        Thread keypressThread = new Thread() {
            public void run(){
                try {
                    while(!this.isInterrupted()){
                        char c = (char)reader.read(); // throws IOException
                        if(!this.isInterrupted()) {
                            myGame.onKeyPress(c);
                            if(shouldRenderAfterKeyPress) {
                                render();
                            }
                        }
                    }
                } catch(IOException e) {
                    shutdown();
                    System.out.println("Exception in TerminalManager::main");
                    System.out.println(e.getStackTrace());
                    System.out.println(e.getMessage());
                }
            }
        };
        keypressThread.start();

        // run both threads until we get a shutdown signal
		while(!shouldShutDown) {
            // wait 1ms
            Thread.sleep(1);
		}

        // end update executor 
        updateExecutor.shutdown();

        // end onKeyPress thread
        keypressThread.interrupt();

        // final render
        if(shouldRenderAfterShutdown) {
            render();
        }
        
        // Turn off terminal raw mode
        TerminalHelper.setTerminalRawMode(false);
	}

    public static void shutdown() {
        shouldShutDown = true; 
    }

    public static void setRenderAfterUpdate(boolean b) {
        shouldRenderAfterUpdate = b;
    }

    public static void setRenderAfterKeyPress(boolean b) {
        shouldRenderAfterKeyPress = b;
    }

    public static void setRenderAfterShutdown(boolean b) {
        shouldRenderAfterShutdown = b;
    }

    private static void render() {
        if(myGame == null) return;

        // REAL clear screen
        //System.out.print(" ".repeat(200*10));

        // redirect System.out, and capture myGame.render() 
        PrintStream originalOut = System.out; // Save original System.out
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos);
        System.setOut(newOut);
        myGame.render();
        System.out.flush(); 
        System.setOut(originalOut); // Restore original System.out
        String capturedOutput = baos.toString();
        capturedOutput = capturedOutput.replace("\n", "\n\r");

        // print by overriding characters
        // prevents large history, which causes lag
        TerminalHelper.moveCursor(1, 1);
        System.out.print(capturedOutput);
        System.out.print("              \r"); // used to cover up the characters from keyboard input
    }

}
