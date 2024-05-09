/**
 * This class represents the king piece
 */
public class King extends Piece {
    /**
     * This initializes the queen class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public King(int color, int col, int row){
        super(color, col, row);
        super.pieceType = "King";
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-king");
        } else {
            super.image = getImage("/piece/b-king");
        }
    }

    /**
     * This determines whether the king can move to the destination based on its current location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the king can move to the spot or not
     */
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

    /**
     * This determines if the king can castle based on the destination location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the king can castle or not
     */
    public boolean canCastle(int targetCol, int targetRow){
        if (!super.moved){
            if (targetCol == super.preCol + 2 && targetRow == super.preRow && !super.pieceOnStraightLine(targetCol, targetRow)){ //kingside castle
                for (Piece p : GamePanel.simPieces){
                    if (p.col == super.preCol + 3 && p.row == super.preRow && !p.moved){
                        GamePanel.setCastlingP(p);
                        return true;
                    }
                }
            }
            if (targetCol == super.preCol - 2 && targetRow == super.preRow && !super.pieceOnStraightLine(targetCol, targetRow)){ //queenside castle
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
