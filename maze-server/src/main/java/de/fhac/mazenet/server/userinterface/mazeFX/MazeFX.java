package de.fhac.mazenet.server.userinterface.mazeFX;
/**
 * Created by Richard Zameitat on 25.05.2016.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.networking.Client;
import de.fhac.mazenet.server.tools.Messages;
import de.fhac.mazenet.server.userinterface.GameController;
import de.fhac.mazenet.server.userinterface.UI;
import de.fhac.mazenet.server.userinterface.mazeFX.objects.Board3dFX;
import de.fhac.mazenet.server.userinterface.mazeFX.objects.BoardVisualisation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MazeFX extends Application implements UI {

    private static MazeFX instance;
    private Stage primaryStage;
    private Parent root;
    private MainUI controller;
    private Map<Integer, PlayerStatFX> playerStats = new ConcurrentHashMap<>();
    private Integer currentPlayer;

    // DIRTY HACK FOR GETTING AN INSTANCE STARTS HERE
    // (JavaFX ist not very good at creating instances ...)
    private static CountDownLatch instanceCreated = new CountDownLatch(1);
    private static MazeFX lastInstance = null;
    private GameController gameControler;
    private BoardVisualisation bFx;

    // private waere schoener wegen instance, crashed aber
    public MazeFX() {
    }

    private synchronized static MazeFX newInstance() {
        new Thread(() -> Application.launch(MazeFX.class)).start();
        try {
            instanceCreated.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        MazeFX instance = lastInstance;
        lastInstance = null;
        instanceCreated = new CountDownLatch(1);
        return instance;
    }

    private void instanceReady() {
        lastInstance = this;
        instanceCreated.countDown();
    }
    // END OF HACK

    public static MazeFX getInstance() {
        if (instance == null)
            instance = newInstance();
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.onCloseRequestProperty().setValue(e -> {
            System.exit(0);
        });
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/layouts/MainUI.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("locale"));
        root = fxmlLoader.load();
        controller = fxmlLoader.getController();

        bFx = new Board3dFX();
        controller.addBoard(bFx.getRoot());
        // stop all animations when focus is lost
        primaryStage.focusedProperty().addListener((ov, o, n) -> {
            if (!n) {
                bFx.focusLost();
            }
        });

        controller.addStartServerListener(this::startActionPerformed);
        controller.addStopServerListener(this::stopActionPerformed);

        primaryStage.setTitle(Messages.getString("MazeFX.WindowTitle"));
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon/maze.png")));

        primaryStage.setFullScreen(Settings.FULLSCREEN);
        if (!Settings.FULLSCREEN) {
            double width = Double.parseDouble(Settings.WINDOW_SIZE.split("x")[0]);
            double height = Double.parseDouble(Settings.WINDOW_SIZE.split("x")[1]);
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.setResizable(Settings.RESIZEABLE);
        }
        primaryStage.show();
        instanceReady();
    }

    private PlayerStatFX createPlayerStat(int teamId) throws IOException {
        return new PlayerStatFX(teamId);
    }

    private void updatePlayerStats(List<Player> stats, Integer current) {
        currentPlayer = current;
        stats.forEach(p -> {
            // TODO checken ob ein Spieler rausgeflogen ist
            try {
                PlayerStatFX stat = playerStats.get(p.getId());
                if (stat == null) {
                    playerStats.put(p.getId(), stat = createPlayerStat(p.getId()));
                    controller.addPlayerStat(stat.rootNode);
                }
                stat.update(p);
                stat.active(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        playerStats.get(current).active(true);

    }

    private void startActionPerformed() {
        controller.gameStarted();
        Settings.NUMBER_OF_PLAYERS = controller.getMaxPlayer();
        // clears the UI
        controller.clearPlayerStats();
        // Clears the cached data
        gameControler = Settings.GAMECONTROLLER;
        gameControler.startGame();
        playerStats = new ConcurrentHashMap<>();
    }

    private void stopActionPerformed() {
        gameControler = Settings.GAMECONTROLLER;
        gameControler.stopGame();
        // controller.gameStopped();
    }

    private void initFromBoard(Board startBoard) {
        clearBoard();
        bFx.initFromBoard(startBoard);

    }

    private void clearBoard() {
        bFx.clearBoard();
        currentPlayer = null;
        if (playerStats != null) {
            playerStats.clear();

        }
    }

    @Override
    public void displayMove(MoveMessageData moveMessage, Board boardAfterMove, long moveDelay, long shiftDelay,
            boolean treasureReached) {
        CountDownLatch lock = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                bFx.animateMove(moveMessage, boardAfterMove, moveDelay, shiftDelay, treasureReached, lock, currentPlayer);
            } catch (Exception e) {
                lock.countDown();
            }
        });

        // Sorgt dafuer, dass die Methode blockiert
        do {
            try {
                lock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (lock.getCount() != 0);
    }

    @Override
    public void updatePlayerStatistics(List<Player> statistics, Integer currentPlayerID) {
        currentPlayer = currentPlayerID;
        Platform.runLater(() -> this.updatePlayerStats(statistics, currentPlayerID));
    }

    @Override
    public void init(Board startBoard) {
        Platform.runLater(() -> initFromBoard(startBoard));
    }

    @Override
    public void gameEnded(Client winner) {
        Platform.runLater(() -> {
            controller.gameStopped();
            if (winner != null) {
                int playerId = winner.getId();
                PlayerStatFX stats = playerStats.get(playerId);
                stats.setWinner();
                List<Player> players = playerStats.values().stream().map(PlayerStatFX::getPlayer)
                        .collect(Collectors.toList());
                controller.setStats(players);
                controller.showWinner(stats.getPlayer().getName());
            }
        });
    }

    @Override
    public String toString() {
        return "MazeFX";
    }
}
