package de.fhac.mazenet.server.userinterface.CLIUI;

import de.fhac.mazenet.server.config.Settings;

import java.util.Scanner;

/**
 * Created by bongen on 27.06.17.
 */
public class UserPrompt implements Runnable {
    private boolean running;
    private Scanner fromUser;
    private CommandLineUI commandLineUI;

    public UserPrompt(CommandLineUI cliui) {
        running = true;
        fromUser = new Scanner(System.in);
        commandLineUI = cliui;
    }

    @Override
    public void run() {
        while (running) {
            System.out.println("What do you want to do?");
            System.out.println("1) start new Game");
            System.out.println("2) stop current Game");
            System.out.println("3) set Client");
            System.out.println("4) Exit");
            String line = fromUser.nextLine();
            int choice = -1;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.err.println("please type in a number");
            }

            switch (choice) {
            case 1:
                // start new Game
                commandLineUI.startGame();
                break;
            case 2:
                // stop Game
                commandLineUI.stopGame();
                break;
            case 3:
                // set Client
                System.out.println("Wieviele Spieler sollen im nächsten Spiel teilnehmen? ");
                line = fromUser.nextLine();
                choice = -1;
                try {
                    choice = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    System.err.println("please type in a number");
                }
                if (choice > 0 && choice < 5) {
                    Settings.NUMBER_OF_PLAYERS = choice;
                } else {
                    System.err.println("Spieleranzahl muss zwischen 1 und 4 liegen");
                }
                break;
            case 4:
                System.exit(0);
                break;
            default:
                System.err.println("Diese Option ist nicht verfügbar");
                break;
            }
        }
    }

}
