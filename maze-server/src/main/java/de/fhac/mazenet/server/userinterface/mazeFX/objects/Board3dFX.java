package de.fhac.mazenet.server.userinterface.mazeFX.objects;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import de.fhac.mazenet.server.game.Board;
import de.fhac.mazenet.server.game.Card;
import de.fhac.mazenet.server.game.Position;
import de.fhac.mazenet.server.generated.CardData;
import de.fhac.mazenet.server.generated.MoveMessageData;
import de.fhac.mazenet.server.generated.PositionData;
import de.fhac.mazenet.server.userinterface.mazeFX.Board3d;
import de.fhac.mazenet.server.userinterface.mazeFX.animations.AddTransition;
import de.fhac.mazenet.server.userinterface.mazeFX.animations.AnimationFactory;
import de.fhac.mazenet.server.userinterface.mazeFX.animations.UntilTransition;
import de.fhac.mazenet.server.userinterface.mazeFX.data.Translate3D;
import de.fhac.mazenet.server.userinterface.mazeFX.data.VectorInt2;
import de.fhac.mazenet.server.userinterface.mazeFX.util.FakeTranslateBinding;
import de.fhac.mazenet.server.userinterface.mazeFX.util.MoveStateCalculator;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * Board3d
 */
public class Board3dFX implements BoardVisualisation {
    private final Board3d controller;
    private final Pane rootNode;
    private static final Translate3D SHIFT_CARD_TRANSLATE = new Translate3D(0, -3.3, 0);

    private static final int BOARD_WIDTH = 7;
    private static final int BOARD_HEIGHT = 7;
    private static final double CAM_ROTATE_X_INITIAL = -50;
    private static final double CAM_ROTATE_Y_INITIAL = 0;

    private Rotate camRotateX = new Rotate(CAM_ROTATE_X_INITIAL, Rotate.X_AXIS);
    private Rotate camRotateY = new Rotate(CAM_ROTATE_Y_INITIAL, Rotate.Y_AXIS);

    private AddTransition cameraRotationDown;
    private AddTransition cameraRotationRight;
    private AddTransition cameraRotationLeft;
    private AddTransition cameraRotationUp;
    private Group scene3dRoot;

    private CardFX shiftCard;
    private CardFX[][] boardCards;
    private Map<Integer, PlayerFX> players;
    private Board recentBoard;

