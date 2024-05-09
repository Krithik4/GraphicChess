public class Knight extends Piece {
    public Knight(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "Knight";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-knight");
        } else {
            super.image = getImage("/piece/b-knight");
        }
    }

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
