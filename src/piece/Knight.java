package piece;

import main.GamePanel;
import main.Type;

public class Knight extends Piece{
    public Knight(int color, int col, int row){
        super(color, col, row);
        pieceType = Type.KNIGHT;
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-knight");
        } else {
            super.image = getImage("/piece/b-knight");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow)){
            if (Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 2){
                if (isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}
