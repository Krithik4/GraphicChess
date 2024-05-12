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

/**
 * This class represents the game that is displayed in the JFrame window
 */
public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 750;
    public static final int HEIGHT = 480;
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
    public static Piece castlingP;

    private boolean canMove;
    private boolean validSquare;
    private boolean promotion;
    private boolean gameOver;
    private boolean stalemate;

    public static boolean canCastle;

    /**
     * This initializes an instance of the GamePanel class by setting up the panel and configuring mouse input
     * The pieces list and the backup pieces list are filled
     */
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

    /**
     * This method sets the piece that needs to be castled
     * castlingP is modified to whatever the parameter is
     * @param p The new castling piece
     */
    public static void setCastlingP(Piece p){
        castlingP = p;
    }

    /**
     * This method starts the game
     */
    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * This continuously updates the panel using a game loop
     */
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

    /**
     * This sets the pieces on the board at their proper locations
     * The pieces list is filled
     */
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

    /**
     * This method copies pieces from one list to another list
     * @param from The original list
     * @param to The destination list
     */
    private void copyPieces(LinkedList<Piece> from, LinkedList<Piece> to){
        to.clear();
        for (Piece piece : from) {
            to.add(piece);
        }
    }

    /**
     * This method determines what is outputted to the window
     * It registers piece movement and also shows promotion
     * Nothing happens if the game is over or if it is stalemate
     */
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

    /**
     * This method determines what happens after the mouse is released
     * The piece can update position and several message appear is certain cases occur (i. e. check)
     */
    public void updatePieceAfterMouseRelease(){
        if (currPiece != null){
            if (validSquare){
                currPiece.updatePosition();
                copyPieces(simPieces, pieces);
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
                if (castlingP != null){
                    castlingP.resetPosition();
                    castlingP = null;
                }
                currPiece = null;
            }
        }
    }

    /**
     * This method determines if the king is in check
     * @return whether the king is in check or not
     */
    private boolean kingInCheck(){
        Piece king = getKing(true);
        for (Piece p : simPieces){
            if (p.canMove(king.getCol(), king.getRow())){
                checkingP = p;
                return true;
            }
        }
        checkingP = null;
        return false;
    }


    /**
     * This method retrieves the king piece from the pieces list
     * @param opponent If opponent king needs to be retrieved, this is true; otherwise, it's false
     * @return the king piece
     */
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

    /**
     * This method determines if any piece can block check that occurs vertically
     * @param king The king piece
     * @return whether a block on the vertical can occur
     */
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

    /**
     * This method determines if any piece can block check that occurs horizontally
     * @param king The king piece
     * @return whether a block on the horizontal can occur
     */
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

    /**
     * This method determines if any piece can block check that occurs diagonally below the king
     * @param king The king piece
     * @return whether a block on the lower diagonals can occur
     */
    public boolean noBlockOnLowerDiagonal(Piece king){
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

    /**
     * This method determines if any piece can block check that occurs diagonally above the king
     * @param king The king piece
     * @return whether a block on the upper diagonals can occur
     */
    public boolean noBlockOnUpperDiagonal(Piece king){
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

    /**
     * This method determines if checkmate has occured
     * @return If it is checkmate, it returns true; otherwise, it's false
     */
    private boolean checkMate(){
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
                if (checkingP.getRow() > king.getRow()){
                    return noBlockOnLowerDiagonal(king);
                } else {
                    return noBlockOnUpperDiagonal(king);
                }
            }
        }
        return true;
    }

    /**
     * This method determines if the king can move to get out of check
     * @param king The king that is attempting to move
     * @return If the king can move out of check
     */
    private boolean kingCanMove(Piece king){
        if (isValidMoveForKing(king, -1, -1)) {return true;}
        if (isValidMoveForKing(king, 0, -1)) {return true;}
        if (isValidMoveForKing(king, 1, -1)) {return true;}
        if (isValidMoveForKing(king, -1, 0)) {return true;}
        if (isValidMoveForKing(king, 1, 0)) {return true;}
        if (isValidMoveForKing(king, -1, 1)) {return true;}
        if (isValidMoveForKing(king, 0, 1)) {return true;}
        if (isValidMoveForKing(king, 1, 1)) {return true;}
        return false;
    }

    /**
     * This determines if a destination is a valid move for the king
     * @param king The king trying to move
     * @param colPlus The difference in column numbers between origin and destination
     * @param rowPlus The difference in row numbers between origin and destination
     * @return whether it is a valid move for the king
     */
    private boolean isValidMoveForKing(Piece king, int colPlus, int rowPlus){
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
        copyPieces(pieces, simPieces);
        return validMove;
    }

    /**
     * This determines if the game is in stalemate or not
     * @return if it is stalemate, it is true; otherwise, it is false
     */
    private boolean staleMate(){
        /**
         * Fix stalemate
         * case 1: two kings only -> done
         * case 2: no piece of the next turn can move
         */

        if (simPieces.size() == 2 && simPieces.get(0).getPieceType().equals("King") && simPieces.get(1).getPieceType().equals("King")){
            return true;
        }

        for (int i = 0; i < simPieces.size(); i++){
              Piece p = simPieces.get(i);
            if (p.getColor() != currentColor){
                String type = p.getPieceType();
                boolean canMove = false;
                switch (type){
                    case "King":
                        canMove = kingCanMove(p);
                        break;
                    case "Queen":
                        canMove = queenCanMove(p);
                        break;
                    case "Rook":
                        canMove = rookCanMove(p);
                        break;
                    case "Bishop":
                        canMove = bishopCanMove(p);
                        break;
                    case "Knight":
                        canMove = knightCanMove(p);
                        break;
                    case "Pawn":
                        canMove = pawnCanMove(p);
                        break;
                    default:
                        break;
                }
                copyPieces(pieces, simPieces);
                if (canMove){
                    return false;
                }
            }
        }

//        int count = 0;
//        for (Piece p : simPieces){
//            if (p.getColor() != currentColor){
//                count++;
//            }
//        }
//
//        if (count == 1){
//            if (!kingCanMove(getKing(true))){
//                return true;
//            }
//        }
//        return false;
        return true;
    }

    public boolean knightCanMove(Piece knight){
        int col = knight.getCol();
        int row = knight.getRow();

        if (knight.canMove(col + 1, row - 2)){ //top right
            knight.setCol(col + 1);
            knight.setRow(row - 2);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col - 1, row - 2)){ //top left
            knight.setCol(col - 1);
            knight.setRow(row - 2);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col - 1, row + 2)){ //bottom left
            knight.setCol(col - 1);
            knight.setRow(row + 2);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col + 1, row + 2)){ //bottom right
            knight.setCol(col + 1);
            knight.setRow(row + 2);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col - 2, row - 1)){ //most left and 1 up
            knight.setCol(col - 2);
            knight.setRow(row - 1);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col + 2, row - 1)){ //most right and 1 up
            knight.setCol(col + 2);
            knight.setRow(row - 1);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col - 2, row + 1)){ //most left and 1 dow
            knight.setCol(col - 2);
            knight.setRow(row + 1);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        if (knight.canMove(col + 2, row + 1)){ //most right and 1 dow
            knight.setCol(col + 2);
            knight.setRow(row + 1);
            if (!kingInCheck()){
                knight.resetPosition();
                return true;
            }
        }
        knight.resetPosition();
        return false;
    }

    public boolean pawnCanMove(Piece pawn){
        int moveIncrement = (currentColor == WHITE) ? -1 : 1;
        int col = pawn.getCol();
        int row = pawn.getRow();

        if (pawn.canMove(col, row + moveIncrement)){
            pawn.setRow(row + moveIncrement);
            if (!kingInCheck()){
                pawn.resetPosition();
                return true;
            }
        }
        pawn.resetPosition();
        if (pawn.canMove(col, row + moveIncrement * 2)){
            pawn.setRow(row + moveIncrement * 2);
            if (!kingInCheck()){
                pawn.resetPosition();
                return true;
            }
        }
        pawn.resetPosition();
        if (pawn.canMove(col + 1, row + moveIncrement)){
            pawn.setCol(col + 1);
            pawn.setRow(row + moveIncrement);
            if (!kingInCheck()){
                pawn.resetPosition();
                return true;
            }
        }
        pawn.resetPosition();
        if (pawn.canMove(col - 1, row + moveIncrement)){
            pawn.setCol(col - 1);
            pawn.setRow(row + moveIncrement);
            if (!kingInCheck()){
                pawn.resetPosition();
                return true;
            }
        }
        pawn.resetPosition();
        return false;
    }

    public boolean rookCanMove(Piece rook){
        int col = rook.getCol();
        int row = rook.getRow();

        for (int i = -7; i < 8; i++){
            if (rook.canMove(col + i, row)){
                rook.setCol(col + i);
                if (!kingInCheck()){
                    rook.resetPosition();
                    return true;
                }
            }
            rook.resetPosition();
            if (rook.canMove(col, row + i)){
                rook.setRow(row + i);
                if (!kingInCheck()){
                    rook.resetPosition();
                    return true;
                }
            }
            rook.resetPosition();
        }
        rook.resetPosition();
        return false;
    }

    public boolean bishopCanMove(Piece bishop){
        int col = bishop.getCol();
        int row = bishop.getRow();

        for (int i = -7; i < 8; i++){
            if (bishop.canMove(col + i, row + i)){
                bishop.setCol(col + i);
                bishop.setRow(row + i);
                if (!kingInCheck()){
                    bishop.resetPosition();
                    return true;
                }
            }
            bishop.resetPosition();
            if (bishop.canMove(col + i, row - i)){
                bishop.setCol(col + i);
                bishop.setRow(row - i);
                if (!kingInCheck()){
                    bishop.resetPosition();
                    return true;
                }
            }
            bishop.resetPosition();
            if (bishop.canMove(col - i, row - i)){
                bishop.setCol(col - i);
                bishop.setRow(row - i);
                if (!kingInCheck()){
                    bishop.resetPosition();
                    return true;
                }
            }
            bishop.resetPosition();
            if (bishop.canMove(col + i, row - i)){
                bishop.setCol(col + i);
                bishop.setRow(row - i);
                if (!kingInCheck()){
                    bishop.resetPosition();
                    return true;
                }
            }
            bishop.resetPosition();
        }
        bishop.resetPosition();
        return false;
    }

    public boolean queenCanMove(Piece queen){
        int col = queen.getCol();
        int row = queen.getRow();

        for (int i = -7; i < 8; i++){
            if (queen.canMove(col + i, row)){
                queen.setCol(col + i);
                if (!kingInCheck()){
                    queen.resetPosition();
                    return true;
                }
            }
            queen.resetPosition();
            if (queen.canMove(col, row + i)){
                queen.setRow(row + i);
                if (!kingInCheck()){
                    queen.resetPosition();
                    return true;
                }
            }
            queen.resetPosition();

            if (queen.canMove(col + i, row + i)){
                queen.setCol(col + i);
                queen.setRow(row + i);
                if (!kingInCheck()){
                    queen.resetPosition();
                    return true;
                }
            }
            queen.resetPosition();
            if (queen.canMove(col + i, row - i)){
                queen.setCol(col + i);
                queen.setRow(row - i);
                if (!kingInCheck()){
                    queen.resetPosition();
                    return true;
                }
            }
            queen.resetPosition();
            if (queen.canMove(col - i, row - i)){
                queen.setCol(col - i);
                queen.setRow(row - i);
                if (!kingInCheck()){
                    queen.resetPosition();
                    return true;
                }
            }
            queen.resetPosition();
            if (queen.canMove(col + i, row - i)){
                queen.setCol(col + i);
                queen.setRow(row - i);
                if (!kingInCheck()){
                    queen.resetPosition();
                    return true;
                }
            }
            queen.resetPosition();
        }

        queen.resetPosition();
        return false;
    }



    /**
     * This updates the location of the piece when the user has already selected a piece
     */
    private void updatePieceLocationWhenHeld(){
        canMove = false;
        validSquare = false;
        copyPieces(pieces, simPieces);
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

    /**
     * This method helps set up the castling by checking to see if there is another piece
     * in the casting piece variable
     */
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

    /**
     * This determines whether a move is illegal or not
     * @param king The king that is moving
     * @return the move's illegality (true if it is illegal)
     */
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

    /**
     * This determines if the current color's king can be captured by the opponent
     * @return if the opponent can capture the king or not
     */
    private boolean opponentCanCaptureKing(){
        Piece king = getKing(false);
        for (Piece p : simPieces){
            if (p.getColor() != king.getColor() && p.canMove(king.getCol(), king.getRow())){
                return true;
            }
        }
        return false;
    }

    /**
     * This determines whether the king is in check or not based on its stationary position
     * This is used to determine whether the king can castle or not
     * Other requirements need to be satisfied in order to fully confirm if the king can castle or not
     * @return if the current king is in check
     */
    private boolean isCurrentKingInCheck(){
        Piece king = getKing(false);
        for (Piece p : simPieces){
            if (p.getColor() != king.getColor() && p.canMove(king.getPreCol(), king.getPreRow())){
                return true;
            }
        }
        return false;
    }

    /**
     * This draws all the components necessary for the game (board and pieces) as well information
     * regarding turn, check, stalemate, and checkmate
     * This updates the canCastle variable to determine if the king is currently in check and is eligible to castle
     * This iterates through the pieces list to display on the panel
     * @param g the plotter that puts the graphics on the panel/window
     */
    public void paintComponent(Graphics g){
        canCastle = !isCurrentKingInCheck();
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        gameBoard.draw(g2D);//board
        for (Piece p : simPieces){
            p.draw(g2D);
        }
        if (currPiece != null){
            if (canMove){
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2D.setColor(new Color(74, 159, 232));
                g2D.fillRect(currPiece.getCol() * Board.SQUARE_SIZE, currPiece.getRow() * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
            currPiece.draw(g2D);
        }
        displayOtherScenarioInfo(g2D);
    }

    /**
     * This displays other info other than the board and pieces such as turn info and game status (like check)
     * @param g2D The plotter that displays the information
     */
    public void displayOtherScenarioInfo(Graphics2D g2D){
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.setFont(new Font("Book Antique", Font.PLAIN, 40));
        g2D.setColor(Color.white);
        if (promotion){ //extra options for promotion
            g2D.setFont(new Font("Book Antique", Font.PLAIN, 30));
            g2D.drawString("Promote to:", 500, 75);
            for (Piece p : promoPieces){
                g2D.drawImage(p.image, Piece.calcX(p.getCol()), Piece.calcY(p.getRow()), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } else { //to display turn information, and check information
            String text = (currentColor == WHITE) ? "White's " : "Black's ";
            int yCoord = (currentColor == WHITE) ? 400 : 100;
            g2D.drawString(text + "Turn", 500, yCoord);
            if (currentColor == WHITE && checkingP != null && checkingP.getColor() == BLACK){
                g2D.setFont(new Font("Book Antique", Font.PLAIN, 20));
                g2D.setColor(Color.red);
                g2D.drawString("The King is in check!", 500, 450);
            } else if (currentColor == BLACK && checkingP != null && checkingP.getColor() == WHITE){
                g2D.setFont(new Font("Book Antique", Font.PLAIN, 20));
                g2D.setColor(Color.red);
                g2D.drawString("The King is in check!", 500, 150);
            }
        }
        if (gameOver){ //checkmate info
            String text = (currentColor == WHITE) ? "White Wins" : "Black Wins";
            g2D.setFont(new Font("Arial", Font.PLAIN, 60));
            g2D.setColor(Color.BLUE);
            g2D.drawString(text, 100, 250);
        }
        if (stalemate){ //stalemate info
            g2D.setFont(new Font("Arial", Font.PLAIN, 60));
            g2D.setColor(Color.BLUE);
            g2D.drawString("Stalemate", 100, 250);
        }
    }

    /**
     * This changes the turn either from black to white or white to black
     */
    private void changeTurn(){
        currentColor = (currentColor == WHITE) ? BLACK : WHITE;
        for (Piece p : pieces){
            if (p.getColor() == currentColor){
                p.resetTwoStepStatus();
            }
        }
        currPiece = null;
    }

    /**
     * This determines if the current piece can promote
     * Promotion only occurs if the current piece is a pawn
     * @return true if the piece can promote, otherwise return false
     */
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

    /**
     * This puts the new piece from promotion into the piece list so it can be displayed on the board
     * Then the turn changes
     */
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
                    copyPieces(simPieces, pieces);
                    currPiece = null;
                    promotion = false;
                    changeTurn();
                }
            }
        }
    }
}
