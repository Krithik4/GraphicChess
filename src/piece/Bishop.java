package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends Piece{
    public Bishop(int color, int col, int row){
        super(color, col, row);
        pieceType = Type.BISHOP;
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-bishop");
        } else {
            super.image = getImage("/piece/b-bishop");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)){
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)){
                if (isValidSquare(targetCol, targetRow) && !pieceOnDiagonalLine(targetCol, targetRow)){
                    return true;
                }
            }
        }

        return false;
    }


}
