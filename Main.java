import javax.swing.JFrame;

import view.GamePanel;

public class Main {

    public static void main(String[] args) {

        JFrame window = new JFrame("Creative Work 4");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocation(400, 100);

        window.setContentPane(new GamePanel());

        window.pack();
        window.setVisible(true);
    }
}
