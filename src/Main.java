import javax.swing.JOptionPane;
import javax.swing.JFrame;

public class Main {
    /**
     * This method runs the game and allows the game to be closed by clicking the "x" button
     */
    public static void main(String[] args) {
        //From Rihan
        JOptionPane.showMessageDialog(null, "Welcome to the game of Chess! Press OK to play");

        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //allows the frame to be closed
        window.setResizable(false);
        GamePanel gPanel = new GamePanel();
        window.add(gPanel);
        window.pack(); //proper adjusts the game panel to the frame
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gPanel.launchGame(); //initiates the game

    }
}