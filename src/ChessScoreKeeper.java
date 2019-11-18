//--------------------------------------------------------------------------------------
// ChessScoreKeeper.java
//
// This file contains the java source to keep the history of a chess game and
// to find legal moves.
//
// Author - Michael Keating
//--------------------------------------------------------------------------------------

import java.util.Vector;

class ChessScoreKeeper
{
    final char newGamePosition [] = {'r','n','b','q','k','b','n','r',
                                     'p','p','p','p','p','p','p','p',
                                     '-','-','-','-','-','-','-','-',
                                     '-','-','-','-','-','-','-','-',
                                     '-','-','-','-','-','-','-','-',
                                     '-','-','-','-','-','-','-','-',
                                     'P','P','P','P','P','P','P','P',
                                     'R','N','B','Q','K','B','N','R'};

    private char    lastPosition[];
    private char    currentPosition[];
    private int     currentMoveIndex;
    private int     totalMoves;
    private boolean isWhitesMove;
    private boolean isGameOver;
    private Vector  chessMoves;

    public ChessScoreKeeper()
    {
        currentPosition  = newGamePosition;
        lastPosition     = currentPosition;
        currentMoveIndex = 0;
        totalMoves       = 0;
        isWhitesMove     = true;
        isGameOver       = false;
        chessMoves       = new Vector();
    }

    public char[] getCurrentPosition()
    {
        return (char[])currentPosition.clone();
    }

    public int getCurrentMoveIndex()
    {
        return currentMoveIndex;
    }

    public int getTotalMoves()
    {
        return totalMoves;
    }

    public boolean isWhitesMove()
    {
        return isWhitesMove;
    }

    public void reverseSideOnMove()
    {
        isWhitesMove = isWhitesMove ? false : true;
    }

    public String getPGNMoveAt(int index)
    {
        if (index > totalMoves)
        {
            return "error";
        }

        ChessMove chessMove = (ChessMove)chessMoves.elementAt(index);

        return chessMove.getMove();
    }

