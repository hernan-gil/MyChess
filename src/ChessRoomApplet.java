import java.awt.*;
import java.awt.List;
import java.applet.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ChessRoomApplet extends Applet implements ActionListener, ItemListener, Runnable
{
    ChessBoardFrame chessBoardFrame;
    TextField       chatInputField;
    TextField       minutesInputField;
    TextField       secondsInputField;
    List            whoIsHereList;
    List            challengeToList;
    List            challengeFromList;
    Vector          vChatStrings;
    Vector          playerInfoVector;
    PlayerInfo      selectedPlayer;
    Button          btnChallengeSelectedPlayer;
    Button          btnViewGame;
    Checkbox        chbxSetTimeControl;
    int             intRightSideWidth;
    int             intRightSideLeft;
    int             intLowerRightTop;
    boolean         bInitError;
    boolean         bDrawChatAreaOnly;
    String          strInitErrorMessage;
    String          strMyHandle;
    Socket          roomSocket;
    Image           offImage;
    Rectangle       chatDisplayRect;
    Font            chatFont;
    AudioClip       tap2;

    // below are the client-server commuication strings
    // server-to_client communication strings

    final String cstrServerSendPlayersListKey   = "SENDING_PLAYERS_LIST";
    final String cstrServerRoomFullKey          = "ROOM_FULL";
    final String cstrServerNewPlayerKey         = "SENDING_NEW_PLAYER";
    final String cstrServerRemovePlayerKey      = "REMOVING_PLAYER";
    final String cstrServerChallengeRecievedKey = "CHALLENGE_RECIEVED";
    final String cstrServerChallengeRemovedKey  = "CHALLENGE_REMOVE";
    final String cstrerverWelcomeKey            = "WELCOME";
    final String cstrServerStartGameKey         = "START_GAME";
    final String cstrServerUpdatePlayerKey      = "UPDATE_PLAYER";

    // client_to_server communication strings

    final String cstrClientSendingPlayerIDKey = "/SENDING_PLAYER_ID";
    final String cstrClientChallengeKey       = "/CHALLENGE";
    final String cstrClientAcceptChallengeKey = "/ACCEPT_CHALLENGE";

    // the general string suffix

    final String cstrSuffix = "^@!";

    public void init()
    {
        setLayout(null);
        setBackground(new Color(0x007000));
        selectedPlayer   = null;
        playerInfoVector = new Vector();
        vChatStrings     = new Vector();

        // create a list component what will display the users in the room

        whoIsHereList = new List();
        whoIsHereList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        whoIsHereList.addItemListener(this);
        add(whoIsHereList);

        // create a list component what will display the users this
        // player is challenging

        challengeToList = new List();
        challengeToList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        challengeToList.addItemListener(this);
        add(challengeToList);

        // create a list component what will display the users that
        // are challenging this player

        challengeFromList = new List();
        challengeFromList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        challengeFromList.addActionListener(this);
        add(challengeFromList);

        // create the chat input component ...

        chatInputField = new TextField();
        chatInputField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chatInputField.addActionListener(this);
        add(chatInputField);

        intRightSideWidth = 200;
        intRightSideLeft  = getSize().width - intRightSideWidth;
        intLowerRightTop  = getSize().height - (getSize().height / 3);

        // create the btnChallengeSelectedPlayer and btnShowPr0file buttons
        // but don't display them yet

        btnChallengeSelectedPlayer = new Button();
        btnChallengeSelectedPlayer.setFont(new Font("Dialog", Font.BOLD, 12));
        btnChallengeSelectedPlayer.setVisible(false);
        btnChallengeSelectedPlayer.addActionListener(this);
        add(btnChallengeSelectedPlayer);

        btnViewGame = new Button("View Game");
        btnViewGame.setFont(new Font("Dialog", Font.BOLD, 12));
        btnViewGame.setVisible(false);
        btnViewGame.addActionListener(this);
        add(btnViewGame);

        // create the time control check box - if it is set there will
        // will be no time control

        chbxSetTimeControl = new Checkbox("Use a chess clock", false);
        chbxSetTimeControl.setFont(new Font("Dialog", Font.PLAIN, 12));
        chbxSetTimeControl.setVisible(false);
        chbxSetTimeControl.addItemListener(this);
        add(chbxSetTimeControl);

        // create the time controls minutes and seconds input fields ...

        minutesInputField = new TextField();
        minutesInputField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        minutesInputField.setVisible(false);
        add(minutesInputField);

        secondsInputField = new TextField("", 3);
        secondsInputField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        secondsInputField.setVisible(false);
        add(secondsInputField);

        // set the dispay characteristic for whoList

        final int whoListWidth  = intRightSideWidth - 8;
        final int whoListHeight = intLowerRightTop - 8;
        final int whoListLeft   = intRightSideLeft + 4;
        final int whoListTop    = 4;

        whoIsHereList.setBounds(whoListLeft, whoListTop, whoListWidth, whoListHeight);
        whoIsHereList.setBackground(new Color(0x001000));
        whoIsHereList.setForeground(Color.cyan);

        // set the dispay characteristic for the bottom lists

        getGraphics().setFont(new Font("Dialog", Font.PLAIN, 13));
        FontMetrics fm = getGraphics().getFontMetrics();

        final int intHalfOfBottom = intRightSideLeft / 2;

        final int intListWidth  = intHalfOfBottom - 10;
        final int intListHeight = getSize().height - intLowerRightTop - fm.getHeight() - 16;
        final int intListTop    = intLowerRightTop + fm.getHeight() + 8;
              int intListLeft   = 5;

        challengeToList.setBounds(5, intListTop, intListWidth, intListHeight);
        challengeFromList.setBounds(intHalfOfBottom  + 5, intListTop, intListWidth, intListHeight);
        challengeToList.setBackground(Color.black);
        challengeToList.setForeground(Color.cyan);
        challengeFromList.setBackground(Color.black);
        challengeFromList.setForeground(Color.green);

        // set the dispay characteristic for chatInput

        final int chatLeft        = 4;
        final int chatWidth       = intRightSideLeft - 4;
        final int chatInputHeight = chatInputField.getPreferredSize().height;
        final int chatInputTop    = intLowerRightTop - chatInputHeight - 4;
        chatInputField.setBounds(chatLeft, chatInputTop, chatWidth, chatInputHeight);
        chatInputField.setBackground(Color.white);
        chatInputField.setForeground(Color.black);
        chatInputField.setEditable(true);

        // set the dispay characteristic for chatDisplay

        final int chatDisplayTop    = whoListTop;
        final int chatDisplayHeight = chatInputTop - 8;
        chatDisplayRect = new Rectangle(chatLeft, chatDisplayTop, chatWidth, chatDisplayHeight);
        chatFont        = new Font("SansSerif", Font.PLAIN, 13);

        bDrawChatAreaOnly    = false;
        bInitError           = false;
        strInitErrorMessage  = "";
        strMyHandle          = "";

        // get the tap sound for our chessboard

        tap2 = getAudioClip(getCodeBase(), "tap2.au");

        Thread ConnectThread = new Thread(this, "ConnectToChessServer");
        ConnectThread.start();
    }

    public void run()
    {
        if (Thread.currentThread().getName().equals("ConnectToChessServer"))
        {
            if (!connectToChessServer())
            {
                repaint();
                return;
            }

            monitorConnectionInput();
        }
    }

    public void destroy()
    {
        if (roomSocket != null)
        {
            try
            {
                roomSocket.close();
            }
            catch (Exception event)
            {
            }
        }
    }

    private void addChatMessage(String strMessage)
    {
        // limit the string length to 256 characters

        if (strMessage.length() > 256)
        {
            strMessage = strMessage.substring(0, 256);
        }

        // first break up the message if it's too long

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

        // add the latest message

        vChatStrings.addElement(strMessage);

        // for now, only store enough chat message that can display

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
        repaint();
    }

    public void update(Graphics g)
    {
        if (bInitError)
        {
            drawErrorMessage(g, strInitErrorMessage, getSize().width / 2, getSize().height / 2);
            return;
        }

        if (offImage == null)
        {
            offImage = this.createImage(getSize().width, getSize().height);
        }

        Graphics offGraphics  = offImage.getGraphics();

        if (bDrawChatAreaOnly)
        {
            drawChatArea(offGraphics);
            g.drawImage(offImage, 0, 0, this);
            bDrawChatAreaOnly = false;
            return;
        }

        drawChatArea(offGraphics);

        // draw the list titles

        offGraphics.setFont(new Font("Dialog", Font.PLAIN, 13));
        offGraphics.setColor(Color.cyan);
        FontMetrics fm = offGraphics.getFontMetrics();

        String strChallengeToTitle   = "Challenges To You";
        String strChallengeFromTitle = "Challenges By You";
        int intHalfOfBottom = intRightSideLeft / 2;
        offGraphics.drawString(strChallengeFromTitle,
                              (intHalfOfBottom / 2) - (fm.stringWidth(strChallengeFromTitle) / 2),
                               intLowerRightTop + fm.getHeight());
        offGraphics.drawString(strChallengeToTitle,
                               intHalfOfBottom + (intHalfOfBottom / 2) - (fm.stringWidth(strChallengeToTitle) / 2) ,
                               intLowerRightTop + fm.getHeight());

        // draw the select player on the lower right side

        if (selectedPlayer == null)
        {
            chbxSetTimeControl.setVisible(false);
            minutesInputField.setVisible(false);
            secondsInputField.setVisible(false);
            btnChallengeSelectedPlayer.setVisible(false);
            btnViewGame.setVisible(false);

            // erase the previous selected player by drawing
            // a rectangle over it.

            offGraphics.setColor(new Color(0x007000));
            offGraphics.fillRect(intRightSideLeft + 4,
                                 intLowerRightTop,
                                 intRightSideWidth - 8,
                                 getSize().height - intLowerRightTop - 4);

            // draw some instuctions for new users

            String strString1 = "To challenge a player:";
            String strString2 = "Click the player above";
            String strString3 = "To view a game in progress:";
            String strString4 = "Click the game above";
            String strString5 = "To accept a challenge to you:";
            String strString6 = "Double-click the challenge";

            offGraphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
            fm = offGraphics.getFontMetrics();

            // draw the player's handle

            int intIndent = 15;
            int intHeight = getSize().height - intLowerRightTop - 4;
            int intTextY  = intLowerRightTop + 15 + (intHeight / 2) - ((fm.getHeight() * 7) / 2);
            int intTextX  = intRightSideLeft + 10;

            offGraphics.setColor(Color.cyan);
            offGraphics.drawString(strString1, intTextX, intTextY);
            intTextY += fm.getHeight();
            offGraphics.drawString(strString2, intTextX + intIndent, intTextY);
            intTextY += fm.getHeight() + (fm.getHeight() / 2);
            offGraphics.drawString(strString3, intTextX, intTextY);
            intTextY += fm.getHeight();
            offGraphics.drawString(strString4, intTextX + intIndent, intTextY);
            intTextY += fm.getHeight() + (fm.getHeight() / 2);
            offGraphics.drawString(strString5, intTextX, intTextY);
            intTextY += fm.getHeight();
            offGraphics.drawString(strString6, intTextX + intIndent, intTextY);
        }
        else
        {
            final int intGrayBoxLeft   = intRightSideLeft + 4;
            final int intGrayBoxTop    = intLowerRightTop;
            final int intGrayBoxWidth  = intRightSideWidth - 8;
            final int intGrayBoxHeight = getSize().height - intLowerRightTop - 4;

            Color bkColor = new Color(0xb9b9b9);
            offGraphics.setColor(bkColor);
            offGraphics.fillRect(intGrayBoxLeft,
                                 intGrayBoxTop,
                                 intGrayBoxWidth,
                                 intGrayBoxHeight);

            final int intRightSideCenter = intRightSideLeft + ((getSize().width - intRightSideLeft) / 2);
            offGraphics.setColor(Color.black);
            offGraphics.setFont(new Font("Dialog", Font.BOLD, 13));
            fm = offGraphics.getFontMetrics();

            // draw the player's handle

            int intTextY = intLowerRightTop + fm.getHeight();
            offGraphics.drawString(selectedPlayer.strHandle, intRightSideCenter - (fm.stringWidth(selectedPlayer.strHandle) / 2), intTextY);

            // draw the player's rating

            offGraphics.setFont(new Font("Dialog", Font.PLAIN, 12));
            fm = offGraphics.getFontMetrics();

            intTextY += fm.getHeight() + 2;
            String strDisplayString = "Rating: " + selectedPlayer.strRating;

            offGraphics.drawString(strDisplayString,
                                   intRightSideCenter - (fm.stringWidth(strDisplayString) / 2),
                                   intTextY);

            // draw the wins, losses and draws for this player

            intTextY += fm.getHeight();
            strDisplayString = selectedPlayer.strWins   + " wins, "   +
                               selectedPlayer.strLosses + " losses, " +
                               selectedPlayer.strDraws  + " draws";

            offGraphics.drawString(strDisplayString,
                                   intRightSideCenter - (fm.stringWidth(strDisplayString) / 2),
                                   intTextY);

            offGraphics.setFont(new Font("Dialog", Font.PLAIN, 12));
            fm = offGraphics.getFontMetrics();

            // display the challenge controls if the selected player
            // is not ourself

            if (strMyHandle.equals(selectedPlayer.strHandle))
            {
                chbxSetTimeControl.setVisible(false);
                minutesInputField.setVisible(false);
                secondsInputField.setVisible(false);
                btnChallengeSelectedPlayer.setVisible(false);
                btnViewGame.setVisible(false);

                String strString = "These are your statistics";
                offGraphics.drawString(strString, intRightSideCenter - (fm.stringWidth(strString) / 2), intGrayBoxTop + (intGrayBoxHeight / 2));
            }
            else if (selectedPlayer.strStatus.startsWith("Vs"))
            {
                chbxSetTimeControl.setVisible(false);
                minutesInputField.setVisible(false);
                secondsInputField.setVisible(false);
                btnChallengeSelectedPlayer.setVisible(false);

                String strString = "Playing " + selectedPlayer.strStatus.substring(3, selectedPlayer.strStatus.length());
                offGraphics.drawString(strString, intRightSideCenter - (fm.stringWidth(strString) / 2), intGrayBoxTop + (intGrayBoxHeight / 2) - 5);

                btnViewGame.setSize(btnViewGame.getPreferredSize());
                btnViewGame.setLocation(intRightSideCenter - (btnViewGame.getSize().width / 2), intGrayBoxTop + (intGrayBoxHeight / 2) + 10);
                btnViewGame.setBackground(bkColor);
                btnViewGame.setVisible(true);
            }
            else
            {
                btnViewGame.setVisible(false);

                final int intLeftJustify = intRightSideLeft + 15;

                // draw the "No Time Control" check box - this turns of the time
                // control settings if checked

                intTextY += 4;
                chbxSetTimeControl.setSize(chbxSetTimeControl.getPreferredSize());
                chbxSetTimeControl.setLocation(intLeftJustify, intTextY);
                chbxSetTimeControl.setBackground(bkColor);
                chbxSetTimeControl.setVisible(true);

                // draw the time control input fields

                intTextY += chbxSetTimeControl.getSize().height + 2;
                minutesInputField.setColumns(1);
                minutesInputField.setSize(minutesInputField.getPreferredSize(minutesInputField.getColumns()));
                minutesInputField.setLocation(intLeftJustify, intTextY);
                if (!chbxSetTimeControl.getState())
                {
                    minutesInputField.setEditable(false);
                    minutesInputField.setBackground(bkColor);
                }
                else
                {
                    minutesInputField.setEditable(true);
                    minutesInputField.setBackground(Color.white);
                }
                minutesInputField.setVisible(true);

                intTextY += (minutesInputField.getSize().height / 2) + (fm.getAscent() / 2);
                offGraphics.drawString("Initial minutes", minutesInputField.getLocation().x + minutesInputField.getSize().width + 4, intTextY);

                intTextY = minutesInputField.getLocation().y + minutesInputField.getSize().height + 2;
                secondsInputField.setColumns(1);
                secondsInputField.setSize(minutesInputField.getPreferredSize(secondsInputField.getColumns()));
                secondsInputField.setLocation(intLeftJustify, intTextY);
                if (!chbxSetTimeControl.getState())
                {
                    secondsInputField.setEditable(false);
                    secondsInputField.setBackground(bkColor);
                }
                else
                {
                    secondsInputField.setEditable(true);
                    secondsInputField.setBackground(Color.white);
                }
                secondsInputField.setVisible(true);

                intTextY += (secondsInputField.getSize().height / 2) + (fm.getAscent() / 2);
                offGraphics.drawString("Added seconds per move", secondsInputField.getLocation().x + secondsInputField.getSize().width + 4, intTextY);

                // add the "challenge player" button

                intTextY = secondsInputField.getLocation().y + secondsInputField.getSize().height + 4;
                btnChallengeSelectedPlayer.setLabel("Challenge " + selectedPlayer.strHandle);
                btnChallengeSelectedPlayer.setSize(btnChallengeSelectedPlayer.getPreferredSize());
                btnChallengeSelectedPlayer.setLocation(intRightSideCenter - (btnChallengeSelectedPlayer.getSize().width / 2), intTextY);
                btnChallengeSelectedPlayer.setBackground(bkColor);
                btnChallengeSelectedPlayer.setVisible(true);
            }
        }

        // slap down our image

        g.drawImage(offImage, 0, 0, this);
    }

    public void paint(Graphics g)
    {
        update(g);
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

    private boolean connectToChessServer()
    {
        try
        {
            roomSocket = new Socket(getCodeBase().getHost(), 5555);
        }
        catch (Exception e)
        {
            strInitErrorMessage = e.toString();
            chatInputField.setVisible(false);
            whoIsHereList.setVisible(false);

            bInitError = true;
            return false;
        }

        if (roomSocket == null)
        {
            strInitErrorMessage = "Failed to connect with server";
            chatInputField.setVisible(false);
            whoIsHereList.setVisible(false);
            bInitError = true;
            return false;
        }

        // send the player ID to the server

        sendString(cstrClientSendingPlayerIDKey, getParameter("ID"));

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
            serverInputStream = new BufferedInputStream(roomSocket.getInputStream());
        }
        catch (Exception e)
        {
            try
            {
                roomSocket.close();
            }
            catch (Exception event)
            {
            }

            strInitErrorMessage = e.toString();
            chatInputField.setVisible(false);
            whoIsHereList.setVisible(false);
            bInitError = true;
            return;
        }

        // here is where we monitor the input stream - for the
        // life of the connection

        byte byBuffer[] = new byte[51200];
        for (;;)
        {
            int intLength;

            try
            {
                intLength = serverInputStream.read(byBuffer, 0, byBuffer.length);

                if (intLength == -1)
                {
                    try
                    {
                        roomSocket.close();
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
                    roomSocket.close();
                }
                catch (Exception event)
                {
                    addChatMessage(e.toString());
                }

                addChatMessage(e.toString());
                addChatMessage("\nConnection Closed\n");
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

        if (strMessage.startsWith(cstrServerSendPlayersListKey))
        {
            // we're being sent a list of players - create playerInfoVector

            PlayerInfo playerInfo = new PlayerInfo();
            String strWorkMessage = strMessage.substring(strMessage.indexOf(',') + 1, strMessage.indexOf(cstrSuffix));

            String strCurrentString;
            int intFieldNumber = 0;
            while (strWorkMessage.indexOf(',') != -1)
            {
                strCurrentString = strWorkMessage.substring(0, strWorkMessage.indexOf(','));
                strWorkMessage   = strWorkMessage.substring(strWorkMessage.indexOf(',') + 1, strWorkMessage.length());
                switch (++intFieldNumber)
                {
                    case 1:

                        playerInfo.strHandle = strCurrentString;
                        break;

                    case 2:

                        playerInfo.strRating = strCurrentString.equals("0") ? "0000" : strCurrentString;
                        break;

                    case 3:
                        playerInfo.strWins = strCurrentString;
                        break;

                    case 4:

                        playerInfo.strLosses = strCurrentString;
                        break;

                    case 5:

                        playerInfo.strDraws = strCurrentString;
                        break;

                    case 6:

                        playerInfo.strStatus = strCurrentString;
                        playerInfoVector.addElement((PlayerInfo)playerInfo.clone());
                        intFieldNumber = 0;
                        break;
                }
            }

            // now populate whoIsHereList from playerInfoVector

            for (int i = 0; i < playerInfoVector.size(); ++i)
            {
                playerInfo = (PlayerInfo)playerInfoVector.elementAt(i);

                if (playerInfo.strStatus.startsWith("Vs"))
                {
                    whoIsHereList.add(playerInfo.strRating + " " + playerInfo.strHandle + " " + playerInfo.strStatus);
                }
                else
                {
                    whoIsHereList.add(playerInfo.strRating + " " + playerInfo.strHandle + " - " + playerInfo.strStatus);
                }
            }
        }
        else if (strMessage.startsWith(cstrServerNewPlayerKey))
        {
            // add a player to our vector and display list.
            // first parse strWorkMessage for the player info

            String strWorkMessage = strMessage.substring(strMessage.indexOf(',') + 1, strMessage.indexOf(cstrSuffix));
            PlayerInfo playerInfo = new PlayerInfo();
            String strCurrentString;
            for (int i = 0; i < 6; ++i)
            {
                if (strWorkMessage.indexOf(',') == -1)
                {
                    break;
                }

                strCurrentString = strWorkMessage.substring(0, strWorkMessage.indexOf(','));
                strWorkMessage   = strWorkMessage.substring(strWorkMessage.indexOf(',') + 1, strWorkMessage.length());
                switch (i)
                {
                    case 0:

                        playerInfo.strHandle = strCurrentString;
                        break;

                    case 1:

                        playerInfo.strRating = strCurrentString.equals("0") ? "0000" : strCurrentString;
                        break;

                    case 2:
                        playerInfo.strWins = strCurrentString;
                        break;

                    case 3:

                        playerInfo.strLosses = strCurrentString;
                        break;

                    case 4:

                        playerInfo.strDraws = strCurrentString;
                        break;

                    case 5:

                        playerInfo.strStatus = strCurrentString;

                        // add the player to our vector and display list

                        playerInfoVector.addElement((PlayerInfo)playerInfo.clone());
                        whoIsHereList.add(playerInfo.strRating + " " + playerInfo.strHandle + " - " + playerInfo.strStatus);

                        // alert the client that the new player has joined the room

                        addChatMessage("");
                        addChatMessage("   ***   " + playerInfo.strHandle + " has joined the Chess Room   ***");
                        addChatMessage("");

                        break;
                }
            }
        }
        else if (strMessage.startsWith(cstrServerUpdatePlayerKey))
        {
            // add a player to our vector and display list.
            // first parse strWorkMessage for the player info

            String strWorkMessage = strMessage.substring(strMessage.indexOf(',') + 1, strMessage.indexOf(cstrSuffix));
            PlayerInfo playerInfo = new PlayerInfo();
            String strCurrentString;
            for (int i = 0; i < 6; ++i)
            {
                if (strWorkMessage.indexOf(',') == -1)
                {
                    break;
                }

                strCurrentString = strWorkMessage.substring(0, strWorkMessage.indexOf(','));
                strWorkMessage   = strWorkMessage.substring(strWorkMessage.indexOf(',') + 1, strWorkMessage.length());
                switch (i)
                {
                    case 0:

                        playerInfo.strHandle = strCurrentString;
                        break;

                    case 1:

                        playerInfo.strRating = strCurrentString.equals("0") ? "0000" : strCurrentString;
                        break;

                    case 2:
                        playerInfo.strWins = strCurrentString;
                        break;

                    case 3:

                        playerInfo.strLosses = strCurrentString;
                        break;

                    case 4:

                        playerInfo.strDraws = strCurrentString;
                        break;

                    case 5:

                        playerInfo.strStatus = strCurrentString;

                        // now find the index of this player then updte his playerInfoVector and whoIsHerelist

                        PlayerInfo currentPlayerInfo = new PlayerInfo();
                        for (int j = 0; j < playerInfoVector.size(); ++j)
                        {
                            currentPlayerInfo = (PlayerInfo)playerInfoVector.elementAt(j);

                            if (currentPlayerInfo.strHandle.equals(playerInfo.strHandle))
                            {
                                playerInfoVector.setElementAt(playerInfo.clone(), j);
                                whoIsHereList.replaceItem(playerInfo.strRating + " " + playerInfo.strHandle + " - " + playerInfo.strStatus, j);
                            }
                        }

                        break;
                }
            }
        }
        else if (strMessage.startsWith(cstrServerRemovePlayerKey))
        {
            String     strHandle = strMessage.substring(strMessage.indexOf(',') + 1, strMessage.indexOf(cstrSuffix));
            PlayerInfo playerInfo;

            // find the given handle in the playerInfoVector and remove
            // both that element from both the vector and its
            // corresponding whoIsHereList.

            for (int i = 0; i < playerInfoVector.size(); ++i)
            {
                playerInfo = (PlayerInfo)playerInfoVector.elementAt(i);

                if (playerInfo.strHandle.equals(strHandle))
                {
                    playerInfoVector.removeElementAt(i);
                    whoIsHereList.remove(i);

                    // let the client know that we just removed the given player

                    addChatMessage("");
                    addChatMessage("   ***   " + strHandle + " has left the Chess Room   ***");
                    addChatMessage("");
                }
            }

            if (selectedPlayer != null && strHandle.equals(selectedPlayer.strHandle))
            {
                selectedPlayer = null;
                repaint();
            }
        }
        else if (strMessage.startsWith(cstrServerChallengeRecievedKey))
        {
            // remove the string suffix

            strMessage = strMessage.substring(0, strMessage.indexOf(cstrSuffix));

            // parse strMessage with StringTokenizer

            StringTokenizer st = new StringTokenizer(strMessage, ",");

            // discard the first token - it's our key

            st.nextToken();

            // build the challenge string for the "challenged" list
            // and corresponding vector

            String strToOrFrom   = "";
            String strHandle     = "";
            String strMinutes    = "";
            String strSeconds    = "";

            for (int i = 0; st.hasMoreTokens(); ++i)
            {
                switch (i)
                {
                    case 0:

                        // either "TO" or "FROM"

                        strToOrFrom = st.nextToken();
                        break;

                    case 1:

                        // the players's handle

                        strHandle = st.nextToken();
                        break;

                    case 2:

                        // the initial game time in minutes
                        // "0" indicates unlimited time

                        strMinutes = st.nextToken();
                        break;

                    case 3:

                        // Addition seconds per move

                        strSeconds += st.nextToken();
                        break;

                    default:

                        break;
                }
            }

            // build the Challenge String

            String strChallenge = strHandle;

            if (strMinutes.equals("0"))
            {
                strChallenge += " - No Clock";
            }
            else
            {
                strChallenge += " - Time: " + strMinutes + "/" + strSeconds;
            }

            // now add this string to the proper list

            if (strToOrFrom.equals("TO"))
            {
                // we can only challenge and recieve challenges from
                // one player at a time - replace the previous
                // challenge if necessary

                boolean bReplaced = false;

                for (int i = 0; i < challengeToList.getItemCount(); ++i)
                {
                    if (challengeToList.getItem(i).startsWith(strHandle))
                    {
                        challengeToList.replaceItem(strChallenge, i);
                        bReplaced = true;
                        break;
                    }
                }

                // if this is a new challenge - add it

                if (!bReplaced)
                {
                    challengeToList.add(strChallenge);
                }
            }
            else if (strToOrFrom.equals("FROM"))
            {
                // we can only challenge and recieve challenges from
                // one player at a time - replace the previous
                // challenge if necessary

                boolean bReplaced = false;

                for (int i = 0; i < challengeFromList.getItemCount(); ++i)
                {
                    if (challengeFromList.getItem(i).startsWith(strHandle))
                    {
                        challengeFromList.replaceItem(strChallenge, i);
                        bReplaced = true;
                        break;
                    }
                }

                // if this is a new challenge - add it

                if (!bReplaced)
                {
                    challengeFromList.add(strChallenge);
                }
            }
        }
        else if (strMessage.startsWith(cstrServerChallengeRemovedKey))
        {
            // remove the string suffix

            strMessage = strMessage.substring(0, strMessage.indexOf(cstrSuffix));

            // parse strMessage with StringTokenizer

            StringTokenizer st = new StringTokenizer(strMessage, ",");

            // discard the first token - it's our key

            st.nextToken();

            // build the challenge string for the "challenged" list
            // and corresponding vector

            String strToOrFrom   = "";
            String strHandle     = "";

            for (int i = 0; st.hasMoreTokens(); ++i)
            {
                switch (i)
                {
                    case 0:

                        // either "TO" or "FROM"

                        strToOrFrom = st.nextToken();
                        break;

                    case 1:

                        // the players's handle

                        strHandle = st.nextToken();
                        break;

                    default:

                        break;
                }
            }

            // now find the challenge fromt he proper list and
            // remove it

            if (strToOrFrom.equals("TO"))
            {
                for (int i = 0; i < challengeToList.getItemCount(); ++i)
                {
                    if (challengeToList.getItem(i).startsWith(strHandle))
                    {
                        challengeToList.remove(i);
                        break;
                    }
                }
            }
            else if (strToOrFrom.equals("FROM"))
            {
                for (int i = 0; i < challengeFromList.getItemCount(); ++i)
                {
                    if (challengeFromList.getItem(i).startsWith(strHandle))
                    {
                        challengeFromList.remove(i);
                        break;
                    }
                }
            }
        }
        else if (strMessage.startsWith(cstrerverWelcomeKey))
        {
            strMyHandle = strMessage.substring(strMessage.indexOf(',') + 1, strMessage.indexOf(cstrSuffix));

            addChatMessage("");
            addChatMessage("   ***   Welcome to the Chess Room " + strMyHandle + "    ***");
            addChatMessage("");
        }
        else if (strMessage.startsWith(cstrServerStartGameKey))
        {
            // remove the string suffix

            strMessage = strMessage.substring(0, strMessage.indexOf(cstrSuffix));

            // parse strMessage with StringTokenizer

            StringTokenizer st = new StringTokenizer(strMessage, ",");

            // discard the first token - it's our key

            st.nextToken();

            // build the challenge string for the "challenged" list
            // and corresponding vector

            String strWhiteHandle = "";
            String strBlackHandle = "";
            String strWhiteRating = "";
            String strBlackRating = "";
            int intMinutes = 0;
            int intSeconds = 0;


            for (int i = 0; st.hasMoreTokens(); ++i)
            {
                switch (i)
                {
                    case 0:

                        strWhiteHandle = st.nextToken();
                        break;

                    case 1:

                        strBlackHandle = st.nextToken();
                        break;

                    case 2:

                        strWhiteRating = st.nextToken();
                        break;

                    case 3:

                        strBlackRating = st.nextToken();
                        break;

                    case 4:

                        String strMinutes = st.nextToken();
                        intMinutes = Integer.valueOf(strMinutes).intValue();
                        break;

                    case 5:

                        String strSeconds = st.nextToken();
                        intSeconds = Integer.valueOf(strSeconds).intValue();
                        break;

                    default:

                        break;
                }
            }

            addChatMessage("");
            addChatMessage("   ***   Chess game starting - please wait...   ***");

            chessBoardFrame = new ChessBoardFrame(strMyHandle,
                                                  strWhiteHandle, strBlackHandle,
                                                  strWhiteRating, strBlackRating,
                                                  intMinutes, intSeconds, tap2, getCodeBase());

            chessBoardFrame.show();
        }
        else
        {
            // no key - just display the message

            strMessage.trim();
            addChatMessage(strMessage.substring(0, strMessage.indexOf(cstrSuffix)));
        }

        // process the sub-string if necessary

        if (!strSubMessage.equals(""))
        {
            processServerMessage(strSubMessage);
        }
    }

    private void sendString(String strKey, String strString)
    {
        String strBuiltString;

        if (strKey != "")
        {
            strBuiltString = strKey + ',' + strString;
        }
        else
        {
            strBuiltString = strString;
        }

        try
        {
            roomSocket.getOutputStream().write(strBuiltString.getBytes());
        }
        catch (Exception e)
        {
            addChatMessage(e.toString());
        }
    }

    // this process list selections

    public void itemStateChanged(ItemEvent e)
    {
        Object object = e.getSource();

        if (object == whoIsHereList)
        {
            if (whoIsHereList.getSelectedIndex() != -1)
            {
                selectedPlayer = (PlayerInfo)playerInfoVector.elementAt(whoIsHereList.getSelectedIndex());
            }

            repaint();
        }
        if (object == chbxSetTimeControl)
        {
            if (!chbxSetTimeControl.getState())
            {
                minutesInputField.setEditable(false);
                secondsInputField.setEditable(false);
                minutesInputField.setBackground(new Color(0xb9b9b9));
                secondsInputField.setBackground(new Color(0xb9b9b9));
            }
            else
            {
                minutesInputField.setEditable(true);
                secondsInputField.setEditable(true);
                minutesInputField.setBackground(Color.white);
                secondsInputField.setBackground(Color.white);
            }
        }

    }

    // this processes our applet's controls

    public void actionPerformed(ActionEvent event)
    {
        Object object = event.getSource();

        if (object == chatInputField && chatInputField.getText().length() > 0)
        {
            // send the chat input message to the server
            // don't let the message begin with '/'

            String strString = chatInputField.getText();
            if (strString.charAt(0) == '/')
            {
                strString = strString.substring(1, strString.length());
            }

            sendString("", strString);

            chatInputField.setText("");
        }
        else if (object == btnViewGame)
        {
            addChatMessage("");
            addChatMessage("   ***   Chess game starting - please wait...   ***");

            chessBoardFrame = new ChessBoardFrame(strMyHandle, selectedPlayer.strHandle,
                                                  null, null, null, 0, 0, tap2, getCodeBase());

            chessBoardFrame.show();
        }
        else if (object == btnChallengeSelectedPlayer)
        {
            String strString;

            if (!chbxSetTimeControl.getState())
            {
                // no time control wanted

                strString = selectedPlayer.strHandle + ",0,0";
            }
            else
            {
                // time control wanted - validate the time control

                int intMinutes;
                try
                {
                    intMinutes = Integer.valueOf(minutesInputField.getText()).intValue();

                    // mintues must be bewtween 1 and 180

                    if (intMinutes == 0 || intMinutes > 180)
                    {
                        intMinutes = -1;
                    }

                }
                catch (Exception e)
                {
                    // invalid integer format

                    intMinutes = -1;
                }

                int intSeconds;
                try
                {
                    intSeconds = Integer.valueOf(secondsInputField.getText()).intValue();

                    // seconds must be bewtween 1 and 600

                    if (intSeconds > 600)
                    {
                        intSeconds = -1;
                    }

                }
                catch (Exception e)
                {
                    // invalid integer format

                    if (secondsInputField.getText().equals(""))
                    {
                        // an empty string in the seconds field is
                        // valid and considered zero

                        intSeconds = 0;
                    }
                    else
                    {
                        intSeconds = -1;
                    }
                }


                if (intMinutes == -1 || intSeconds == -1)
                {
                    // invalid chess clock settings - display error setting to the user

                    addChatMessage("  Challenge Error: Invalid chess clock setting");
                    addChatMessage("   - Minutes must be between 1 and 180");
                    addChatMessage("   - Seconds can not be greater than 600");
                    return;
                }

                strString = selectedPlayer.strHandle + "," + intMinutes + "," + intSeconds;
            }

            // send the challenge string to the server

            sendString(cstrClientChallengeKey, strString);
        }
        else if (object == challengeFromList)
        {
            // get the challenger's handle

            String strHandle = challengeFromList.getItem(challengeFromList.getSelectedIndex());
            strHandle = strHandle.substring(0, strHandle.indexOf(' '));

            // now tell the server we've accepted this guy's challenge

            sendString(cstrClientAcceptChallengeKey, strHandle);
        }
    }
}

class PlayerInfo implements Cloneable
{
    String strHandle;
    String strRating;
    String strWins;
    String strLosses;
    String strDraws;
    String strStatus;

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
