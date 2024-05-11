import javax.swing.JFrame;
//hi there
/*
 Things to do
 Do comments
 Checkmate/stalemate is a little buggy
 add a start screen
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