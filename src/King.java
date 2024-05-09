public class King extends Piece {
    public King(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "King";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-king");
        } else {
            super.image = getImage("/piece/b-king");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (super.onBoard(targetCol, targetRow)){
            if (Math.abs(targetCol - super.preCol) + Math.abs(targetRow - super.preRow) == 1){ //hori and vert
                if (super.isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }
            if (Math.abs(targetCol - super.preCol) == 1 && Math.abs(targetRow - super.preRow) == 1){ //diag
                if (super.isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }

            //castling
            return canCastle(targetCol, targetRow);
        }
        return false;
    }

    public boolean canCastle(int targetCol, int targetRow){
        if (!super.moved){
            if (targetCol == super.preCol + 2 && targetRow == super.preRow && !super.pieceOnStraightLine(targetCol, targetRow)){
                for (Piece p : GamePanel.simPieces){
                    if (p.col == super.preCol + 3 && p.row == super.preRow && !p.moved){
                        GamePanel.setCastlingP(p);
                        return true;
                    }
                }
            }
            if (targetCol == super.preCol - 2 && targetRow == super.preRow && !super.pieceOnStraightLine(targetCol, targetRow)){
                Piece[] pieces = new Piece[2];
                for (Piece p : GamePanel.simPieces){
                    if (p.col == super.preCol - 3 && p.row == targetRow){
                        pieces[0] = p;
                    }
                    if (p.col == super.preCol - 4 && p.row == targetRow){
                        pieces[1] = p;
                    }
                    if (pieces[0] == null && pieces[1] != null && !pieces[1].moved){
                        GamePanel.setCastlingP(pieces[1]);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
