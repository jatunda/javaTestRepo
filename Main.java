
import com.jatunda.terminalgame.util.TerminalHelper;

public class Main {
    public static void main(String[] args) {
        TerminalHelper.setTerminalRawMode(true);

        System.out.println("asdf".repeat(100));
        // Reader reader = System.console().reader();
        // while(true){
        //     try {
        //         System.out.print("\n\rEnter a string: ");
        //         char[] cbuffer = new char[100];
        //         reader.read(cbuffer);
        //         int[] ibuffer = new int[cbuffer.length];
        //         for (int i = 0; i < cbuffer.length; i++) {
        //             ibuffer[i] = cbuffer[i];
        //         }
        //         System.out.println("\n\rYou entered: " + new String(cbuffer));
        //     } catch (Exception e) {
        //         System.out.println("\n\rAn error occurred: " + e.getMessage());
        //         break;
        //     }
        // }
        // TerminalHelper.setTerminalRawMode(false);
    }
}
