/**
 * This class represents the rook piece
 */
public class Rook extends Piece {
    /**
     * This initializes the rook class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public Rook(int color, int col, int row){
        super(color, col, row);
        pieceType = "Rook";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-rook");
        } else {
            super.image = getImage("/piece/b-rook");
        }
    }

    /**
     * This determines whether the rook can move to the destination based on its current location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the rook can move to the spot or not
     */
    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow)){
            if (targetCol == preCol ^ targetRow == preRow){ //either the row or the column are the same
                if (isValidSquare(targetCol, targetRow) && !pieceOnStraightLine(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }


}