    public Board3dFX() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/layouts/Board3d.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("locale"));
        rootNode = fxmlLoader.load();
        controller = fxmlLoader.getController();
        players = new ConcurrentHashMap<>();
        init3dStuff();
    }

    public static Translate3D getCardTranslateForPosition(int x, int z) {
        final double midX = BOARD_WIDTH / 2.;
        final double midZ = BOARD_HEIGHT / 2.;
        final double offX = 0.5;
        final double offZ = -0.5;
        double newX = (x - midX) * 1 + offX;
        double newZ = (midZ - z) * 1 + offZ;
        return new Translate3D(newX, 0, newZ);
    }

    private void init3dStuff() {
        // scene graph
        scene3dRoot = new Group();

        Pane parent3d = controller.getParent3D();
        SubScene sub3d = controller.getSub3D();
        // replacing original Subscene with antialised one ...
        // TODO: do it in a nicer way!
        parent3d.getChildren().remove(sub3d);
        sub3d = new SubScene(scene3dRoot, 300, 300, true, SceneAntialiasing.BALANCED);
        parent3d.getChildren().add(0, sub3d);
        sub3d.setManaged(false);
        sub3d.heightProperty().bind(parent3d.heightProperty());
        sub3d.widthProperty().bind(parent3d.widthProperty());

        Translate camTranZ = new Translate(0, 0, -15);

        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(camRotateY, camRotateX, camTranZ);

        camTranZ.zProperty().bind(controller.getCamZoomSlide().valueProperty());

        // create rotation animations
        // rotate right
        cameraRotationRight = new AddTransition(Duration.millis(3000), camRotateY.angleProperty(), 360);
        cameraRotationRight.setUpperLimit(180);
        cameraRotationRight.setInterpolator(Interpolator.LINEAR);
        cameraRotationRight.setCycleCount(Animation.INDEFINITE);
        cameraRotationRight.setAutoReverse(false);
        controller.addCamRotateRightStartListener(cameraRotationRight::play);
        controller.addCamRotateRightStopListener(cameraRotationRight::stop);

        // rotate left
        cameraRotationLeft = new AddTransition(Duration.millis(3000), camRotateY.angleProperty(), -360);
        cameraRotationLeft.setLowerLimit(-180);
        cameraRotationLeft.setInterpolator(Interpolator.LINEAR);
        cameraRotationLeft.setCycleCount(Animation.INDEFINITE);
        cameraRotationLeft.setAutoReverse(false);
        controller.addCamRotateLeftStartListener(cameraRotationLeft::play);
        controller.addCamRotateLeftStopListener(cameraRotationLeft::stop);

        // rotate up
        cameraRotationUp = new AddTransition(Duration.millis(3000), camRotateX.angleProperty(), -360);
        cameraRotationUp.setLowerLimit(-90);
        cameraRotationUp.setInterpolator(Interpolator.LINEAR);
        cameraRotationUp.setCycleCount(Animation.INDEFINITE);
        cameraRotationUp.setAutoReverse(false);
        controller.addCamRotateUpStartListener(cameraRotationUp::play);
        controller.addCamRotateUpStopListener(cameraRotationUp::stop);

        // rotate down
        cameraRotationDown = new AddTransition(Duration.millis(3000), camRotateX.angleProperty(), 360);
        cameraRotationDown.setUpperLimit(0);
        cameraRotationDown.setInterpolator(Interpolator.LINEAR);
        cameraRotationDown.setCycleCount(Animation.INDEFINITE);
        cameraRotationDown.setAutoReverse(false);
        controller.addCamRotateDownStartListener(cameraRotationDown::play);
        controller.addCamRotateDownStopListener(cameraRotationDown::stop);

        // rotate default
        UntilTransition camRotDefaultX = new UntilTransition(Duration.millis(1000), camRotateX.angleProperty(), 360,
                CAM_ROTATE_X_INITIAL);
        UntilTransition camRotDefaultY = new UntilTransition(Duration.millis(1000), camRotateY.angleProperty(), 360,
                CAM_ROTATE_Y_INITIAL);
        controller.addCamRotateDefaultListener(camRotDefaultX::play);
        controller.addCamRotateDefaultListener(camRotDefaultY::play);

        // add stuff to scene graph
        scene3dRoot.getChildren().add(camera);
        sub3d.setFill(Color.WHITESMOKE);
        sub3d.setCamera(camera);

    }

    @Override
    public void animateMove(MoveMessageData moveMessage, Board targetBoard, long moveDelay, long shiftDelay,
            boolean treasureReached, CountDownLatch lock, Integer playerID) {
        final Duration durationBefore = Duration.millis(shiftDelay / 3);
        final Duration durationShift = Duration.millis(shiftDelay / 3);
        final Duration durationAfter = Duration.millis(shiftDelay / 3);
        final Duration durationMove = Duration.millis(moveDelay);

        final PlayerFX pin = players.getOrDefault(playerID, null);

        PositionData playerPositionBeforeShift = recentBoard.findPlayer(playerID);

        // targetBoard ist der Stand des Spielbretts nach dem Zug
        // deswegen geht targetBoard.findPlayer(currentPlayer.playerId); nicht
        PositionData playerPositionAfterMove = moveMessage.getNewPinPos();

        MoveStateCalculator moveStateCalculator = new MoveStateCalculator(moveMessage, recentBoard);
        // TODO direkt Karten erzeugen
        List<VectorInt2> shiftedCardsPositions = moveStateCalculator.getCardsToShift();
        // Konvertiert Positionen zu Karten, Problem MoveState Calc hat keinen
        // zugriff auf CardFX referenzen
        List<CardFX> movingCards = shiftedCardsPositions.stream().map(v -> boardCards[v.y][v.x])
                .collect(Collectors.toList());

        // Naming verwirrend aber passend
        VectorInt2 pushedOutCardPosition = moveStateCalculator.getPushedOutPlayersPosition();
        VectorInt2 pushedOutPlayerNewPosition = moveStateCalculator.getNewPlayerPosition();
        VectorInt2 shiftCardStart = moveStateCalculator.getShiftCardStart();
        VectorInt2 shiftDelta = moveStateCalculator.getShiftDelta();
        VectorInt2 preShiftPosition = VectorInt2.copy(playerPositionBeforeShift);
        VectorInt2 postShiftPosition = moveStateCalculator.getPlayerPositionAfterShift(preShiftPosition);

        // Fuegt die Shiftkarte ans Ende
        movingCards.add(shiftCard);
        CardFX pushedOutCard = boardCards[pushedOutCardPosition.y][pushedOutCardPosition.x];
        // Spielfiguren auf der herausgeschobenen Karte
        // Kann man eigentlich aus der Karte rausholen
        List<PlayerFX> pushedOutPlayers = players.values().stream()
                .filter(player -> player.getBoundCard() == pushedOutCard).collect(Collectors.toList());
        Translate3D pushedOutPlayersMoveTo = getCardTranslateForPosition(pushedOutPlayerNewPosition.x,
                pushedOutPlayerNewPosition.y);
        // Bewege Spielfiguren auf rausgeschobener Karte auf andere Seite des
        // Spielbrettes
        Animation movePushedOutPlayers = AnimationFactory.moveShiftedOutPlayers(pushedOutPlayers,
                pushedOutPlayersMoveTo, shiftCard, durationMove.multiply(4));

        FakeTranslateBinding pinBind = null;
        if (pin.getBoundCard() != null) {
            pinBind = new FakeTranslateBinding(pin, pin.getBoundCard(), pin.getOffset());
            pin.unbindFromCard();
            pinBind.bind();
        }
        FakeTranslateBinding pinBind_final = pinBind;

        CardFX shiftCardC = shiftCard;
        Card c = new Card(moveMessage.getShiftCard());
        PositionData newCardPos = moveMessage.getShiftPosition();
        int newRotation = c.getOrientation().value();
        // prevent rotating > 180°
        int oldRotation = shiftCardC.rotateProperty().intValue();
        int rotationDelta = newRotation - oldRotation;
        if (rotationDelta > 180) {
            shiftCardC.rotateProperty().setValue(oldRotation + 360);
        } else if (rotationDelta < -180) {
            shiftCardC.rotateProperty().setValue(oldRotation - 360);
        }

        Translate3D newCardBeforeShiftT = getCardTranslateForPosition(shiftCardStart.x, shiftCardStart.y); // getCardTranslateForShiftStart(newCardPos);

        // before before
        // TODO: less time for "before before" more time for "before"
        TranslateTransition animBeforeBefore = new TranslateTransition(durationAfter, shiftCardC);
        // animBeforeBefore.setToX(SHIFT_CARD_TRANSLATE.x);
        animBeforeBefore.setToY(SHIFT_CARD_TRANSLATE.y);
        // animBeforeBefore.setToZ(SHIFT_CARD_TRANSLATE.z);

        // before shift
        RotateTransition cardRotateBeforeT = new RotateTransition(durationBefore, shiftCardC);
        cardRotateBeforeT.setToAngle(newRotation);
        TranslateTransition cardTranslateBeforeT = new TranslateTransition(durationBefore, shiftCardC);
        cardTranslateBeforeT.setToX(newCardBeforeShiftT.x);
        cardTranslateBeforeT.setToY(newCardBeforeShiftT.y);
        cardTranslateBeforeT.setToZ(newCardBeforeShiftT.z);
        Animation animBefore = new ParallelTransition(cardRotateBeforeT,
                new SequentialTransition(animBeforeBefore, cardTranslateBeforeT));

        // shifting
        // invert delta shift, because graphics coordinates are the other way
        // round!
        Translate3D shiftTranslate = new Translate3D(shiftDelta.x, 0, -shiftDelta.y);
        updateAndGetShiftedCards(newCardPos);
        Animation[] shiftAnimations = new Animation[movingCards.size()];
        int i = 0;
        for (CardFX crd : movingCards) {
            TranslateTransition tmpTransitions = new TranslateTransition(durationShift, crd);
            tmpTransitions.setByX(shiftTranslate.x);
            tmpTransitions.setByY(shiftTranslate.y);
            tmpTransitions.setByZ(shiftTranslate.z);
            shiftAnimations[i++] = tmpTransitions;
        }
        Animation animationShift = new ParallelTransition(shiftAnimations);

        // after
        TranslateTransition animAfter = new TranslateTransition(durationAfter, shiftCard);
        animAfter.setToX(SHIFT_CARD_TRANSLATE.x);
        animAfter.setToY(SHIFT_CARD_TRANSLATE.y);
        animAfter.setToZ(SHIFT_CARD_TRANSLATE.z);

        recentBoard.proceedShift(moveMessage);

        Position from = new Position(postShiftPosition.y, postShiftPosition.x);
        Position to = new Position(playerPositionAfterMove);
        Timeline moveAnim = AnimationFactory.createMoveTimeline(recentBoard, from, to, pin, durationMove);

        // a little bit of time to switch focus from shifting to moving ^^
        Transition pause = new PauseTransition(Duration.millis(100));

        SequentialTransition allTransitions = new SequentialTransition(animBefore, animationShift, movePushedOutPlayers,
                pause, moveAnim);
        allTransitions.setInterpolator(Interpolator.LINEAR);
        allTransitions.setOnFinished(e -> {
            if (pinBind_final != null) {
                pinBind_final.unbind();
            }
            if (treasureReached) {
                boardCards[playerPositionAfterMove.getRow()][playerPositionAfterMove.getCol()].getTreasure()
                        .treasureFound();
            }
            pin.bindToCard(boardCards[playerPositionAfterMove.getRow()][playerPositionAfterMove.getCol()]);

            lock.countDown();
        });
        allTransitions.play();
        // Nachdem der move animiert wurde, wird das Board ersetzt durch das von
        // Game uebergebene Board
        // TODO: Aufpassen wegen race-condition
        this.recentBoard = (Board) targetBoard.clone();
    }

    public List<CardFX> updateAndGetShiftedCards(PositionData shiftPos) {
        List<CardFX> cards = new LinkedList<>();
        cards.add(shiftCard);
        CardFX oldShiftCard = shiftCard;

        if (shiftPos.getCol() == 0) {
            int sRow = shiftPos.getRow();
            cards.add(shiftCard = boardCards[sRow][BOARD_WIDTH - 1]);
            for (int x = BOARD_WIDTH - 1; x > 0; cards.add(boardCards[sRow][x] = boardCards[sRow][x - 1]), x--)
                ;
            boardCards[sRow][0] = oldShiftCard;
        } else if (shiftPos.getCol() == BOARD_WIDTH - 1) {
            int sRow = shiftPos.getRow();
            cards.add(shiftCard = boardCards[sRow][0]);
            for (int x = 0; x < BOARD_WIDTH - 1; cards.add(boardCards[sRow][x] = boardCards[sRow][x + 1]), x++)
                ;
            boardCards[sRow][BOARD_WIDTH - 1] = oldShiftCard;
        } else if (shiftPos.getRow() == 0) {
            int sCol = shiftPos.getCol();
            cards.add(shiftCard = boardCards[BOARD_HEIGHT - 1][sCol]);
            for (int z = BOARD_HEIGHT - 1; z > 0; cards.add(boardCards[z][sCol] = boardCards[z - 1][sCol]), z--)
                ;
            boardCards[0][sCol] = oldShiftCard;
        } else if (shiftPos.getRow() == BOARD_HEIGHT - 1) {
            int sCol = shiftPos.getCol();
            cards.add(shiftCard = boardCards[0][sCol]);
            for (int z = 0; z < BOARD_HEIGHT - 1; cards.add(boardCards[z][sCol] = boardCards[z + 1][sCol]), z++)
                ;
            boardCards[BOARD_HEIGHT - 1][sCol] = oldShiftCard;
        }

        return cards;
    }

    @Override
    public void clearBoard() {

        if (shiftCard != null) {
            shiftCard.removeFrom(scene3dRoot);
            shiftCard = null;
        }
        if (boardCards != null) {
            for (CardFX[] ca : boardCards) {
                for (CardFX c : ca) {
                    c.removeFrom(scene3dRoot);
                }
            }
            boardCards = null;
        }
        if (players != null) {
            scene3dRoot.getChildren().removeAll(players.values());
            players.clear();
        }
        this.recentBoard = null;
    }

    @Override
    public void initFromBoard(Board startBoard) {
        clearBoard();
        this.recentBoard = (Board) startBoard.clone();
        boardCards = new CardFX[BOARD_HEIGHT][BOARD_WIDTH];
        for (int z = 0; z < BOARD_HEIGHT; z++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                CardData card = recentBoard.getCard(z, x);
                CardFX card3d = new CardFX(card, scene3dRoot);
                boardCards[z][x] = card3d;
                getCardTranslateForPosition(x, z).applyTo(card3d);
                scene3dRoot.getChildren().add(card3d);
                CardData.Pin pin = card.getPin();
                if (pin != null) {
                    pin.getPlayerID().forEach(pid -> {
                        PlayerFX player = new PlayerFX(pid, card3d);
                        players.put(pid, player);
                        scene3dRoot.getChildren().add(player);
                    });
                }
            }
        }
        CardData card = recentBoard.getShiftCard();
        shiftCard = new CardFX(card, scene3dRoot);
        SHIFT_CARD_TRANSLATE.applyTo(shiftCard);
        scene3dRoot.getChildren().add(shiftCard);

    }

    @Override
    public void focusLost() {
        cameraRotationRight.stop();
        cameraRotationLeft.stop();
        cameraRotationUp.stop();
        cameraRotationDown.stop();
    }

    @Override
    public Pane getRoot() {
        return rootNode;
    }


}