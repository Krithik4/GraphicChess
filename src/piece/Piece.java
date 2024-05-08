package piece;

import main.Board;
import main.GamePanel;
import main.Type;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Piece {
    public Type pieceType;
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingP;
    public boolean moved, twoStepped;

    public Piece(int color, int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        this.x = getX(col);
        this.y = getY(row);
        preCol = col;
        preRow = row;
    }

    public BufferedImage getImage(String path){
        BufferedImage tempImage = null;
        try {
            tempImage = ImageIO.read(getClass().getResourceAsStream(path + ".png"));
        } catch(IOException io){
            io.printStackTrace();
        }
        return tempImage;
    }

    public int getX(int col){
        return col * Board.SQUARE_SIZE;
    }

    public int getY(int row){
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x){
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getRow(int y){
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public void updatePosition(){
        if (pieceType == Type.PAWN){
            if (Math.abs(row - preRow) == 2){
                twoStepped = true;
            }
        }

        this.x = getX(col);
        this.y = getY(row);
        preCol = getCol(this.x);
        preRow = getRow(this.y);
        moved = true;
    }

    public void resetPosition(){
        col = preCol;
        row = preRow;
        x = getX(col);
        y = getY(row);
    }

    public boolean canMove(int targetCol, int targetRow){
        return false;
    }

    public boolean onBoard(int targetCol, int targetRow){
        return (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7);
    }

    public Piece gettingHitP(int targetCol, int targetRow){
        for (Piece piece : GamePanel.simPieces){
            if (piece.col == targetCol && piece.row == targetRow  && piece != this){
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol, int targetRow){
        hittingP = gettingHitP(targetCol, targetRow);
        if (hittingP == null){
            return true;
        } else {
            if (hittingP.color != this.color){
                return true;
            } else {
                hittingP = null;
            }
        }
        return false;
    }

    public int getIndex(){
        for (int i = 0; i < GamePanel.simPieces.size(); i++){
            if (GamePanel.simPieces.get(i) == this){
                return i;
            }
        }
        return 0;
    }

    public boolean isSameSquare(int targetCol, int targetRow){
        return (targetCol == preCol && targetRow == preRow);
    }

    public boolean pieceOnStraightLine(int targetCol, int targetRow){
        for (int c = preCol - 1; c > targetCol; c--){ //left
            for (Piece p : GamePanel.simPieces){
                if (p.col == c && p.row == targetRow){
                    hittingP = p;
                    return true;
                }
            }
        }
        for (int c = preCol + 1; c < targetCol; c++){ //right
            for (Piece p : GamePanel.simPieces){
                if (p.col == c && p.row == targetRow){
                    hittingP = p;
                    return true;
                }
            }
        }
        for (int r = preRow - 1; r > targetRow; r--){ //up
            for (Piece p : GamePanel.simPieces){
                if (p.col == targetCol && p.row == r){
                    hittingP = p;
                    return true;
                }
            }
        }
        for (int r = preRow + 1; r < targetRow; r++){ //down
            for (Piece p : GamePanel.simPieces){
                if (p.col == targetCol && p.row == r){
                    hittingP = p;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean pieceOnDiagonalLine(int targetCol, int targetRow){
        if (targetRow < preRow){//up
            for (int c = preCol - 1; c > targetCol; c--){ //left
                int diff = Math.abs(c - preCol);
                for (Piece p : GamePanel.simPieces){
                    if (p.col == c && p.row == preRow - diff){
                        hittingP = p;
                        return true;
                    }
                }
            }

            for (int c = preCol + 1; c < targetCol; c++){ //right
                int diff = Math.abs(c - preCol);
                for (Piece p : GamePanel.simPieces){
                    if (p.col == c && p.row == preRow - diff){
                        hittingP = p;
                        return true;
                    }
                }
            }
        }

        if (targetRow > preRow){//down
            for (int c = preCol - 1; c > targetCol; c--){ //left
                int diff = Math.abs(c - preCol);
                for (Piece p : GamePanel.simPieces){
                    if (p.col == c && p.row == preRow + diff){
                        hittingP = p;
                        return true;
                    }
                }
            }

            for (int c = preCol + 1; c < targetCol; c++){ //right
                int diff = Math.abs(c - preCol);
                for (Piece p : GamePanel.simPieces){
                    if (p.col == c && p.row == preRow + diff){
                        hittingP = p;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void draw(Graphics2D g2D){
        g2D.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}
