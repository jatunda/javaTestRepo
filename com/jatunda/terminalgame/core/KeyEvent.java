package com.jatunda.terminalgame.core;

public class KeyEvent {
    public final long when;
    public final KeyCode keyCode;

    public KeyEvent(long when, KeyCode keyCode) {
        this.when = when;
        this.keyCode = keyCode;
    }

    public String toString() {
        return "KeyEvent{" +
                "when=" + when +
                ", keyCode=" + keyCode +
                '}';
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        KeyEvent keyEvent = (KeyEvent) obj;

        if (when != keyEvent.when) return false;
        return keyCode == keyEvent.keyCode;
    }
}

