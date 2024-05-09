/**
 * This class represents the pawn piece
 */
public class Pawn extends Piece {
    /**
     * This initializes the pawn class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public Pawn(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "Pawn";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-pawn");
        } else {
            super.image = getImage("/piece/b-pawn");
        }
    }

    /**
     * This determines whether the pawn can move to the destination based on its current location
     * It also includes en passant
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the pawn can move to the spot or not
     */
    public boolean canMove(int targetCol, int targetRow){
        if (super.onBoard(targetCol, targetRow) && !super.isSameSquare(targetCol, targetRow)){
            int moveValue = (super.color == GamePanel.WHITE) ? -1 : 1;
            super.hittingP = gettingHitP(targetCol, targetRow);
            if (targetCol == super.preCol && targetRow == super.preRow + moveValue && super.hittingP == null){ //one step
                return true;
            }
            if (targetCol == super.preCol && targetRow == super.preRow + moveValue * 2 && super.hittingP == null && !super.moved
                  && !pieceOnStraightLine(targetCol, targetRow)){ //two step
                return true;
            }
            if (Math.abs(targetCol - super.preCol) == 1 && targetRow == super.preRow + moveValue
                    && super.hittingP != null && super.hittingP.color != super.color){ //capture
                return true;
            }
            //en passant
            if (Math.abs(targetCol - super.preCol) == 1 && targetRow == super.preRow + moveValue){
                for (Piece p : GamePanel.simPieces){
                    if (p.col == targetCol && p.row == super.preRow && p.twoStepped){
                        super.hittingP = p;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
