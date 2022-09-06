package de.fhac.mazenet.server.userinterface.mazeFX;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.Messages;
import de.fhac.mazenet.server.userinterface.mazeFX.objects.PlayerFX;
import de.fhac.mazenet.server.userinterface.mazeFX.util.BetterOutputStream;
import de.fhac.mazenet.server.userinterface.mazeFX.util.ImageResourcesFX;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Created by Richard Zameitat on 25.05.2016.
 */
public class MainUI implements Initializable {

    @FXML
    private SplitPane rootPane;


    @FXML
    private TextArea logArea;

    @FXML
    private Label serverStatusText;

    @FXML
    private Button serverStart;

    @FXML
    private Button serverStop;

    @FXML
    private Button openSettings;

    @FXML
    private Spinner<Number> maxPlayer;

    @FXML
    private VBox playerStatsContrainer;

    @FXML
    private Label playerStatsPlaceholder;

    @FXML
    private BorderPane winnerPanel;

    @FXML
    private Label winnerText;

    @FXML
    private Button soundEnabled;

    @FXML
    private Slider volumeSlider;

    @FXML
    private BarChart<String, Integer> kiStats;

    @FXML
    private Accordion statistikAccordion;

    @FXML
    private Label winnerPlayer;

    @FXML
    private AnchorPane boardContainer;

    private List<Runnable> startServerListeners = new LinkedList<>();
    private List<Runnable> stopServerListeners = new LinkedList<>();
    private List<Runnable> soundEnabledChangedListener = new LinkedList<>();
    private MediaPlayer backgroundMusic;
    private boolean musicEnabled = Settings.AUTOPLAY_MUSIC;

    public int getMaxPlayer() {
        return maxPlayer.getValue().intValue();
    }

    public void addBoard(Pane board) {
        AnchorPane.setTopAnchor(board, 0.0);
        AnchorPane.setRightAnchor(board, 0.0);
        AnchorPane.setBottomAnchor(board, 0.0);
        AnchorPane.setLeftAnchor(board, 0.0);
        boardContainer.getChildren().add(board);
    }

    @FXML
    private void serverStartAction(ActionEvent aevt) {
        startServerListeners.forEach(r -> r.run());
    }

    @FXML
    private void serverStopAction(ActionEvent aevt) {
        stopServerListeners.forEach(r -> r.run());
    }

    @FXML
    private void soundEnabledChanged(ActionEvent aevt) {
        soundEnabledChangedListener.forEach(r -> r.run());
    }

