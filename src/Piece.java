import javax.imageio.ImageIO;
//import java.awt.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This class represents a piece
 * This class is overridden by specific piece classes
 */
public class Piece {
    protected String pieceType;
    protected BufferedImage image;
    protected int x, y;
    protected int col, row, preCol, preRow;
    protected int color;
    protected Piece hittingP;
    protected boolean moved, twoStepped;

    /**
     * This initializes the piece class
     * @param color The color of the piece
     * @param col The column coordinate of the piece
     * @param row The row coordinate of the piece
     */
    public Piece(int color, int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        this.x = Piece.calcX(col);
        this.y = Piece.calcY(row);
        this.preCol = col;
        this.preRow = row;
    }

    /**
     * This gets the piece type as a string
     * @return the piece type
     */
    public String getPieceType(){
        return this.pieceType;
    }

    /**
     * This gets the color of the piece
     * @return the color as an integer (1 is white, 0 is black)
     */
    public int getColor(){
        return this.color;
    }

    //https://stackoverflow.com/questions/1978445/get-image-from-relative-path
    //https://www.geeksforgeeks.org/class-getresourceasstream-method-in-java-with-examples/
    /**
     * This gets the image that represents the piece
     * @param path The file path of the image
     * @return The image
     */
    public BufferedImage getImage(String path){
        BufferedImage tempImage = null;
        try {
            tempImage = ImageIO.read(this.getClass().getResourceAsStream(path + ".png"));
        } catch(IOException io){
            System.out.println("Couldn't find image");
            io.printStackTrace();
        }
        return tempImage;
    }

    /**
     * This calculates the x coordinate on the board based on a specified column
     * The board class' size variable is accessed
     * @param column The specified column
     * @return The x coordinate
     */
    public static int calcX(int column){
        return column * Board.SQUARE_SIZE;
    }

    /**
     * This calculates the y coordinate on the board based on a specified row
     * The board class' size variable is accessed
     * @param rowParam The specified row
     * @return The y coordinate
     */
    public static int calcY(int rowParam){
        return rowParam * Board.SQUARE_SIZE;
    }

