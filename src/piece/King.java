package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece{
    public King(int color, int col, int row){
        super(color, col, row);
        pieceType = Type.KING;
        if (color == GamePanel.WHITE){
            super.image = getImage("/piece/w-king");
        } else {
            super.image = getImage("/piece/b-king");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (onBoard(targetCol, targetRow)){
            if (Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1){
                if (isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }
            if (Math.abs(targetCol - preCol) == 1 && Math.abs(targetRow - preRow) == 1){
                if (isValidSquare(targetCol, targetRow)){
                    return true;
                }
            }

            //castling
            if (!moved){
                if (targetCol == preCol + 2 && targetRow == preRow && !pieceOnStraightLine(targetCol, targetRow)){
                    for (Piece p : GamePanel.simPieces){
                        if (p.col == preCol + 3 && p.row == preRow && !p.moved){
                            GamePanel.castlingP = p;
                            return true;
                        }
                    }
                }
                if (targetCol == preCol - 2 && targetRow == preRow && !pieceOnStraightLine(targetCol, targetRow)){
                    Piece[] pieces = new Piece[2];
                    for (Piece p : GamePanel.simPieces){
                        if (p.col == preCol - 3 && p.row == targetRow){
                            pieces[0] = p;
                        }
                        if (p.col == preCol - 4 && p.row == targetRow){
                            pieces[1] = p;
                        }
                        if (pieces[0] == null && pieces[1] != null && !pieces[1].moved){
                            GamePanel.castlingP = pieces[1];
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
