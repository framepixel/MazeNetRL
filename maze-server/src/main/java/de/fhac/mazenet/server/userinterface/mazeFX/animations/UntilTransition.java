package de.fhac.mazenet.server.userinterface.mazeFX.animations;

/**
 * Created by harald on 26.07.17.
 */

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;

/**
 * This Transition implementation adds a specified number to a DoubleProperty
 * per animation cycle.
 */
public class UntilTransition extends Transition {
    /**
     * Property to change
     */
    private DoubleProperty target;
    /**
     * Amount to add (per cycle)
     */
    private double increasePerCycle;
    /**
     * Last fraction, used for delta calculation
     */
    private double lastFrac = 0;

    /**
     * Upper (total) limit. In case of a none finite value, no limit is assumed.
     * <p>
     * (Default: NaN)
     */
    private double targetValue = Double.NaN;

    /**
     * Constructor
     *
     * @param cycleDuration
     *            duration of one animation cycle
     * @param target
     *            property to change
     * @param increasePerCycle
     *            amount to add (per cycle)
     * @param targetValue
     *            Value that stops the animation
     */
    public UntilTransition(Duration cycleDuration, DoubleProperty target, double increasePerCycle,
            double targetValue) {
        setCycleDuration(cycleDuration);
        this.target = target;
        this.targetValue = targetValue;
        this.increasePerCycle = Math.abs(increasePerCycle);
        this.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Status.STOPPED) {
                lastFrac = 0;
            }
        });
    }

    @Override
    protected void interpolate(double frac) {
        double fracDelta = frac - lastFrac;

        // check for new cycle
        if (fracDelta < 0 && !isAutoReverse()) {
            fracDelta = 1 - fracDelta;
        }

        double add = Math.signum(targetValue - target.getValue()) * fracDelta * increasePerCycle;
        double newVal = target.getValue() + add;

        // apply limits
        if (Math.abs(targetValue - target.getValue()) < 5) {
            newVal = targetValue;
        }

        target.setValue(newVal);

        lastFrac = frac;
    }
}
