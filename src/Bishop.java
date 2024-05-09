public class Bishop extends Piece {
    public Bishop(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "Bishop";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-bishop");
        } else {
            super.image = getImage("/piece/b-bishop");
        }
    }

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
