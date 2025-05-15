import java.io.Console;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {
    
    private static final int UPDATES_PER_SECOND = 1;

    private static Game myGame;
    private static boolean shouldShutDown = false;

    public static void main(String[] args) throws IOException, InterruptedException {

        // set console to raw mode
        // raw mode is how we are able to get instant input for onKeyPress
		String[] cmd = {"/bin/sh", "-c", "stty raw </dev/tty"};
		Runtime.getRuntime().exec(cmd).waitFor(); // throws IOException, InterruptedException

        // initialize game
        myGame = new Game();

        // init onUpdate thread
        ScheduledExecutorService updateExecutor = Executors.newSingleThreadScheduledExecutor();
        long updateIntervalNanos = 1_000_000_000L / UPDATES_PER_SECOND;
        updateExecutor.scheduleAtFixedRate(
            () -> {
                if(!shouldShutDown) {
                    myGame.onUpdate();
                    myGame.render();
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
                            myGame.render();
                        }
                    }
                } catch(Exception e) {
                    shutdown();
                    GameManager.println("IO Exception!");
                }
            }
        };
        keypressThread.start();

        // run both threads until we get a shutdown signal
		while(!shouldShutDown) {
            // wait 1ms
            Thread.sleep(1);
		}

        // end execution of update
        updateExecutor.shutdown();

        // end execution of onKeyPress
        keypressThread.interrupt();

        // final render
        myGame.render();
        
        // turn off RAW mode
		cmd = new String[] {"/bin/sh", "-c", "stty sane </dev/tty"};
		Runtime.getRuntime().exec(cmd).waitFor(); // throws IOException, InterruptedException
	}

    public static void clearScreen() { 
        System.out.print("\033[H\033[2J"); 
        System.out.flush(); 
    }

    /**
     * When console is in RAW mode, you have to the manually reset the cursor 
     * to the leftmost position every line printed, using '\r'
     *  */ 
    public static void println(String s) {
        System.out.println(s + "\r");
    }

    /**
     * included for completeness sake. 
     */
    public static void print(String s) {
        System.out.print(s);
    }

    public static void shutdown() {
        shouldShutDown = true; 
    }
}
