//import GamePanel;
//import Type;

public class Queen extends Piece {
    public Queen(int color, int col, int row){
        super(color, col, row);
        pieceType = "Queen";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-queen");
        } else {
            super.image = getImage("/piece/b-queen");
        }
    }

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
