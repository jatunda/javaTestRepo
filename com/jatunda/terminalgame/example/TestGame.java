package com.jatunda.terminalgame.example;

import com.jatunda.terminalgame.core.KeyEvent;
import com.jatunda.terminalgame.core.TerminalGame;

public class TestGame extends TerminalGame {

    private KeyEvent keyEvent = null;
    int x = 0;

    @Override
    public void onStart() {
        System.out.println("Game started");
    }

    @Override
    public void onUpdate() {
        x++;
    }

    @Override
    public void onKeyPress(KeyEvent keyEvent) {
        this.keyEvent = keyEvent;
    }

    @Override
    public void onRender() {
        System.out.println(System.nanoTime());
        System.out.println(keyEvent);
    }

    @Override
    public void onShutdown() {
        System.out.println("Game shutdown");
    }

    public static void main(String[] args) {
        TestGame game = new TestGame();
        game.play();
    }
}
