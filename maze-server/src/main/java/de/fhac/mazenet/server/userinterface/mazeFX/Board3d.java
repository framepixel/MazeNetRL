package de.fhac.mazenet.server.userinterface.mazeFX;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import de.fhac.mazenet.server.userinterface.mazeFX.objects.BoardVisualisation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Board3d
 */
public class Board3d implements Initializable {
    @FXML
    private StackPane parent3D;

    @FXML
    private SubScene sub3D;

    @FXML
    private Pane cntrls3D;

    @FXML
    private Slider camZoomSlide;

    private List<Runnable> camRotateRightStartListeners = new LinkedList<>();
    private List<Runnable> camRotateRightStopListeners = new LinkedList<>();
    private List<Runnable> camRotateLeftStartListeners = new LinkedList<>();
    private List<Runnable> camRotateLeftStopListeners = new LinkedList<>();
    private List<Runnable> camRotateUpStartListeners = new LinkedList<>();
    private List<Runnable> camRotateUpStopListeners = new LinkedList<>();
    private List<Runnable> camRotateDownStartListeners = new LinkedList<>();
    private List<Runnable> camRotateDownStopListeners = new LinkedList<>();
    private List<Runnable> camRotateDefaultListener = new LinkedList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void camRotRightBtMousePress(MouseEvent evt) {
        camRotateRightStartListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotRightBtMouseRelease(MouseEvent evt) {
        camRotateRightStopListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotLeftBtMousePress(MouseEvent evt) {
        camRotateLeftStartListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotLeftBtMouseRelease(MouseEvent evt) {
        camRotateLeftStopListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotUpBtMousePress(MouseEvent evt) {
        camRotateUpStartListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotUpBtMouseRelease(MouseEvent evt) {
        camRotateUpStopListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotDownBtMousePress(MouseEvent evt) {
        camRotateDownStartListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotDownBtMouseRelease(MouseEvent evt) {
        camRotateDownStopListeners.forEach(r -> r.run());
    }

    @FXML
    private void camRotateDefaultPress(MouseEvent evt) {
        camRotateDefaultListener.forEach(r -> r.run());
    }

    public Pane getParent3D() {
        return parent3D;
    }

    public SubScene getSub3D() {
        return sub3D;
    }

    public Slider getCamZoomSlide() {
        return camZoomSlide;
    }

    public void addCamRotateRightStartListener(Runnable r) {
        camRotateRightStartListeners.add(r);
    }

    public void addCamRotateRightStopListener(Runnable r) {
        camRotateRightStopListeners.add(r);
    }

    public void addCamRotateLeftStartListener(Runnable r) {
        camRotateLeftStartListeners.add(r);
    }

    public void addCamRotateLeftStopListener(Runnable r) {
        camRotateLeftStopListeners.add(r);
    }

    public void addCamRotateUpStartListener(Runnable r) {
        camRotateUpStartListeners.add(r);
    }

    public void addCamRotateUpStopListener(Runnable r) {
        camRotateUpStopListeners.add(r);
    }

    public void addCamRotateDownStartListener(Runnable r) {
        camRotateDownStartListeners.add(r);
    }

    public void addCamRotateDownStopListener(Runnable r) {
        camRotateDownStopListeners.add(r);
    }

    public void addCamRotateDefaultListener(Runnable r) {
        camRotateDefaultListener.add(r);
    }

}