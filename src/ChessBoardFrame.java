//--------------------------------------------------------------------------------------
// ChessBoardFrame.java
//
// This file contains the source for the applet which provides the client side of the
// ChessRoomManager.
//
// Author - Michael Keating
//--------------------------------------------------------------------------------------

import java.awt.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.DateFormat;
import java.text.NumberFormat;

public class ChessBoardFrame extends Frame implements MouseListener, MouseMotionListener, WindowListener, Runnable, ActionListener, ItemListener
{
    final int squareWidth          = 45;
    final int squareHeight         = 45;
    final int leftMargin           = squareWidth  / 2 + 3;
    final int rightMargin          = squareWidth  / 2 + 3;
    final int topMargin            = squareHeight / 2 + 3;
    final int bottomMargin         = squareHeight / 2 + 3;
    final int boardWidth           = (squareWidth  * 8) + (leftMargin * 2);
    final int boardHeight          = (squareHeight * 8) + (topMargin  * 2);
    final int topSpaceHeight       = 22;
    final int leftSpaceWidth       = 4;
    final int rightSpaceLeft       = boardWidth + leftSpaceWidth;
    final int rightSpaceTop        = topSpaceHeight;
    final int rightSpaceWidth      = 220;
    final int rightSpaceHeight     = boardHeight;
    final int tagBoxHeight         = 25;
    final int moveLines            = 20;
    final int scoreSheetLeft       = rightSpaceLeft   + 5;
    final int scoreSheetTop        = rightSpaceTop + 5;
    final int scoreSheetWidth      = rightSpaceWidth  - 10;
    final int scoreSheetHeight     = rightSpaceHeight - 10;
    final int scoreSheetRight      = scoreSheetLeft   + scoreSheetWidth;
    final int scoreSheetMovesTop   = scoreSheetTop  + (tagBoxHeight * 4);
    final int scoreSheetMiddle     = scoreSheetLeft + (scoreSheetWidth / 2);
    final int boxForMoveNumsWidth  = 20;
    final int moveBoxHeight        = (scoreSheetHeight - (tagBoxHeight  * 4)) / moveLines;
    final int moveBoxWidth         = ((scoreSheetWidth / 2) - boxForMoveNumsWidth) / 2;
    final int whitesLeftColLeft    = scoreSheetLeft     + boxForMoveNumsWidth;
    final int whitesRightColLeft   = scoreSheetMiddle   + boxForMoveNumsWidth;
    final int blacksLeftColLeft    = whitesLeftColLeft  + moveBoxWidth;
    final int blacksRightColLeft   = whitesRightColLeft + moveBoxWidth;
    final int bottomSpaceLeft      = leftSpaceWidth;
    final int bottomSpaceTop       = topSpaceHeight + boardHeight;
    final int bottomSpaceWidth     = boardWidth + rightSpaceWidth;
    final int bottomSpaceHeight    = 150;

    // below are the client-server commuication strings
    // server-to_client communication strings

    final String cstrServerSendingMoveKey      = "SENDING_MOVE";
    final String cstrServerIllegalMoveKey      = "ILLEGAL_MOVE";
    final String cstrServerStalemateKey        = "STALEMATE";
    final String cstrServerBlackIsMatedKey     = "BLACK_IS_MATED";
    final String cstrServerWhiteIsMatedKey     = "WHITE_IS_MATED";
    final String cstrServerWhiteLostOnTimeKey  = "WHITE_LOST_ON_TIME";
    final String cstrServerBlackLostOnTimeKey  = "BLACK_LOST_ON_TIME";
    final String cstrServerWhiteResignedKey    = "WHITE_RESIGNED";
    final String cstrServerBlackResignedKey    = "BLACK_RESIGNED";
    final String cstrServerDrawByRepetitionKey = "DRAW_BY_REPETITION";
    final String cstrServerDrawOfferedKey      = "DRAW_OFFERED";
    final String cstrServerDrawAgreedKey       = "DRAW_AGREED";
    final String cstrServerUpdateRatingsKey    = "UPDATE_RATINGS";
    final String cstrServerNewGameKey          = "NEW_GAME";
    final String cstrServerViewerInfoKey       = "VIEWER_INFO";

    // client_to_server communication strings

    final String cstrClientIAmAChessGameKey = "/I_AM_A_CHESS_GAME";
    final String cstrClientIAmAViewerKey    = "/I_AM_A_VIEWER";
    final String cstrClientSendingMoveKey   = "/SENDING_MOVE";
    final String cstrClientMyTimeExpiredKey = "/MY_TIME_EXPIRED";
    final String cstrClientClaimFlagKey     = "/CLAIM_FLAG";
    final String cstrClientIResignKey       = "/I_RESIGN";
    final String cstrClientOfferDrawKey     = "/OFFER_DRAW";
    final String cstrClientDrawAgreedKey    = "/DRAW_AGREED";
    final String cstrClientPlayAgainKey     = "/PLAY_AGAIN";

    // the general string suffix

    final String cstrSuffix = "^@!";

    int       pieceOffsetX;
    int       pieceOffsetY;
    int       squarePressed;
    int       squareDraggedOver;
    int       animatedPieceX, animatedPieceY;
    int       animatedPieceOldX, animatedPieceOldY;
    int       draggedPieceX, draggedPieceY;
    int       draggedPieceOldX, draggedPieceOldY;
    int       draggedPieceToMouseX, draggedPieceToMouseY;
    int       animatedSourceSquare;
    int       animatedDestSquare;
    int       intStartingMinutes;
    int       intIncrementSeconds;
    int       intLastDestSquare;
    int       intClockTop;
    int       intButtonsTop;
    long      longMillisLeftForWhite;
    long      longMillisLeftForBlack;
    char      animatedSquare;
    char      animatedPiece;
    char      draggedPiece;
    char      animationPosition[];
    Color     squareLightColor, squareDarkColor;
    boolean   whiteFromBottom;
    boolean   isDraggingOn;
    boolean   isConnected;
    boolean   bConnectionFailed;
    boolean   bGameStarted;
    boolean   isAnimation;
    boolean   bInitError;
    boolean   bIsDownloading;
    boolean   bAnimateMoves;
    boolean   bSound;
    boolean   bWaitForFrameToPaint;
    boolean   bPaintBoardOnly;
    boolean   bShowPromotionDialog;
    boolean   bShowYesNoDialog;
    boolean   bIsWhitePlayer;
    boolean   bPaintClockOnly;
    boolean   bGameIsInProgress;
    boolean   bStopConnectionMonitor;
    boolean   bIsMate;
    boolean   bDrawChatAreaOnly;
    boolean   bSwitchSides;
    boolean   bViewerMode;
    boolean   bOppMoved;
    Image     wPawnImage, wKnightImage, wBishopImage, wRookImage, wQueenImage, wKingImage;
    Image     bPawnImage, bKnightImage, bBishopImage, bRookImage, bQueenImage, bKingImage;
    Image     offImage;
    Vector    pgnGameTagsVector;
    Vector    vChatStrings;
    Button    btnMenu;
    Button    btnResign;
    Button    btnDraw;
    Button    btnYes;
    Button    btnNo;
    Button    btnPlayAgain;
    String    strInitErrorMessage;
    String    strYesNoMessage;
    String    strHandle;
    String    strWhitesClock;
    String    strBlacksClock;
    String    strPlayerToView;
    Thread    animationThread;
    Thread    clockThread;
    Thread    ConnectThread;
    Socket    socket;
    MenuItem  miFlipBoard;
    MenuItem  miAnimateMoves;
    PopupMenu preferencesMenu;
    Rectangle chatDisplayRect;
    Font      chatFont;
    TextField chatInputField;
    URL       codeBaseURL;
    AudioClip tap;
    PgnGameTags      currentGameTags;
    ChessScoreKeeper scoreKeeper;
    //Applet    applet;

    Panel            pnlPromotion;
    CheckboxGroup    cbgPromotion;
    Checkbox         cbxQueen;
    Checkbox         cbxRook;
    Checkbox         cbxBishop;
    Checkbox         cbxKnight;
    Button           btnPromotePawn;

    Checkbox         cbxAnimateMoves;
    Checkbox         cbxSound;

    class PgnGameTags implements Cloneable
    {
        String event;
        String site;
        String date;
        String round;
        String white;
        String black;
        String whiteRating;
        String blackRating;
        String result;
        String ECOCode;

        // override Object.clone

        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                throw new InternalError();
            }
        }
    }

