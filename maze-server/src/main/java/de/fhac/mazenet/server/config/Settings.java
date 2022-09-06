package de.fhac.mazenet.server.config;

import de.fhac.mazenet.server.Server;
import de.fhac.mazenet.server.manager.VisualManager;
import de.fhac.mazenet.server.tools.Debug;
import de.fhac.mazenet.server.tools.DebugLevel;
import de.fhac.mazenet.server.tools.Messages;
import de.fhac.mazenet.server.userinterface.CLIUI.CommandLineUI;
import de.fhac.mazenet.server.userinterface.GameController;
import de.fhac.mazenet.server.userinterface.UI;
import de.fhac.mazenet.server.userinterface.betterUI.BetterUI;
import de.fhac.mazenet.server.userinterface.mazeFX.MazeFX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class Settings {
    public static int PORT;
    public static int SSL_PORT;

    public static String SSL_CERT_STORE;
    public static String SSL_CERT_STORE_PASSWD;

    /**
     * Die maximal erlaubte Laenge des Loginnamens
     */
    public static int MAX_NAME_LENGTH;

    /**
     * Den Detailgrad der Ausgaben festlegen
     */
    public static DebugLevel DEBUGLEVEL;

    /**
     * Startwert fuer die Spieleranzahl Kann aber noch veraendert werden, deshalb
     * nicht final
     */
    public static int NUMBER_OF_PLAYERS;
    public static Locale LOCALE;
    /**
     * Die Zeit in Milisekunden, nach der ein Logintimeout eintritt LOGINTIMEOUT =
     * 60000 entspricht einer Minute
     */
    public static long LOGINTIMEOUT;
    public static int LOGINTRIES;
    /**
     * Die Zeit in Milisekunden, die die Animation eines Zug (die Bewegung des Pins)
     * benoetigen soll
     */
    public static int MOVEDELAY;
    /**
     * Die maximale Anzahl der Versuche einen gueltigen Zug zu uebermitteln
     */
    public static int MOVETRIES;
    public static long SENDTIMEOUT;
    /**
     * Die Zeit in Milisekunden, die das Einschieben der Shiftcard dauern soll
     */
    public static int SHIFTDELAY;
    /**
     * Wenn TESTBOARD = true ist, dann ist das Spielbrett bei jedem Start identisch
     * (zum Debugging)
     */
    public static boolean TESTBOARD;
    /**
     * Hiermit lassen sich die Testfaelle anpassen (Pseudozufallszahlen)
     */
    public static long TESTBOARD_SEED;
    /**
     * USERINTERFACE definiert die zu verwendende GUI Gueltige Werte: BetterUI,
     * MazeFX
     */
    public static UI USERINTERFACE;
    public static GameController GAMECONTROLLER;

    /**
     * Definiert, ob das Fenster in der Groeße geaendert werden kann Funktioniert
     * nur bei MazeFX und nicht in BetterUI
     */
    public static boolean RESIZEABLE;

    /**
     * Gibt die Fenstergroeße im Format 1280x720 [pixel] an
     */
    public static String WINDOW_SIZE;

    /**
     * Setzt MazeFX in Fullscreen, funktioniert nicht bei BetterUI
     */
    public static boolean FULLSCREEN;

    /**
     * Setzt die Hintergrundmusic in der MazeFX UI
     */
    public static boolean AUTOPLAY_MUSIC;

    private Settings() {
    }

    public static void reload(String path) {
        Properties properties = new Properties();
        if (path != null) {
            try (InputStream propStream = new FileInputStream(new File(path))) {
                properties.load(propStream);
            } catch (IOException e) {
                System.err.println(Messages.getString("Settings.configNotFound"));
            }
        }
        MAX_NAME_LENGTH = Integer.parseInt(properties.getProperty("MAX_NAME_LENGTH", "30"));
        NUMBER_OF_PLAYERS = Integer.parseInt(properties.getProperty("NUMBER_OF_PLAYERS", "1"));
        LOCALE = new Locale(properties.getProperty("LOCALE", "de"));
        LOGINTIMEOUT = Integer.parseInt(properties.getProperty("LOGINTIMEOUT", "120000"));
        LOGINTRIES = Integer.parseInt(properties.getProperty("LOGINTRIES", "3"));
        MOVEDELAY = Integer.parseInt(properties.getProperty("MOVEDELAY", "400"));
        MOVETRIES = Integer.parseInt(properties.getProperty("MOVETRIES", "3"));
        PORT = Integer.parseInt(properties.getProperty("PORT", "5123"));
        SSL_PORT = Integer.parseInt(properties.getProperty("SSL_PORT", "5432"));
        SSL_CERT_STORE = properties.getProperty("SSL_CERT_STORE", "");
        SSL_CERT_STORE_PASSWD = properties.getProperty("SSL_CERT_STORE_PASSWD", "");
        SENDTIMEOUT = Integer.parseInt(properties.getProperty("SENDTIMEOUT", "20000"));
        SHIFTDELAY = Integer.parseInt(properties.getProperty("SHIFTDELAY", "700"));
        TESTBOARD = Boolean.parseBoolean(properties.getProperty("TESTBOARD", "true"));
        TESTBOARD_SEED = Integer.parseInt(properties.getProperty("TESTBOARD_SEED", "0"));
        String logLevel = properties.getProperty("DEBUGLEVEL", "DEFAULT");
        switch (logLevel.toUpperCase()) {
        case "NONE":
            DEBUGLEVEL = DebugLevel.NONE;
            break;
        case "DEFAULT":
            DEBUGLEVEL = DebugLevel.DEFAULT;
            break;
        case "VERBOSE":
            DEBUGLEVEL = DebugLevel.VERBOSE;
            break;
        case "DEBUG":
            DEBUGLEVEL = DebugLevel.DEBUG;
            break;
        default:
            DEBUGLEVEL = DebugLevel.DEFAULT;
            break;
        }
        RESIZEABLE = Boolean.parseBoolean(properties.getProperty("RESIZEABLE", "true"));
        WINDOW_SIZE = properties.getProperty("WINDOW_SIZE", "1280x720");
        FULLSCREEN = Boolean.parseBoolean(properties.getProperty("FULLSCREEN", "false"));
        AUTOPLAY_MUSIC = Boolean.parseBoolean(properties.getProperty("AUTOPLAY_MUSIC", "true"));
        String ui = properties.getProperty("USERINTERFACE", "MazeFX");
        switch (ui) {
        case "BetterUI":
            USERINTERFACE = BetterUI.getInstance();
            break;
        case "MazeFX":
            USERINTERFACE = MazeFX.getInstance();
            break;
        case "CLIUI":
            USERINTERFACE = CommandLineUI.getInstance();
            break;
        default:
            Debug.print(Messages.getString("Settings.noUserInterface"), DebugLevel.DEFAULT);
            System.exit(1);
            break;
        }
        String controller = properties.getProperty("GAMECONTROLLER", "Server");
        switch (controller) {
            case "Server":
                GAMECONTROLLER = Server.getInstance();
                break;
            case "VisualManager":
                GAMECONTROLLER = VisualManager.getInstance();
                break;
            default:
                Debug.print(Messages.getString("Settings.noGameController"), DebugLevel.DEFAULT);
                System.exit(1);
                break;
        }
    }

}
