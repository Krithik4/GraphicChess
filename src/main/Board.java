package main;

import java.awt.Graphics2D;
import java.awt.Color;

public class Board {
    final int MAX_COL = 8;
    final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;

    public void draw(Graphics2D g2D){
        Color white = new Color(235, 236, 208);
        Color black = new Color(119, 149, 86);
        boolean whiteFirst = true;
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                if ((8 * row + col) % 2 == 0){
                    g2D.setColor((whiteFirst) ? white : black);
                } else {
                    g2D.setColor((whiteFirst) ? black : white);
                }
                g2D.fillRect(col * SQUARE_SIZE,row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            whiteFirst = !whiteFirst;
        }
    }
}