/*    public static void main(String[] args)
    {
       URL url = null;

        try
        {
            url = new URL("HTTP://www.mychess.com");
        }
        catch (Exception e) {}

        ChessBoardFrame cb;

        cb = new ChessBoardFrame("WhiteKnight", "You", "Me", "2400", "2200", 5, 2, url);

        cb.show();
    }*/

    ChessBoardFrame(String strMyHandle, String strWhiteHandle, String strBlackHandle, String strWhiteRating,
                    String strBlackRating, int intMinutes, int intSeconds, AudioClip tap2, URL codeBase)
    {
        if (strBlackHandle == null)
        {
            bViewerMode = true;
        }
        else
        {
            bViewerMode = false;
        }

        tap                 = tap2;
        codeBaseURL         = codeBase;
        strHandle           = strMyHandle;
        intStartingMinutes  = intMinutes;
        intIncrementSeconds = intSeconds;

        // set the fame attributes

        if (!bViewerMode)
        {
            setTitle(strWhiteHandle + " vs. " + strBlackHandle);
        }
        else
        {
            setTitle("Viewing Chess on MyChess.com");
            strPlayerToView = strWhiteHandle;
        }

        setBounds(75,50,638,603);
        setResizable(false);

        // set up our game tags

        currentGameTags = new PgnGameTags();
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

        if (!bViewerMode)
        {
            currentGameTags.event       = "Casual Game";
            currentGameTags.site        = "MyChess.com";
            currentGameTags.date        = df.format(new Date());
            currentGameTags.round       = "--";
            currentGameTags.white       = strWhiteHandle;
            currentGameTags.black       = strBlackHandle;
            currentGameTags.whiteRating = strWhiteRating;
            currentGameTags.blackRating = strBlackRating;
            currentGameTags.result      = "--";
            currentGameTags.ECOCode     = "--";

            // are we playing white or black?

            whiteFromBottom             = strMyHandle.equals(strWhiteHandle);
            bIsWhitePlayer              = whiteFromBottom;
        }
        else
        {
            currentGameTags.event       = "Casual Game";
            currentGameTags.site        = "MyChess.com";
            currentGameTags.date        = df.format(new Date());
            currentGameTags.round       = "--";
            currentGameTags.white       = "";
            currentGameTags.black       = "";
            currentGameTags.whiteRating = "";
            currentGameTags.blackRating = "";
            currentGameTags.result      = "--";
            currentGameTags.ECOCode     = "--";

            // are we playing white or black?

            whiteFromBottom             = true;
            bIsWhitePlayer              = true;
        }

        init();
    }

    public void init()
    {
        isConnected          = false;
        bConnectionFailed    = false;
        squareLightColor     = new Color(0xf0f0b8);
        squareDarkColor      = new Color(0x009000);
        isDraggingOn         = true;
        bInitError           = false;
        bAnimateMoves        = false;
        bSound               = false;
        bWaitForFrameToPaint = false;
        bPaintBoardOnly      = false;
        bPaintClockOnly      = false;
        bShowPromotionDialog = false;
        bShowYesNoDialog     = false;
        bGameStarted         = false;
        bOppMoved            = false;
        bSwitchSides         = bViewerMode;
        animatedSourceSquare = -1;
        animatedDestSquare   = -1;
        intLastDestSquare    = -1;
        intClockTop          = -1;
        intButtonsTop        = -1;
        strInitErrorMessage  = "";
        strYesNoMessage      = "";
        draggedPiece         = '-';

        // create the promotion dialog

        setLayout(null);

        // add listeners

        addMouseListener(this);
        addMouseMotionListener(this);
        addWindowListener(this);

        // get our images

        MediaTracker tracker = new MediaTracker(this);
        Toolkit      tk      = getToolkit();

        try
        {
            wPawnImage   = tk.getImage(new URL(codeBaseURL, "wp.gif"));
                           tracker.addImage(wPawnImage, 0);
            wKnightImage = tk.getImage(new URL(codeBaseURL, "wn.gif"));
                           tracker.addImage(wKnightImage, 0);
            wBishopImage = tk.getImage(new URL(codeBaseURL, "wb.gif"));
                           tracker.addImage(wBishopImage, 0);
            wRookImage   = tk.getImage(new URL(codeBaseURL, "wr.gif"));
                           tracker.addImage(wRookImage, 0);
            wQueenImage  = tk.getImage(new URL(codeBaseURL, "wq.gif"));
                           tracker.addImage(wQueenImage, 0);
            wKingImage   = tk.getImage(new URL(codeBaseURL, "wk.gif"));
                           tracker.addImage(wKingImage, 0);
            bPawnImage   = tk.getImage(new URL(codeBaseURL, "bp.gif"));
                           tracker.addImage(bPawnImage, 0);
            bKnightImage = tk.getImage(new URL(codeBaseURL, "bn.gif"));
                           tracker.addImage(bKnightImage, 0);
            bBishopImage = tk.getImage(new URL(codeBaseURL, "bb.gif"));
                           tracker.addImage(bBishopImage, 0);
            bRookImage   = tk.getImage(new URL(codeBaseURL, "br.gif"));
                           tracker.addImage(bRookImage, 0);
            bQueenImage  = tk.getImage(new URL(codeBaseURL, "bq.gif"));
                           tracker.addImage(bQueenImage, 0);
            bKingImage   = tk.getImage(new URL(codeBaseURL, "bk.gif"));
                           tracker.addImage(bKingImage, 0);

            // wait for those images

            tracker.waitForAll();
        }
        catch (Exception e)
        {
            strInitErrorMessage = "Error loading chess piece images";
            bInitError          = true;
        }

        if (tracker.isErrorAny())
        {
            strInitErrorMessage = "Error loading chess piece images";
            bInitError          = true;
        }

        pieceOffsetX      = (squareWidth / 2) - (wPawnImage.getWidth(this) / 2);
        pieceOffsetY      = squareHeight - wPawnImage.getHeight(this);
        squarePressed     = -1;
        squareDraggedOver = -1;
        scoreKeeper       = new ChessScoreKeeper();
        bIsMate           = false;
        bDrawChatAreaOnly = false;

        //applet            = new Applet();

        // set up our "Preferences" popup menu

        preferencesMenu = new PopupMenu();

        miFlipBoard = new MenuItem("Flip Board");
        miFlipBoard.addActionListener(this);
        miFlipBoard.setFont(new Font("Dialog", Font.PLAIN, 12));

        miAnimateMoves = new MenuItem("Turn Animation Off");
        miAnimateMoves.addActionListener(this);
        miAnimateMoves.setFont(new Font("Dialog", Font.PLAIN, 12));

        preferencesMenu.add(miAnimateMoves);
        preferencesMenu.add(miFlipBoard);

        add(preferencesMenu);

        // create the pawn promotion dialog

        pnlPromotion   = new Panel(new GridLayout(1, 4));
        cbgPromotion   = new CheckboxGroup();
        cbxQueen       = new Checkbox("Queen", cbgPromotion, true);
        cbxRook        = new Checkbox("Rook", cbgPromotion, false);
        cbxKnight      = new Checkbox("Knight", cbgPromotion, false);
        cbxBishop      = new Checkbox("Bishop", cbgPromotion, false);
        btnPromotePawn = new Button("Promote Pawn");
        pnlPromotion.add(cbxQueen);
        pnlPromotion.add(cbxRook);
        pnlPromotion.add(cbxKnight);
        pnlPromotion.add(cbxBishop);
        pnlPromotion.setSize(250, 25);
        add(pnlPromotion);
        btnPromotePawn.addActionListener(this);
        add(btnPromotePawn);
        pnlPromotion.setVisible(false);
        btnPromotePawn.setVisible(false);

        // set the promotion dialog's colors

        cbxQueen.setBackground(squareLightColor);
        cbxRook.setBackground(squareLightColor);
        cbxKnight.setBackground(squareLightColor);
        cbxBishop.setBackground(squareLightColor);
        pnlPromotion.setBackground(squareLightColor);
        btnPromotePawn.setBackground(squareLightColor);
        cbxQueen.setForeground(new Color(0x007000));
        cbxRook.setForeground(new Color(0x007000));
        cbxKnight.setForeground(new Color(0x007000));
        cbxBishop.setForeground(new Color(0x007000));
        pnlPromotion.setForeground(new Color(0x007000));
        btnPromotePawn.setForeground(new Color(0x007000));

        // set the "preferences" controls

        cbxAnimateMoves = new Checkbox("Animate Moves", false);
        cbxAnimateMoves.addItemListener(this);
        add(cbxAnimateMoves);
        cbxAnimateMoves.setFont(new Font("Dialog", Font.PLAIN, 11));
        cbxAnimateMoves.setVisible(true);
        cbxAnimateMoves.setBackground(new Color(0x007000));
        cbxAnimateMoves.setForeground(Color.cyan);

        cbxSound = new Checkbox("Sound", false);
        cbxSound.addItemListener(this);
        add(cbxSound);
        cbxSound.setFont(new Font("Dialog", Font.PLAIN, 11));
        cbxSound.setVisible(true);
        cbxSound.setBackground(new Color(0x007000));
        cbxSound.setForeground(Color.cyan);

        vChatStrings     = new Vector();

        btnMenu = new Button("Game Menu");
        btnMenu.addActionListener(this);
        btnMenu.setVisible(false);
        add(btnMenu);

        btnResign = new Button("Resign");
        btnResign.setSize(0, 0);
        btnResign.addActionListener(this);
        btnResign.setVisible(false);
        add(btnResign);

        btnDraw = new Button("Offer Draw");
        btnDraw.addActionListener(this);
        btnDraw.setVisible(false);
        add(btnDraw);

        btnYes = new Button("Yes");
        btnYes.setSize(0, 0);
        btnYes.setBackground(squareLightColor);
        btnYes.setForeground(new Color(0x007000));
        btnYes.addActionListener(this);
        btnYes.setVisible(false);
        add(btnYes);

        btnNo = new Button("No");
        btnNo.setBackground(squareLightColor);
        btnNo.setForeground(new Color(0x007000));
        btnNo.addActionListener(this);
        btnNo.setVisible(false);
        add(btnNo);

        btnPlayAgain = new Button("Play Again");
        btnPlayAgain.addActionListener(this);
        btnPlayAgain.setVisible(false);
        add(btnPlayAgain);

        // play the move sound so it will be cached for the game

        tap.play();
        tap.stop();

        ConnectThread = new Thread(this, "ConnectToChessServer");
        ConnectThread.start();
    }

    public void run()
    {
        if (Thread.currentThread().getName().equals("Animation"))
        {
            animateMove();
        }
        else if (Thread.currentThread().getName().equals("ConnectToChessServer"))
        {
            if (!connectToChessServer())
            {
                repaint();
                return;
            }

            // now look for messages from the server

            monitorConnectionInput();
        }
        else if (Thread.currentThread().getName().equals("TimeClock"))
        {
            timeClock();
        }
    }

    private void timeClock()
    {
        // starting a new time clock - get the starting time
        // in viewer mode the time is set when the viewer info key is received

        if (!bViewerMode)
        {
            longMillisLeftForWhite = intStartingMinutes * 60 * 1000;
            longMillisLeftForBlack = longMillisLeftForWhite;
        }

        // return if there is no starting time

        if (longMillisLeftForWhite == 0)
        {
            return;
        }

        long    longMillisToCountFrom  = longMillisLeftForWhite;
        long    longStartingTime       = System.currentTimeMillis();
        int     intLastDisplayedSecond = 0;
        boolean bRunningWhitesClock    = true;
        boolean bDisplayIt             = true;

        // reduce the thread priority

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        bGameIsInProgress = true;
        while (bGameIsInProgress)
        {
            // sleep for 1/5 second - save some CPU cycles

            try
            {
                Thread.sleep(200);
            }
            catch (Exception e)
            {
                return;
            }

            if (scoreKeeper.isWhitesMove())
            {
                // it's whites's move
                // did it just become white's move?

                if (!bRunningWhitesClock)
                {
                    bRunningWhitesClock = true;

                    // get a starting point for counting down the time.

                    longMillisToCountFrom = longMillisLeftForWhite;
                    longStartingTime = System.currentTimeMillis();
                }

                // count elasping time

                longMillisLeftForWhite = longMillisToCountFrom - (System.currentTimeMillis() - longStartingTime);

                if (bIsWhitePlayer && longMillisLeftForWhite <= 0 && !bViewerMode)
                {
                    // local player's clock expires at zero milliSeconds

                    bGameIsInProgress = false;
                    sendString(cstrClientMyTimeExpiredKey, strHandle);
                    bDisplayIt = true;
                }
                else if (!bIsWhitePlayer && longMillisLeftForWhite <= -15000 && !bViewerMode)
                {
                    // remote player's clock expires at minus 15 seconds
                    // this is grace time for lag considerations

                    bGameIsInProgress = false;
                    if (bOppMoved)
                    {
                        sendString(cstrClientClaimFlagKey, strHandle);
                    }
                    bDisplayIt = true;
                }
                else if (intLastDisplayedSecond != (int)(longMillisLeftForWhite / 1000))
                {
                    // only paint the clock when a second has gone by

                    intLastDisplayedSecond = (int)(longMillisLeftForWhite / 1000);
                    bDisplayIt = true;
                }
                else
                {
                    bDisplayIt = false;
                }
            }
            else
            {
                // it's black's move
                // did it just become black's move?

                if (bRunningWhitesClock)
                {
                    bRunningWhitesClock = false;

                    // get a starting point for counting down the time.

                    longMillisToCountFrom = longMillisLeftForBlack;
                    longStartingTime = System.currentTimeMillis();
                }

                // count elasping time

                longMillisLeftForBlack = longMillisToCountFrom - (System.currentTimeMillis() - longStartingTime);

                if (!bIsWhitePlayer && longMillisLeftForBlack <= 0 && !bViewerMode)
                {
                    // local player's clock expires at zero milliSeconds

                    bGameIsInProgress = false;
                    sendString(cstrClientMyTimeExpiredKey, strHandle);
                    longMillisLeftForBlack = 0;
                    bDisplayIt = true;
                }
                else if (bIsWhitePlayer && longMillisLeftForBlack <= -15000 && !bViewerMode)
                {
                    // remote player's clock expires at minus 15 seconds
                    // this is grace time for lag considerations

                    bGameIsInProgress = false;
                    sendString(cstrClientClaimFlagKey, strHandle);
                    bDisplayIt = true;
                }
                else if (intLastDisplayedSecond != (int)(longMillisLeftForBlack / 1000))
                {
                    // only paint the clock when a second has gone by

                    intLastDisplayedSecond = (int)(longMillisLeftForBlack / 1000);
                    bDisplayIt = true;
                }
                else
                {
                    bDisplayIt = false;
                }
            }

            // has a second gone by?

            if (bDisplayIt)
            {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumIntegerDigits(2);

                int intSecondsOnWhitesClock = longMillisLeftForWhite > 0 ? (int)(longMillisLeftForWhite / 1000) : 0;
                int intSecondsOnBlacksClock = longMillisLeftForBlack > 0 ? (int)(longMillisLeftForBlack / 1000) : 0;

                int intWhitesHours   = intSecondsOnWhitesClock / 3600;
                int intBlacksHours   = intSecondsOnBlacksClock / 3600;
                int intWhitesMinutes = intSecondsOnWhitesClock > 3599 ? (intSecondsOnWhitesClock % 3600) / 60 : intSecondsOnWhitesClock / 60;
                int intBlacksMinutes = intSecondsOnBlacksClock > 3599 ? (intSecondsOnBlacksClock % 3600) / 60 : intSecondsOnBlacksClock / 60;
                int intWhitesSeconds = intSecondsOnWhitesClock % 60;
                int intBlacksSeconds = intSecondsOnBlacksClock % 60;

                // build the clock display strings

                strWhitesClock = intWhitesHours + ":" + nf.format(intWhitesMinutes) + ":" + nf.format(intWhitesSeconds);
                strBlacksClock = intBlacksHours + ":" + nf.format(intBlacksMinutes) + ":" + nf.format(intBlacksSeconds);

                // now paint the clock

                bPaintClockOnly = true;
                repaint();
            }
        }
    }


    private void sendString(String strKey, String strString)
    {
        String strBuiltString;

        if (strKey != null && strKey != "")
        {
            strBuiltString = strKey + ',' + strString + '\0';
        }
        else
        {
            strBuiltString = strString + '\0';
        }

        try
        {
            socket.getOutputStream().write(strBuiltString.getBytes());
        }
        catch (Exception e)
        {
        }
    }

    private boolean connectToChessServer()
    {
        try
        {
            socket = new Socket(codeBaseURL.getHost(), 5555);
        }
        catch (Exception e)
        {
            strInitErrorMessage = e.toString();
            bConnectionFailed = true;
            bInitError = true;
            return false;
        }

        if (socket == null)
        {
            strInitErrorMessage = "Failed to connect with server";
            bConnectionFailed = true;
            bInitError = true;
            return false;
        }

        if (!bViewerMode)
        {
            sendString(cstrClientIAmAChessGameKey, strHandle);
        }
        else
        {
            sendString(cstrClientIAmAViewerKey, strHandle + "," + strPlayerToView);
        }

        isConnected = true;
        return true;
    }

    private void monitorConnectionInput()
    {
        String strChatMessage;
        BufferedInputStream serverInputStream;

        //repaint();

        // first get the imput stream we will be monitoring

        try
        {
            serverInputStream = new BufferedInputStream(socket.getInputStream());
        }
        catch (Exception e)
        {
            try
            {
                socket.close();
            }
            catch (Exception event)
            {
            }

            strInitErrorMessage = e.toString();
            bInitError = true;
            isConnected = false;
            return;
        }

        // here is where we monitor the input stream - for the
        // life of the connection

        bStopConnectionMonitor = false;
        byte byBuffer[] = new byte[8192];
        while (!bStopConnectionMonitor)
        {
            int intLength;

            try
            {
                intLength = serverInputStream.read(byBuffer, 0, byBuffer.length);

                if (intLength == -1)
                {
                    try
                    {
                        socket.close();
                    }
                    catch (Exception event)
                    {
                        addChatMessage(event.toString());
                    }

                    addChatMessage("Connection closed by server");
                    addChatMessage("");
                    return;
                }
            }
            catch (Exception e)
            {
                try
                {
                    socket.close();
                }
                catch (Exception event)
                {
                    addChatMessage(e.toString());
                }

                addChatMessage(e.toString());
                addChatMessage("Connection Closed");
                addChatMessage("");
                return;
            }

            // process the data we just read from the socket

            processServerMessage(new String(byBuffer, 0, intLength));
        }
    }

    private void processServerMessage(String strMessage)
    {
        // does the string must have a string suffix?

        if (strMessage.indexOf(cstrSuffix) == -1)
        {
            // no, display an error message and abort

            addChatMessage(strMessage);
            addChatMessage("");
            addChatMessage("Invalid message from server - improper suffix");
            return;
        }

        // is there more than one message?

        String strSubMessage  = "";

        if (strMessage.indexOf(cstrSuffix) < strMessage.length() - cstrSuffix.length())
        {
            // yes, separate the subMessage from the first message (our 'work' message)
            // we'll recursively pass strSubMessage to this function after processing strMessage

            strSubMessage = strMessage.substring(strMessage.indexOf(cstrSuffix) + cstrSuffix.length(), strMessage.length());
        }

        // check for keys at the beginning of the message

        if (strMessage.startsWith(cstrServerSendingMoveKey)  ||
            strMessage.startsWith(cstrServerWhiteIsMatedKey) ||
            strMessage.startsWith(cstrServerBlackIsMatedKey) ||
            strMessage.startsWith(cstrServerStalemateKey))
        {
            // remove the string suffix

            strMessage = strMessage.substring(0, strMessage.indexOf(cstrSuffix));

            // parse strMessage with StringTokenizer

            StringTokenizer st = new StringTokenizer(strMessage, ",");

            // the first token is our key

            String strKey = st.nextToken();

            // build the challenge string for the "challenged" list
            // and corresponding vector

            String strMove       = "";
            String strMillisLeft = "";

            for (int i = 0; st.hasMoreTokens(); ++i)
            {
                switch (i)
                {
                    case 0:

                        // our move

                        strMove = st.nextToken();
                        break;

                    case 1:

                        // the players's remaining time in milliseconds

                        strMillisLeft = st.nextToken();
                        break;

                    default:

                        break;
                }
            }

            if (strMove.length() < 4)
            {
                addChatMessage(strMessage);
                addChatMessage("Invalid message from server - Invalid move! Report this to development");
                return;
            }

            // first convert the source square to a square number

            int intFile = (int)(strMove.charAt(0) - 'a');
            int intRank = (int)(strMove.charAt(1) - '0');
                intRank = (intRank - 8) * -1;
            int intSource = (intRank * 8) + intFile;

            // now the dest square

            intFile     = (int)(strMove.charAt(2) - 'a');
            intRank     = (int)(strMove.charAt(3) - '0');
            intRank     = (intRank - 8) * -1;
            int intDest = (intRank * 8) + intFile;

            // get the promotion character - if any

            char cPromotionPiece = '\0';
            if (strMove.length() > 4)
            {
                cPromotionPiece = strMove.charAt(4);
            }

            ChessMove chessMove = new ChessMove(intSource,
                                                intDest,
                                                "",
                                                scoreKeeper.getCurrentPosition(),
                                                cPromotionPiece);

            strMove = chessMove.getLongAlg();

            // now make the move

            if (scoreKeeper.makeMove(strMove))
            {
                // adjust the oppents time

                if (scoreKeeper.isWhitesMove())
                {
                    longMillisLeftForBlack = Long.valueOf(strMillisLeft).intValue();
                }
                else
                {
                    longMillisLeftForWhite = Long.valueOf(strMillisLeft).intValue();
                }

                // set intLastDestSquare so it can be hightlighted

                intLastDestSquare = intDest;
                bOppMoved         = true;

                // display the move

                if (bAnimateMoves)
                {
                    scoreKeeper.gotoPosition(scoreKeeper.getCurrentMoveIndex() -1);
                    AnimateNextMove();
                    moveSound();
                }
                else
                {
                    bPaintBoardOnly = true;
                    repaint();
                    moveSound();
                }
            }

            // display a message if it is mate or stalemate

            if (strMessage.startsWith(cstrServerWhiteIsMatedKey))
            {
                bIsMate = true;
                gameOver("   *** White is Mated -- Black Wins ***", "0-1");
            }
            else if (strMessage.startsWith(cstrServerBlackIsMatedKey))
            {
                bIsMate = true;
                gameOver("   *** Black is Mated -- White Wins ***", "1-0");
            }
            else if (strMessage.startsWith(cstrServerStalemateKey))
            {
                gameOver("   *** Stalemate -- The Game is a Draw ***", "1/2");
            }
        }
        else if (strMessage.startsWith(cstrServerWhiteLostOnTimeKey))
        {
            gameOver("   *** White Lost on Time -- Black Wins ***", "0-1");
        }
        else if (strMessage.startsWith(cstrServerBlackLostOnTimeKey))
        {
            gameOver("   *** Black Lost on Time -- White Wins ***", "0-1");
        }
        else if (strMessage.startsWith(cstrServerWhiteResignedKey))
        {
            gameOver("   *** White Resigned -- Black Wins ***", "0-1");
        }
        else if (strMessage.startsWith(cstrServerBlackResignedKey))
        {
            gameOver("   *** Black Resigned -- White Wins ***", "1-0");
        }
        else if (strMessage.startsWith(cstrServerDrawByRepetitionKey))
        {
            gameOver("   *** Three-fold repetition -- draw ***", "1/2");
        }
        else if (strMessage.startsWith(cstrServerDrawAgreedKey))
        {
            gameOver("   *** White and Black have agreed to a draw ***", "1/2");
        }
        else if (strMessage.startsWith(cstrServerDrawOfferedKey))
        {
            strYesNoMessage = "Your opponent has offered a draw.  Do you accept?";
            bShowYesNoDialog = true;
            repaint();
        }
        else if (strMessage.startsWith(cstrServerUpdateRatingsKey))
        {
            // remove the string suffix

            strMessage = strMessage.substring(0, strMessage.indexOf(cstrSuffix));

            // parse strMessage with StringTokenizer

            StringTokenizer st = new StringTokenizer(strMessage, ",");

            // the first token is our key

            String strKey = st.nextToken();

            for (int i = 0; st.hasMoreTokens(); ++i)
            {
                switch (i)
                {
                    case 0:

                        // our move

                        currentGameTags.whiteRating = st.nextToken();
                        break;

                    case 1:

                        // the players's remaining time in milliseconds

                        currentGameTags.blackRating = st.nextToken();
                        break;

                    default:

                        break;
                }
            }
        }
        else if (strMessage.startsWith(cstrServerIllegalMoveKey))
        {
            addChatMessage("");
            addChatMessage("   *** The Server received an illegal move ***");
            addChatMessage("");
            scoreKeeper.undoLastMove();
            intLastDestSquare = -1;
            bPaintBoardOnly = true;
            repaint();
        }
        else if (strMessage.startsWith(cstrServerNewGameKey))
        {
            bGameStarted      = true;
            bGameIsInProgress = true;
            bOppMoved         = false;

            // start the timer thread...

            clockThread = new Thread(this, "TimeClock");
            clockThread.start();

            // switch sides if this isn't the first game

            if (bSwitchSides)
            {
                whiteFromBottom = whiteFromBottom ? false : true;
                bIsWhitePlayer  = bIsWhitePlayer  ? false : true;

                String strTemp              = currentGameTags.white;
                currentGameTags.white       = currentGameTags.black;
                currentGameTags.black       = strTemp;

                strTemp                     = currentGameTags.whiteRating;
                currentGameTags.whiteRating = currentGameTags.blackRating;
                currentGameTags.blackRating = strTemp;
            }

            // make sure we switch sides on all "play agains"

            bSwitchSides = true;

            // start a new game in the scoreKeeper

            scoreKeeper = new ChessScoreKeeper();
            intLastDestSquare = -1;
            repaint();

            if (!bViewerMode)
            {
                btnResign.setVisible(true);
                btnDraw.setVisible(true);
            }
        }
        else if (strMessage.startsWith(cstrServerViewerInfoKey))
        {
            // remove the string suffix

            strMessage = strMessage.substring(0, strMessage.indexOf(cstrSuffix));

            // parse strMessage with StringTokenizer

            StringTokenizer st = new StringTokenizer(strMessage, ",");

            // the first token is our key - pass it up

            String strKey = st.nextToken();

            // first the the game tags and time control

            for (int i = 0; i < 8 && st.hasMoreTokens(); ++i)
            {
                switch (i)
                {
                    case 0:

                        currentGameTags.white = st.nextToken();
                        break;

                    case 1:

                        currentGameTags.black = st.nextToken();
                        break;

                    case 2:

                        currentGameTags.whiteRating = st.nextToken();
                        break;

                    case 3:

                        currentGameTags.blackRating = st.nextToken();
                        break;

                    case 4:

                        longMillisLeftForWhite = Long.valueOf(st.nextToken()).intValue();
                        break;

                    case 5:

                        longMillisLeftForBlack = Long.valueOf(st.nextToken()).intValue();
                        break;

                    case 6:

                        intStartingMinutes = Integer.valueOf(st.nextToken()).intValue();
                        break;

                    case 7:

                        intIncrementSeconds = Integer.valueOf(st.nextToken()).intValue();
                        break;

                    default:

                        break;
                }
            }

            // now get the moves of the game we are viewing

            while (st.hasMoreTokens())
            {
                if (!scoreKeeper.makeMove(st.nextToken()))
                {
                    addChatMessage("Illegal Move");
                    break;
                }
            }

            repaint();

            clockThread = new Thread(this, "TimeClock");
            clockThread.start();

            bGameStarted = true;
        }
        else
        {
            // no key - just display the chat message

            strMessage.trim();
            addChatMessage(strMessage.substring(0, strMessage.indexOf(cstrSuffix)));
        }

        // process the sub-string if necessary

        if (!strSubMessage.equals(""))
        {
            processServerMessage(strSubMessage);
        }
    }

    private void gameOver(String strMessage, String strResult)
    {
        bGameIsInProgress = false;
        btnDraw.setVisible(false);
        btnResign.setVisible(false);

        if (!bViewerMode)
        {
            btnPlayAgain.setVisible(true);
        }

        addChatMessage("");
        addChatMessage(strMessage);
        addChatMessage("");
        currentGameTags.result = strResult;

        // waite for competing painting threads to complete

        while (isAnimation || bPaintBoardOnly)
        {
            try
            {
                Thread.sleep(200);
            } catch (Exception e) {}
        }

        repaint();
    }

    private synchronized void animateMove()
    {
        final int   factor       = 5;
        int         fraction     = 1;
        Vector      framesVector = new Vector(factor * 2);
        final Point startPoint   = squareNumberToPoint(animatedSourceSquare);
        final Point endPoint     = squareNumberToPoint(animatedDestSquare);
        Point       midPoint     = new Point();

        // This logic is taken from Winboard.  The piece moves
        // in slow and speeds up towards the middle, then slows
        // down again toward the end.

        startPoint.x += pieceOffsetX;
        startPoint.y += pieceOffsetY;
        endPoint.x   += pieceOffsetX;
        endPoint.y   += pieceOffsetY;

        if (animatedPiece != 'N' && animatedPiece != 'n')
        {
            // all pieces, except knights, move in a straight line

            midPoint.x = startPoint.x + ((endPoint.x - startPoint.x) / 2);
            midPoint.y = startPoint.y + ((endPoint.y - startPoint.y) / 2);
        }
        else
        {
            // knights move straight and then diagonally

            midPoint.x = startPoint.x + (endPoint.x - startPoint.x) / 2;
            midPoint.y = endPoint.y;
        }
        for (int i = 0; i < factor; i++)
        {
            fraction *= 2;
        }

        for (int i = 0; i < factor; i++)
        {
            int x, y;

            // slow to fast - 1/16, 1/8, etc...

            x = startPoint.x + ((midPoint.x - startPoint.x) / fraction);
            y = startPoint.y + ((midPoint.y - startPoint.y) / fraction);
            framesVector.addElement(new Point(x, y));
            fraction /= 2;
        }

        fraction = 2;
        for (int i = 0; i < factor; i++)
        {
            int x, y;

            // fast to slow - 1/16, 1/8, etc...

            x = endPoint.x - ((endPoint.x - midPoint.x) / fraction);
            y = endPoint.y - ((endPoint.y - midPoint.y) / fraction);
            framesVector.addElement(new Point(x, y));
            fraction *= 2;
        }

        // add the final destination point

        framesVector.addElement(endPoint);

        animatedPieceX = -1;
        animatedPieceY = -1;
        long tm = System.currentTimeMillis();
        for (int i = 0; i < framesVector.size(); ++i)
        {
            Point point       = (Point)framesVector.elementAt(i);
            animatedPieceOldX = animatedPieceX;
            animatedPieceOldY = animatedPieceY;
            animatedPieceX    = point.x;
            animatedPieceY    = point.y;

            try
            {
                tm += 15;
                Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
            }
            catch (InterruptedException e)
            {
            }

            // wait for up to 1/10th of a second for the last frame
            // to finish painting (necessary on slow machines and slow
            // VM's such as Netscape 4.06).  bWaitForFrameToPaint is
            // set to false by update() once the frame is drawn.

            for (int j = 0; j < 20; j++)
            {
                if (!bWaitForFrameToPaint)
                {
                    break;
                }

                try
                {
                    Thread.sleep(5);
                }
                catch (Exception e)
                {
                }
            }

            // setting a lower priority before repainting eliminates flicker
            // it does, however, slow down animation quite a bit - but it's
            // the best I can figure our for now

            Thread.currentThread().setPriority(Thread.currentThread().getPriority() + 1);
            bWaitForFrameToPaint = true;
            repaint();
            Thread.currentThread().setPriority(Thread.currentThread().getPriority() - 1);
        }

        isAnimation = false;

        Thread.currentThread().setPriority(Thread.currentThread().getPriority() + 1);
        bPaintBoardOnly = true;
        repaint();
        Thread.currentThread().setPriority(Thread.currentThread().getPriority() - 1);
    }

    private void paintAnimation(Graphics g)
    {
        int pieceWidth  = wPawnImage.getWidth(this);
        int pieceHeight = wPawnImage.getHeight(this);

        int square1 = xyToSquareNumber(animatedPieceOldX, animatedPieceOldY);
        int square2 = xyToSquareNumber(animatedPieceOldX + pieceWidth, animatedPieceOldY);
        int square3 = xyToSquareNumber(animatedPieceOldX, animatedPieceOldY + pieceHeight);
        int square4 = xyToSquareNumber(animatedPieceOldX + pieceWidth, animatedPieceOldY + pieceHeight);

        // repaint the necessary squares affected by our last animation frame

        repaintSquare(g, square1);
        if (square2 != square1)
        {
            repaintSquare(g, square2);
        }
        if (square3 != square2 && square3 != square1)
        {
            repaintSquare(g, square3);
        }
        if (square4 != square3 && square4 != square2 && square4 != square1)
        {
            repaintSquare(g, square4);
        }

        // now draw the animation piece

        drawPiece(g, animatedPiece, animatedPieceX, animatedPieceY);
    }

    private void paintDraggedPiece(Graphics g)
    {
        int pieceWidth  = wPawnImage.getWidth(this);
        int pieceHeight = wPawnImage.getHeight(this);

        int square1 = xyToSquareNumber(draggedPieceOldX, draggedPieceOldY);
        int square2 = xyToSquareNumber(draggedPieceOldX + pieceWidth, draggedPieceOldY);
        int square3 = xyToSquareNumber(draggedPieceOldX, draggedPieceOldY + pieceHeight);
        int square4 = xyToSquareNumber(draggedPieceOldX + pieceWidth, draggedPieceOldY + pieceHeight);

        // repaint the necessary squares affected by our last dragging

        repaintSquare(g, square1);
        if (square2 != square1)
        {
            repaintSquare(g, square2);
        }
        if (square3 != square2 && square3 != square1)
        {
            repaintSquare(g, square3);
        }
        if (square4 != square3 && square4 != square2 && square4 != square1)
        {
            repaintSquare(g, square4);
        }

        if (squareDraggedOver == -1)
        {
            return;
        }
        // draw the red highlight

        Point point = squareNumberToPoint(squareDraggedOver);

        g.setColor(Color.blue);
        g.fillRect(point.x, point.y, squareWidth, squareHeight);

        g.setColor(scoreKeeper.isLightSquare(squareDraggedOver) ? squareLightColor : squareDarkColor);
        g.fillRect(point.x + 3, point.y + 3, squareWidth - 6, squareHeight - 6);

        // is there a piece on squareDraggedOver? if so draw it
        // since our hightlight just erased it.

        if (scoreKeeper.getCurrentPosition()[squareDraggedOver] != '-' &&
            squareDraggedOver != squarePressed)
        {
            drawPiece(g, scoreKeeper.getCurrentPosition()[squareDraggedOver],
                      point.x + pieceOffsetX, point.y + pieceOffsetY);
        }

        // make sure the animated piece is visible if necessary

        if (isAnimation)
        {
            drawPiece(g, animatedPiece, animatedPieceX, animatedPieceY);
        }

        // now draw the dragged piece

        drawPiece(g, draggedPiece, draggedPieceX, draggedPieceY);

        // the piece leaves itself drawn on the margines - this looks bad.  So, we'll
        // update the margines if the piece is near it.

        int file, rank;
        if (whiteFromBottom)
        {
            file = (squareDraggedOver + 8) % 8;
            rank =  squareDraggedOver / 8;
        }
        else
        {
            file = (63 - squareDraggedOver + 8) % 8;
            rank = (63 - squareDraggedOver) / 8;
        }

        if (file == 0)
        {
            //  left margin

            g.setColor(squareDarkColor);
            g.fillRect(leftSpaceWidth, topSpaceHeight, leftMargin - 2, boardHeight);

            g.setColor(squareLightColor);
            g.fillRect(leftSpaceWidth + leftMargin - 2, topSpaceHeight + topMargin - 2, 2, squareHeight * 8 + 4);

            drawNotationMarkers(g);
        }
        else if (file == 7)
        {
            // right margin

            g.setColor(squareDarkColor);
            g.fillRect(leftSpaceWidth + boardWidth - rightMargin + 2, topSpaceHeight, rightMargin - 2, boardHeight);

            g.setColor(squareLightColor);
            g.fillRect(leftSpaceWidth + boardWidth - rightMargin, topSpaceHeight + topMargin - 2, 2, squareHeight * 8 + 4);
        }
        if (rank == 0)
        {
            //  top margin

            g.setColor(squareDarkColor);
            g.fillRect(leftSpaceWidth, topSpaceHeight, boardWidth, topMargin - 2);

            g.setColor(squareLightColor);
            g.fillRect(leftSpaceWidth + leftMargin - 2, topSpaceHeight + bottomMargin - 2, squareWidth * 8 + 4, 2);
        }
        if (rank == 7)
        {
            //  bottom margin

            g.setColor(squareDarkColor);
            g.fillRect(leftSpaceWidth, topSpaceHeight + boardHeight - bottomMargin + 2, boardWidth, bottomMargin - 2);

            g.setColor(squareLightColor);
            g.fillRect(leftSpaceWidth + leftMargin - 2, topSpaceHeight + boardHeight - bottomMargin, squareWidth * 8 + 4, 2);

            drawNotationMarkers(g);
        }
    }

    private void repaintSquare(Graphics g, int square)
    {
        if (square < 0 || square > 63)
        {
            return;
        }

        Point point = squareNumberToPoint(square);

        // draw the square

        g.setColor(scoreKeeper.isLightSquare(square) ? squareLightColor : squareDarkColor);
        g.fillRect(point.x, point.y, squareWidth, squareHeight);

        // now draw whatever piece that might be there

        if (isAnimation && animationPosition[square] != '-')
        {
            drawPiece(g, animationPosition[square], point.x + pieceOffsetX, point.y + pieceOffsetY);
        }
        else if (scoreKeeper.getCurrentPosition()[square] != '-' &&
                 square != squarePressed)
        {
            drawPiece(g, scoreKeeper.getCurrentPosition()[square], point.x + pieceOffsetX, point.y + pieceOffsetY);
        }
    }

    private void showPromotionDialog(Graphics g)
    {
        // first draw a rectandle as our dialogs background

        final int intCenterX = boardWidth  / 2 + leftSpaceWidth;
        final int intCenterY = boardHeight / 2 + topSpaceHeight;
        final int intDialogWidth  = 275;
        final int intDialogHeight = 75;

        g.setColor(squareLightColor);
        g.fillRect(intCenterX - (intDialogWidth / 2),
                   intCenterY - (intDialogHeight / 2),
                   intDialogWidth, intDialogHeight);
        g.setColor(new Color(0x007000));
        g.drawRect(intCenterX - (intDialogWidth / 2),
                   intCenterY - (intDialogHeight / 2),
                   intDialogWidth, intDialogHeight);

        // show the check box group

        pnlPromotion.setLocation(intCenterX - (pnlPromotion.getSize().width / 2),
                                 intCenterY - (intDialogHeight / 2) + 10);
        pnlPromotion.setVisible(true);

        // show the Promote Pawn button

        btnPromotePawn.setSize(btnPromotePawn.getPreferredSize());
        btnPromotePawn.setLocation(intCenterX - (btnPromotePawn.getSize().width / 2),
                                   pnlPromotion.getLocation().y + pnlPromotion.getSize().height + 5);
        btnPromotePawn.setVisible(true);
    }

    private void showYesNoDialog(Graphics g)
    {
        // set btnYes/No size if it hasn't be done yet

        if (btnYes.getSize().width == 0)
        {
            btnYes.setFont(new Font("Dialog", Font.BOLD, 12));
            btnYes.setSize(btnYes.getPreferredSize());
            btnNo.setFont(new Font("Dialog", Font.BOLD, 12));
            btnNo.setSize(btnYes.getPreferredSize());
        }

        // draw a rectandle as our dialogs background

        final int intCenterX = boardWidth  / 2 + leftSpaceWidth;
        final int intCenterY = boardHeight / 2 + topSpaceHeight;
        final int intDialogMargin = 15;

        // get the Dialog width (figure it by the string width)

        g.setFont(new Font("Dialog", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        final int intDialogWidth  = fm.stringWidth(strYesNoMessage) + (intDialogMargin * 2);
        final int intDialogHeight = (fm.getHeight() * 2) + btnYes.getSize().height + (intDialogMargin * 2);
        final int intDialogLeft   = intCenterX - (intDialogWidth / 2);
        final int intDialogTop    = intCenterY - (intDialogHeight / 2);

        g.setColor(squareLightColor);
        g.fillRect(intDialogLeft, intDialogTop, intDialogWidth, intDialogHeight);
        g.setColor(new Color(0x007000));
        g.drawRect(intDialogLeft, intDialogTop, intDialogWidth, intDialogHeight);

        // draw the yes/no question string

        g.drawString(strYesNoMessage, intCenterX - (fm.stringWidth(strYesNoMessage) / 2), intDialogTop + intDialogMargin + fm.getHeight());

        // draw the buttons

        btnYes.setLocation(intCenterX - btnYes.getSize().width - 10, intDialogTop + intDialogMargin + (fm.getHeight() * 2));
        btnNo.setLocation(intCenterX + 10, intDialogTop + intDialogMargin + (fm.getHeight() * 2));
        btnYes.setVisible(true);
        btnNo.setVisible(true);
    }


    private void addChatMessage(String strMessage)
    {
        // limit the string length to 256 characters

        if (strMessage.length() > 256)
        {
            strMessage = strMessage.substring(0, 256);
        }

        // first break up the message if it's too long.
        // if we get here to soon, this method breaks - so
        // we have this kludge where vChatStrings must have
        // at least two lines.


        if (vChatStrings.size() > 3)
        {
            FontMetrics fm = getToolkit().getFontMetrics(chatFont);
            if (fm.stringWidth(strMessage) > chatDisplayRect.width - 8)
            {
                int intDisplayWidth = 0;

                for (int i = 0; i < strMessage.length(); ++i)
                {
                    intDisplayWidth += fm.charWidth(strMessage.charAt(i));

                    if (intDisplayWidth > chatDisplayRect.width - 8)
                    {
                        // cut the string off here and add the chatStrings vector

                        vChatStrings.addElement(strMessage.substring(0, i));

                        // get the next string

                        strMessage = strMessage.substring(i, strMessage.length());

                        // repeat the process if the second string is too long
                        // otherwise break out

                        if (fm.stringWidth(strMessage) > chatDisplayRect.width - 8)
                        {
                            i = 0;
                            intDisplayWidth = 0;
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
        }

        // add the latest message

        vChatStrings.addElement(strMessage);

        // for now, only store enough chat message thats can display
        // if we get here to soon, this method breaks - so
        // we have this kludge where vChatStrings must have
        // at least two lines.

        if (vChatStrings.size() > 3)
        {
            FontMetrics fm = getToolkit().getFontMetrics(chatFont);
            int intDisplayHeight = 6;
            for (int i = vChatStrings.size() - 1; i > -1; --i)
            {
                intDisplayHeight += fm.getHeight();

                if (intDisplayHeight > chatDisplayRect.height)
                {
                    // cut it down to size

                    vChatStrings.removeElementAt(i);
                }
            }

            bDrawChatAreaOnly = true;
        }

        repaint();
    }

    public void update(Graphics g)
    {
        boolean bPaintAll = true;

        if (offImage == null)
        {
            offImage = this.createImage(getSize().width, getSize().height);
        }

        Graphics offGraphics  = offImage.getGraphics();

        if (bShowPromotionDialog)
        {
            showPromotionDialog(offGraphics);
            g.drawImage(offImage, 0, 0, this);
            return;
        }

        if (bShowYesNoDialog)
        {
            showYesNoDialog(offGraphics);
            g.drawImage(offImage, 0, 0, this);
            return;
        }

        if (bPaintClockOnly)
        {
            drawClock(offGraphics);
            bPaintClockOnly = false;
            bPaintAll = false;
        }

        if (isAnimation)
        {
            paintAnimation(offGraphics);
            bWaitForFrameToPaint = false;
            bPaintAll = false;
        }

        if (squarePressed != -1)
        {
            // we're dragging a piece here

            paintDraggedPiece(offGraphics);
            bPaintAll = false;
        }

        if (bDrawChatAreaOnly)
        {
            drawChatArea(offGraphics);
            bDrawChatAreaOnly = false;
            bPaintAll = false;
        }

       // if (!isAnimation && (squarePressed == -1))

        if (bPaintBoardOnly)
        {
            drawBoard(offGraphics);
            updateHighlightedMove(offGraphics);
            bPaintBoardOnly = false;
            bPaintAll = false;
        }

        // don't paint everything if an "only" flag was set

        if (!bPaintAll)
        {
            g.drawImage(offImage, 0, 0, this);
            return;
        }

        drawBoard(offGraphics);

        // draw the rightSpace (scoresheet stuff)

        drawRightSpace(offGraphics);

        // draw the area below the chess board

        drawBottomSpace(offGraphics);

        // slap down the offscreen image

        g.drawImage(offImage, 0, 0, this);

    /*    // connect if necessary

        if (!isConnected && !bConnectionFailed)
        {
           // addChatMessage("About to run isconnected");

            Thread connectThread = new Thread(this, "ConnectToChessServer");
            connectThread.start();
        }*/
    }

    public void paint(Graphics g)
    {
        update(g);
    }

    void drawBoard(Graphics g)
    {
        // seet the background to the dark square color

        g.setColor(squareDarkColor);
        g.fillRect(leftSpaceWidth, topSpaceHeight, boardWidth, boardHeight);

        // draw the light squares here (by an inner background rect)

        g.setColor(squareLightColor);
        g.fillRect(leftSpaceWidth + leftMargin - 2,
                   topSpaceHeight + topMargin    - 2,
                  (squareWidth  * 8) + 4,
                  (squareHeight * 8) + 4);

       // now draw the dard squares and the pieces

        int x, y;
        int file = 1;
        int rank = 1;

        g.setColor(squareDarkColor);
        for (int i = 0; i < 64; i++)
        {
            // Calculate x, y for the squares

            x = leftMargin + (squareWidth  * (file - 1)) + leftSpaceWidth;
            y = topMargin  + (squareHeight * (rank - 1)) + topSpaceHeight;

            // draw a dark square (white squares are already there - see above)

            if ((file + rank) % 2 != 0)
            {
                g.fillRect(x, y, squareWidth, squareHeight);
            }

            // draw the piece at this square (if any)

            char position[] = !isAnimation ? scoreKeeper.getCurrentPosition() :
                                             animationPosition;

            char pieceToDraw = whiteFromBottom ? position[i] : position[63 - i];
            drawPiece(g, pieceToDraw, x + pieceOffsetX, y + pieceOffsetY);

            // next rank ?

            if (file == 8 )
            {
                file = 1;
                ++rank;
            }
            else
            {
                ++file;
            }
        }

        // draw a hightlight on the last destination square to alert the opponent

        if (intLastDestSquare != -1)
        {
            Point point = squareNumberToPoint(intLastDestSquare);

            g.setColor(Color.cyan);
            g.fillRect(point.x, point.y, squareWidth, squareHeight);

            g.setColor(scoreKeeper.isLightSquare(intLastDestSquare) ? squareLightColor : squareDarkColor);
            g.fillRect(point.x + 3, point.y + 3, squareWidth - 6, squareHeight - 6);

            // is there a piece on squareDraggedOver? if so draw it
            // since our hightlight just erased it.

            drawPiece(g, scoreKeeper.getCurrentPosition()[intLastDestSquare],
                      point.x + pieceOffsetX, point.y + pieceOffsetY);
        }

        // draw algrebraec notation character markers

        drawNotationMarkers(g);
    }

    void drawNotationMarkers(Graphics g)
    {
        // set font info

        g.setFont(new Font("Dialog", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();

        g.setColor(squareLightColor);

        // draw algrebraec notation characters here

        char fileChar, rankChar;
        for (int i = 0; i < 8; i++)
        {
            if (whiteFromBottom)
            {
                fileChar = (char)('a' + i);
                rankChar = (char)('8' - i);
            }
            else
            {
                fileChar = (char)('h' - i);
                rankChar = (char)('1' + i);
            }

            // draw the file characters (a - h) in the bottom margin

            g.drawString("" + fileChar,
                         leftMargin + leftSpaceWidth +
                                      (squareWidth * i) +
                                      (squareWidth /2 ) -
                                      (fm.charWidth(fileChar) /2),
                         topMargin  + topSpaceHeight +
                                      (squareHeight * 8) + 4 +
                                      fm.getAscent());

            // draw the rank characters (1 - 8) in the left margin

            g.drawString("" + rankChar,
                        (leftSpaceWidth + leftMargin - 7) - fm.charWidth(rankChar),
                         topMargin + topSpaceHeight +
                                     (squareHeight * i) +
                                     (squareHeight / 2) +
                                     (fm.getAscent() / 2));
        }
    }

    void drawRightSpace(Graphics g)
    {

        // draw the rightSpace's background

        g.setColor(new Color(0x007000));
        g.fillRect(rightSpaceLeft,
                   rightSpaceTop,
                   rightSpaceWidth,
                   rightSpaceHeight);

        // draw a boarder around that background

        g.setColor(new Color(0x000000));
        g.drawRect(rightSpaceLeft,
                   rightSpaceTop,
                   rightSpaceWidth - 1,
                   rightSpaceHeight);

        // draw pager-like background over the above background

        g.setColor(new Color(0xfffff0));
        g.fillRect(scoreSheetLeft,
                   scoreSheetTop,
                   scoreSheetWidth,
                   scoreSheetHeight);

        // draw a boarder around that background

        g.setColor(new Color(0x000000));
        g.drawRect(scoreSheetLeft,
                   scoreSheetTop,
                   scoreSheetWidth - 1,
                   scoreSheetHeight);


        // draw the scoreSheet form
        // event and year are on the same line

        final int dateTagBoxWidth  = 70;
        final int eventTagBoxWidth = scoreSheetWidth - dateTagBoxWidth;
        final int eventTagBoxLeft  = scoreSheetLeft;
        final int dateTagBoxLeft   = eventTagBoxLeft + eventTagBoxWidth;

        // site, round, result, and ECO code are on this line

        final int ecoTagBoxWidth    = 30;
        final int resultTagBoxWidth = 27;
        final int roundTagBoxWidth  = 20;
        final int siteTagBoxWidth   = scoreSheetWidth - ecoTagBoxWidth - roundTagBoxWidth - resultTagBoxWidth;
        final int siteTagBoxLeft    = scoreSheetLeft;
        final int roundTagBoxLeft   = scoreSheetLeft + siteTagBoxWidth;
        final int resultTagBoxLeft  = scoreSheetLeft + siteTagBoxWidth + roundTagBoxWidth;
        final int ecoTagBoxLeft     = scoreSheetLeft + siteTagBoxWidth + roundTagBoxWidth + resultTagBoxWidth;

        // white player and rating are on this line

        final int whiteRatingTagBoxWidth = 40;
        final int whiteTagBoxWidth       = scoreSheetWidth - whiteRatingTagBoxWidth;
        final int whiteTagBoxLeft        = scoreSheetLeft;
        final int whiteRatingTagBoxLeft  = whiteTagBoxLeft + whiteTagBoxWidth;

        // black player and rating are on this line

        final int blackRatingTagBoxWidth = 40;
        final int blackTagBoxWidth       = scoreSheetWidth - blackRatingTagBoxWidth;
        final int blackTagBoxLeft        = scoreSheetLeft;
        final int blackRatingTagBoxLeft  = blackTagBoxLeft + blackTagBoxWidth;

        // draw the tag labels

        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.black);

        int y = scoreSheetTop;
        g.drawString("event", eventTagBoxLeft + 1, y + fm.getAscent());
        g.drawString("date",  dateTagBoxLeft  + 1, y + fm.getAscent());

        y += tagBoxHeight;
        g.drawString("site",  siteTagBoxLeft   + 1, y + fm.getAscent());
        g.drawString("rnd",   roundTagBoxLeft  + 1, y + fm.getAscent());
        g.drawString("score", resultTagBoxLeft + 1, y + fm.getAscent());
        g.drawString("eco",   ecoTagBoxLeft    + 1, y + fm.getAscent());

        y += tagBoxHeight;
        g.drawString("white",  whiteTagBoxLeft + 1,        y + fm.getAscent());
        g.drawString("rating", whiteRatingTagBoxLeft  + 1, y + fm.getAscent());

        y += tagBoxHeight;
        g.drawString("black",  blackTagBoxLeft + 1,        y + fm.getAscent());
        g.drawString("rating", blackRatingTagBoxLeft  + 1, y + fm.getAscent());

        // now draw the tag boxes

        g.setColor(Color.black);

        y = scoreSheetTop;
        g.drawRect(eventTagBoxLeft, y, eventTagBoxWidth,    tagBoxHeight);
        g.drawRect(dateTagBoxLeft,  y, dateTagBoxWidth - 1, tagBoxHeight);

        y += tagBoxHeight;
        g.drawRect(siteTagBoxLeft,   y, siteTagBoxWidth,    tagBoxHeight);
        g.drawRect(roundTagBoxLeft,  y, roundTagBoxWidth,   tagBoxHeight);
        g.drawRect(resultTagBoxLeft, y, resultTagBoxWidth,  tagBoxHeight);
        g.drawRect(ecoTagBoxLeft,    y, ecoTagBoxWidth - 1, tagBoxHeight);

        y += tagBoxHeight;
        g.drawRect(whiteTagBoxLeft,        y, whiteTagBoxWidth,           tagBoxHeight);
        g.drawRect(whiteRatingTagBoxLeft,  y, whiteRatingTagBoxWidth - 1, tagBoxHeight);

        y += tagBoxHeight;
        g.drawRect(blackTagBoxLeft,        y, blackTagBoxWidth,           tagBoxHeight);
        g.drawRect(blackRatingTagBoxLeft,  y, blackRatingTagBoxWidth - 1, tagBoxHeight);

        // draw the tags values

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fm = g.getFontMetrics();
        g.setColor(Color.darkGray);

        y = scoreSheetTop + tagBoxHeight;
        g.drawString(currentGameTags.event,
                     eventTagBoxLeft + ((eventTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.event) / 2)),
                     y - 4);
        g.drawString(currentGameTags.date,
                     dateTagBoxLeft + ((dateTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.date) / 2)),
                     y - 4);

        y += tagBoxHeight;
        g.drawString(currentGameTags.site,
                     siteTagBoxLeft + ((siteTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.site) / 2)),
                     y - 4);
        g.drawString(currentGameTags.round,
                     roundTagBoxLeft + ((roundTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.round) / 2)),
                     y - 4);
        g.drawString(currentGameTags.result.equals("1/2-1/2") ? "1/2" : currentGameTags.result,
                     resultTagBoxLeft + ((resultTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.result.equals("1/2-1/2") ? "1/2" : currentGameTags.result) / 2)),
                     y - 4);
        g.drawString(currentGameTags.ECOCode,
                     ecoTagBoxLeft + ((ecoTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.ECOCode) / 2)),
                     y - 4);

        y += tagBoxHeight;
        g.drawString(currentGameTags.white,
                     whiteTagBoxLeft + ((whiteTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.white) / 2)),
                     y - 4);
        g.drawString(currentGameTags.whiteRating,
                     whiteRatingTagBoxLeft + ((whiteRatingTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.whiteRating) / 2)),
                     y - 4);

        y += tagBoxHeight;
        g.drawString(currentGameTags.black,
                     blackTagBoxLeft + ((blackTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.black) / 2)),
                     y - 4);
        g.drawString(currentGameTags.blackRating,
                     blackRatingTagBoxLeft + ((blackRatingTagBoxWidth / 2) - (fm.stringWidth(currentGameTags.blackRating) / 2)),
                     y - 4);

        // now to the area where we'll be actually show chess moves
        // draw a horizontal line for form one "bold" line between the
        // tag headers secions and the moves section

        g.drawLine(scoreSheetLeft, scoreSheetMovesTop - 1, scoreSheetLeft + scoreSheetWidth - 1, scoreSheetMovesTop - 1);

        // draw the move boxes - vertical lines first

        y = scoreSheetMovesTop;
        g.drawLine(whitesLeftColLeft,  y, whitesLeftColLeft,  scoreSheetTop + scoreSheetHeight);
        g.drawLine(blacksLeftColLeft,  y, blacksLeftColLeft,  scoreSheetTop + scoreSheetHeight);
        g.drawLine(scoreSheetMiddle,   y, scoreSheetMiddle,   scoreSheetTop + scoreSheetHeight);
        g.drawLine(whitesRightColLeft, y, whitesRightColLeft, scoreSheetTop + scoreSheetHeight);
        g.drawLine(blacksRightColLeft, y, blacksRightColLeft, scoreSheetTop + scoreSheetHeight);

        // draw the horizontal lines and move numbers

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        fm = g.getFontMetrics();
        g.setColor(Color.black);

        // what scoresheet page is this?

        int movesPerPage = moveLines * 4;
        int page = (scoreKeeper.getCurrentMoveIndex() - 1) / movesPerPage;

        for (int i = 1; i <= moveLines; i++)
        {
            int moveNumber;
            moveNumber = (page * (movesPerPage / 2)) + i;

            y += moveBoxHeight;
            g.drawLine(scoreSheetLeft, y, scoreSheetLeft + scoreSheetWidth - 1, y);

            // the left sid move numbers

            g.drawString(String.valueOf(moveNumber),
                         scoreSheetLeft + (boxForMoveNumsWidth / 2) - (fm.stringWidth(String.valueOf(moveNumber)) / 2),
                         y - 3);

            // the right side move numbers

            g.drawString(String.valueOf(moveNumber + moveLines),
                         scoreSheetMiddle + (boxForMoveNumsWidth / 2) - (fm.stringWidth(String.valueOf(moveNumber + moveLines)) / 2),
                         y - 3);
        }

        // now draw the moves of the game

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fm = g.getFontMetrics();
        g.setColor(Color.darkGray);

        y = scoreSheetMovesTop;
        for (int i = 0; i < movesPerPage; i++)
        {
            int moveIndex;
            moveIndex = (page * movesPerPage) + i;

            if (moveIndex >= scoreKeeper.getTotalMoves())
            {
                break;
            }

            String pgnMove;
            pgnMove = scoreKeeper.getPGNMoveAt(moveIndex);

            // replace the check character '+' with the 'mate' character '#' if necessary

            if (bIsMate && moveIndex == scoreKeeper.getTotalMoves() - 1)
            {
                pgnMove = pgnMove.replace('+', '#');
            }

            // if this is the current move - make it bold

            if (moveIndex == scoreKeeper.getCurrentMoveIndex() - 1)
            {
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                fm = g.getFontMetrics();
            }
            else if (pgnMove.endsWith("err"))
            {
                g.setColor(Color.red);
            }
            else if (fm.getFont().isBold())
            {
                // else turn of the Bold if it was on

                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                fm = g.getFontMetrics();
            }

            if (i == moveLines * 2)
            {
                y = scoreSheetMovesTop;
            }

            if (i % 2 == 0)
            {
                // this is a white move

                y += moveBoxHeight;
                if (i < moveLines * 2)
                {
                    // left side of the scoresheet

                    g.drawString(pgnMove,
                                 whitesLeftColLeft + (moveBoxWidth / 2) - (fm.stringWidth(pgnMove) / 2),
                                 y - 2);
                }
                else
                {
                    // right side of the scoresheet

                    g.drawString(pgnMove,
                                 whitesRightColLeft + (moveBoxWidth / 2) - (fm.stringWidth(pgnMove) / 2),
                                 y - 2);
                }
            }
            else
            {
                // this is a black move

                if (i < moveLines * 2)
                {
                    // left side of the scoresheet

                    g.drawString(pgnMove,
                                 blacksLeftColLeft + (moveBoxWidth / 2) - (fm.stringWidth(pgnMove) / 2),
                                 y - 2);
                }
                else
                {
                    // right side of the scoresheet

                    g.drawString(pgnMove,
                                 blacksRightColLeft + (moveBoxWidth / 2) - (fm.stringWidth(pgnMove) / 2),
                                 y - 2);
                }
            }
        }
    }

    private void updateHighlightedMove(Graphics g)
    {
        // if are no moves - just return

        if (scoreKeeper.getCurrentMoveIndex() == 0)
        {
            return;
        }

        // if we're starting a new page redraw the scoresheet

        if ((scoreKeeper.getCurrentMoveIndex() - 1) % (moveLines * 4) == 0)
        {
              drawRightSpace(g);
              return;
        }

        int    iMoveIndex = scoreKeeper.getCurrentMoveIndex() -1;
        int    iLastIndex = iMoveIndex - 1;
        String pgnMove    = scoreKeeper.getPGNMoveAt(iMoveIndex);
        String pgnOldMove = scoreKeeper.getPGNMoveAt(iMoveIndex - 1);

        // calculate y - I know this is difficult to decipher...sorry Charlie

        int iLastMoveX    = 0;
        int iCurrentMoveX = 0;
        int iCurrentMoveY = scoreSheetMovesTop + ((((iMoveIndex % (moveLines * 2)) - (iMoveIndex % 2)) / 2) * moveBoxHeight) + moveBoxHeight;
        int iLastMoveY    = scoreSheetMovesTop + ((((iLastIndex % (moveLines * 2)) - (iLastIndex % 2)) / 2) * moveBoxHeight) + moveBoxHeight;

        // erase the old moves

        iMoveIndex = iMoveIndex % (moveLines * 4);
        iLastIndex = iLastIndex % (moveLines * 4);

        if (iMoveIndex % 2 == 0)
        {
            // this is a white move

            iCurrentMoveX = iMoveIndex < moveLines * 2 ? whitesLeftColLeft : whitesRightColLeft;
            iLastMoveX    = iLastIndex < moveLines * 2 ? blacksLeftColLeft : blacksRightColLeft;
        }
        else
        {
            // this is a black move

            iCurrentMoveX = iMoveIndex < moveLines * 2 ? blacksLeftColLeft : blacksRightColLeft;
            iLastMoveX    = iLastIndex < moveLines * 2 ? whitesLeftColLeft : whitesRightColLeft;
        }

        // remove the old highLighted move

        g.setColor(new Color(0xfffff0));
        g.fillRect(iLastMoveX + 1,  iLastMoveY - moveBoxHeight + 1, moveBoxWidth - 2, moveBoxHeight - 2);

        // draw the old move PLAIN

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.darkGray);
        g.drawString(pgnOldMove,
                     iLastMoveX + (moveBoxWidth / 2) - (fm.stringWidth(pgnOldMove) / 2),
                     iLastMoveY - 2);


        // erase the old move with a rectangle

        g.setColor(new Color(0xfffff0));
        g.fillRect(iCurrentMoveX + 1,  iCurrentMoveY - moveBoxHeight + 1, moveBoxWidth - 2, moveBoxHeight - 2);

        // draw the move in BOLD

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        fm = g.getFontMetrics();
        g.setColor(Color.darkGray);
        g.drawString(pgnMove,
                     iCurrentMoveX + (moveBoxWidth / 2) - (fm.stringWidth(pgnMove) / 2),
                     iCurrentMoveY - 2);

    }

    private void drawBottomSpace(Graphics g)
    {
        // draw the background

        g.setColor(new Color(0x007000));
        g.fillRect(bottomSpaceLeft,
                   bottomSpaceTop,
                   bottomSpaceWidth,
                   bottomSpaceHeight);

        // draw a boarder around that background

        g.setColor(new Color(0x000000));
        g.drawRect(bottomSpaceLeft,
                   bottomSpaceTop,
                   bottomSpaceWidth  - 1,
                   bottomSpaceHeight - 1);

        // draw the game list if we are reading a PGN file

        int pgnGameListX = bottomSpaceLeft + 4;
        int pgnGameListY = bottomSpaceTop  + 4;

        // draw "www.MyChess.com" - my credit

        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        String strMyCredit = new String("www.MyChess.com");

        g.setColor(new Color(0x00C000));
        g.drawString(strMyCredit,
                     rightSpaceLeft + (rightSpaceWidth / 2) - (fm.stringWidth(strMyCredit) / 2),
                     bottomSpaceTop + bottomSpaceHeight - (fm.getHeight() / 2));

        if (bInitError)
        {
            drawErrorMessage(g, strInitErrorMessage,
                             bottomSpaceLeft + (bottomSpaceWidth  / 2),
                             bottomSpaceTop  + (bottomSpaceHeight / 2));
            return;
        }
        else if (isConnected)
        {
            // create the chat imput and display area so players can chat.
            // Label the chat input first - users need to know what it's for

            g.setColor(Color.lightGray);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            fm = g.getFontMetrics();
            g.drawString("Chat input here:", bottomSpaceLeft + 4, bottomSpaceTop  + fm.getHeight());

            // create and show the chat input field

            if (chatInputField == null)
            {
                chatInputField = new TextField(10);
                chatInputField.addActionListener(this);
                chatInputField.setFont(new Font("SansSerif", Font.PLAIN, 12));
                add(chatInputField);

                final int chatLeft        = bottomSpaceLeft + 4;
                final int chatWidth       = bottomSpaceWidth - rightSpaceWidth - 8;
                final int chatInputTop    = bottomSpaceTop + fm.getHeight() + 4;
                final int chatInputHeight = chatInputField.getPreferredSize().height;
                chatInputField.setBounds(chatLeft, chatInputTop, chatWidth, chatInputHeight);
                chatInputField.setBackground(Color.white);
                chatInputField.setForeground(Color.black);
                chatInputField.setEditable(true);

                // initialize the chat display

                final int chatDisplayTop    = chatInputTop + chatInputHeight + 4;
                final int chatDisplayHeight = (bottomSpaceTop + bottomSpaceHeight) - chatDisplayTop - 4;
                chatDisplayRect = new Rectangle(chatLeft, chatDisplayTop, chatWidth, chatDisplayHeight);
                chatFont        = new Font("SansSerif", Font.PLAIN, 13);

                intClockTop = chatInputTop;
            }

            drawChatArea(g);
            drawClock(g);

            // set our button bounds if they haven't been set yet
            // the should under the clock

            int intButtonY = 0;
            int intButtonHeight = 0;
            if (btnResign.getSize().height == 0)
            {
                btnDraw.setFont(new Font("Dialog", Font.PLAIN, 12));
                btnResign.setFont(new Font("Dialog", Font.PLAIN, 12));
                btnPlayAgain.setFont(new Font("Dialog", Font.PLAIN, 12));

                // set draw button

                int intButtonWidth  = btnDraw.getPreferredSize().width;
                    intButtonHeight = btnDraw.getPreferredSize().height;
                int intCenter       = rightSpaceLeft + (rightSpaceWidth / 2);
                int intButtonX      = intCenter - intButtonWidth - 5;
                    intButtonY      = intButtonsTop != -1 ? intButtonsTop : intClockTop;
                btnDraw.setBounds(intButtonX, intButtonY, intButtonWidth, intButtonHeight);

                // set resign button

                intButtonX = intCenter + 5;
                btnResign.setBounds(intButtonX, intButtonY, intButtonWidth, intButtonHeight);

                // set the new game button

                intButtonWidth = btnPlayAgain.getPreferredSize().width;
                intButtonX     = intCenter - (intButtonWidth / 2);
                btnPlayAgain.setBounds(intButtonX, intButtonY, intButtonWidth, intButtonHeight);
            }

            /*int buttonY      = bottomSpaceTop + fm.getHeight() + 4;;
            int buttonWidth  = btnMenu.getPreferredSize().width;
            int buttonHeight = btnMenu.getPreferredSize().height;
            int buttonLeft   = rightSpaceLeft + (rightSpaceWidth / 2) - (buttonWidth / 2);
            //btnMenu.setBounds(buttonLeft, buttonY, buttonWidth, buttonHeight);
            //btnMenu.setVisible(false);*/

            // the AnimateMoves Checkbox

            if (cbxAnimateMoves.getSize().height == 0 && intButtonY != 0)
            {
                cbxAnimateMoves.setSize(cbxAnimateMoves.getPreferredSize());
                cbxSound.setSize(cbxSound.getPreferredSize());

                int intX = rightSpaceLeft + (rightSpaceWidth / 2) - ((cbxAnimateMoves.getSize().width  + cbxSound.getSize().width + 4) / 2);
                int intY = intButtonY + intButtonHeight + 5;

                cbxSound.setLocation(intX, intY);
                cbxAnimateMoves.setLocation(intX + cbxSound.getSize().width + 4, intY);
            }
        }
    }

    private void drawClock(Graphics g)
    {
        // if starting minutes is less than one "No Clock" was selected
        // also make sure know where the top of the time clock is

        if (intStartingMinutes < 1 || intClockTop == -1)
        {
            return;
        }

        // first draw a light rectangle for the background

        int intRectX     = rightSpaceLeft + 10;
        int intRectY     = intClockTop;
        int intRectWidth = rightSpaceWidth - 20;

        // caculate the rect height - it will be based on the
        // fonts to be drawn which are different pixel sizes
        // in different browsers.

        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        int intRectHeight = (int)(fm.getHeight() * 4.6);

        // set the global intButtonsTop here since now we know
        // where the bottom of the clock is

        intButtonsTop = intRectY + intRectHeight + 5;

        // draw the rectangle

        g.setColor(new Color(0xfffff0));
        g.fillRect(intRectX, intRectY, intRectWidth, intRectHeight);
        g.setColor(Color.black);
        g.drawRect(intRectX, intRectY, intRectWidth, intRectHeight);

        g.setColor(Color.black);
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        fm = g.getFontMetrics();

        // prepare our strings, their coordinates, and draw 'em

        int intCenter      = rightSpaceLeft + (rightSpaceWidth / 2);
        int intWhiteCenter = rightSpaceLeft + (rightSpaceWidth / 4);
        int intBlackCenter = rightSpaceLeft + rightSpaceWidth - (rightSpaceWidth / 4);

        String strTitle = "Time Control: " + intStartingMinutes + '/' + intIncrementSeconds;

        int intClockX = intCenter - (fm.stringWidth(strTitle) / 2);
        int intClockY = intRectY + fm.getHeight() + (fm.getHeight() / 6);
        g.drawString(strTitle, intClockX, intClockY);

        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        fm = g.getFontMetrics();
        intClockX  = intWhiteCenter - (fm.stringWidth(currentGameTags.white) / 2);
        intClockY += fm.getHeight() + (fm.getHeight() / 3);
        g.drawString(currentGameTags.white, intClockX, intClockY);

        intClockX  = intBlackCenter - (fm.stringWidth(currentGameTags.black) / 2);
        g.drawString(currentGameTags.black, intClockX, intClockY);

        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        fm = g.getFontMetrics();
        intClockX  = intWhiteCenter - (fm.stringWidth(strWhitesClock) / 2);
        intClockY += fm.getHeight() + (fm.getHeight() / 6);
        g.setColor(Color.blue);
        g.drawString(strWhitesClock, intClockX, intClockY);

        intClockX  = intBlackCenter - (fm.stringWidth(strBlacksClock) / 2);
        g.drawString(strBlacksClock, intClockX, intClockY);
    }

    private void drawChatArea(Graphics g)
    {
        // draw the chat background

        g.setColor(new Color(0x000000));
        g.fillRect(chatDisplayRect.x, chatDisplayRect.y, chatDisplayRect.width, chatDisplayRect.height);

        // draw the strings in the vChatStrings vector

        g.setFont(chatFont);
        FontMetrics fm = g.getFontMetrics();
        int y = chatDisplayRect.y + fm.getHeight();
        int x = chatDisplayRect.x + 6;
        for (int i = 0; i < vChatStrings.size(); ++i)
        {
            String strChatString = (String)vChatStrings.elementAt(i);

            // draw the player's name it cyan

            if (strChatString.indexOf(':') != -1 && strChatString.indexOf(':') < 22)
            {
                String strName = strChatString.substring(0, strChatString.indexOf(':') + 1);
                strChatString  = strChatString.substring(strChatString.indexOf(':') + 1, strChatString.length());

                g.setColor(Color.cyan);
                g.drawString(strName, x, y);
                g.setColor(Color.white);
                g.drawString(strChatString, x + fm.stringWidth(strName), y);
            }
            else
            {
                g.setColor(Color.white);
                g.drawString(strChatString, x, y);
            }

            y += fm.getHeight();
        }
    }

    private void drawPiece(Graphics g, char piece, int x, int y)
    {
        switch (piece)
        {
            case 'P':

                g.drawImage(wPawnImage, x, y, this);
                break;

            case 'N':

                g.drawImage(wKnightImage, x, y, this);
                break;

            case 'B':

                g.drawImage(wBishopImage, x, y, this);
                break;

            case 'R':

                g.drawImage(wRookImage, x, y, this);
                break;

            case 'Q':

                g.drawImage(wQueenImage, x, y, this);
                break;

            case 'K':

                g.drawImage(wKingImage, x, y, this);
                break;

            case 'p':

                g.drawImage(bPawnImage, x, y, this);
                break;

            case 'n':

                g.drawImage(bKnightImage, x, y, this);
                break;

            case 'b':

                g.drawImage(bBishopImage, x, y, this);
                break;

            case 'r':

                g.drawImage(bRookImage, x, y, this);
                break;

            case 'q':

                g.drawImage(bQueenImage, x, y, this);
                break;

            case 'k':

                g.drawImage(bKingImage, x, y, this);
                break;

            default:

                break;
        }
    }

    /*
     *    This draws a "MessageBox" with the given foreground
     *    and background.  It will be draw so that the given x, y
     *    coordinate is the center of the box.  strMessage can be
     *    several lines separated be the newline character '\n'.
     *    The box is auto-sized around strMessage.
     **/

    private void drawMessageBox(Graphics g,
                                String strMessage,
                                Color foreground,
                                Color background,
                                int x,
                                int y)
    {
        // first, get the lines in our error message as 'tokens'

        StringTokenizer st = new StringTokenizer(strMessage, "\n");

        // set the font

        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        // get the dimension of the message box

        int iBoxHeight = fm.getHeight() * (st.countTokens() + 2);
        int iBoxWidth = 0;

        while (st.hasMoreTokens())
        {
            // find the longest string

            String strLine = st.nextToken();

            if (fm.stringWidth(strLine) > iBoxWidth - 40)
            {
                iBoxWidth = fm.stringWidth(strLine) + 40;
            }
        }

        // make the given x, y point the center of the message box

        x -= iBoxWidth / 2;
        y -= iBoxHeight / 2;

        // draw the box background with a black border

        g.setColor(background);
        g.fillRect(x,
                   y,
                   iBoxWidth,
                   iBoxHeight);

        g.setColor(Color.black);
        g.drawRect(x,
                   y,
                   iBoxWidth,
                   iBoxHeight);

        // now draw the message text

        int iTextX;
        int iTextY = y + fm.getAscent();
        st = new StringTokenizer(strMessage, "\n");

        g.setColor(foreground);
        while (st.hasMoreTokens())
        {
            String strLine = st.nextToken();

            iTextX  = x + ((iBoxWidth / 2) - (fm.stringWidth(strLine) / 2));
            iTextY += fm.getHeight();

            g.drawString(strLine, iTextX, iTextY);
        }
    }

    /*
     *    This draws an error "MessageBox" with a red background and
     *    a black forground.  It will be draw so that the given x, y
     *    coordinate is the center of the box.  strMessage can be
     *    several lines separated be the newline character '\n'.  The box
     *    is automatically sized around strMessage.
     **/

    private void drawErrorMessage(Graphics g, String strMessage, int x, int y)
    {

        drawMessageBox(g,
                       strMessage,
                       Color.black,
                       Color.red,
                       x,
                       y);
    }

    private void moveSound()
    {
        if (bSound)
        {
            tap.stop();
            tap.play();
        }
    }

    // Mouse Listener methods

    public void mouseMoved(MouseEvent e)
    {
        if (squarePressed != -1)
        {
            // return if were already dragging a piece - the mouse won't change

            return;
        }

        // what square are we over?

        int  squareOver = xyToSquareNumber(e.getX(), e.getY());
        char cPiece     = squareOver == -1 ? '-' : scoreKeeper.getCurrentPosition()[squareOver];

        // change from a hand cursor to the default cursor if needed

        if (cPiece == '-')
        {
            if (getCursor().getType() == Cursor.HAND_CURSOR)
            {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            return;
        }

        // change to a hand cursor if needed

        else
        {
            if (getCursor().getType() != Cursor.HAND_CURSOR &&
               (bIsWhitePlayer && Character.isUpperCase(cPiece)) ||
              (!bIsWhitePlayer && Character.isLowerCase(cPiece)))
            {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }
    }

    public void mouseDragged(MouseEvent e)
    {
        // have we picked up a piece? return if not

        if (squarePressed == -1)
        {
            return;
        }

        // assign the previous x, y of draggedPiece

        draggedPieceOldX = draggedPieceX;
        draggedPieceOldY = draggedPieceY;

        // calculated the curent x, y of draggedPiece

        draggedPieceX = e.getX() - draggedPieceToMouseX;
        draggedPieceY = e.getY() - draggedPieceToMouseY;

        // set the squareDraggedOver so we can highlight it

        squareDraggedOver = xyToSquareNumber(e.getX(), e.getY());

        // repaint!

        repaint();
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        // if dragging is off, just return

        if (!isDraggingOn)
        {
            return;
        }

        if (!bGameIsInProgress)
        {
            return;
        }

        // make sure we're not already dragging a piece around
        // it is possible to press another button during dragging

        if (squarePressed != -1)
        {
            return;
        }

        if (bViewerMode)
        {
            return;
        }

        // only move our own pieces

        int  intSquare = xyToSquareNumber(e.getX(), e.getY());
        char cPiece    = scoreKeeper.getCurrentPosition()[intSquare];

        if ((bIsWhitePlayer && Character.isLowerCase(cPiece)) ||
           (!bIsWhitePlayer && Character.isUpperCase(cPiece)) ||
            cPiece == '-')
        {
            return;
        }

        // okay, set squarePressed to the selected square

        squarePressed = xyToSquareNumber(e.getX(), e.getY());

        if (squarePressed == -1)
        {
            return;
        }

        // this is the piece we'll be dragging
        // remove it from the board

        draggedPiece  = scoreKeeper.getCurrentPosition()[squarePressed];
        squareDraggedOver = squarePressed;

        // calculate the the x, y coordinate of the dragged piece

        int file, rank;
        if (whiteFromBottom)
        {
            file = (squarePressed + 8) % 8;
            rank = squarePressed  / 8;
        }
        else
        {
            file = (63 - squarePressed + 8) % 8;
            rank = (63 - squarePressed) / 8;
        }

        int pieceWidth    = wPawnImage.getWidth(this);
        int pieceHeight   = wPawnImage.getHeight(this);
        int pieceOffsetX  = (squareWidth / 2) - (pieceWidth / 2);
        int pieceOffsetY  = squareHeight - pieceHeight;

        draggedPieceX = leftSpaceWidth + leftMargin + (file * squareWidth)  + pieceOffsetX;
        draggedPieceY = topSpaceHeight + topMargin  + (rank * squareHeight) + pieceOffsetY;

        // now, calculate to relative distance from the mouse's
        // current x, y to draggedPiece's current x,y

        draggedPieceToMouseX = e.getX() - draggedPieceX;
        draggedPieceToMouseY = e.getY() - draggedPieceY;

        repaint();
    }

    public void mouseReleased(MouseEvent e)
    {
        if (squarePressed == -1)
        {
            // we are not moving a piece here - do nothing

            return;
        }

        if (squareDraggedOver == squarePressed || squareDraggedOver == -1 || !bGameIsInProgress)
        {
            // not a valid destination square

            scoreKeeper.getCurrentPosition()[squarePressed] = draggedPiece;
        }
        else
        {
            // get ready to move the piece...
            // if we need to promote a pawn - display the pawn promotion
            // dialog and make the move after the user reponds

            if (scoreKeeper.getCurrentPosition()[squarePressed] == 'P')
            {
                if ((squarePressed > 7 && squarePressed < 16) && (squareDraggedOver < 8))
                {
                    bShowPromotionDialog = true;
                    repaint();
                    return;
                }
            }
            else if (scoreKeeper.getCurrentPosition()[squarePressed] == 'p')
            {
                if ((squarePressed > 47 && squarePressed < 56) && (squareDraggedOver > 55))
                {
                    bShowPromotionDialog = true;
                    repaint();
                    return;
                }
            }

            // not promoting any pawns this time - make the chess move

            ChessMove chessMove = new ChessMove(squarePressed,
                                                squareDraggedOver,
                                                "",
                                                scoreKeeper.getCurrentPosition(),
                                                '\0');

            // move it!  (if it's legal)

            if (scoreKeeper.makeMove(chessMove.getLongAlg()))
            {
                // Send the move along with the time left in milliseconds

                String strMillisLeft;

                if (scoreKeeper.isWhitesMove())
                {
                    longMillisLeftForBlack += intIncrementSeconds * 1000;
                    strMillisLeft = Long.toString(longMillisLeftForBlack);
                }
                else
                {
                    longMillisLeftForWhite += intIncrementSeconds * 1000;
                    strMillisLeft = Long.toString(longMillisLeftForWhite);
                }

                // set intLastDestMove so it will be hightlighted

                intLastDestSquare = squareDraggedOver;

                sendString(cstrClientSendingMoveKey, chessMove.getBasicAlg() + "," + strMillisLeft);
                moveSound();
            }
        }

        squarePressed     = -1;
        squareDraggedOver = -1;
        draggedPiece      = '-';
        bPaintBoardOnly   = true;
        repaint();
    }

    /**
     *     This method converts an x, y coordinate
     *     (from a mouse most likely) to a square number.  This
     *     number can then be used to locate the piece a user is
     *     attempting to pick up (or the square to drop a piece off)
     *     by indexing into currentPosition.
     */

    private int xyToSquareNumber(int x, int y)
    {
        // are we on a valid chess square?

        if (x < leftSpaceWidth + leftMargin || x > leftSpaceWidth + leftMargin + (squareWidth  * 8) - 1 ||
            y < topSpaceHeight + topMargin  || y > topSpaceHeight + topMargin  + (squareHeight * 8) - 1)
        {
            return -1;
        }

        // now calulate the square number

        int squaresToTheRight  = (x - leftMargin - leftSpaceWidth) / squareWidth;
        int squaresToTheBottom = (y - topMargin  - topSpaceHeight) / squareHeight;
        int squareNum = squaresToTheBottom * 8 + squaresToTheRight;

        return whiteFromBottom ? squareNum : 63 - squareNum;
    }

    /**
     *    This method finds the point location (left, top) of
     *    the given square number.
     */

    private Point squareNumberToPoint(int square)
    {
        int workSquare = !whiteFromBottom ? Math.abs(square - 63) : square;
        int file       = workSquare % 8;
        int rank       = workSquare / 8;
        int x          = leftMargin + (file * squareWidth)  + leftSpaceWidth;
        int y          = topMargin  + (rank * squareHeight) + topSpaceHeight;

        return new Point(x, y);
    }

    private void AnimateNextMove()
    {
        // wait for the previous animation to finish if the user
        // is wildly clicking the nextMove button

        if (animationThread == null || !animationThread.isAlive())
        {
            ChessMove cm = scoreKeeper.getCurrentChessMove();

            isAnimation          = true;
            animatedSourceSquare = cm.getSourceSquare();
            animatedDestSquare   = cm.getDestSquare();
            animationPosition    = (char[])scoreKeeper.getCurrentPosition().clone();
            animatedPiece        = animationPosition[animatedSourceSquare];
            animationPosition[animatedSourceSquare] = '-';

            scoreKeeper.gotoPosition(scoreKeeper.getCurrentMoveIndex() + 1);

            animationThread = new Thread(this, "Animation");
            animationThread.start();
        }
    }

    // this processes our applet's controls

    public void actionPerformed(ActionEvent event)
    {
        Object object = event.getSource();

        if (object == btnMenu)
        {
            Point p       = btnMenu.getLocation();

            preferencesMenu.show(this, p.x, p.y + btnMenu.getSize().height);
        }
        if (object == miFlipBoard)
        {
            whiteFromBottom = whiteFromBottom ? false : true;
            repaint();
        }
        else if (object == miAnimateMoves)
        {
            if (bAnimateMoves)
            {
                bAnimateMoves = false;
                miAnimateMoves.setLabel("Turn Animation On");
            }
            else
            {
                bAnimateMoves = true;
                miAnimateMoves.setLabel("Turn Animation Off");
            }
        }
        else if (object == chatInputField && chatInputField.getText().length() > 0)
        {
            // send the chat input message to the server

            if (bGameStarted)
            {
                sendString("", chatInputField.getText());
            }

            chatInputField.setText("");
        }
        else if (object == btnPromotePawn)
        {
            // we are promoting a pawn - first, get the piece selection

            Checkbox checkbox = cbgPromotion.getSelectedCheckbox();
            char cPromotionPiece = 'Q';
                 if (checkbox == cbxRook)   cPromotionPiece = 'R';
            else if (checkbox == cbxBishop) cPromotionPiece = 'B';
            else if (checkbox == cbxKnight) cPromotionPiece = 'N';

            // now setup up a Chessmove

            ChessMove chessMove = new ChessMove(squarePressed,
                                                squareDraggedOver,
                                                "",
                                                scoreKeeper.getCurrentPosition(),
                                                cPromotionPiece);

            // move it!  (if it's legal)

            if (scoreKeeper.makeMove(chessMove.getLongAlg()))
            {
                // Send the move along with the time left in milliseconds

                String strMillisLeft;

                if (scoreKeeper.isWhitesMove())
                {
                    longMillisLeftForBlack += intIncrementSeconds * 1000;
                    strMillisLeft = Long.toString(longMillisLeftForBlack);
                }
                else
                {
                    longMillisLeftForWhite += intIncrementSeconds * 1000;
                    strMillisLeft = Long.toString(longMillisLeftForWhite);
                }

                // set intLastDestMove so it will be hightlighted

                intLastDestSquare = squareDraggedOver;

                sendString(cstrClientSendingMoveKey, chessMove.getBasicAlg() + "," + strMillisLeft);
                moveSound();
            }

            // set intLastDestMove so it will be hightlighted

            intLastDestSquare = squareDraggedOver;

            squarePressed        = -1;
            squareDraggedOver    = -1;
            draggedPiece         = '-';

            pnlPromotion.setVisible(false);
            btnPromotePawn.setVisible(false);
            bShowPromotionDialog = false;
            bPaintBoardOnly      = true;
            repaint();
        }
        else if (object == btnResign)
        {
            strYesNoMessage = "Are you sure you want to resign?";
            bShowYesNoDialog = true;
            repaint();
        }
        else if (object == btnDraw)
        {
            // only offer draws on our move - don't harrass

            if ((bIsWhitePlayer && !scoreKeeper.isWhitesMove()) || (!bIsWhitePlayer && scoreKeeper.isWhitesMove()))
            {
                addChatMessage("");
                addChatMessage(" Sorry, you can only offer a draw on you own move.");
                addChatMessage("");
                return;
            }

            strYesNoMessage = "Offer a draw?";
            bShowYesNoDialog = true;
            repaint();
        }
        else if (object == btnYes)
        {
            if (strYesNoMessage.equals("Are you sure you want to resign?"))
            {
                sendString(cstrClientIResignKey, strHandle);
            }
            else if (strYesNoMessage.equals("Offer a draw?"))
            {
                sendString(cstrClientOfferDrawKey, strHandle);
            }
            else if (strYesNoMessage.equals("Your opponent has offered a draw.  Do you accept?"))
            {
                sendString(cstrClientDrawAgreedKey, strHandle);
            }

            btnYes.setVisible(false);
            btnNo.setVisible(false);
            strYesNoMessage = "";
            bShowYesNoDialog = false;
            btnYes.setVisible(false);
            repaint();
        }
        else if (object == btnNo)
        {
            // nothing to do - just remove the dialog

            btnYes.setVisible(false);
            btnNo.setVisible(false);
            strYesNoMessage = "";
            bShowYesNoDialog = false;
            repaint();
        }
        else if (object == btnPlayAgain)
        {
            // send the request an remove the button from view
            // to keep the user from harrassing the other player

            sendString(cstrClientPlayAgainKey, strHandle);
            btnPlayAgain.setVisible(false);
        }
    }

    public void itemStateChanged(ItemEvent e)
    {
        Object object     = e.getSource();

        if (object == cbxAnimateMoves)
        {
            bAnimateMoves = cbxAnimateMoves.getState();
        }
        if (object == cbxSound)
        {
            bSound = cbxSound.getState();
        }

    }

    // windows listeners

    public void windowActivated(WindowEvent e)
    {
    }
    public void windowDeactivated(WindowEvent e)
    {
    }
    public void windowClosed(WindowEvent e)
    {
    }
    public void windowClosing(WindowEvent e)
    {
        bGameIsInProgress = false;
        bStopConnectionMonitor = true;

        // sleep for 1/4 second to allow timeClock() to see bGamesIsInProgress is false
        // and exit it's loop (it sleeps for 1/5 second)

        try
        {
            Thread.sleep(250);
        } catch (Exception ex) {}

        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (Exception event)
            {
            }
        }

        dispose();
        System.exit(0);
    }
    public void windowOpened(WindowEvent e)
    {
    }
    public void windowIconified(WindowEvent e)
    {
    }
    public void windowDeiconified(WindowEvent e)
    {
    }
}