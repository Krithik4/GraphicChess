import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.util.ArrayList;
import java.util.HashSet;

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
    public static HashSet<Piece> pieces = new HashSet<>();
    public static HashSet<Piece> piecesShownOnBoard = new HashSet<>();
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
        copyPieces(pieces, piecesShownOnBoard);
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
     * The pieces list is filled and updated
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
    private void copyPieces(HashSet<Piece> from, HashSet<Piece> to){
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
            opponentCanCaptureKing();
        } else if (!gameOver && !stalemate){
            if (userMouse.isPressed()) {
                if (currPiece == null) {
                    for (Piece piece : piecesShownOnBoard) {
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
                copyPieces(piecesShownOnBoard, pieces);
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
                copyPieces(pieces, piecesShownOnBoard);
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
        for (Piece p : piecesShownOnBoard){
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
        for (Piece p : piecesShownOnBoard){
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
        boolean checkAbove = checkingP.getRow() < king.getRow();
        int increment = (checkAbove) ? 1 : -1;
        for (int row = checkingP.getRow(); checkAbove ? row < king.getRow() : row > king.getRow(); row += increment){
            for (Piece p : piecesShownOnBoard){
                if (p != king && p.getColor() != currentColor && p.canMove(checkingP.getCol(), row)){
                    return false;
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
        boolean checkLeft = checkingP.getCol() < king.getCol();
        int increment = (checkLeft) ? 1 : -1;
        for (int col = checkingP.getCol(); checkLeft ? col < king.getCol() : col > king.getCol(); col += increment){
            for (Piece p : piecesShownOnBoard){
                if (p != king && p.getColor() != currentColor && p.canMove(col, checkingP.getRow())){
                    return false;
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
            boolean checkLeft = checkingP.getCol() < king.getCol();
            int increment = (checkLeft) ? 1 : -1;
            for (int col = checkingP.getCol(), row = checkingP.getRow();
                 checkLeft ? col < king.getCol() : col > king.getCol(); col += increment, row--){
                for (Piece p : piecesShownOnBoard){
                    if (p != king && p.getColor() != currentColor && p.canMove(col, row)){
                        return false;
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
            boolean checkLeft = checkingP.getCol() < king.getCol();
            int increment = (checkLeft) ? 1 : -1;
            for (int col = checkingP.getCol(), row = checkingP.getRow();
                 checkLeft ? col < king.getCol() : col > king.getCol(); col += increment, row++){
                for (Piece p : piecesShownOnBoard){
                    if (p != king && p.getColor() != currentColor && p.canMove(col, row)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * This method determines if checkmate has occurred
     * @return If it is checkmate, it returns true; otherwise, it's false
     */
    private boolean checkMate(){
        Piece king = getKing(true);
        if (kingCanMove(king)){
            return false;
        } else { //blocking
            int colDiff = Math.abs(checkingP.getCol() - king.getCol());
            int rowDiff = Math.abs(checkingP.getRow() - king.getRow());
            if (colDiff == 0){ // rook or queen attacking
                return noBlockOnVertical(king);
            } else if (rowDiff == 0){ //rook or queen attacking
                return noBlockOnHorizontal(king);
            } else if (rowDiff == colDiff){ //bishop or queen attacking
                if (checkingP.getRow() > king.getRow()){
                    return noBlockOnLowerDiagonal(king);
                } else {
                    return noBlockOnUpperDiagonal(king);
                }
            } else { //knight attacking
                for (Piece p : piecesShownOnBoard){
                    if (p.getColor() != currentColor && p.canMove(checkingP.getCol(), checkingP.getRow())){
                        return false;
                    }
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
                piecesShownOnBoard.remove(king.getCapturedP());
            }
            if (!isIllegal(king)){
                validMove = true;
            }
        }
        king.resetPosition();
        copyPieces(pieces, piecesShownOnBoard);
        return validMove;
    }

    /**
     * This determines if the game is in stalemate or not
     * @return if it is stalemate, it is true; otherwise, it is false
     */
    private boolean staleMate(){
        if (piecesShownOnBoard.size() == 2){
            //only kings are left
            return true;
        }
        boolean kingCantMove = !kingCanMove(getKing(true));
        for (Piece p : pieces){ //goes through all pieces and determines if any piece can make a legal move
            if (p.getColor() != currentColor && p != getKing(true)){
                String pType = p.getPieceType();
                switch (pType){
                    case "Pawn":
                        if (pawnCanMove(p)){return false;}
                        break;
                    case "Knight":
                        if (knightCanMove(p)){return false;}
                        break;
                    case "Bishop":
                        if (bishopCanMove(p)){return false;}
                        break;
                    case "Rook":
                        if (rookCanMove(p)){return false;}
                        break;
                    case "Queen":
                        if (queenCanMove(p)){return false;}
                        break;
                }
            }
        }
        return kingCantMove;
    }

    /**
     * This determines all the possible locations a pawn could move from its current location
     * If it can move anywhere legally, then the method returns true
     * @param pawn The pawn piece
     * @return if the pawn can move anywhere legally
     */
    public boolean pawnCanMove(Piece pawn){
        int[] colDiff = {0, 0, 1, -1};
        int[] rowDiff = {1, 2, 1, 1};
        for (int i = 0; i < colDiff.length; i++){
            rowDiff[i] = (pawn.getColor() == WHITE) ? rowDiff[i] * -1 : rowDiff[i];
            pawn.setCol(pawn.getCol() + colDiff[i]);
            pawn.setRow(pawn.getRow() + rowDiff[i]);
            if (pawn.canMove(pawn.getCol(), pawn.getRow())){
                if (pawn.getCapturedP() != null){
                    piecesShownOnBoard.remove(pawn.getCapturedP());
                }
                if (!kingInCheck()){
                    pawn.resetPosition();
                    copyPieces(pieces, piecesShownOnBoard);
                    return true;
                }
            }
            pawn.resetPosition();
            copyPieces(pieces, piecesShownOnBoard);
        }
        return false;
    }

    /**
     * This determines all the possible locations a knight could move from its current location
     * If it can move anywhere legally, then the method returns true
     * @param knight The knight piece
     * @return if the knight can move anywhere legally
     */
    public boolean knightCanMove(Piece knight){
        int [] colDiff = {-2, -2, -1, -1, 1, 1, 2, 2};
        int [] rowDiff = {-1, 1, -2, 2, -2, 2, -1, 1};

        for (int i = 0; i < colDiff.length; i++){
            knight.setCol(knight.getCol() + colDiff[i]);
            knight.setRow(knight.getRow() + rowDiff[i]);
            if (knight.canMove(knight.getCol(), knight.getRow())){
                if (knight.getCapturedP() != null){
                    piecesShownOnBoard.remove(knight.getCapturedP());
                }
                if (!kingInCheck()){
                    knight.resetPosition();
                    copyPieces(pieces, piecesShownOnBoard);
                    return true;
                }
            }
            knight.resetPosition();
            copyPieces(pieces, piecesShownOnBoard);
        }
        return false;
    }

    /**
     * This determines all the possible locations a bishop can move from its current location
     * If it can move anywhere legally, it returns true
     * @param bishop The bishop piece
     * @return if the bishop can move anywhere legally
     */
    public boolean bishopCanMove(Piece bishop){
        int[] colDiff = {1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1};
        int[] rowDiff = {1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1};

        for (int i = 0; i < colDiff.length; i++){
            bishop.setCol(bishop.getCol() + colDiff[i]);
            bishop.setRow(bishop.getRow() + rowDiff[i]);
            if (bishop.canMove(bishop.getCol(), bishop.getRow())){
                if (bishop.getCapturedP() != null){
                    piecesShownOnBoard.remove(bishop.getCapturedP());
                }
                if (!kingInCheck()){
                    bishop.resetPosition();
                    copyPieces(pieces, piecesShownOnBoard);
                    return true;
                }
            }
            bishop.resetPosition();
            copyPieces(pieces, piecesShownOnBoard);
        }
        return false;
    }

    /**
     * This determines all the possible locations a rook can move from its current location
     * If it can move anywhere legally, it returns true
     * @param rook The rook piece
     * @return if the rook can move anywhere legally
     */
    public boolean rookCanMove(Piece rook){
        int[] colDiff = {1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1};
        int[] rowDiff = {1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1};

        for (int i = 0; i < colDiff.length * 2; i++){
            if (i < colDiff.length){
                rook.setCol(rook.getCol() + colDiff[i]);
            } else {
                rook.setRow(rook.getRow() + rowDiff[i - colDiff.length]);
            }
            if (rook.canMove(rook.getCol(), rook.getRow())){
                if (rook.getCapturedP() != null){
                    piecesShownOnBoard.remove(rook.getCapturedP());
                }
                if (!kingInCheck()){
                    rook.resetPosition();
                    copyPieces(pieces, piecesShownOnBoard);
                    return true;
                }
            }
            rook.resetPosition();
            copyPieces(pieces, piecesShownOnBoard);
        }
        return false;
    }

    /**
     * This determines all the possible locations a queen can move from its current location
     * If it can move anywhere legally, it returns true
     * @param queen The queen piece
     * @return if the queen can move anywhere legally
     */
    public boolean queenCanMove(Piece queen){
        int[] colDiff = {1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1};
        int[] rowDiff = {1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1};

        for (int i = 0; i < colDiff.length * 3; i++){
            if (i < colDiff.length){ //diagonal
                queen.setCol(queen.getCol() + colDiff[i]);
                queen.setRow(queen.getRow() + rowDiff[i]);
            } else if (i < colDiff.length * 2){ //left and right
                queen.setCol(queen.getCol() + colDiff[i - colDiff.length]);
            } else { //up and down
                queen.setRow(queen.getRow() + rowDiff[i - colDiff.length * 2]);
            }
            if (queen.canMove(queen.getCol(), queen.getRow())){
                if (queen.getCapturedP() != null){
                    piecesShownOnBoard.remove(queen.getCapturedP());
                }
                if (!kingInCheck()){
                    queen.resetPosition();
                    copyPieces(pieces, piecesShownOnBoard);
                    return true;
                }
            }
            queen.resetPosition();
            copyPieces(pieces, piecesShownOnBoard);
        }
        return false;
    }

    /**
     * This updates the location of the piece when the user has already selected a piece
     */
    private void updatePieceLocationWhenHeld(){
        canMove = false;
        validSquare = false;
        copyPieces(pieces, piecesShownOnBoard);
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
                    piecesShownOnBoard.remove(currPiece.getCapturedP());
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
            for (Piece p : piecesShownOnBoard){
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
        for (Piece p : piecesShownOnBoard){
            if (p.getColor() != king.getColor() && p.canMove(king.getCol(), king.getRow())){
                checkingP = p;
                return true;
            }
        }
        checkingP = null;
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
        canCastle = !opponentCanCaptureKing();
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        gameBoard.draw(g2D);//board
        for (Piece p : piecesShownOnBoard){
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
                        case "Rook": piecesShownOnBoard.add(new Rook(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        case "Knight": piecesShownOnBoard.add(new Knight(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        case "Bishop": piecesShownOnBoard.add(new Bishop(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        case "Queen": piecesShownOnBoard.add(new Queen(currentColor, currPiece.getCol(), currPiece.getRow())); break;
                        default: break;
                    }
                    piecesShownOnBoard.remove(currPiece);
                    copyPieces(piecesShownOnBoard, pieces);
                    currPiece = null;
                    promotion = false;
                    changeTurn();
                }
            }
        }
    }
}
