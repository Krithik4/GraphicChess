/**
 * This class represents the king piece
 */
public class King extends Piece {
    /**
     * This initializes the king class
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
     * This method uses the static variable canCastle from the game panel class to ensure that the king does not castle
     * out of check; no change is made to the variable, it is only accessed
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the king can move to the spot or not
     */
    public boolean canMove(int targetCol, int targetRow){
        if (super.onBoard(targetCol, targetRow)){
            if (Math.abs(targetCol - super.preCol) + Math.abs(targetRow - super.preRow) == 1){ //horizontal and vertical
                if (super.isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }
            if (Math.abs(targetCol - super.preCol) == 1 && Math.abs(targetRow - super.preRow) == 1){ //diagonal
                if (super.isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }

            //castling
            if (GamePanel.canCastle){
                return canCastle(targetCol, targetRow);
            }
        }
        return false;
    }

    /**
     * This determines if the king can castle based on the destination location
     * This iterates through the pieces list of the game panel class and updates the castling piece variable
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether the king can castle or not
     */
    public boolean canCastle(int targetCol, int targetRow){
        if (!super.moved){
            if (targetCol == super.preCol + 2 && targetRow == super.preRow && !super.pieceOnStraightLine(targetCol, targetRow)){ //kingside castle
                for (Piece p : GamePanel.piecesShownOnBoard){
                    if (p.col == super.preCol + 3 && p.row == super.preRow && !p.moved){
                        GamePanel.setCastlingP(p);
                        return true;
                    }
                }
            }
            if (targetCol == super.preCol - 2 && targetRow == super.preRow && !super.pieceOnStraightLine(targetCol, targetRow)){ //queenside castle
                for (Piece p : GamePanel.piecesShownOnBoard){
                    if (p.col == super.preCol - 4 && p.row == super.preRow && !p.moved){
                        GamePanel.setCastlingP(p);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
