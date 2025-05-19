package com.jatunda.terminalgame.core;

public enum KeyCode {

    UNSUPPORTED,

    // letters
    VK_A,
    VK_B,
    VK_C,
    VK_D,
    VK_E,
    VK_F,
    VK_G,
    VK_H,
    VK_I,
    VK_J,
    VK_K,
    VK_L,
    VK_M,
    VK_N,
    VK_O,
    VK_P,
    VK_Q,
    VK_R,
    VK_S,
    VK_T,
    VK_U,
    VK_V,
    VK_W,
    VK_X,
    VK_Y,
    VK_Z,

    // numbers
    VK_0,
    VK_1,
    VK_2,
    VK_3,
    VK_4,
    VK_5,
    VK_6,
    VK_7,
    VK_8,
    VK_9,

    // numpad
    // VK_NUMPAD0,
    // VK_NUMPAD1,
    // VK_NUMPAD2,
    // VK_NUMPAD3,
    // VK_NUMPAD4,
    // VK_NUMPAD5,
    // VK_NUMPAD6,
    // VK_NUMPAD7,
    // VK_NUMPAD8,
    // VK_NUMPAD9,

    // symbols
    VK_SPACE,
    VK_COMMA,
    VK_PERIOD,
    VK_FORWARDSLASH,
    VK_SEMICOLON,
    VK_APOSTROPHE,
    VK_LEFTBRACKET,
    VK_RIGHTBRACKET,
    VK_BACKSLASH,
    VK_MINUS,
    VK_EQUALS,
    VK_TAB,
    VK_ENTER,
    VK_ESCAPE,
    VK_DELETE,
    VK_BACKSPACE,
    VK_GRAVE_TILDE,

    // arrows
    VK_UP,
    VK_DOWN,
    VK_LEFT,
    VK_RIGHT,

    // other keys
    VK_INSERT,
    VK_HOME,
    VK_END,
    VK_PAGE_UP,
    VK_PAGE_DOWN,

    // function keys
    VK_F1,
    VK_F2,
    VK_F3,
    VK_F4,
    // VK_F5,
    // VK_F6,
    // VK_F7,
    // VK_F8,
    // VK_F9,
    // VK_F10,
    // VK_F11,
    // VK_F12,
    // VK_F13,
    // VK_F14,
    // VK_F15,
    // VK_F16,
    // VK_F17,
    // VK_F18,
    // VK_F19,
    // VK_F20,
    // VK_F21,
    // VK_F22,
    // VK_F23,
    // VK_F24;

    ;

