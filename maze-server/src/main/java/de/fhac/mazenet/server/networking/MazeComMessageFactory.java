package de.fhac.mazenet.server.networking;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Player;
import de.fhac.mazenet.server.generated.*;
import de.fhac.mazenet.server.generated.WinMessageData.Winner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MazeComMessageFactory {

    static private ObjectFactory objectFactory = new ObjectFactory();

    /**
     * privater Konstruktor wegen Factory
     */
    private MazeComMessageFactory() {
    }

    public static MazeCom createLoginReplyMessage(int newID) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.LOGINREPLY);
        mazeCom.setId(newID);
        mazeCom.setLoginReplyMessage(objectFactory.createLoginReplyMessageData());
        mazeCom.getLoginReplyMessage().setNewID(newID);
        return mazeCom;
    }

    public static MazeCom createAcceptMessage(int playerID, Errortype errortype) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.ACCEPT);
        mazeCom.setId(playerID);
        mazeCom.setAcceptMessage(objectFactory.createAcceptMessageData());
        mazeCom.getAcceptMessage().setErrortypeCode(errortype);
        mazeCom.getAcceptMessage().setAccept(errortype == Errortype.NOERROR);
        return mazeCom;
    }

    public static MazeCom createWinMessage(int playerID, int winnerId, String name, Board board, List<StatisticData> stats) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.WIN);
        mazeCom.setId(playerID);
        mazeCom.setWinMessage(objectFactory.createWinMessageData());
        Winner winner = objectFactory.createWinMessageDataWinner();
        winner.setId(winnerId);
        winner.setValue(name);
        mazeCom.getWinMessage().setWinner(winner);
        mazeCom.getWinMessage().setBoard(board);
        mazeCom.getWinMessage().getStatistics().addAll(stats);
        return mazeCom;
    }

    public static MazeCom createDisconnectMessage(int playerID, String name, Errortype errortype) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.DISCONNECT);
        mazeCom.setId(playerID);
        mazeCom.setDisconnectMessage(objectFactory.createDisconnectMessageData());
        mazeCom.getDisconnectMessage().setErrortypeCode(errortype);
        mazeCom.getDisconnectMessage().setName(name);
        return mazeCom;
    }

    public static MazeCom createAwaitMoveMessage(HashMap<Integer, Player> players, Integer currentPlayerID,
            Board board) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.AWAITMOVE);
        mazeCom.setId(players.get(currentPlayerID).getId());
        mazeCom.setAwaitMoveMessage(objectFactory.createAwaitMoveMessageData());
        // Brett uebergeben
        mazeCom.getAwaitMoveMessage().setBoard(board);
        mazeCom.getAwaitMoveMessage().setTreasureToFindNext(players.get(currentPlayerID).getCurrentTreasure());
        List<Integer> sortedPlayers = new ArrayList<>(players.keySet());
        Collections.sort(sortedPlayers);
        for (Integer playerID : sortedPlayers) {
            TreasuresToGoData treasuresToGo = objectFactory.createTreasuresToGoData();
            treasuresToGo.setPlayer(playerID);
            treasuresToGo.setTreasures(players.get(playerID).treasuresToGo());
            mazeCom.getAwaitMoveMessage().getTreasuresToGo().add(treasuresToGo);
        }
        return mazeCom;
    }

    /**
     * convenience-Methode für Clients, wird vom Server nicht benötigt
     *
     * @param name der gewählte Gruppenname
     * @return MazeCom-Nachricht mit beinhalteter LoginMessage
     */
    public static MazeCom createLoginMessage(String name) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.LOGIN);
        mazeCom.setId(-1);
        mazeCom.setLoginMessage(objectFactory.createLoginMessageData());
        mazeCom.getLoginMessage().setName(name);
        return mazeCom;
    }

    public static MazeCom createMoveInfoMessage(Board spielBrett, MoveMessageData move, boolean found,
            int currentPlayerID) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.MOVEINFO);
        MoveInfoData moveInfoData = objectFactory.createMoveInfoData();
        moveInfoData.setBoardAfterMove(spielBrett);
        moveInfoData.setPlayerId(currentPlayerID);
        moveInfoData.setSendMove(move);
        moveInfoData.setTreasureReached(found);
        mazeCom.setMoveInfoMessage(moveInfoData);
        return mazeCom;
    }

    public static MazeCom createGameStatusMessage(Board board, MoveMessageData move, boolean found,
            Integer currentPlayerId, List<Player> playerList) {
        MazeCom mazeCom = objectFactory.createMazeCom();
        mazeCom.setMessagetype(MazeComMessagetype.GAMESTATUS);
        GameStatusData gameStatusData = objectFactory.createGameStatusData();
        gameStatusData.setBoardAfterMove(board);
        gameStatusData.setPlayerId(currentPlayerId);
        gameStatusData.setSendMove(move);
        gameStatusData.setTreasureReached(found);
        for (Player player : playerList) {
            GameStatusData.PlayerStatus playerStatus = objectFactory.createGameStatusDataPlayerStatus();
            playerStatus.setPlayerID(player.getId());
            playerStatus.setPlayerName(player.getName());
            playerStatus.setCurrentTreasure(player.getCurrentTreasure());
            //TODO Testen ob Reihenfolge erhalten bleibt
            playerStatus.getTreasures().addAll(new ArrayList<Treasure>(player.getTreasures()));
            gameStatusData.getPlayerStatus().add(playerStatus);
        }
        mazeCom.setGameStatusMessage(gameStatusData);
        return mazeCom;
    }
}
