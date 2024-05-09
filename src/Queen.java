//import GamePanel;
//import Type;

/**
 * This class represents the queen piece
 */
public class Queen extends Piece {
    /**
     * This initializes the queen class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public Queen(int color, int col, int row){
        super(color, col, row);
        pieceType = "Queen";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-queen");
        } else {
            super.image = getImage("/piece/b-queen");
        }
    }

    /**
     * This determines whether the queen can move to the destination based on its current location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the queen can move to the spot or not
     */
    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)){
            if (targetCol == preCol ^ targetRow == preRow){
                if (isValidSquare(targetCol, targetRow) && !pieceOnStraightLine(targetCol, targetRow)){
                    return true;
                }
            }
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)){
                if (isValidSquare(targetCol, targetRow) && !pieceOnDiagonalLine(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}
