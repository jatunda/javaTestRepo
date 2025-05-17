import java.io.Reader;

public class TerminalHelper {


    public static TerminalDimensions getTerminalDimensions() {

        int height = -1;
        int width = -1;

        // get the size of terminal
        // request terminal size
        System.out.print("\u001b[s");             // save cursor position
        System.out.print("\u001b[5000;5000H");    // move to col 5000 row 5000
        System.out.print("\u001b[6n");            // request cursor position
        System.out.print("\u001b[u");             // restore cursor position

        try {
            // read response about terminal size
            Reader inputReader = System.console().reader();
            int byteBuffer;
            StringBuilder buffer = new StringBuilder();
            while ((byteBuffer = inputReader.read()) > -1) {
                if (byteBuffer == 3) {
                    break;
                } else if (byteBuffer == 27) {
                    buffer.append("\\033");
                } else {
                    buffer.append((char)byteBuffer);
                    if ('R' == byteBuffer) {
                        break;
                    }
                }
            }
            
            String s = buffer.substring(5);
            height = Integer.parseInt( s.substring(0, s.indexOf(";")));
            width = Integer.parseInt(s.substring(s.indexOf(";")+1, s.indexOf("R")));
            return new TerminalDimensions(width, height);
        } 
        catch (Exception e) 
        {
            System.out.println("Error caught in ConsoleGameManager::getTerminalDimensions");
            System.out.println(e.getStackTrace());
            System.out.println(e.getMessage());
        }

        return null; // should never reach here

    }

    public static void clearScreen() { 
        System.out.print("\033[H\033[2J"); 
        System.out.flush(); 
    }

    public static void moveCursor(int row, int column) {
        char escCode = 0x1B;
        System.out.print(String.format("%c[%d;%df",escCode,row,column));
    }

    /**
     * @return true if it worked, false if an exception was thrown
     */
    public static boolean setTerminalRawMode(boolean newValue) {
        String[] cmd;
        if(newValue) {
            cmd = new String[] {"/bin/sh", "-c", "stty raw </dev/tty"};
        } else {
            cmd = new String[] {"/bin/sh", "-c", "stty sane </dev/tty"};
        }

        try {
        	Runtime.getRuntime().exec(cmd).waitFor(); // throws IOException, InterruptedException
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            return false;
        }
        return true;
    }
}

class TerminalDimensions{
    public final int width;
    public final int height;
    public TerminalDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}