    public ChessMove getCurrentChessMove()
    {
        try
        {
            return (ChessMove)chessMoves.elementAt(currentMoveIndex);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public ChessMove getChessMoveAt(int index)
    {
        return (ChessMove)chessMoves.elementAt(index);
    }

    public void gotoPosition(int index)
    {
        if (index <= totalMoves)
        {
            currentMoveIndex = index;

            if (index == totalMoves)
            {
                // the move vector is always one less that total moves
                // so there is no "currentChessMove" here.  So, use
                // lastPosition instead.

                currentPosition = lastPosition;
            }
            else
            {
                currentPosition = getCurrentChessMove().getPosition();
            }
        }
    }

    public void undoLastMove()
    {
        gotoPosition(totalMoves - 1);
        --totalMoves;
        chessMoves.removeElementAt(chessMoves.size() - 1);
        reverseSideOnMove();
    }

    public boolean makeMove(String pgnMove)
    {
        int     source, dest;
        char    piece;
        char    rankChar      = '\0';
        char    fileChar      = '\0';
        char    promotionChar = '\0';
        String  destString;

        // these booleans are used to help construct a short
        // algebraic move from a long one.

        boolean bIsLongPgn  = false;
        boolean bIsCaptured = false;
        boolean bIsCheck    = false;
        boolean bIsMate     = false;

        // this must be the latest move

        if (currentMoveIndex != totalMoves || isGameOver)
        {
            return false;
        }

        // use workMove so that pngMove never get's changed

        String workMove = pgnMove;

        // if there is a 'check' symbol(+), or the 'mate'
        // symbol (#), remove it from the workMove.

        if (workMove.endsWith("+"))
        {
            bIsCheck = true;
            workMove = workMove.substring(0, workMove.length() - 1);
        }
        else if (workMove.endsWith("#"))
        {
            bIsMate = true;
            workMove = workMove.substring(0, workMove.length() - 1);
        }

        // are we promoting a pawn? Get the promotion piece and
        // remove it from the workMove

        if (workMove.charAt(workMove.length() - 2) == '=')
        {
            promotionChar = workMove.charAt(workMove.length() - 1);
            workMove = workMove.substring(0, workMove.length() - 2);
        }

        // determine the piece type (pawn, rook, etc) to
        // move, and determine the destination square in
        // algebraic notaion (e4, f6, etc)

        // castling?

        if (workMove.equals("O-O"))
        {
            if (isWhitesMove)
            {
                piece      = 'K';
                destString = "g1";
            }
            else
            {
                piece      = 'k';
                destString = "g8";
            }
        }
        else if (workMove.equals("O-O-O"))
        {
            if (isWhitesMove)
            {
                piece      = 'K';
                destString = "c1";
            }
            else
            {
                piece      = 'k';
                destString = "c8";
            }
        }

        // is this a win, loss, or draw - game over dude

        else if (workMove.equals("1-0") || workMove.equals("0-1") || workMove.equals("1/2-1/2"))
        {
            ChessMove chessMove = new ChessMove(0, 0, pgnMove, (char[])currentPosition.clone(), promotionChar);
            chessMoves.addElement(chessMove);
            ++totalMoves;
            ++currentMoveIndex;
            isGameOver = true;
            return true;
        }

        else if (workMove.length() == 2)
        {
            // this is a pawn move such as e4, d6, etc.

            piece      = 'P';
            destString = workMove;
        }
        else if (workMove.length() == 3)
        {
            // moves like Nf3, Bg5

            piece      = workMove.charAt(0);
            destString = workMove.substring(1, 3);
        }
        else if (workMove.length() == 4)
        {
            // moves like Bxe5, exd4, Ngd2

            destString = workMove.substring(2, 4);

            if (workMove.charAt(1) != 'x')
            {
                // this is a move like Nge2 or R8f7

                piece = workMove.charAt(0);

                if (Character.isDigit(workMove.charAt(1)))
                {
                    rankChar = workMove.charAt(1);
                }
                else
                {
                    fileChar = workMove.charAt(1);
                }
            }
            else
            {
                if (Character.isLowerCase(workMove.charAt(0)))
                {
                    // a move like exd5 - a capturing pawn move

                    piece = 'P';
                    fileChar = workMove.charAt(0);
                }
                else
                {
                    // a move like Nxd5

                    piece = workMove.charAt(0);
                }
            }
        }
        else if (workMove.length() == 5)
        {
           // first, look for LONG algebraic moves - e2-e4, c5xd4

            if (Character.isLowerCase(workMove.charAt(0)) &&
               (workMove.charAt(2) == '-' || workMove.charAt(2) == 'x'))
            {
                piece      = 'P';
                destString = workMove.substring(3, 5);
                fileChar   = workMove.charAt(0);
                rankChar   = workMove.charAt(1);

                if (workMove.charAt(2) == 'x')
                {
                    bIsCaptured = true;
                }

                bIsLongPgn = true;
            }
            else
            {
                // next, SHORT algebraic moves like Raxd2

                piece      = workMove.charAt(0);
                destString = workMove.substring(3, 5);

                if (Character.isDigit(workMove.charAt(1)))
                {
                    rankChar = workMove.charAt(1);
                }
                else
                {
                    fileChar = workMove.charAt(1);
                }
            }
        }
        else if (workMove.length() == 6)
        {
            // moves like Ng1-f3, or Qb6xb2

            piece      = workMove.charAt(0);
            destString = workMove.substring(4, 6);

            fileChar = workMove.charAt(1);
            rankChar = workMove.charAt(2);

            if (workMove.charAt(3) == 'x')
            {
                bIsCaptured = true;
            }

            bIsLongPgn = true;
        }
        else
        {
            return false;
        }

        // first convert the destString to 'int dest'

        int file = (int)(destString.charAt(0) - 'a');
        int rank = (int)(destString.charAt(1) - '0');

        // the pgn rank is the reverse to our ChessBoard rank
        // so let's reverse it to our system here.

        rank = (rank - 8) * -1;

        // now calculate our dest square

        dest = (rank * 8) + file;

        // the key here is to find the source square - given the piece and the
        // destination square, a legal move check of the whole board will give
        // us the source square

        for (int i = 0; i < 64; ++i)
        {
            // if we're given a rankOrFile (as in Nge2, N3e2), make sure
            // we're on that rank or file

            if (rankChar != '\0')
            {
                if (fileChar != '\0')
                {
                    // both rank and file are given - caculate
                    // the source square

                    int sourceRank = Character.digit(rankChar, 10);

                    // convert the rank to our 'chessboard' rank

                    sourceRank = Math.abs(sourceRank - 8);

                    int sourceFile = (int)(fileChar - 'a');

                    int sourceSquare = sourceRank * 8 + sourceFile;

                    if (i != sourceSquare)
                    {
                        continue;
                    }
                }

                // no file was given - find the piece on the give rank

                else
                {
                    int sourceRank = Character.digit(rankChar, 10);
                    int thisRank   = i / 8;

                    // our ChessBoard rank needs to be revered

                    thisRank = (thisRank - 8) * -1;

                    // now just make sure we're on the given rank

                    if (thisRank != sourceRank)
                    {
                        continue;
                    }
                }
            }
            else if (fileChar != '\0')
            {
                 if ((fileChar == 'a') && (!is_a_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'b') && (!is_b_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'c') && (!is_c_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'd') && (!is_d_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'e') && (!is_e_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'f') && (!is_f_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'g') && (!is_g_File(i)))
                 {
                     continue;
                 }
                 else if ((fileChar == 'h') && (!is_h_File(i)))
                 {
                     continue;
                 }
            }

            // if it's black's move make piece lower case

            piece = isWhitesMove ? piece : Character.toLowerCase(piece);

            if (piece == currentPosition[i])
            {
                ChessMove m = new ChessMove(i,
                                            dest,
                                            pgnMove,
                                            (char[])currentPosition.clone(),
                                            promotionChar);

                if (isLegal(m, isWhitesMove))
                {
                    // set bIsCaptured if necessary

                    if (currentPosition[dest] != '-' || m.isEnPassent())
                    {
                        bIsCaptured = true;
                    }

                    // now make the actual move

                    moveThePiece(m.getSourceSquare(),
                                 m.getDestSquare(),
                                 currentPosition,
                                 m.getPromotionChar(),
                                 m.isEnPassent());

                    // get a short version of the move (if necessary) to
                    // make the score sheet more pleasant to the eye.

                    String strMove = "";

                    if (bIsLongPgn)
                    {
                        if (bIsCaptured)
                        {
                            strMove += pgnMove.substring(0, 1) + 'x';
                        }
                        else if (piece != 'P' && piece != 'p')
                        {
                           strMove += pgnMove.substring(0, 1);
                        }

                        strMove += destString;

                        if (Character.isLetter(promotionChar))
                        {
                            strMove += "=" + promotionChar;
                        }

                        // casting?

                        if (piece == 'K' || piece == 'k')
                        {
                            if ((i == 60 && dest == 62) || (i == 4 && dest == 6))
                            {
                                strMove = "O-O";
                            }
                            else if ((i == 60 && dest == 58) || (i == 4 && dest == 2))
                            {
                                strMove = "O-O-O";
                            }
                        }

                        if (isWhiteInCheck(currentPosition) || isBlackInCheck(currentPosition) && !bIsMate)
                        {
                            bIsCheck = true;
                        }

                        if (bIsCheck)
                        {
                            strMove += "+";
                        }

                        if (bIsMate)
                        {
                            strMove += "#";
                        }
                    }
                    else
                    {
                        strMove = pgnMove;
                    }

                    // now set our pretty short algebraic PGN style move

                    m.setMoveString(strMove);

                    lastPosition = (char[])currentPosition.clone();
                    chessMoves.addElement(m);
                    isWhitesMove = isWhitesMove ? false : true;
                    ++totalMoves;
                    ++currentMoveIndex;
                    return true;
                }
            }
        }

       // add "err" to the move for our PGN applet

      //  ChessMove m = new ChessMove(0,
      //                              0,
      //                              pgnMove + "-err",
      //                              (char[])currentPosition.clone(),
      //                              '\0');
      //  chessMoves.addElement(m);
      //  totalMoves++;

        return false;
    }

    /**
     *   IsLegal determines if the move from sourceSquare to destquare
     *   is a legal chess move given the current game's state.
     */

    public boolean isLegal(ChessMove m)
    {
        return isLegal(m, isWhitesMove);
    }

    boolean isLegal(ChessMove m, boolean bIsWhitesMove)
    {
        char position[] = m.getPosition();
        char piece      = position[m.getSourceSquare()];

        if((bIsWhitesMove && Character.isLowerCase(piece)) ||
          (!bIsWhitesMove && Character.isUpperCase(piece)))
        {
            // hey! wait your turn

            return false;
        }

        // make sure we're not capturing a friendly piece

        if ((bIsWhitesMove && Character.isUpperCase(position[m.getDestSquare()])) ||
           (!bIsWhitesMove && Character.isLowerCase(position[m.getDestSquare()])))
        {
            return false;
        }

        switch (piece)
        {
            case 'p':
            case 'P':

                if (!isPawnMove(m))
                {
                    return false;
                }

                break;

            case 'n':
            case 'N':

                if (!isKnightMove(m))
                {
                    return false;
                }

                break;

            case 'b':
            case 'B':

                if (!isBishopMove(m))
                {
                    return false;
                }

                break;

            case 'r':
            case 'R':

                if (!isRookMove(m))
                {
                    return false;
                }

                break;

            case 'q':
            case 'Q':

                if (!isQueenMove(m))
                {
                    return false;
                }

                break;

            case 'k':
            case 'K':

                if (!isKingMove(m))
                {
                    return false;
                }

                break;

            default:

                return false;
        }

        // now move the piece and see if we're left in check

        char tempPosition[] = (char[])m.getPosition().clone();
        moveThePiece(m.getSourceSquare(),
                     m.getDestSquare(),
                     tempPosition,
                     '\0',
                     m.isEnPassent());

        if (bIsWhitesMove && isWhiteInCheck(tempPosition))
        {
            return false;
        }

        if (!bIsWhitesMove && isBlackInCheck(tempPosition))
        {
            return false;
        }

        return true;
    }

    boolean isPawnMove(ChessMove m)
    {
        char position[] = m.getPosition();

        final char piece    = position[m.getSourceSquare()];
        final int  distance = m.getSourceSquare() - m.getDestSquare();

        // black pawns

        if (piece == 'p')
        {
            // are we moving two squares?

            if (distance == -16)
            {
                // not on the second rank

                if (m.getSourceSquare() > 15)
                {
                    return false;
                }

                // is there an enemy piece blocking us?

                else if (position[m.getDestSquare() - 8] != '-' ||
                         position[m.getDestSquare()]     != '-')
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            // are we moving one square?

            else if (distance == - 8)
            {
                // we can't capture a piece in front of us

                if (position[m.getDestSquare()] != '-')
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            // is this pawn capturing a piece?

            else if (distance == -7 || distance == -9)
            {
                // don't jump over the edge of the board

                if (is_a_File(m.getSourceSquare()) && is_h_File(m.getDestSquare()))
                {
                    return false;
                }

                if (is_h_File(m.getSourceSquare()) && is_a_File(m.getDestSquare()))
                {
                    return false;
                }

                // check for En Passent here
                // First, we must be on the sixth rank to be an e.p. move

                if (m.getDestSquare() / 8 == 5)
                {
                    // we must examine the last move to determine if this is
                    // en passent

                    ChessMove lastMove = (ChessMove)chessMoves.elementAt(currentMoveIndex - 1);

                    if ((lastMove.getDestSquare()   == (m.getDestSquare() - 8)) &&
                        (lastMove.getSourceSquare() == (m.getDestSquare() + 8)))
                    {
                        m.setEnPassent();
                        return true;
                    }
                }

                // make sure we're capturing an enemy piece

                if (!Character.isUpperCase(position[m.getDestSquare()]))
                {
                    return false;
                }

                return true;
            }

            // if we made it here we're illegal dude

            return false;
        }

        // white pawns

        else if (piece == 'P')
        {
            // are we moving two squares?

            if (distance == 16)
            {
                // not on the second rank

                if (m.getSourceSquare() < 48)
                {
                    return false;
                }

                // is there an enemy piece blocking us?

                else if (position[m.getDestSquare() + 8] != '-' ||
                         position[m.getDestSquare()]     != '-')
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            // are we moving one square?

            else if (distance == 8)
            {
                // we can't capture a piece in front of us

                if (position[m.getDestSquare()] != '-' )
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

            // is this pawn capturing a piece?

            else if (distance == 7 || distance == 9)
            {

                // don't jump over the edge of the board

                if (is_a_File(m.getSourceSquare()) && is_h_File(m.getDestSquare()))
                {
                    return false;
                }

                if (is_h_File(m.getSourceSquare()) && is_a_File(m.getDestSquare()))
                {
                    return false;
                }

                // check for En Passent here
                // First, we must be on the sixth rank to be an e.p. move

                if (m.getDestSquare() / 8 == 2)
                {
                    // we must examine the last move to determine if this is
                    // en passent

                    ChessMove lastMove = (ChessMove)chessMoves.elementAt(currentMoveIndex - 1);

                    if ((lastMove.getDestSquare()   == (m.getDestSquare() + 8)) &&
                        (lastMove.getSourceSquare() == (m.getDestSquare() - 8)))
                    {
                        m.setEnPassent();
                        return true;
                    }
                }

                // make sure we're capturing an enemy piece

                if (!Character.isLowerCase(position[m.getDestSquare()]))
                {
                    return false;
                }

                return true;
            }

            // if we made it here we're illegal dude

            return false;
        }
        else
        {
            // hey, we're supposed to be moving a pawn dude!

            return false;
        }
    }

    boolean isKnightMove(ChessMove m)
    {
        // Do not let the knight jump over the side of board

        if ((is_a_File(m.getSourceSquare()) || is_b_File(m.getSourceSquare())) &&
            (is_g_File(m.getDestSquare())   || is_h_File(m.getDestSquare())))
        {
            return false;
        }

        if ((is_a_File(m.getDestSquare())   || is_b_File(m.getDestSquare())) &&
            (is_g_File(m.getSourceSquare()) || is_h_File(m.getSourceSquare())))
        {
            return false;
        }

        // okay, now look for a valid knight move

        final int distance = m.getSourceSquare() - m.getDestSquare();

        if (distance ==  17 || distance == -17 ||
            distance ==  10 || distance == -10 ||
            distance ==  15 || distance == -15 ||
            distance ==   6 || distance ==  -6)
        {
            return true;
        }

        return false;
    }

    boolean isBishopMove(ChessMove m)
    {
        char position[] = m.getPosition();

        int distance = Math.abs(m.getSourceSquare() - m.getDestSquare());

        // must be moved diagonally

        if ((distance % 7 != 0) && (distance % 9 != 0))
        {
            return false;
        }

        // bishops cannot move to opposite color squares

        if (isLightSquare(m.getSourceSquare()) && isDarkSquare(m.getDestSquare()))
        {
            return false;
        }
        if (isDarkSquare(m.getDestSquare()) && isLightSquare(m.getDestSquare()))
        {
            return false;
        }

        // now make sure there are no pieces blocking us and
        // that we don't jump off the board

        // first, the right top to left bottom diagonal

        if (distance % 7 == 0 && distance != 63)
        {
            // are we moving from top to bottom?

            if (m.getDestSquare() > m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() + 7; i < m.getDestSquare(); i += 7)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }

                    // don't move beyond the bottom or left
                    // edge of the board

                    if (isBoardTop(i) || is_h_File(i))
                    {
                        return false;
                    }
                }
                if (isBoardTop(m.getDestSquare()) || is_h_File(m.getDestSquare()))
                {
                    return false;
                }
            }

            // are we moving from bottom to top?

            else if (m.getDestSquare() < m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() - 7; i > m.getDestSquare(); i -= 7)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }

                    // don't move beyond the top or right
                    // edge of the board

                    if (isBoardBottom(i) || is_a_File(i))
                    {
                        return false;
                    }
                }

                // don't land beyond the bottom or top
                // edge of the board

                if (isBoardBottom(m.getDestSquare()) || is_a_File(m.getDestSquare()))
                {
                    return false;
                }
            }
        }

        // left top to right bottom diagonal below

        else if (distance % 9 == 0)
        {
            // are we moving from top to bottom?

            if (m.getDestSquare() > m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() + 9; i < m.getDestSquare(); i += 9)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }

                    // don't move beyond the bottom or left
                    // edge of the board

                    if (isBoardTop(i) || is_a_File(i))
                    {
                        return false;
                    }
                }

