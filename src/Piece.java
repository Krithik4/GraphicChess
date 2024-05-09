import javax.imageio.ImageIO;
//import java.awt.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Piece {
    protected String pieceType;
    protected BufferedImage image;
    protected int x, y;
    protected int col, row, preCol, preRow;
    protected int color;
    protected Piece hittingP;
    protected boolean moved, twoStepped;

    public Piece(int color, int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        this.x = Piece.calcX(col);
        this.y = Piece.calcY(row);
        this.preCol = col;
        this.preRow = row;
    }

    public String getPieceType(){
        return this.pieceType;
    }

    public int getColor(){
        return this.color;
    }

    //https://stackoverflow.com/questions/1978445/get-image-from-relative-path
    //https://www.geeksforgeeks.org/class-getresourceasstream-method-in-java-with-examples/
    public BufferedImage getImage(String path){
        BufferedImage tempImage = null;
        try {
            tempImage = ImageIO.read(this.getClass().getResourceAsStream(path + ".png"));
        } catch(IOException io){
            io.printStackTrace();
        }
        return tempImage;
    }

    public static int calcX(int column){
        return column * Board.SQUARE_SIZE;
    }

    public static int calcY(int rowParam){
        return rowParam * Board.SQUARE_SIZE;
    }

    public static int calcCol(int xParam){
        return (xParam + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public static int calcRow(int yParam){
        return (yParam + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public void updatePosition(){
        if ("Pawn".equals(this.pieceType)){
            if (Math.abs(this.row - this.preRow) == 2){
                this.twoStepped = true;
            }
        }

        this.x = Piece.calcX(this.col);
        this.y = Piece.calcY(this.row);
        this.preCol = Piece.calcCol(this.x);
        this.preRow = Piece.calcRow(this.y);
        this.moved = true;
    }

    public void resetPosition(){
        this.col = this.preCol;
        this.row = this.preRow;
        this.x = Piece.calcX(this.col);
        this.y = Piece.calcY(this.row);
    }

    public boolean canMove(int targetCol, int targetRow){
        return false;
    }

    public boolean onBoard(int targetCol, int targetRow){
        return (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7);
    }

    public Piece gettingHitP(int targetCol, int targetRow){
        for (Piece p : GamePanel.simPieces){
            if (p.col == targetCol && p.row == targetRow  && p != this){
                return p;
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
        return pieceOnHorizontalLine(targetCol, targetRow) || pieceOnVerticalLine(targetCol, targetRow);
    }

    public boolean pieceOnHorizontalLine(int targetCol, int targetRow){
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
        return false;
    }

    public boolean pieceOnVerticalLine(int targetCol, int targetRow){
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
        return pieceOnLowerDiags(targetCol, targetRow) || pieceOnUpperDiags(targetCol, targetRow);
    }

    public boolean pieceOnUpperDiags(int targetCol, int targetRow){
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
        return false;
    }

    public boolean pieceOnLowerDiags(int targetCol, int targetRow){
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
    public int getPreCol(){
        return this.preCol;
    }

    public int getCol(){
        return this.col;
    }

    public void setCol(int column){
        this.col = column;
    }

    public int getRow(){
        return this.row;
    }

    public void setRow(int rowParam){
        this.row = rowParam;
    }

    public int getX(){
        return this.x;
    }

    public void setX(int xParam){
        this.x = xParam;
    }

    public int getY(){
        return this.y;
    }

    public void setY(int yParam){
        this.y = yParam;
    }

    public Piece getCapturedP(){
        return this.hittingP;
    }

    public void draw(Graphics2D g2D){
        g2D.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}