    public static KeyCode fromCharArray(char[] chars) {
        char c = chars[0];

        // Handle letters
        if (c >= 'A' && c <= 'Z') {
            return KeyCode.valueOf("VK_" + c);
        } else if (c >= 'a' && c <= 'z') {
            return KeyCode.valueOf("VK_" + Character.toUpperCase(c));
        }
        // Handle digits
        else if (c >= '0' && c <= '9') {
            return KeyCode.valueOf("VK_" + c);
        }
        // Handle numpad digits (ASCII codes 0x60-0x69, but may not be sent by all
        // terminals)
        // else if (code >= 0x60 && code <= 0x69) {
        // return KeyCode.valueOf("VK_NUMPAD" + (code - 0x60));
        // }
        else {
            switch (c) {
                // Handle shifted number keys
                case '!':
                    return KeyCode.VK_1;
                case '@':
                    return KeyCode.VK_2;
                case '#':
                    return KeyCode.VK_3;
                case '$':
                    return KeyCode.VK_4;
                case '%':
                    return KeyCode.VK_5;
                case '^':
                    return KeyCode.VK_6;
                case '&':
                    return KeyCode.VK_7;
                case '*':
                    return KeyCode.VK_8;
                case '(':
                    return KeyCode.VK_9;
                case ')':
                    return KeyCode.VK_0;

                // Handle symbols and special keys, including shifted versions
                case ' ':
                    return KeyCode.VK_SPACE;
                case ',':
                case '<': // Shift + ,
                    return KeyCode.VK_COMMA;
                case '.':
                case '>': // Shift + .
                    return KeyCode.VK_PERIOD;
                case '/':
                case '?': // Shift + /
                    return KeyCode.VK_FORWARDSLASH;
                case ';':
                case ':': // Shift + ;
                    return KeyCode.VK_SEMICOLON;
                case '\'':
                case '"': // Shift + '
                    return KeyCode.VK_APOSTROPHE;
                case '[':
                case '{': // Shift + [
                    return KeyCode.VK_LEFTBRACKET;
                case ']':
                case '}': // Shift + ]
                    return KeyCode.VK_RIGHTBRACKET;
                case '\\':
                case '|': // Shift + \
                    return KeyCode.VK_BACKSLASH;
                case '-':
                case '_': // Shift + -
                    return KeyCode.VK_MINUS;
                case '=':
                case '+': // Shift + =
                    return KeyCode.VK_EQUALS;
                case '\b':
                    return KeyCode.VK_BACKSPACE; // reads as delete instead
                case '\t':
                    return KeyCode.VK_TAB;
                case '\n':
                case '\r':
                    return KeyCode.VK_ENTER;
                case 127:
                    return KeyCode.VK_BACKSPACE;
                case '`': // Grave accent
                case '~': // Shift + `
                    return KeyCode.VK_GRAVE_TILDE;
                default:
                    // Handle escape sequences for arrows and function keys
                    if (c == 27) { // ESC
                        if (chars[1] == '[') {
                            switch (chars[2]) {
                                case 'A':
                                    return KeyCode.VK_UP;
                                case 'B':
                                    return KeyCode.VK_DOWN;
                                case 'C':
                                    return KeyCode.VK_RIGHT;
                                case 'D':
                                    return KeyCode.VK_LEFT;
                                case 'H':
                                    return KeyCode.VK_HOME;
                                case 'F':
                                    return KeyCode.VK_END;
                                case '2':
                                    if (chars[3] == '~')
                                        return KeyCode.VK_INSERT;
                                case '3':
                                    if (chars[3] == '~')
                                        return KeyCode.VK_DELETE;
                                    // Shift+Delete: ESC [ 3 ; 2 ~
                                    if (chars[3] == ';' && chars[4] == '2' && chars[5] == '~')
                                        return KeyCode.VK_DELETE;
                                case '1':
                                    // Shift+End: ESC [ 4 ; 2 ~
                                    if (chars[3] == ';' && chars[4] == '2' && chars[5] == 'F')
                                        return KeyCode.VK_END;
                                    // Shift+Home: ESC [ 1 ; 2 ~
                                    if (chars[3] == ';' && chars[4] == '2' && chars[5] == 'H')
                                        return KeyCode.VK_HOME;
                                case '5':
                                    if (chars[3] == '~')
                                        return KeyCode.VK_PAGE_UP;
                                    // Shift+Page Up: ESC [ 5 ; 2 ~
                                    if (chars[3] == ';' && chars[4] == '2' && chars[5] == '~')
                                        return KeyCode.VK_PAGE_UP;
                                case '6':
                                    if (chars[3] == '~')
                                        return KeyCode.VK_PAGE_DOWN;
                                    // Shift+Page Down: ESC [ 6 ; 2 ~
                                    if (chars[3] == ';' && chars[4] == '2' && chars[5] == '~')
                                        return KeyCode.VK_PAGE_DOWN;
                                case 'Z': // Shift + Tab
                                    return KeyCode.VK_TAB;
                            }
                        } else if (chars[1] == 'O') {
                            switch (chars[2]) {
                                case 'P':
                                    return KeyCode.VK_F1;
                                case 'Q':
                                    return KeyCode.VK_F2;
                                case 'R':
                                    return KeyCode.VK_F3;
                                case 'S':
                                    return KeyCode.VK_F4;
                            }
                        } else {
                            return KeyCode.VK_ESCAPE;
                        }
                    }
            }
        }
        return KeyCode.UNSUPPORTED;
    }
}
