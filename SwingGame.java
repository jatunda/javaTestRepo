import javax.swing.*;
import java.awt.*;

public class SwingGame {
    public static void main(String[] args) {
        // create the frame
        JFrame frame = new JFrame("Test frame name");

        JPanel mainPanel = new JPanel();

        mainPanel.setPreferredSize(new Dimension(800, 600));
        mainPanel.setLayout(new BorderLayout());
        frame.add(mainPanel);

        JPanel blue = new JPanel();
        blue.setPreferredSize(new Dimension(150, 100));
        blue.setBackground(Color.blue);
        mainPanel.add(blue, BorderLayout.LINE_END);

        JPanel yellow = new JPanel();
        yellow.setPreferredSize(new Dimension(100, 100));
        yellow.setBackground(Color.yellow);
        mainPanel.add(yellow);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}