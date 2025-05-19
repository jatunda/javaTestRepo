package com.jatunda.terminalgame.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TerminalHelper {

    public static TerminalDimensions getTerminalDimensions() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty size </dev/tty" });
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    int height = Integer.parseInt(parts[0]);
                    int width = Integer.parseInt(parts[1]);
                    return new TerminalDimensions(width, height);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void moveCursor(int row, int column) {
        char escCode = 0x1B;
        System.out.print(String.format("%c[%d;%df", escCode, row, column));
    }

    /**
     * Clears the screen using ANSI escape codes.
     * Moves all the console history to be above the current view.
     * Note: This may not work on all terminals.
     */
    public static void clearScreenANSI() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }



    public static void clearScreenManual() {
        TerminalDimensions td = getTerminalDimensions();
        moveCursor(1, 1);
        if (td != null) {
            System.out.print(" ".repeat(td.width * td.height));
            //System.out.flush();
        }
    }

    /**
     * @return true if it worked, false if an exception was thrown
     */
    public static boolean setTerminalRawMode(boolean newValue) {
        String[] cmd;
        if (newValue) {
            cmd = new String[] { "/bin/sh", "-c", "stty raw </dev/tty" };
        } else {
            cmd = new String[] { "/bin/sh", "-c", "stty sane </dev/tty" };
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

    public static void setTerminalWrapping(boolean newValue) {
        // ANSI escape code: DECSCNM (Enable/Disable line wrap)
        // Enable:  \033[?7h
        // Disable: \033[?7l
        if (newValue) {
            System.out.print("\033[?7h"); // Enable line wrapping
        } else {
            System.out.print("\033[?7l"); // Disable line wrapping
        }
        System.out.flush();
    }
}

