import javax.swing.JPanel;
//import java.awt.*;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.util.ArrayList;
import java.util.LinkedList;


public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    private Thread gameThread;
    private Board gameBoard;
    private Mouse userMouse;

    //color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    private int currentColor = WHITE;

    //pieces
    public static LinkedList<Piece> pieces = new LinkedList<>();
    public static LinkedList<Piece> simPieces = new LinkedList<>();
    private ArrayList<Piece> promoPieces;
    private Piece currPiece, checkingP;
    public static Piece castlingP; //hmm

    private boolean canMove;
    private boolean validSquare;
    private boolean promotion;
    private boolean gameOver;
    private boolean stalemate;

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        userMouse = new Mouse();
        gameBoard = new Board();
        promoPieces = new ArrayList<>();
        addMouseMotionListener(userMouse);
        addMouseListener(userMouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public static void setCastlingP(Piece p){
        castlingP = p;
    }
    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000/60;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        while (gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;
            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    private void setPieces(){
        for (int i = 0; i < 8; i++){
            pieces.add(new Pawn(WHITE, i, 6));
        }
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        for (int i = 0; i < 8; i++){
            pieces.add(new Pawn(BLACK, i, 1));
        }
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }

    private void copyPieces(LinkedList<Piece> from, LinkedList<Piece> to){
        to.clear();
        for (Piece piece : from) {
            to.add(piece);
        }
    }

    private void update(){
        if (promotion){
            promoting();
        } else if (!gameOver && !stalemate){
            if (userMouse.isPressed()) {
                if (currPiece == null) {
                    for (Piece piece : simPieces) {
                        if (piece.getColor() == currentColor && piece.getCol() == userMouse.getX() / Board.SQUARE_SIZE
                                && piece.getRow() == userMouse.getY() / Board.SQUARE_SIZE) {
                            currPiece = piece;
                        }
                    }
                } else {
                    updatePieceLocationWhenHeld();
                }
            } else {
                updatePieceAfterMouseRelease();
            }
        }
    }

    public void updatePieceAfterMouseRelease(){
        if (currPiece != null){
            if (validSquare){
                currPiece.updatePosition();
                copyPieces(simPieces, pieces); //hmm
                if (castlingP != null){
                    castlingP.updatePosition();
                }
                if (kingInCheck() && checkMate()){
                    gameOver = true;
                } else if (!kingInCheck() && staleMate()){
                    stalemate = true;
                } else {
                    if (canPromote()) {
                        promotion = true;
                    } else {
                        changeTurn();
                    }
                }
            } else {
                copyPieces(pieces, simPieces);
                currPiece.resetPosition();
                currPiece = null;
            }
        }
    }

    private boolean kingInCheck(){
        Piece king = getKing(true);
        if (currPiece.canMove(king.getCol(), king.getRow())){
            checkingP = currPiece;
            return true;
        } else {
            checkingP = null;
        }
        return false;
    }

    private Piece getKing(boolean opponent){
        Piece king = null;
        for (Piece p : simPieces){
            if (opponent){
                if ("King".equals(p.getPieceType()) && p.getColor() != currentColor){
                    king = p;
                }
            } else {
                if ("King".equals(p.getPieceType()) && p.getColor() == currentColor){
                    king = p;
                }
            }
        }
        return king;
    }

    public boolean noBlockOnVertical(Piece king){
        if (checkingP.getRow() < king.getRow()){
            for (int row = checkingP.getRow(); row < king.getRow(); row++){
                for (Piece p : simPieces){
                    if (p != king && p.getColor() != currentColor && p.canMove(checkingP.getCol(), row)){
                        return false;
                    }
                }
            }
        }
        if (checkingP.getRow() > king.getRow()){
            for (int row = checkingP.getRow(); row > king.getRow(); row--){
                for (Piece p : simPieces){
                    if (p != king && p.getColor() != currentColor && p.canMove(checkingP.getCol(), row)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean noBlockOnHorizontal(Piece king){
        if (checkingP.getCol() < king.getCol()){
            for (int col = checkingP.getCol(); col < king.getRow(); col++){
                for (Piece p : simPieces){
                    if (p != king && p.getColor() != currentColor && p.canMove(col, checkingP.getRow())){
                        return false;
                    }
                }
            }
        }
        if (checkingP.getCol() > king.getCol()){
            for (int col = checkingP.getCol(); col > king.getRow(); col--){
                for (Piece p : simPieces){
                    if (p != king && p.getColor() != currentColor && p.canMove(col, checkingP.getRow())){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean noBlockOnUpperDiagonal(Piece king){
        if (checkingP.getRow() > king.getRow()){
            if (checkingP.getCol() < king.getCol()){
                for (int col = checkingP.getCol(), row = checkingP.getRow(); col < king.getCol(); col++, row--){
                    for (Piece p : simPieces){
                        if (p != king && p.getColor() != currentColor && p.canMove(col, row)){
                            return false;
                        }
                    }
                }
            }
            if (checkingP.getCol() > king.getCol()){
                for (int col = checkingP.getCol(), row = checkingP.getRow(); col > king.getCol(); col--, row--){
                    for (Piece p : simPieces){
                        if (p != king && p.getColor() != currentColor && p.canMove(col, row)){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean noBlockOnLowerDiagonal(Piece king){
        if (checkingP.getRow() < king.getRow()){
            if (checkingP.getCol() < king.getCol()){
                for (int col = checkingP.getCol(), row = checkingP.getRow(); col < king.getCol(); col++, row++){
                    for (Piece p : simPieces){
                        if (p != king && p.getColor() != currentColor && p.canMove(col, row)){
                            return false;
                        }
                    }
                }
            }
            if (checkingP.getCol() > king.getCol()){
                for (int col = checkingP.getCol(), row = checkingP.getRow(); col > king.getCol(); col--, row++){
                    for (Piece p : simPieces){
                        if (p != king && p.getColor() != currentColor && p.canMove(col, row)){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    private boolean checkMate(){ //split to adhere to style guide
        Piece king = getKing(true);
        if (kingCanMove(king)){
            return false;
        } else { //blocking
            int colDiff = Math.abs(checkingP.getCol() - king.getCol());
            int rowDiff = Math.abs(checkingP.getRow() - king.getRow());
            if (colDiff == 0){
                return noBlockOnVertical(king);
            } else if (rowDiff == 0){
                return noBlockOnHorizontal(king);
            } else if (rowDiff == colDiff){
                return noBlockOnUpperDiagonal(king) || noBlockOnLowerDiagonal(king);
            }
        }
        return true;
    }

    private boolean kingCanMove(Piece king){
        if (isValidMove(king, -1, -1)) {return true;}
        if (isValidMove(king, 0, -1)) {return true;}
        if (isValidMove(king, 1, -1)) {return true;}
        if (isValidMove(king, -1, 0)) {return true;}
        if (isValidMove(king, 1, 0)) {return true;}
        if (isValidMove(king, -1, 1)) {return true;}
        if (isValidMove(king, 0, 1)) {return true;}
        if (isValidMove(king, 1, 1)) {return true;}
        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus){
        boolean validMove = false;
        king.setCol(king.getCol() + colPlus);
        king.setRow(king.getRow() + rowPlus);
        if (king.canMove(king.getCol(), king.getRow())){
            if (king.getCapturedP() != null){
                simPieces.remove(king.getCapturedP().getIndex());
            }
            if (!isIllegal(king)){
                validMove = true;
            }
        }
        king.resetPosition();
        copyPieces(pieces, simPieces); //hmm
        return validMove;
    }

    private boolean staleMate(){
        int count = 0;
        for (Piece p : simPieces){
            if (p.getColor() != currentColor){
                count++;
            }
        }

        if (count == 1){
            if (!kingCanMove(getKing(true))){
                return true;
            }
        }
        return false;
    }

    private void updatePieceLocationWhenHeld(){
        canMove = false;
        validSquare = false;
        copyPieces(pieces, simPieces); //hmm
        if (castlingP != null){
            castlingP.setCol(castlingP.getPreCol());
            castlingP.setX(Piece.calcX(castlingP.getCol()));
            castlingP = null;
        }
        if (currPiece != null){
            currPiece.setX(userMouse.getX() - Board.HALF_SQUARE_SIZE);
            currPiece.setY(userMouse.getY() - Board.HALF_SQUARE_SIZE);
            currPiece.setCol(Piece.calcCol(currPiece.getX()));
            currPiece.setRow(Piece.calcRow(currPiece.getY()));
            if (currPiece.canMove(currPiece.getCol(), currPiece.getRow())){
                canMove = true;
                if (currPiece.getCapturedP() != null){
                    simPieces.remove(currPiece.getCapturedP().getIndex());
                }
                checkCastling();
                if (!isIllegal(currPiece) && !opponentCanCaptureKing()){
                    validSquare = true;
                }
            }
        }
    }

    private void checkCastling(){
        if (castlingP != null){
            if (castlingP.getCol() == 0){
                castlingP.setCol(castlingP.getCol() + 3);
            } else if (castlingP.getCol() == 7){
                castlingP.setCol(castlingP.getCol() - 2);
            }
            castlingP.setX(Piece.calcX(castlingP.getCol()));
        }
    }

    private boolean isIllegal(Piece king){
        if ("King".equals(king.getPieceType())){
            for (Piece p : simPieces){
                if (p != king && p.getColor() != king.getColor() && p.canMove(king.getCol(), king.getRow())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(){
        Piece king = getKing(false);
        for (Piece p : simPieces){
            if (p.getColor() != king.getColor() && p.canMove(king.getCol(), king.getRow())){
                return true;
            }
        }
        return false;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        gameBoard.draw(g2D);//board
        for (Piece p : simPieces){
            p.draw(g2D);
        }
        if (currPiece != null){
            if (canMove){
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.7f));
                g2D.fillRect(currPiece.getCol() * Board.SQUARE_SIZE, currPiece.getRow() * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
            currPiece.draw(g2D);
        }
        displayOtherScenarioInfo(g2D);
    }

    public void displayOtherScenarioInfo(Graphics2D g2D){
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.setFont(new Font("Book Antique", Font.PLAIN, 40));
        g2D.setColor(Color.white);
        if (promotion){
            g2D.drawString("Promote to:", 840, 150);
            for (Piece p : promoPieces){
                g2D.drawImage(p.image, Piece.calcX(p.getCol()), Piece.calcY(p.getRow()), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } else {
            String text = (currentColor == WHITE) ? "White's " : "Black's ";
            int yCoord = (currentColor == WHITE) ? 600 : 200;
            g2D.drawString(text + "Turn", 840, yCoord);
            if (currentColor == WHITE && checkingP != null && checkingP.getColor() == BLACK){
                g2D.setColor(Color.red);
                g2D.drawString("The King", 840, 650);
                g2D.drawString("is in check!", 840, 700);
            } else if (currentColor == BLACK && checkingP != null && checkingP.getColor() == WHITE){
                g2D.setColor(Color.red);
                g2D.drawString("The King", 840, 100);
                g2D.drawString("is in check!", 840, 150);
            }
        }
        if (gameOver){
            String text = (currentColor == WHITE) ? "White Wins" : "Black Wins";
            g2D.setFont(new Font("Arial", Font.PLAIN, 90));
            g2D.setColor(Color.green);
            g2D.drawString(text, 200, 420);
        }
        if (stalemate){
            g2D.setFont(new Font("Arial", Font.PLAIN, 90));
            g2D.setColor(Color.green);
            g2D.drawString("Stalemate", 200, 420);
        }
    }

    private void changeTurn(){
        currentColor = (currentColor == WHITE) ? BLACK : WHITE;
        currPiece = null;
    }

    private boolean canPromote(){
        if ("Pawn".equals(currPiece.getPieceType())){
            if (currentColor == WHITE && currPiece.getRow() == 0 || currentColor == BLACK && currPiece.getRow() == 7){
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9, 2));
                promoPieces.add(new Knight(currentColor, 9, 3));
                promoPieces.add(new Bishop(currentColor, 9, 4));
                promoPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }
        return false;
    }

    private void promoting(){
        if (userMouse.isPressed()){
            for (Piece p : promoPieces){
                if (p.getCol() == userMouse.getX()/ Board.SQUARE_SIZE && p.getRow() == userMouse.getY()/ Board.SQUARE_SIZE){
                    switch(p.getPieceType()){
                        case "Rook": simPieces.add(new Rook(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        case "Knight": simPieces.add(new Knight(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        case "Bishop": simPieces.add(new Bishop(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        case "Queen": simPieces.add(new Queen(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        default: break;
                    }
                    simPieces.remove(currPiece.getIndex());
                    copyPieces(simPieces, pieces); //hmm
                    currPiece = null;
                    promotion = false;
                    changeTurn();
                }
            }
        }
    }
}
