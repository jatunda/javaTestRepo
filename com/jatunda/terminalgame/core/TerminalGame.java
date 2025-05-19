package com.jatunda.terminalgame.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.jatunda.terminalgame.util.*;

public abstract class TerminalGame {

    private static boolean shouldShutDown = false;
    private float updatesPerSecond = 30;
    private ScheduledExecutorService updateExecutor;
    private List<String> frameBuffer = new ArrayList<>();
    private boolean shouldRenderAfterUpdate = true;
    private boolean shouldRenderAfterKeyPress = false;
    private boolean shouldRenderAfterShutdown = true;

    public final void play() {
        // set console to raw mode
        // raw mode is how we are able to get instant input for onKeyPress
        TerminalHelper.setTerminalRawMode(true);
        TerminalHelper.setTerminalWrapping(false);
        TerminalHelper.clearScreenANSI();

        onStart();
        startUpdateExecutor();
        Thread keypressThread = startOnKeypressThread();

        while (!shouldShutDown) {
            try {
                Thread.sleep(1); // sleep for 1ms to prevent busy waiting
            } catch (InterruptedException e) {
                initiateShutdown();
                System.out.println("InterruptedException in TerminalManager::main");
                System.out.println(e.getStackTrace());
                System.out.println(e.getMessage());
            }
        }

        updateExecutor.shutdown();
        keypressThread.interrupt();
        onShutdown();
        if (shouldRenderAfterShutdown) render();

        TerminalHelper.setTerminalWrapping(true);
        TerminalHelper.setTerminalRawMode(false);
    }

    private Thread startOnKeypressThread() {
        Thread keypressThread = new Thread() {
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        KeyEvent keyEvent = readKeyEvent(System.console().reader());
                        if (!this.isInterrupted()) {
                            onKeyPress(keyEvent);
                            if (shouldRenderAfterKeyPress) {
                                render();
                            }
                        }
                    }
                } catch (IOException e) {
                    initiateShutdown();
                    System.out.println("IOException in TerminalManager::main");
                    System.out.println(e.getStackTrace());
                    System.out.println(e.getMessage());
                }
            }
        };
        keypressThread.start();
        return keypressThread;
    }  

    private KeyEvent readKeyEvent(Reader reader) throws IOException {
        char[] cbuff = new char[10];
        if (reader.read(cbuff) == -1) return null;
        KeyCode keyCode = KeyCode.fromCharArray(cbuff);
        return keyCode == null ? null : new KeyEvent(System.nanoTime(), keyCode);
    }

    private void render() {
        TerminalHelper.clearScreenManual();

        // redirect System.out, and capture myGame.render()
        PrintStream originalOut = System.out; // Save original System.out
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        onRender();
        System.out.flush();
        System.setOut(originalOut); // Restore original System.out
        String capturedOutput = baos.toString();

        // Split capturedOutput into lines and store in frameBuffer
        List<String> newFrame = Arrays.asList(capturedOutput.split("\\R"));
        if(newFrame == null) newFrame = new ArrayList<>();
        


        // if a line in newFrame does not match the one in frameBuffer, re-render the line
        TerminalDimensions td = TerminalHelper.getTerminalDimensions();
        for (int i = 0; i < newFrame.size(); i++) {
            if(i >= frameBuffer.size() || !newFrame.get(i).equals(frameBuffer.get(i))) {
                TerminalHelper.moveCursor(i + 1, 1);
                int pad = td.width - newFrame.get(i).length();
                System.out.print(newFrame.get(i) + " ".repeat(Math.max(0, pad)));
            }
        }
        frameBuffer = new ArrayList<>(newFrame);

        capturedOutput = capturedOutput.replace("\n", "\n\r");

        // print by overriding characters
        TerminalHelper.moveCursor(1, 1);
        System.out.print(capturedOutput); 
        System.out.print("              \r"); // used to cover up the characters from keyboard input
    }

    private void startUpdateExecutor() {
        updateExecutor = Executors.newSingleThreadScheduledExecutor();
        long nanosPerUpdate = (long) (1_000_000_000L / updatesPerSecond);
        updateExecutor.scheduleAtFixedRate(() -> {
            if (!shouldShutDown) {
                onUpdate();
                if (shouldRenderAfterUpdate) render();
            }
        }, 0, nanosPerUpdate, TimeUnit.NANOSECONDS);
    }

    public void setUpdatesPerSecond(float newRate) {
        updatesPerSecond = newRate;
        if (updateExecutor != null && !updateExecutor.isShutdown()) {
            updateExecutor.shutdown();
        }
        startUpdateExecutor();
    }

    public void initiateShutdown() { shouldShutDown = true; }
    public void setShouldRenderAfterUpdate(boolean v) { shouldRenderAfterUpdate = v; }
    public void setShouldRenderAfterKeyPress(boolean v) { shouldRenderAfterKeyPress = v; }
    public void setShouldRenderAfterShutdown(boolean v) { shouldRenderAfterShutdown = v; }

    public abstract void onStart();
    public abstract void onUpdate();
    public abstract void onKeyPress(KeyEvent keyEvent);
    public abstract void onRender();
    public abstract void onShutdown();
}