    /**
     * This calculates the column on the board based on a specified x value
     * The board class' size and half-size variables are accessed
     * @param xParam The specified x value
     * @return The column
     */
    public static int calcCol(int xParam){
        return (xParam + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    /**
     * This calculates the row on the board based on a specified y value
     * The board class' size and half-size variables are accessed
     * @param yParam The specified y value
     * @return The column
     */
    public static int calcRow(int yParam){
        return (yParam + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    /**
     * This updates the position of the piece by changing x and y values
     */
    public void updatePosition(){
        if ("Pawn".equals(this.pieceType)){
            if (Math.abs(this.row - this.preRow) == 2){ //used for en passant checking
                this.twoStepped = true;
            }
        }

        this.x = Piece.calcX(this.col);
        this.y = Piece.calcY(this.row);
        this.preCol = Piece.calcCol(this.x);
        this.preRow = Piece.calcRow(this.y);
        this.moved = true;
    }

    /**
     * This resets the position of the piece by assigning row and col values to its previous values
     */
    public void resetPosition(){
        this.col = this.preCol;
        this.row = this.preRow;
        this.x = Piece.calcX(this.col);
        this.y = Piece.calcY(this.row);
    }

    /**
     * This determines if the piece can move to a certain row and column
     * This method is overridden in the specific piece class so they abide by chess rules
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if piece can move to destination
     */
    public boolean canMove(int targetCol, int targetRow){
        return false;
    }

    /**
     * This determines if the destination row and column is inside the board
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if the piece is inside the board
     */
    public boolean onBoard(int targetCol, int targetRow){
        return (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7);
    }

    /**
     * This determines what piece is being captured based on a destination location
     * This iterates through the pieces list of the game panel class
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return the piece being captured (null if no piece is captured)
     */
    public Piece gettingHitP(int targetCol, int targetRow){
        for (Piece p : GamePanel.piecesShownOnBoard){
            if (p.col == targetCol && p.row == targetRow  && p != this){
                return p;
            }
        }
        return null;
    }

    /**
     * This determines if the destination is valid (regardless of specific piece rules)
     * It checks to see if destination is occupied and then determines if it is a valid square
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return whether destination is valid or not
     */
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

    /**
     * The determines if the destination is the same as the piece's current location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if piece hasn't moved
     */
    public boolean isSameSquare(int targetCol, int targetRow){
        return (targetCol == preCol && targetRow == preRow);
    }

    /**
     * This determines if there is a piece in any straight path in the way of the destination location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if there is piece obstructing any straight path
     */
    public boolean pieceOnStraightLine(int targetCol, int targetRow){
        return pieceOnHorizontalLine(targetCol, targetRow) || pieceOnVerticalLine(targetCol, targetRow);
    }

    /**
     * This determines if there is a piece in any horizontal path in the way of the destination location
     * The pieces list from the game panel class is iterated through
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if there is piece obstructing any horizontal path
     */
    public boolean pieceOnHorizontalLine(int targetCol, int targetRow){
        boolean targetOnRight = preCol < targetCol;
        int increment = (targetOnRight) ? 1 : -1;
        //the loop header determines if the destination is to the left or right and increments accordingly
        for (int c = preCol + increment; targetOnRight ? c < targetCol : c > targetCol; c += increment){
            for (Piece p : GamePanel.piecesShownOnBoard){
                if (p.col == c && p.row == targetRow){
                    hittingP = p;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This determines if there is a piece in any vertical path in the way of the destination location
     * The pieces list from the game panel class is iterated through
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if there is piece obstructing any vertical path
     */
    public boolean pieceOnVerticalLine(int targetCol, int targetRow){
        boolean targetAbove = preRow > targetRow;
        int increment = (targetAbove) ? -1 : 1;
        //the loop header determines if the destination is above or below and increments accordingly
        for (int r = preRow + increment; targetAbove ? r > targetRow : r < targetRow; r += increment){
            for (Piece p : GamePanel.piecesShownOnBoard){
                if (p.col == targetCol && p.row == r){
                    hittingP = p;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This determines if there is a piece in any diagonal path in the way of the destination location
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if there is piece obstructing a diagonal path
     */
    public boolean pieceOnDiagonalLine(int targetCol, int targetRow){
        if (targetRow < preRow){
            return pieceOnUpperDiags(targetCol, targetRow);
        } else {
            return pieceOnLowerDiags(targetCol, targetRow);
        }
    }

    /**
     * This determines if there is a piece in any upper diagonal path in the way of the destination location
     * The pieces list from the game panel class is iterated through
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if there is piece obstructing any upper diagonal path
     */
    public boolean pieceOnUpperDiags(int targetCol, int targetRow){
        if (targetRow < preRow){//up
            boolean targetOnRight = preCol < targetCol;
            int increment = (targetOnRight) ? 1 : -1;
            //loop header determines whether the target is on the right or left and increments accordingly
            for (int c = preCol + increment; targetOnRight ? c < targetCol : c > targetCol; c += increment){
                int diff = Math.abs(c - preCol);
                for (Piece p : GamePanel.piecesShownOnBoard){
                    if (p.col == c && p.row == preRow - diff){
                        hittingP = p;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This determines if there is a piece in any lower diagonal path in the way of the destination location
     * The pieces list from the game panel class is iterated through
     * @param targetCol The destination column
     * @param targetRow The destination row
     * @return if there is piece obstructing any lower diagonal path
     */
    public boolean pieceOnLowerDiags(int targetCol, int targetRow){
        if (targetRow > preRow){//down
            boolean targetOnRight = preCol < targetCol;
            int increment = (targetOnRight) ? 1 : -1;
            //loop header determines whether the target is on the right or left and increments accordingly
            for (int c = preCol + increment; targetOnRight ? c < targetCol : c > targetCol; c += increment){
                int diff = Math.abs(c - preCol);
                for (Piece p : GamePanel.piecesShownOnBoard){
                    if (p.col == c && p.row == preRow + diff){
                        hittingP = p;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This gets the previous column number of the piece
     * @return the previous column number
     */
    public int getPreCol(){
        return this.preCol;
    }

    /**
     * This gets the previous row number of the piece
     * @return the previous row number
     */
    public int getPreRow(){
        return this.preRow;
    }

    /**
     * This gets the current column number of the piece
     * @return the current column number
     */
    public int getCol(){
        return this.col;
    }

    /**
     * This sets the current column number of piece with a specifed column number
     * @param column The specified column number
     */
    public void setCol(int column){
        this.col = column;
    }

    /**
     * This gets the current row number of the piece
     * @return the current row number
     */
    public int getRow(){
        return this.row;
    }

    /**
     * This sets the current row number of piece with a specifed row number
     * @param rowParam The specified row number
     */
    public void setRow(int rowParam){
        this.row = rowParam;
    }

    /**
     * This gets the x value of the piece
     * @return the x value
     */
    public int getX(){
        return this.x;
    }

    /**
     * This sets the x value of the piece with a specified x value
     * @param xParam the specified x value
     */
    public void setX(int xParam){
        this.x = xParam;
    }

    /**
     * This gets the y value of the piece
     * @return the y value
     */
    public int getY(){
        return this.y;
    }

    /**
     * This sets the x value of the piece with a specified x value
     * @param yParam the specified x value
     */
    public void setY(int yParam){
        this.y = yParam;
    }

    /**
     * This gets the piece that is being captured
     * @return the captured piece
     */
    public Piece getCapturedP(){
        return this.hittingP;
    }

    /**
     * This resets the two step moved status so its status is immediately set to false after any two step has been made
     * This method only matters for pawns and is a helper for en passant
     */
    public void resetTwoStepStatus(){
        this.twoStepped = false;
    }

    /**
     * This draws the image of the piece
     * This accesses the static variable for size from the board class
     * @param g2D the plotter that draws on the panel
     */
    public void draw(Graphics2D g2D){
        g2D.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}
