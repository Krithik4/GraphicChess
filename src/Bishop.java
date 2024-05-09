/**
 * This class represents the bishop piece
 */
public class Bishop extends Piece {
    /**
     * This initializes the bishop class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public Bishop(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "Bishop";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-bishop");
        } else {
            super.image = getImage("/piece/b-bishop");
        }
    }

    /**
     * This determines whether the queen can move to the destination based on its current location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the queen can move to the spot or not
     */
    public boolean canMove(int targetCol, int targetRow){
        if (super.onBoard(targetCol, targetRow) && !super.isSameSquare(targetCol, targetRow)){
            if (Math.abs(targetCol - super.preCol) == Math.abs(targetRow - super.preRow)){
                if (super.isValidSquare(targetCol, targetRow) && !super.pieceOnDiagonalLine(targetCol, targetRow)){
                    return true;
                }
            }
        }

        return false;
    }


}
