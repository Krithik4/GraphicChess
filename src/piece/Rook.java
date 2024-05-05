package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece{
    public Rook(int color, int col, int row){
        super(color, col, row);
        pieceType = Type.ROOK;
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-rook");
        } else {
            super.image = getImage("/piece/b-rook");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow)){
            if (targetCol == preCol ^ targetRow == preRow){
                if (isValidSquare(targetCol, targetRow) && !pieceOnStraightLine(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }


}
