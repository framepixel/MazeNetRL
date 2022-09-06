package de.fhac.mazenet.server.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.generated.ClientRole;
import de.fhac.mazenet.server.generated.Errortype;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.generated.Treasure;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.networking.MazeComMessageFactory;
import de.fhac.mazenet.server.timeouts.TimeOutManager;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;
import de.fhac.mazenet.server.userinterface.UI;

public class Game extends Thread {
    public CyclicBarrier waitForPlayer;

    private TimeOutManager timeOutManager;
    private Board board;
    /**
     * -2 Spiel wurde gestoppt
     * -1 Spiel laeuft noch (Initialwert)
     * 0 unbenutzt
     * 1-4 SiegerID
     */
    private Integer winner;
    private UI userinterface;
    /**
     * beinhaltet die Spieler, die mit dem Server verbunden sind und die durch
     * die id zugreifbar sind
     */
    private HashMap<Integer, Player> players;
    private Status gameStatus;

    public Game() {
        winner = -1;
        players = new HashMap<>();
        timeOutManager = new TimeOutManager(this);
        gameStatus = Status.NOGAMEACTIVE;
    }

    public Status getGameStatus() {
        return gameStatus;
    }

    public TimeOutManager getTimeOutManager() {
        return timeOutManager;
    }

    public void prepareGame() {
        players.clear();
        winner = -1;

        //Spieler aus der Liste der clienten filtern
        for (Future<Client> client : Server.getInstance().getConnectedClients()) {
            Client possiblePlayer;
            try {
                possiblePlayer = client.get();
                if (possiblePlayer.getRole() == ClientRole.PLAYER) {
                    players.put(possiblePlayer.getId(), (Player) possiblePlayer);
                }
            } catch (InterruptedException e) {
                System.err.println("Login interupted");
            } catch (ExecutionException e) {
                System.err.println("Login execution exception");
            }
        }
        timeOutManager.stopLoginTimeOut();

        // Spielbrett generieren
        board = new Board();
        // Verteilen der Schatzkarten
        List<Treasure> treasureCardPile = new ArrayList<>(24);
        treasureCardPile.add(Treasure.SYM_01);
        treasureCardPile.add(Treasure.SYM_02);
        treasureCardPile.add(Treasure.SYM_03);
        treasureCardPile.add(Treasure.SYM_04);
        treasureCardPile.add(Treasure.SYM_05);
        treasureCardPile.add(Treasure.SYM_06);
        treasureCardPile.add(Treasure.SYM_07);
        treasureCardPile.add(Treasure.SYM_08);
        treasureCardPile.add(Treasure.SYM_09);
        treasureCardPile.add(Treasure.SYM_10);
        treasureCardPile.add(Treasure.SYM_11);
        treasureCardPile.add(Treasure.SYM_12);
        treasureCardPile.add(Treasure.SYM_13);
        treasureCardPile.add(Treasure.SYM_14);
        treasureCardPile.add(Treasure.SYM_15);
        treasureCardPile.add(Treasure.SYM_16);
        treasureCardPile.add(Treasure.SYM_17);
        treasureCardPile.add(Treasure.SYM_18);
        treasureCardPile.add(Treasure.SYM_19);
        treasureCardPile.add(Treasure.SYM_20);
        treasureCardPile.add(Treasure.SYM_21);
        treasureCardPile.add(Treasure.SYM_22);
        treasureCardPile.add(Treasure.SYM_23);
        treasureCardPile.add(Treasure.SYM_24);
        if (!Settings.TESTBOARD)
            Collections.shuffle(treasureCardPile);
        if (players.size() == 0) {
            System.err.println(Messages.getString("Game.noPlayersConnected"));
            stopGame();
            return;
        }
        int numberOfCardsPerPlayer = treasureCardPile.size() / players.size();
        int i = 0;
        Random random = new Random();
        if (Settings.TESTBOARD)
            random.setSeed(Settings.TESTBOARD_SEED);
        for (Integer player : this.players.keySet()) {
            ArrayList<Treasure> cardsPerPlayer = new ArrayList<>();
            for (int j = i * numberOfCardsPerPlayer; j < (i + 1) * numberOfCardsPerPlayer; j++) {
                Treasure treasure = treasureCardPile.get(random.nextInt(treasureCardPile.size()));
                cardsPerPlayer.add(treasure);
                treasureCardPile.remove(treasure);
            }
            this.players.get(player).setTreasure(cardsPerPlayer);
            ++i;
        }
    }