                // don't land on the right or top edge of the board

                if (isBoardTop(m.getDestSquare()) || is_a_File(m.getDestSquare()))
                {
                    return false;
                }
            }

            // are we moving from bottom to top?

            else if (m.getDestSquare() < m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() - 9; i > m.getDestSquare(); i -= 9)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }

                    // don't move beyond the top or right
                    // edge of the board

                    if (isBoardBottom(i) || is_h_File(i))
                    {
                        return false;
                    }
                }

                // don't land beyond the bottom or top edge of the board

                if (isBoardBottom(m.getDestSquare()) || is_h_File(m.getDestSquare()))
                {
                    return false;
                }
            }
        }

        return true;
    }

    boolean isRookMove(ChessMove m)
    {
        char position[] = m.getPosition();

        boolean isXMove  = false;
        boolean isYMove  = false;

        // horizonal (x-move) or verticle (y-move)

        if ((m.getSourceSquare() / 8) == (m.getDestSquare() / 8))
        {
            isXMove = true;
        }
        else if ((m.getSourceSquare() - m.getDestSquare()) % 8 == 0)
        {
            isYMove = true;
        }
        else
        {
            return false;
        }

        // now make sure there are no pieces blocking us and
        // that we don't jump off the board

        // first, handle the verticle moves

        if (isYMove)
        {
            // are we moving from top to bottom?

            if (m.getDestSquare() > m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() + 8; i < m.getDestSquare(); i += 8)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }
                }
            }

            // are we moving from bottom to top?

            else if (m.getDestSquare() < m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() - 8; i > m.getDestSquare(); i -= 8)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }
                }
            }
        }

        // left top to right bottom diagonal below

        else if (isXMove)
        {
            // are we moving from left to right?

            if (m.getDestSquare() > m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() + 1; i < m.getDestSquare(); ++i)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }

                    // don't move beyond the right edge of the board

                    if (is_a_File(i))
                    {
                        return false;
                    }
                }

                // don't land on the left edge of the board

                if (is_a_File(m.getDestSquare()))
                {
                    return false;
                }
            }

            // are we moving from right to left?

            else if (m.getDestSquare() < m.getSourceSquare())
            {
                for (int i = m.getSourceSquare() - 1; i > m.getDestSquare(); i--)
                {
                    // is a piece in our way?

                    if (position[i] != '-')
                    {
                        return false;
                    }

                    // don't move beyone the left edge of the board

                    if (is_h_File(i))
                    {
                        return false;
                    }
                }

                // don't land on the right edge of the board

                if (is_h_File(m.getDestSquare()))
                {
                    return false;
                }
            }
        }

        return true;
    }

    boolean isQueenMove(ChessMove m)
    {
        return isRookMove(m) || isBishopMove(m);
    }

    boolean isKingMove(ChessMove m)
    {
        // check for castling moves

        if (m.getSourceSquare() == 60 && m.getDestSquare() == 62)
        {
            // a white king-side castle

            return true;
        }
        else if (m.getSourceSquare() == 60 && m.getDestSquare() == 58)
        {
            // a white queen-side castle

            return true;
        }
        else if (m.getSourceSquare() ==  4 && m.getDestSquare() == 6)
        {
            // a black king-side castle

            return true;
        }
        else if (m.getSourceSquare() ==  4 && m.getDestSquare() == 2)
        {
            // a black queen-side castle

            return true;
        }
        else
        {
            // don't jump over the edge of the board

            if ((is_a_File(m.getSourceSquare()) && is_h_File(m.getDestSquare())) ||
                (is_h_File(m.getSourceSquare()) && is_a_File(m.getDestSquare())))
            {
                return false;
            }

            // now look for valid king moves

            final int distance = Math.abs(m.getSourceSquare() - m.getDestSquare());
            if  (distance ==  1 || distance == 7 || distance ==  8 || distance == 9)
            {
                return true;
            }
        }

        return false;
    }

    private boolean isSquareAttacked(int attackedSquare, char position[], boolean byWhite)
    {
        ChessMove chessMove;

        for (int i = 0; i < 64; ++i)
        {
            // is a white piece attacking this square?

            if (byWhite)
            {
                if (Character.isUpperCase(position[i]))
                {
                    chessMove = new ChessMove(i, attackedSquare, "", position, '\0');
                    if (isLegal(chessMove, true))
                    {
                        return true;
                    }
                }
            }
            else
            {
                // is a black piece attacking this square?

                if (Character.isLowerCase(position[i]))
                {
                    chessMove = new ChessMove(i, attackedSquare, "", position, '\0');
                    if (isLegal(chessMove, false))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isWhiteInCheck(char position[])
    {
        // find white's king square and see if it is attacked

        for(int i = 0; i < 64; ++i)
        {
            if (position[i] == 'K')
            {
                return isSquareAttacked(i, position, false);
            }
        }

        return false;
    }

    public boolean isBlackInCheck(char position[])
    {
        // find black's king square and see if it is attacked

        for(int i = 0; i < 64; ++i)
        {
            if (position[i] == 'k')
            {
                return isSquareAttacked(i, position, true);
            }
        }

        return false;
    }

    public boolean isLightSquare(int square)
    {
        int file = (square + 8) % 8;
        int rank =  square / 8;

        return (file + rank) % 2 == 0;
    }

    public boolean isDarkSquare(int square)
    {
        int file = (square + 8) % 8;
        int rank =  square / 8;

        return (file + rank) % 2 != 0;
    }

    public boolean isBoardTop(int square)
    {
        return square < 8;
    }

    public boolean isBoardBottom(int square)
    {
        return square > 55;
    }

    boolean is_a_File(int square)
    {
        return (square + 8) % 8 == 0;
    }

    boolean is_b_File(int square)
    {
        return (square + 8) % 8 == 1;
    }

    boolean is_c_File(int square)
    {
        return (square + 8) % 8 == 2;
    }

    boolean is_d_File(int square)
    {
        return (square + 8) % 8 == 3;
    }

    boolean is_e_File(int square)
    {
        return (square + 8) % 8 == 4;
    }

    boolean is_f_File(int square)
    {
        return (square + 8) % 8 == 5;
    }

    boolean is_g_File(int square)
    {
        return (square + 8) % 8 == 6;
    }

    boolean is_h_File(int square)
    {
        return (square + 8) % 8 == 7;
    }

    /**
     *   moveThePiece simply makes the given move in the given position
     */

    private void moveThePiece(int source, int dest, char position[], char promotionChar, boolean bIsEP)
    {
        // castling move?

        if (position[source] == 'K' || position[source] == 'k')
        {
            if (source == 60 && dest == 62)
            {
                // a white king-side castle

                position[60] = '-';
                position[62] = 'K';
                position[63] = '-';
                position[61] = 'R';
                return;
            }
            else if (source == 60 && dest == 58)
            {
                // a white queen-side castle

                position[60] = '-';
                position[58] = 'K';
                position[56] = '-';
                position[59] = 'R';
                return;
            }
            else if (source ==  4 && dest == 6)
            {
                // a black king-side castle

                position[4] = '-';
                position[6] = 'k';
                position[7] = '-';
                position[5] = 'r';
                return;
            }
            else if (source ==  4 && dest == 2)
            {
                // a black queen-side castle

                position[4] = '-';
                position[2] = 'k';
                position[0] = '-';
                position[3] = 'r';
                return;
            }
        }

        // all non-castling moves here

        if (Character.isLetter(promotionChar))
        {
            // promoting here

            position[dest] = isBoardTop(dest) ?
                             Character.toUpperCase(promotionChar) :
                             Character.toLowerCase(promotionChar);
        }
        else
        {
            // regular moves here

            position[dest] = position[source];
        }

        // if this was just an e.p capture remove the
        // catured pawn (which is not on the dest square)

        if (bIsEP)
        {
            if (Character.isUpperCase(position[source]))
            {
                position[dest + 8] = '-';
            }
            else
            {
                position[dest - 8] = '-';
            }
        }

        // remove the moved piece from the source square

        position[source] = '-';
    }
}

/*                               /*
           The Board                        The Old Board

8    0  1  2  3  4  5  6  7      8    1  2  3  4  5  6  7  8
7    8  9 10 11 12 13 14 15      7    9 10 11 12 13 14 15 16
6   16 17 18 19 20 21 22 23      6   17 18 19 20 21 22 23 24
5   24 25 26 27 28 29 30 31      5   25 26 27 28 29 30 31 32
4   32 33 34 35 36 37 38 39      4   33 34 35 36 37 38 39 40
3   40 41 42 43 44 45 46 47      3   41 42 43 44 45 46 47 48
2   48 49 50 51 52 53 54 55      2   49 50 51 52 53 54 55 56
1   56 57 58 59 60 61 62 63      1   57 58 59 60 61 63 63 64

    A  B  C  D  E  F  G  H           A  B  C  D  E  F  G  H
*/