    @FXML
    private void openSettingsAction(ActionEvent aevt) {
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/layouts/Settings.fxml"));
                Parent root1 = (Parent) fxmlLoader.load();
                SettingsController controller = (SettingsController) fxmlLoader.getController();
                Stage stage = new Stage();
                Scene scene = new Scene(root1);
                stage.setScene(scene);
                stage.setTitle("Einstellungen (" + Server.getInstance().getConfigPath() + ")");
                stage.setOnCloseRequest((we) -> {
                    controller.handleCloseEvent(we);
                });
                stage.show();
            } catch (IOException e) {
                System.err.println("Settings went south");
                e.printStackTrace();
            }
        });
    }

   

    public void addStartServerListener(Runnable r) {
        startServerListeners.add(r);
    }

    public void addStopServerListener(Runnable r) {
        stopServerListeners.add(r);
    }

    public void addSoundEnabledChangedListener(Runnable r) {
        soundEnabledChangedListener.add(r);
    }

    public void gameStarted() {
        serverStart.disableProperty().setValue(true);
        maxPlayer.disableProperty().setValue(true);
        serverStop.disableProperty().setValue(false);
        clearPlayerStats();
        ImageResourcesFX.reset();
        serverStatusText.setText(Messages.getString("MazeFX.status.started"));
        playerStatsPlaceholder.setVisible(true);

    }

    public void gameStopped() {
        serverStop.disableProperty().setValue(true);
        maxPlayer.disableProperty().setValue(false);
        serverStart.disableProperty().setValue(false);
        serverStatusText.setText(Messages.getString("MazeFX.status.stopped"));
    }

    public void addPlayerStat(Node statNode) {
        playerStatsPlaceholder.setVisible(false);
        playerStatsContrainer.getChildren().addAll(statNode);
        playerStatsContrainer.setPrefHeight(playerStatsContrainer.getChildren().size() * 60);
    }

    public void clearPlayerStats() {
        hideWinner();
        playerStatsContrainer.getChildren().clear();
        playerStatsContrainer.setPrefHeight(playerStatsContrainer.getChildren().size() * 60);

    }

    public void setStats(List<Player> players) {
        kiStats.getData().clear();
        for (Player player : players) {
            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            series.setName(player.getName());
            series.getData().add(new XYChart.Data<>("Schätze gefunden", player.getStats().getFoundTreasures()));
            series.getData().add(new XYChart.Data<>("Züge gemacht", player.getStats().getMovesDone()));
            series.getData()
                    .add(new XYChart.Data<>("Längste Zuganzahl ohne Schätze", player.getStats().getLongestFailMoves()));
            kiStats.getData().add(series);
        }

        // ueber css klassen die farben anpassen
        for (Player player : players) {
            // Bars
            for (Node node : kiStats.lookupAll(".default-color" + (player.getId() - 1) + ".chart-bar")) {
                node.setStyle("-fx-bar-fill: " +
                        PlayerFX.playerIdToColorString(player.getId()) + ";");
            }

            //Aenderungen muessen nach einander gemacht werden
            kiStats.applyCss();

            // Legende
            for (Node node : kiStats
                    .lookupAll(".default-color" + (player.getId() - 1) + ".chart-bar.bar-legend-symbol")) {
                node.setStyle("-fx-background-color: " + PlayerFX.playerIdToColorString(player.getId()) + ";");
            }
        }
    }

    public void showWinner(String name) {
        winnerPlayer.setText(name);
        winnerPanel.setVisible(true);
    }

    public void hideWinner() {
        winnerPanel.setVisible(false);
    }

    private void initalizeWinnerPanelTextEffect() {
        Blend blend = new Blend();
        blend.setMode(BlendMode.MULTIPLY);

        DropShadow ds1 = new DropShadow();
        ds1.setColor(Color.web("#00c300"));
        ds1.setRadius(20);
        ds1.setSpread(0.2);

        Blend blend2 = new Blend();
        blend2.setMode(BlendMode.MULTIPLY);

        InnerShadow is = new InnerShadow();
        is.setColor(Color.web("#feeb42"));
        is.setRadius(9);
        is.setChoke(0.8);
        blend2.setBottomInput(is);

        InnerShadow is1 = new InnerShadow();
        is1.setColor(Color.web("#f13a00"));
        is1.setRadius(5);
        is1.setChoke(0.4);
        blend2.setTopInput(is1);

        Blend blend1 = new Blend();
        blend1.setMode(BlendMode.MULTIPLY);
        blend1.setBottomInput(ds1);
        blend1.setTopInput(blend2);

        blend.setTopInput(blend1);

        ChangeListener<? super java.lang.Number> updateFont = (observable, oldValue, newValue) -> {
            double w = boardContainer.getWidth();
            double h = boardContainer.getHeight();
            double fontSize = Math.min(w, h) * 0.8;
            winnerText.setStyle("-fx-font-size: " + +fontSize + "%");
            winnerPlayer.setStyle("-fx-font-size: " + fontSize + "%");
        };
        boardContainer.widthProperty().addListener(updateFont);
        boardContainer.heightProperty().addListener(updateFont);
        updateFont.changed(null, null, null);

        winnerText.setEffect(blend);
        winnerPlayer.setEffect(blend);
        statistikAccordion.setExpandedPane(statistikAccordion.getPanes().get(0));
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        Platform.runLater(() -> {
            Debug.addDebugger(new BetterOutputStream(s -> Platform.runLater(() -> logArea.appendText(s))),
                    Settings.DEBUGLEVEL);
            initalizeWinnerPanelTextEffect();

            // prepared background Music Stuff
            // aus mp3 wurde ein unkomprimiertes wav erzeugt:
            // ffmpeg -i background.mp3 -acodec pcm_u8 -ar 22050 background.wav
            addSoundEnabledChangedListener(() -> {
                toggleMusic();
            });
            URL musicFile = MazeFX.class.getClassLoader().getResource("music/background.wav");
            backgroundMusic = new MediaPlayer(new Media(musicFile.toString()));
            volumeSlider.valueProperty().bindBidirectional(backgroundMusic.volumeProperty());
            volumeSlider.valueProperty().addListener((observableValue, oldV, newV) -> {
                backgroundMusic.setVolume(newV.doubleValue());
                // backgroundMusic.stop();
                // backgroundMusic.play();
            });
            backgroundMusic.setVolume(0.5);
            toggleMusic();

            // HOTFIX different implementations of Spinner from OpenJFX and
            // Oracle JavaFX
            Number v = maxPlayer.getValueFactory().getValue();
            if (v instanceof Double) {
                // OpenJFX
                maxPlayer.getValueFactory().setValue(new Double(Settings.NUMBER_OF_PLAYERS));
            } else {
                // OracleJFX
                maxPlayer.getValueFactory().setValue(Settings.NUMBER_OF_PLAYERS);
            }
        });
    }

    public void toggleMusic() {
        volumeSlider.setVisible(musicEnabled);
        if (musicEnabled) {
            soundEnabled.setText("🔊");
            backgroundMusic.setCycleCount(AudioClip.INDEFINITE);
            backgroundMusic.play();
        } else {
            soundEnabled.setText("🔇");
            backgroundMusic.pause();
        }
        musicEnabled = !musicEnabled;
    }

    public void mouseOverSoundButton() {
        volumeSlider.setOpacity(1.0);
    }
}
