package main;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

import piece.*;


public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board gameBoard = new Board();
    Mouse userMouse = new Mouse();

    //color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    //pieces
    public static LinkedList<Piece> pieces = new LinkedList<>();
    public static LinkedList<Piece> simPieces = new LinkedList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece currPiece, checkingP;
    public static Piece castlingP;

    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseMotionListener(userMouse);
        addMouseListener(userMouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
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

    public void setPieces(){
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
            if (userMouse.pressed) {
                if (currPiece == null) {
                    for (Piece piece : simPieces) {
                        if (piece.color == currentColor && piece.col == userMouse.x / Board.SQUARE_SIZE && piece.row == userMouse.y / Board.SQUARE_SIZE) {
                            currPiece = piece;
                        }
                    }
                } else {
                    simulate();
                }
            }

            //just teleports, doesnt move smoothly********
            if (!userMouse.pressed){
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
        }

    }

    private boolean kingInCheck(){
        Piece king = getKing(true);
        if (currPiece.canMove(king.col, king.row)){
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
                if (p.pieceType == Type.KING && p.color != currentColor){
                    king = p;
                }
            } else {
                if (p.pieceType == Type.KING && p.color == currentColor){
                    king = p;
                }
            }
        }
        return king;
    }

    private boolean checkMate(){
        Piece king = getKing(true);
        if (kingCanMove(king)){
            return false;
        } else { //blocking
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);
            if (colDiff == 0){
                if (checkingP.row < king.row){
                    for (int row = checkingP.row; row < king.row; row++){
                        for (Piece p : simPieces){
                            if (p != king && p.color != currentColor && p.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.row > king.row){
                    for (int row = checkingP.row; row > king.row; row--){
                        for (Piece p : simPieces){
                            if (p != king && p.color != currentColor && p.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff == 0){
                if (checkingP.col < king.col){
                    for (int col = checkingP.col; col < king.row; col++){
                        for (Piece p : simPieces){
                            if (p != king && p.color != currentColor && p.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.col > king.col){
                    for (int col = checkingP.col; col > king.row; col--){
                        for (Piece p : simPieces){
                            if (p != king && p.color != currentColor && p.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff == colDiff){
                if (checkingP.row < king.row){
                    if (checkingP.col < king.col){
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++){
                            for (Piece p : simPieces){
                                if (p != king && p.color != currentColor && p.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingP.col > king.col){
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++){
                            for (Piece p : simPieces){
                                if (p != king && p.color != currentColor && p.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if (checkingP.row > king.row){
                    if (checkingP.col < king.col){
                        for (int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--){
                            for (Piece p : simPieces){
                                if (p != king && p.color != currentColor && p.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingP.col > king.col){
                        for (int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--){
                            for (Piece p : simPieces){
                                if (p != king && p.color != currentColor && p.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                    //can be removed
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
        king.col += colPlus;
        king.row += rowPlus;
        if (king.canMove(king.col, king.row)){
            if (king.hittingP != null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if (!isIllegal(king)){
                validMove = true;
            }
        }
        king.resetPosition();
        //some copy stuff
        copyPieces(pieces, simPieces); //hmm
        return validMove;
    }

    private boolean staleMate(){
        int count = 0;
        for (Piece p : simPieces){
            if (p.color != currentColor){
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

    private void simulate(){
        canMove = false;
        validSquare = false;
        copyPieces(pieces, simPieces); //hmm
        if (castlingP !=  null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        if (currPiece != null){
            currPiece.x = userMouse.x - Board.HALF_SQUARE_SIZE;
            currPiece.y = userMouse.y - Board.HALF_SQUARE_SIZE;
            currPiece.col = currPiece.getCol(currPiece.x);
            currPiece.row = currPiece.getRow(currPiece.y);
            if (currPiece.canMove(currPiece.col, currPiece.row)){
                canMove = true;

                if (currPiece.hittingP != null){
                    simPieces.remove(currPiece.hittingP.getIndex());
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
            if (castlingP.col == 0){
                castlingP.col += 3;
            } else if (castlingP.col == 7){
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private boolean isIllegal(Piece king){
        if (king.pieceType == Type.KING){
            for (Piece p : simPieces){
                if (p != king && p.color != king.color && p.canMove(king.col, king.row)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(){
        Piece king = getKing(false);
        for (Piece p : simPieces){
            if (p.color != king.color && p.canMove(king.col, king.row)){
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
                if (isIllegal(currPiece) || opponentCanCaptureKing()){
                    g2D.setColor(new Color(255, 71, 77));
                } else {
                    g2D.setColor(new Color(173, 216, 230));
                }

                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0.7f));
                g2D.fillRect(currPiece.col * Board.SQUARE_SIZE, currPiece.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            currPiece.draw(g2D);
        }


        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.setFont(new Font("Book Antique", Font.PLAIN, 40));
        g2D.setColor(Color.white);


        if (promotion){
            g2D.drawString("Promote to:", 840, 150);
            for (Piece p : promoPieces){
                g2D.drawImage(p.image, p.getX(p.col), p.getY(p.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } else {
            String text = (currentColor == WHITE) ? "White's " : "Black's ";
            int yCoord = (currentColor == WHITE) ? 600 : 200;
            g2D.drawString(text + "Turn", 840, yCoord);
            if (currentColor == WHITE && checkingP != null && checkingP.color == BLACK){
                g2D.setColor(Color.red);
                g2D.drawString("The King", 840, 650);
                g2D.drawString("is in check!", 840, 700);
            } else if (currentColor == BLACK && checkingP != null && checkingP.color == WHITE){
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
        for (Piece p : pieces){
            if (p.color == currentColor){
                p.twoStepped = false;
            }
        }
        currPiece = null;
    }

    private boolean canPromote(){
        if (currPiece.pieceType == Type.PAWN){
            if (currentColor == WHITE && currPiece.row == 0 || currentColor == BLACK && currPiece.row == 7){
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
        if (userMouse.pressed){
            for (Piece p : promoPieces){
                if (p.col == userMouse.x/Board.SQUARE_SIZE && p.row == userMouse.y/Board.SQUARE_SIZE){
                    switch(p.pieceType){
                        case ROOK: simPieces.add(new Rook(currentColor, currPiece.col, currPiece.row)); break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, currPiece.col, currPiece.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor, currPiece.col, currPiece.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor, currPiece.col, currPiece.row)); break;
                        default: break;
                    }
                    simPieces.remove(currPiece.getIndex());
                    //copy
                    copyPieces(simPieces, pieces); //hmm
                    currPiece = null;
                    promotion = false;
                    changeTurn();
                }
            }
        }
    }
}