    private List<Player> playerToList() {
        Debug.print(Messages.getString("Game.playerToListFkt"), DebugLevel.VERBOSE);
        return new ArrayList<>(this.players.values());
    }

    private void singleTurn(Integer currentPlayerId) {
        /*
         * Connection.awaitMove checken -> Bei Fehler illegalMove -> liefert
         * neuen Zug
         */
        Debug.print(Messages.getString("Game.singleTurnFkt"), DebugLevel.VERBOSE);
        this.userinterface.updatePlayerStatistics(playerToList(), Integer.valueOf(currentPlayerId));
        Player currentPlayer = this.players.get(currentPlayerId);
        Treasure treasure = currentPlayer.getCurrentTreasure();
        this.board.setTreasure(treasure);
        //Debug.print(Messages.getString("Game.boardBeforeMoveFromPlayerWithID") + currentPlayerId., DebugLevel.VERBOSE);
        Debug.print(this.board.toString(), DebugLevel.DEBUG);
        MoveMessageData move = currentPlayer.getConnectionToClient().awaitMove(this.players, this.board, 0);
        boolean found = false;
        if (move != null) {
            // proceedTurn gibt zurueck ob der Spieler seinen Schatz erreicht
            // hat
            if (this.board.proceedTurn(move, currentPlayerId)) {
                found = true;
                Debug.print(String.format(Messages.getString("Game.foundTreasure"),
                        currentPlayer.getName(), currentPlayerId), DebugLevel.DEFAULT);
                this.board.getFoundTreasures().add(treasure);
                // foundTreasure gibt zurueck wieviele
                // Schaetze noch zu finden sind
                if (currentPlayer.foundTreasure() == 0) {
                    this.winner = currentPlayerId;
                }
            }
            currentPlayer.updateStats(found);
            this.userinterface.displayMove(move, (Board) this.board.clone(), Settings.MOVEDELAY, Settings.SHIFTDELAY, found);

            Server.getInstance().sendToManager(MazeComMessageFactory.createGameStatusMessage(this.board, move, found, currentPlayerId, playerToList()));

            // Informiert alle verbundenen Clients über den gemachten Zug (MOVEINFOMESSAGE):
            // TODO: irgendwie problematisch
            Server.getInstance().sendToAllConnected(MazeComMessageFactory.createMoveInfoMessage(this.board, move, found, currentPlayerId));
        } else {
            Debug.print(Messages.getString("Game.gotNoMove"), DebugLevel.DEFAULT);
        }
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Aufraeumen nach einem Spiel
     */
    private void removeAllPlayers() {
        Debug.print(Messages.getString("Game.cleanUpFkt"), DebugLevel.VERBOSE);
        if (winner > 0) {
            // Jemand hat gewonnen
            userinterface.updatePlayerStatistics(playerToList(), winner);
            String winnerName = players.get(winner).getName();
            Debug.print(String.format(Messages.getString("Game.playerIDwon"), winnerName, winner),
                    DebugLevel.DEFAULT);
            // ArrayList, damit der Iterator nicht untern Fuessen weggeloescht wird
            var stats=players.values().stream().map((player)->{
                return player.getStats().toStatisticData();
            }).collect(Collectors.toList());
            Server.getInstance().sendToAllConnected(MazeComMessageFactory.createWinMessage(0, winner, winnerName, board,stats));

        }
        //Entferne alle Spieler

        for (Integer playerID : new ArrayList<>(players.keySet())) {
            Player client = players.get(playerID);
            Debug.print(client.getStats().toString(), DebugLevel.DEFAULT);
            client.getConnectionToClient().disconnect(Errortype.NOERROR);
        }
//        while (players.size() > 0) {
//            Client player = this.players.get(this.players.keySet().iterator().next());
//            player.getConnectionToClient().disconnect(Errortype.NOERROR);
//        }

    }

    public boolean isGameOver() {
        return !winner.equals(-1);
    }

    public void setUserinterface(UI userinterface) {
        this.userinterface = userinterface;
    }

    public void run() {
        Debug.print(Messages.getString("Game.runFkt"), DebugLevel.VERBOSE);
        Debug.print(Messages.getString("Game.startNewGame"), DebugLevel.DEFAULT);
        Server.getInstance().resetPlayerIds();
        gameStatus = Status.WAITFORPLAYER;

        //Einmal wird hier in run() gewartet und das zweite await() wird von stopWaitingForPlayers() aufgerufen
        waitForPlayer = new CyclicBarrier(2);

        try {
            waitForPlayer.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        gameStatus = Status.PREPARING;
        prepareGame();
        if (players.isEmpty()) {
            stopGame();
            return;
        }
        userinterface.init(board);
        Integer currentPlayer = nextPlayer(0);
        userinterface.updatePlayerStatistics(playerToList(), currentPlayer);
        gameStatus = Status.PLAYING;
        while (!isGameOver()) {
            Debug.print(String.format(Messages.getString("Game.playersTurn"), players.get(currentPlayer).getName(),
                    currentPlayer), DebugLevel.DEFAULT);
            singleTurn(currentPlayer);
            try {
                currentPlayer = nextPlayer(currentPlayer);
            } catch (NoSuchElementException e) {
                // es gibt keinen einzigen Spieler mehr
                Debug.print(Messages.getString("Game.AllPlayersLeft"), DebugLevel.DEFAULT);
                stopGame();
            }
        }
        stopGame();
    }

    private Integer nextPlayer(Integer currentPlayerID) throws NoSuchElementException {
        Debug.print(Messages.getString("Game.nextPlayerFkt"), DebugLevel.VERBOSE);
        List<Integer> sortedclients = new ArrayList<>(players.keySet());
        Collections.sort(sortedclients);
        Iterator<Integer> idIterator = sortedclients.iterator();
        while (idIterator.hasNext()) {
            Integer id = idIterator.next();
            if (id.equals(currentPlayerID)) {
                break;
            }
        }
        if (idIterator.hasNext()) {
            return idIterator.next();
        }
        // Erste id zurueckgeben,
        return players.keySet().iterator().next();
    }

    public void removePlayer(int id) {
        Debug.print(Messages.getString("Game.removePlayerFkt"), DebugLevel.VERBOSE);
        this.players.remove(Integer.valueOf(id));
        if (winner < 0)
            // Nicht ordnungsgemaess beendet
            Debug.print(String.format(Messages.getString("Game.playerIDleftGame"), id), DebugLevel.DEFAULT);
    }

    public void stopWaitingForPlayers() {
        //Einmal wird hier in stopWaitingForPlayers() gewartet und das zweite await() wird von Game.run() aufgerufen
        if (gameStatus == Status.WAITFORPLAYER) {
            try {
                waitForPlayer.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        //loest bei await()-Threads BrokenBarrierException aus
        waitForPlayer.reset();
    }

    public void stopGame() {
        Debug.print(Messages.getString("Game.stopGame"), DebugLevel.DEFAULT);
        if (winner > 0) {
            userinterface.gameEnded(players.get(winner));
        } else {
            userinterface.gameEnded(null);
        }
        removeAllPlayers();
        winner = -2;
        timeOutManager.cancel();
        stopWaitingForPlayers();
        gameStatus = Status.NOGAMEACTIVE;
    }

    public enum Status {
        WAITFORPLAYER,
        PREPARING,
        PLAYING,
        NOGAMEACTIVE;

        public static Game.Status fromValue(String v) {
            return valueOf(v);
        }

        public String value() {
            return name();
        }

    }
}
