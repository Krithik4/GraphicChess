import javax.swing.JFrame;
//hi there
/*
 Things to do
 Make sure king can't castle out of check
 Do comments
 Checkmate is a little buggy
 MAKE SURE IT'S NOT
 */

public class Main {
    /**
     * This method runs the game and allows the game to be closed by clicking the "x" button
     */
    public static void main(String[] args) {
        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gPanel = new GamePanel();
        window.add(gPanel);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gPanel.launchGame();
    }
}