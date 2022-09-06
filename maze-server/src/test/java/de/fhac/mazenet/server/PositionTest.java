package de.fhac.mazenet.server;

import de.fhac.mazenet.server.game.Position;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PositionTest {
    @Test
    public void isLooseShiftPosition() {
        // Zeile 0
        assertFalse(new Position(0, 0).isLooseShiftPosition());
        assertTrue(new Position(0, 1).isLooseShiftPosition());
        assertFalse(new Position(0, 2).isLooseShiftPosition());
        assertTrue(new Position(0, 3).isLooseShiftPosition());
        assertFalse(new Position(0, 4).isLooseShiftPosition());
        assertTrue(new Position(0, 5).isLooseShiftPosition());
        assertFalse(new Position(0, 6).isLooseShiftPosition());
        // Zeile 2
        assertFalse(new Position(2, 0).isLooseShiftPosition());
        assertFalse(new Position(2, 6).isLooseShiftPosition());
        // Zeile 3
        assertTrue(new Position(3, 0).isLooseShiftPosition());
        assertTrue(new Position(3, 6).isLooseShiftPosition());
        // Zeile 4
        assertFalse(new Position(4, 0).isLooseShiftPosition());
        assertFalse(new Position(4, 6).isLooseShiftPosition());
        // Zeile 5
        assertTrue(new Position(5, 0).isLooseShiftPosition());
        assertTrue(new Position(5, 6).isLooseShiftPosition());
        // Zeile 6
        assertFalse(new Position(6, 0).isLooseShiftPosition());
        assertTrue(new Position(6, 1).isLooseShiftPosition());
        assertFalse(new Position(6, 2).isLooseShiftPosition());
        assertTrue(new Position(6, 3).isLooseShiftPosition());
        assertFalse(new Position(6, 4).isLooseShiftPosition());
        assertTrue(new Position(6, 5).isLooseShiftPosition());
        assertFalse(new Position(6, 6).isLooseShiftPosition());
    }
}
