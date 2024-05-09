
/**
 * This class represents the knight piece
 */
public class Knight extends Piece {
    /**
     * This initializes the knight class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public Knight(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "Knight";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-knight");
        } else {
            super.image = getImage("/piece/b-knight");
        }
    }

    /**
     * This determines whether the knight can move to the destination based on its current location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the knight can move to the spot or not
     */
    public boolean canMove(int targetCol, int targetRow){
        if (super.onBoard(targetCol, targetRow)){
            if (Math.abs(targetCol - super.preCol) * Math.abs(targetRow - super.preRow) == 2){
                if (super.isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}
