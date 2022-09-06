package de.fhac.mazenet.server.userinterface.betterUI;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Card;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.game.Position;
import de.fhac.mazenet.server.generated.BoardData;
import de.fhac.mazenet.server.generated.CardData;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;
import de.fhac.mazenet.server.userinterface.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BetterUI extends JFrame implements UI {

    private static final long serialVersionUID = 2L;
    private static final boolean animateMove = true;
    private static final boolean animateShift = true;
    private static final int animationFrames = 10;
    static BetterUI instance;
    public GraphicalCardBuffered shiftCard;
    int currentPlayer;
    UIBoard uiboard = new UIBoard();
    StatsPanel statsPanel = new StatsPanel();
    Object animationFinished = new Object();
    Timer animationTimer;
    AnimationProperties animationProperties = null;
    JSplitPane splitPane;
    private int animationState = 0;
    private JMenu MPlayerSettings;
    private JMenuItem MIStart;
    private JMenuItem MIStop;
    private JMenu jMenu;
    private JMenuBar jMenuBar;
    private StreamToTextArea log;
    private JRadioButtonMenuItem[] MIPlayerSelection;

    private BetterUI() {
        // Eigenname
        super("Better MazeNet UI");
        {
            jMenuBar = new JMenuBar();
            setJMenuBar(jMenuBar);
            {
                jMenu = new JMenu();
                jMenuBar.add(jMenu);
                jMenu.setText(Messages.getString("BetterUI.server"));
                {
                    MIStart = new JMenuItem();
                    jMenu.add(MIStart);
                    MIStart.setText(Messages.getString("BetterUI.start"));
                    MIStart.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            MIStartActionPerformed(evt);
                        }
                    });
                    // MIStart.addActionListener(new StartAction(this) );
                }
                {
                    MIStop = new JMenuItem();
                    jMenu.add(MIStop);
                    MIStop.setText(Messages.getString("BetterUI.stop"));
                    MIStop.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            MIStopActionPerformed(evt);
                        }
                    });
                    MIStart.setEnabled(true);
                    MIStop.setEnabled(false);
                }
            }
            {
                MPlayerSettings = new JMenu();
                jMenuBar.add(MPlayerSettings);
                MPlayerSettings.setText(Messages.getString("BetterUI.playerCount"));
                MIPlayerSelection = new JRadioButtonMenuItem[4];
                {
                    MIPlayerSelection[0] = new JRadioButtonMenuItem();
                    MPlayerSettings.add(MIPlayerSelection[0]);
                    MIPlayerSelection[0].setText(Messages.getString("BetterUI.OnePlayer"));
                    MIPlayerSelection[0].addActionListener(evt -> Settings.NUMBER_OF_PLAYERS = 1);
                }
                {
                    MIPlayerSelection[1] = new JRadioButtonMenuItem();
                    MPlayerSettings.add(MIPlayerSelection[1]);
                    MIPlayerSelection[1].setText(Messages.getString("BetterUI.TwoPlayer"));
                    MIPlayerSelection[1].addActionListener(evt -> Settings.NUMBER_OF_PLAYERS = 2);
                }
                {
                    MIPlayerSelection[2] = new JRadioButtonMenuItem();
                    MPlayerSettings.add(MIPlayerSelection[2]);
                    MIPlayerSelection[2].setText(Messages.getString("BetterUI.ThreePlayer"));
                    MIPlayerSelection[2].addActionListener(evt -> Settings.NUMBER_OF_PLAYERS = 3);
                }
                {
                    MIPlayerSelection[3] = new JRadioButtonMenuItem();
                    MPlayerSettings.add(MIPlayerSelection[3]);
                    MIPlayerSelection[3].setText(Messages.getString("BetterUI.FourPlayer"));
                    MIPlayerSelection[3].addActionListener(evt -> Settings.NUMBER_OF_PLAYERS = 4);

                }
                ButtonGroup spielerAnz = new ButtonGroup();
                for (JRadioButtonMenuItem item : MIPlayerSelection) {
                    spielerAnz.add(item);

                }
                MIPlayerSelection[Settings.NUMBER_OF_PLAYERS - 1].setSelected(true);

            }
        }
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, uiboard, statsPanel);
        this.add(splitPane, BorderLayout.CENTER);
        this.pack();
        int width = Integer.parseInt(Settings.WINDOW_SIZE.split("x")[0]);
        int height = Integer.parseInt(Settings.WINDOW_SIZE.split("x")[1]);
        this.setSize(width, height);
        splitPane.setResizeWeight(0.7);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // hatte ohne InvokeLater keinen Effekt
                splitPane.setDividerLocation(0.8);
                log = new StreamToTextArea(new JTextArea());
                log.getTextArea().setEditable(false);
                log.getTextArea().add(new JScrollBar());
                Debug.addDebugger(log, Settings.DEBUGLEVEL);
            }
        });

    }

    private static Color colorForPlayer(int playerID) {
        switch (playerID) {
            case 0:
                return Color.yellow;
            case 1:
                return Color.GREEN;
            case 2:
                return Color.BLACK;
            case 3:
                return Color.RED;
            case 4:
                return Color.BLUE;
            default:
                throw new IllegalArgumentException(Messages.getString("BetterUI.UInotPreparedForPlayerID"));
        }
    }

    public static BetterUI getInstance() {
        if (instance == null)
            instance = new BetterUI();
        return instance;
    }

    private void MIStopActionPerformed(ActionEvent evt) {
        Debug.print("MIStop.actionPerformed, event=" + evt, DebugLevel.DEBUG);
        Server.getInstance().stopGame();
        MIStart.setEnabled(true);
        MIStop.setEnabled(false);
    }

    private void MIStartActionPerformed(ActionEvent evt) {
        Debug.print("MIStart.actionPerformed, event=" + evt, DebugLevel.DEBUG);
        // g.parsArgs();
        statsPanel.removeAll();
        statsPanel.initiated = false;
        statsPanel.repaint();
        log.getTextArea().setText("");
        statsPanel.setLayout(new BorderLayout());
        statsPanel.add(log.getTextArea());
        Server.getInstance().startGame();
        MIStart.setEnabled(false);
        MIStop.setEnabled(true);
    }

    @Override
    public void displayMove(MoveMessageData moveMessage, Board boardAfterMove, long moveDelay, long shiftDelay, boolean treasureReached) {
        // Die Dauer von shiftDelay bezieht sich auf den kompletten Shift und
        // nicht auf einen einzelnen Frame
        shiftDelay /= animationFrames;
        // shiftCard.setCard(new Card(mm.getShiftCard()));
        if (animateShift) {
            uiboard.board.setShiftCard(moveMessage.getShiftCard());
            animationTimer = new Timer((int) shiftDelay, new ShiftAnimationTimerOperation());
            animationProperties = new AnimationProperties(new Position(moveMessage.getShiftPosition()));
            synchronized (animationFinished) {
                animationTimer.start();
                try {
                    animationFinished.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        uiboard.board.proceedShift(moveMessage);
        Position oldPlayerPos = new Position(uiboard.board.findPlayer(currentPlayer));
        uiboard.setBoard(boardAfterMove);
        // repaint benoetigt alte Karten bleiben sonst,
        // bis zur nächsten Schiebe-Animation sichtbar
        animationTimer = new Timer((int) moveDelay,
                new MoveAnimationTimerOperation(uiboard.board, oldPlayerPos, new Position(moveMessage.getNewPinPos())));
        uiboard.repaint();
        // muss nach repaint() stehen, sonst flickering!
        shiftCard.setCard(new Card(boardAfterMove.getShiftCard()));
        if (animateMove) {
            // Falls unser Spieler sich selbst verschoben hat.
            AnimationProperties props = new AnimationProperties(new Position(moveMessage.getShiftPosition()));
            if (props.vertikal) {
                if (oldPlayerPos.getCol() == props.shiftPosition.getCol()) {
                    oldPlayerPos.setRow((7 + oldPlayerPos.getRow() + props.direction) % 7);
                }
            } else {
                if (oldPlayerPos.getRow() == props.shiftPosition.getRow()) {
                    oldPlayerPos.setCol((7 + oldPlayerPos.getCol() + props.direction) % 7);
                }
            }
            synchronized (animationFinished) {
                animationTimer.start();
                try {
                    animationFinished.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (treasureReached) {
            ImageResources.treasureFound(boardAfterMove.getTreasure().value());
        }
    }

    @Override
    public void updatePlayerStatistics(List<Player> statistics, Integer currentPlayerID) {
        statsPanel.update(statistics, currentPlayerID);
    }

    @Override
    public void init(Board board) {
        ImageResources.reset();
        uiboard.setBoard(board);
        uiboard.repaint();
        this.setVisible(true);
    }


    @Override
    public void gameEnded(Client winner) {
        if (winner != null) {
            JOptionPane.showMessageDialog(this,
                    String.format(Messages.getString("BetterUI.playerIDwon"), winner.getName()
                            , winner.getId()));
        }
        MIStart.setEnabled(true);
        MIStop.setEnabled(false);
    }

    private static class Pathfinding {
        public static int[][] findShortestPath(Board b, Position startPos, Position endPos) {
            // Dijkstra
            boolean[][] visited = new boolean[7][7];
            int[][] weglen = new int[7][7];
            int[][] pfad = new int[7][7];
            for (int y = 0; y < 7; ++y) {
                for (int x = 0; x < 7; ++x) {
                    weglen[y][x] = Integer.MAX_VALUE;
                }
            }
            int currentX = startPos.getCol();
            int currentY = startPos.getRow();
            weglen[currentY][currentX] = 0;
            while (true) {
                visited[currentY][currentX] = true;
                if (currentX > 0 && b.getCard(currentY, currentX).getOpenings().isLeft()
                        && b.getCard(currentY, currentX - 1).getOpenings().isRight()) {
                    if (weglen[currentY][currentX - 1] > weglen[currentY][currentX] + 1) {
                        weglen[currentY][currentX - 1] = weglen[currentY][currentX] + 1;
                        pfad[currentY][currentX - 1] = currentY * 7 + currentX;
                    }
                }
                if (currentY > 0 && b.getCard(currentY, currentX).getOpenings().isTop()
                        && b.getCard(currentY - 1, currentX).getOpenings().isBottom()) {
                    if (weglen[currentY - 1][currentX] > weglen[currentY][currentX] + 1) {
                        weglen[currentY - 1][currentX] = weglen[currentY][currentX] + 1;
                        pfad[currentY - 1][currentX] = currentY * 7 + currentX;
                    }
                }
                if (currentX < 6 && b.getCard(currentY, currentX).getOpenings().isRight()
                        && b.getCard(currentY, currentX + 1).getOpenings().isLeft()) {
                    if (weglen[currentY][currentX + 1] > weglen[currentY][currentX] + 1) {
                        weglen[currentY][currentX + 1] = weglen[currentY][currentX] + 1;
                        pfad[currentY][currentX + 1] = currentY * 7 + currentX;
                    }
                }
                if (currentY < 6 && b.getCard(currentY, currentX).getOpenings().isBottom()
                        && b.getCard(currentY + 1, currentX).getOpenings().isTop()) {
                    if (weglen[currentY + 1][currentX] > weglen[currentY][currentX] + 1) {
                        weglen[currentY + 1][currentX] = weglen[currentY][currentX] + 1;
                        pfad[currentY + 1][currentX] = currentY * 7 + currentX;
                    }
                }
                {
                    int currentMinWegLen = Integer.MAX_VALUE;
                    for (int y = 6; y >= 0; --y) {
                        for (int x = 6; x >= 0; --x) {
                            if (!visited[y][x] && weglen[y][x] < currentMinWegLen) {
                                currentMinWegLen = weglen[y][x];
                                currentX = x;
                                currentY = y;
                            }
                        }
                    }
                    if (currentMinWegLen == Integer.MAX_VALUE)
                        break;
                }
            }
            currentX = endPos.getCol();
            currentY = endPos.getRow();
            int anzahlWegpunkte = weglen[currentY][currentX] + 1;
            // Weg ist ein Array von x und y werten
            int weg[][] = new int[anzahlWegpunkte][2];
            int i = anzahlWegpunkte - 1;
            while (i > 0) {
                weg[i--] = new int[]{currentX, currentY};
                int buf = pfad[currentY][currentX];
                currentX = buf % 7;
                currentY = buf / 7;
            }
            weg[0] = new int[]{currentX, currentY};
            return weg;
        }
    }

    private class UIBoard extends JPanel {
        private static final long serialVersionUID = 2L;
        Board board;
        Image images[][] = new Image[7][7];
        Card[][] cards = new Card[7][7];
        private int pixelsPerField;

        public void setBoard(Board b) {
            if (b == null) {
                this.board = null;
                return;
            }
            this.board = (Board) b.clone();
            int columnIndex = 0, rowIndex;
            for (BoardData.Row r : b.getRow()) {
                rowIndex = 0;
                for (CardData ct : r.getCol()) {
                    Card card = new Card(ct);
                    cards[columnIndex][rowIndex] = card;
                    images[columnIndex][rowIndex] = ImageResources.getImage(card.getShape().toString() + card.getOrientation().value());
                    if (cards[columnIndex][rowIndex].getTreasure() != null) {
                        ImageResources.getImage(cards[columnIndex][rowIndex].getTreasure().value());
                    }
                    rowIndex++;
                }
                columnIndex++;
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null)
                return;
            int width = this.getWidth();
            int height = this.getHeight();
            width = height = Math.min(width, height);
            width = height -= width % 7;
            pixelsPerField = width / 7;

            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < 7; x++) {
                    int topLeftY = pixelsPerField * y;
                    int topLeftX = pixelsPerField * x;
                    if (animationProperties != null) {
                        if (animationProperties.vertikal && x == animationProperties.shiftPosition.getCol()) {
                            topLeftY += animationProperties.direction
                                    * (pixelsPerField * animationState / animationFrames);
                        } else if (!animationProperties.vertikal && y == animationProperties.shiftPosition.getRow()) {
                            topLeftX += animationProperties.direction
                                    * (pixelsPerField * animationState / animationFrames);
                        }
                    }

                    g.drawImage(images[y][x], topLeftX, topLeftY, pixelsPerField, pixelsPerField, null);
                    if (cards[y][x] != null) {

                        if (cards[y][x].getTreasure() != null) {
                            g.drawImage(ImageResources.getImage(cards[y][x].getTreasure().value()),
                                    topLeftX + pixelsPerField / 4, topLeftY + pixelsPerField / 4, pixelsPerField / 2,
                                    pixelsPerField / 2, null);
                        }
                        // paint player pins
                        // TODO should be named getPlayerIDs()
                        List<Integer> pins = new ArrayList<>(cards[y][x].getPin().getPlayerID());
                        for (Integer playerID : pins) {
                            g.setColor(colorForPlayer(playerID));
                            g.fillOval(topLeftX + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) / 2),
                                    topLeftY + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) % 2),
                                    pixelsPerField / 4, pixelsPerField / 4);

                            g.setColor(Color.WHITE);
                            g.drawOval(topLeftX + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) / 2),
                                    topLeftY + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) % 2),
                                    pixelsPerField / 4, pixelsPerField / 4);
                            centerStringInRect((Graphics2D) g, playerID.toString(),
                                    topLeftX + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) / 2),
                                    topLeftY + pixelsPerField / 4 + pixelsPerField / 4 * ((playerID - 1) % 2),
                                    pixelsPerField / 4, pixelsPerField / 4);
                        }
                    } else {
                        System.out.println(String.format(Messages.getString("BetterUI.cardIsNull"), x, y));
                    }
                }
            }
            // Zeichnen der eingeschobenen karte in der animation
            if (animationProperties != null) {
                int topLeftY = pixelsPerField * (animationProperties.shiftPosition.getRow()
                        - (animationProperties.vertikal ? animationProperties.direction : 0));
                int topLeftX = pixelsPerField * (animationProperties.shiftPosition.getCol()
                        - (!animationProperties.vertikal ? animationProperties.direction : 0));
                if (animationProperties.vertikal) {
                    topLeftY += animationProperties.direction * (pixelsPerField * animationState / animationFrames);
                } else {
                    topLeftX += animationProperties.direction * (pixelsPerField * animationState / animationFrames);
                }
                Card card = new Card(board.getShiftCard());
                g.drawImage(ImageResources.getImage(card.getShape().toString() + card.getOrientation().value()),
                        topLeftX, topLeftY, pixelsPerField, pixelsPerField, null);
                if (card.getTreasure() != null) {
                    g.drawImage(ImageResources.getImage(card.getTreasure().value()), topLeftX + pixelsPerField / 4,
                            topLeftY + pixelsPerField / 4, pixelsPerField / 2, pixelsPerField / 2, null);
                }
                g.setColor(Color.YELLOW);
                g.drawRect(topLeftX, topLeftY, pixelsPerField, pixelsPerField);
            }
        }

        public int getPixelsPerField() {
            return pixelsPerField;
        }

        private void centerStringInRect(Graphics2D g2d, String s, int x, int y, int height, int width) {
            Rectangle size = g2d.getFontMetrics().getStringBounds(s, g2d).getBounds();
            float startX = (float) (width / 2 - size.getWidth() / 2);
            float startY = (float) (height / 2 - size.getHeight() / 2);
            g2d.drawString(s, startX + x - size.x, startY + y - size.y);
        }

    }

    private class StatsPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        boolean initiated = false;
        Map<Integer, JLabel> statLabels = new TreeMap<>();
        Map<Integer, JLabel> currentPlayerLabels = new TreeMap<>();
        Map<Integer, JLabel> treasureImages = new TreeMap<>();
        private JScrollPane scrollPane;

        public void update(List<Player> stats, int current) {
            if (initiated) {
                currentPlayerLabels.get(currentPlayer).setText("");
                currentPlayer = current;
                currentPlayerLabels.get(currentPlayer).setText(">");
                for (Player p : stats) {
                    statLabels.get(p.getId()).setText(String.valueOf(p.treasuresToGo() - 1));
                    treasureImages.get(p.getId()).setIcon(new ImageIcon(ImageResources
                            .getImage(p.getCurrentTreasure().value()).getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
                }

            } else {
                // Beim ersten mal erzeugen wir die GUI.
                this.removeAll();
                this.repaint();
                initiated = true;
                GridBagConstraints gc = new GridBagConstraints();
                gc.gridx = GridBagConstraints.RELATIVE;
                gc.anchor = GridBagConstraints.WEST;
                gc.insets = new Insets(0, 0, 0, 0);
                this.setLayout(new GridBagLayout());

                shiftCard = new GraphicalCardBuffered();

                // GridBagConstraints(gridx, gridy, gridwidth, gridheight,
                // weightx, weighty, anchor, fill, insets, ipadx, ipady);
                this.add(shiftCard,
                        new GridBagConstraints(0, 0, 5, 1, 0.5, 0.3, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                new Insets(0, 0, 0, 0), uiboard.getPixelsPerField(), uiboard.getPixelsPerField()));
                // this.getComponentAt(0, 0).get
                for (Player p : stats) {
                    gc.gridy = p.getId();
                    JLabel currentPlayerLabel = new JLabel();
                    currentPlayerLabels.put(p.getId(), currentPlayerLabel);

                    JLabel playerIDLabel = new JLabel(String.valueOf(p.getId()) + ".   ");
                    JLabel playerNameLabel = new JLabel(p.getName());
                    playerNameLabel.setForeground(colorForPlayer(p.getId()));

                    JLabel statLabel = new JLabel(String.valueOf(p.treasuresToGo()));
                    statLabels.put(p.getId(), statLabel);

                    JLabel treasureImage = new JLabel(
                            new ImageIcon(ImageResources.getImage(p.getCurrentTreasure().value())));
                    treasureImages.put(p.getId(), treasureImage);

                    gc.ipadx = 5;
                    this.add(currentPlayerLabel, gc);
                    gc.ipadx = 0;
                    this.add(playerIDLabel, gc);
                    this.add(playerNameLabel, gc);
                    // TODO find out how to realign Image inside the JLabel,
                    // otherwise anchor has no effekt for alligning the
                    // treasure icon
                    // gc.anchor = GridBagConstraints.EAST;
                    this.add(treasureImage, gc);
                    this.add(statLabel, gc);
                    // gc.anchor = GridBagConstraints.WEST;

                }
                currentPlayer = current;
                currentPlayerLabels.get(currentPlayer).setText(">");

                scrollPane = new JScrollPane(log.getTextArea());
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(scrollPane);

                this.add(panel,
                        new GridBagConstraints(0, 5, 5, 1, 0.5, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                new Insets(0, 0, 0, 0), uiboard.getPixelsPerField(), uiboard.getPixelsPerField()));
            }
        }
    }

    private class AnimationProperties {
        public final boolean vertikal;
        public final Position shiftPosition;
        public final int direction;

        public AnimationProperties(Position shiftPosition) {
            this.shiftPosition = shiftPosition;
            if (shiftPosition.getCol() == 6 || shiftPosition.getCol() == 0) {
                vertikal = false;
                direction = shiftPosition.getCol() == 0 ? 1 : -1;
            } else if (shiftPosition.getRow() == 6 || shiftPosition.getRow() == 0) {
                vertikal = true;
                direction = shiftPosition.getRow() == 0 ? 1 : -1;
            } else {
                throw new IllegalArgumentException(Messages.getString("BetterUI.cantShift"));
            }
        }
    }

    private class ShiftAnimationTimerOperation implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            animationState++;
            // ohne repaint keine Animation sondern sprunghaftes Schieben
            uiboard.repaint();
            if (animationState == animationFrames) {
                animationState = 0;
                animationTimer.stop();
                animationTimer = null;
                animationProperties = null;
                synchronized (animationFinished) {
                    animationFinished.notify();
                }
            }
        }
    }

    private class MoveAnimationTimerOperation implements ActionListener {
        int[][] points;
        int i = 0;

        public MoveAnimationTimerOperation(Board b, Position startPos, Position endPos) {
            points = Pathfinding.findShortestPath(b, startPos, endPos);
            // bei remove kein Autoboxing benutzen!
            // https://stackoverflow.com/questions/49617334/why-no-autoboxing-while-removing-primitive-type-from-a-list-in-java
            uiboard.cards[endPos.getRow()][endPos.getCol()].getPin().getPlayerID().remove(new Integer(currentPlayer));
            uiboard.cards[startPos.getRow()][startPos.getCol()].getPin().getPlayerID().add(currentPlayer);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (i + 1 == points.length) {
                synchronized (animationFinished) {
                    animationTimer.stop();
                    animationTimer = null;
                    animationFinished.notify();
                }
                return;
            }
            // KEIN AUTOBOXING!
            uiboard.cards[points[i][1]][points[i][0]].getPin().getPlayerID().remove(new Integer(currentPlayer));
            i++;
            uiboard.cards[points[i][1]][points[i][0]].getPin().getPlayerID().add(currentPlayer);
            // Wird zum animieren der Spielfigur benoetigt
            if (i != 0) { // verbessert den Uebergang vom Schieben zum Ziehen
                uiboard.repaint();
            }
        }
    }

    @Override
    public String toString() {
        return "BetterUI";
    }

}