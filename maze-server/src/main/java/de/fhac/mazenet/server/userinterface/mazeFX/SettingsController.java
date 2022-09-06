package de.fhac.mazenet.server.userinterface.mazeFX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.config.Settings;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * SettingsController
 */
public class SettingsController implements Initializable {

    @FXML
    private Spinner<Integer> nameLength;

    @FXML
    private ChoiceBox<String> debugLevel;

    @FXML
    private Spinner<Integer> playerNumber;

    @FXML
    private Spinner<Integer> loginTimeout;

    @FXML
    private Spinner<Integer> sendTimeout;

    @FXML
    private Spinner<Integer> shiftDelay;

    @FXML
    private Spinner<Integer> moveDelay;

    @FXML
    private Spinner<Integer> loginTries;

    @FXML
    private Spinner<Integer> moveTries;

    @FXML
    private ChoiceBox<String> userinterface;

    @FXML
    private TextField port;

    @FXML
    private TextField sslPort;

    @FXML
    private TextField keystore;

    @FXML
    private PasswordField keystorePassword;

    @FXML
    private CheckBox resizeable;

    @FXML
    private CheckBox testboard;

    @FXML
    private Spinner<Integer> testboardSeed;

    @FXML
    private CheckBox autoplay;

    @FXML
    private CheckBox fullscreen;

    private URL location;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // cahce Parameter for future calls;
        this.location = location;
        this.resources = resources;

        // Intialwerte setzen
        this.port.setText("" + Settings.PORT);
        this.sslPort.setText("" + Settings.SSL_PORT);
        this.keystore.setText(Settings.SSL_CERT_STORE);
        this.keystorePassword.setText(Settings.SSL_CERT_STORE_PASSWD);
        this.debugLevel.valueProperty().set(Settings.DEBUGLEVEL.toString());
        this.nameLength.getValueFactory().setValue(Settings.MAX_NAME_LENGTH);
        this.loginTimeout.getValueFactory().setValue((int) (Settings.LOGINTIMEOUT / 1000));
        this.loginTries.getValueFactory().setValue(Settings.LOGINTRIES);
        this.userinterface.valueProperty().set(Settings.USERINTERFACE.toString());
        this.playerNumber.getValueFactory().setValue(Settings.NUMBER_OF_PLAYERS);
        this.sendTimeout.getValueFactory().setValue((int) (Settings.SENDTIMEOUT / 1000));
        this.moveDelay.getValueFactory().setValue(Settings.MOVEDELAY);
        this.shiftDelay.getValueFactory().setValue(Settings.SHIFTDELAY);
        this.resizeable.selectedProperty().set(Settings.RESIZEABLE);
        this.testboard.selectedProperty().set(Settings.TESTBOARD);
        this.autoplay.selectedProperty().set(Settings.AUTOPLAY_MUSIC);
        this.fullscreen.selectedProperty().set(Settings.FULLSCREEN);
        this.moveTries.getValueFactory().setValue(Settings.MOVETRIES);
        this.testboardSeed.getValueFactory().setValue((int) Settings.TESTBOARD_SEED);
        // TODO...
        // - Locale
        // - WINDOW_SIZE
    }

    public void importSettings(ActionEvent ev) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select your properties");
        fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Config file", ".prop"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Settings.reload(file.getAbsolutePath());
        }
        initialize(location, resources);
    }

    public void exportSettings(ActionEvent ev) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select your properties");
        fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Config file", ".prop"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            saveSettingsToFile(file.getAbsolutePath());
        }
    }

    public void handleCloseEvent(WindowEvent arg0) {
        saveSettingsToFile(Server.getInstance().getConfigPath());
    }

    private void saveSettingsToFile(String path) {
        Properties properties = new Properties();
        properties.setProperty("PORT", this.port.getText());
        properties.setProperty("SSL_PORT", this.sslPort.getText());
        properties.setProperty("SSL_CERT_STORE", this.keystore.getText());
        properties.setProperty("SSL_CERT_STORE_PASSWD", this.keystorePassword.getText());
        properties.setProperty("MAX_NAME_LENGTH", this.nameLength.getValueFactory().getValue().toString());
        properties.setProperty("DEBUGLEVEL", this.debugLevel.getValue());
        properties.setProperty("NUMBER_OF_PLAYERS", this.playerNumber.getValueFactory().getValue().toString());
        properties.setProperty("LOGINTIMEOUT", (1000 * this.loginTimeout.getValueFactory().getValue()) + "");
        properties.setProperty("LOGINTRIES", this.loginTries.getValueFactory().getValue() + "");
        properties.setProperty("MOVETRIES", this.moveTries.getValueFactory().getValue() + "");
        properties.setProperty("SENDTIMEOUT", (1000 * this.sendTimeout.getValueFactory().getValue()) + "");
        properties.setProperty("SHIFTDELAY", this.shiftDelay.getValueFactory().getValue().toString());
        properties.setProperty("MOVEDELAY", this.moveDelay.getValueFactory().getValue().toString());
        properties.setProperty("AUTOPLAY_MUSIC", this.autoplay.selectedProperty().get() + "");
        properties.setProperty("USERINTERFACE", this.userinterface.getValue());
        properties.setProperty("TESTBOARD", this.testboard.selectedProperty().get() + "");
        properties.setProperty("FULLSCREEN", this.fullscreen.selectedProperty().get() + "");
        properties.setProperty("TESTBOARD_SEED", this.testboardSeed.getValueFactory().getValue() + "");

        // TODO...
        // - Locale
        // - WINDOW_SIZE

        try {
            properties.store(new FileOutputStream(path), "storedByUI");
        } catch (FileNotFoundException e) {
            Debug.print(e.getLocalizedMessage(), DebugLevel.DEFAULT);
        } catch (IOException e) {
            Debug.print(e.getLocalizedMessage(), DebugLevel.DEFAULT);
        }

    }
}