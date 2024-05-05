package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece {
    public Pawn(int color, int col, int row){
        super(color, col, row);
        pieceType = Type.PAWN;
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-pawn");
        } else {
            super.image = getImage("/piece/b-pawn");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)){
            int moveValue = (color == GamePanel.WHITE) ? -1 : 1;
            hittingP = gettingHitP(targetCol, targetRow);
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null){
                return true;
            }
            if (targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingP == null && !moved
                  && !pieceOnStraightLine(targetCol, targetRow)){
                return true;
            }
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null && hittingP.color != this.color){
                return true;
            }
            //en passant
            if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue){
                for (Piece p : GamePanel.simPieces){
                    if (p.col == targetCol && p.row == preRow && p.twoStepped){
                        hittingP = p;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
