package main;

import javax.swing.JFrame;
//hi there
/*
 Things to do
 Get pieces to move smoothly
 Make sure king can't castle out of check
 adhere to style guide
 - method length
 - scope
 - simplification
 MAKE SURE IT'S NOT

 */

public class Main {
    public static void main(String[] args) {
        for(int a = 0; a < 10; a++){
            System.out.print(a);
        }
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