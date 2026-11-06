package battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrigateTest {

    private IShip frigate;

    @BeforeEach
    void setUp() {
        frigate = new Frigate(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        frigate = null;
    }

    @Test
    @DisplayName("Test Frigate constructor with NORTH bearing")
    void testConstructorNorth() {
        assertNotNull(frigate);
        assertEquals("Fragata", frigate.getCategory());
        assertEquals(Compass.NORTH, frigate.getBearing());
        assertEquals(4, frigate.getSize());

        List<IPosition> positions = frigate.getPositions();
        assertEquals(4, positions.size());
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(6, 5), positions.get(1));
        assertEquals(new Position(7, 5), positions.get(2));
        assertEquals(new Position(8, 5), positions.get(3));
    }

    @Test
    @DisplayName("Test Frigate constructor with SOUTH bearing")
    void testConstructorSouth() {
        IShip frigateSouth = new Frigate(Compass.SOUTH, new Position(5, 5));
        List<IPosition> positions = frigateSouth.getPositions();

        assertNotNull(frigateSouth);
        assertEquals(Compass.SOUTH, frigateSouth.getBearing());
        assertEquals(4, positions.size());
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(6, 5), positions.get(1));
        assertEquals(new Position(7, 5), positions.get(2));
        assertEquals(new Position(8, 5), positions.get(3));
    }

    @Test
    @DisplayName("Test Frigate constructor with EAST bearing")
    void testConstructorEast() {
        frigate = new Frigate(Compass.EAST, new Position(5, 5));
        List<IPosition> positions = frigate.getPositions();

        assertNotNull(frigate);
        assertEquals(4, positions.size());
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(5, 6), positions.get(1));
        assertEquals(new Position(5, 7), positions.get(2));
        assertEquals(new Position(5, 8), positions.get(3));
    }

    @Test
    @DisplayName("Test Frigate constructor with WEST bearing")
    void testConstructorWest() {
        frigate = new Frigate(Compass.WEST, new Position(5, 5));
        List<IPosition> positions = frigate.getPositions();

        assertNotNull(frigate);
        assertEquals(4, positions.size());
        assertEquals(new Position(5, 5), positions.get(0));
        assertEquals(new Position(5, 6), positions.get(1));
        assertEquals(new Position(5, 7), positions.get(2));
        assertEquals(new Position(5, 8), positions.get(3));
    }

    @Test
    @DisplayName("Test Frigate getSize method")
    void testGetSize() {
        assertEquals(4, frigate.getSize());
    }

    @Test
    @DisplayName("Test stillFloating with no hits")
    void testStillFloating1() {
        assertTrue(frigate.stillFloating());
    }

    @Test
    @DisplayName("Test stillFloating with partial hit")
    void testStillFloating2() {
        frigate.getPositions().get(0).shoot();
        assertTrue(frigate.stillFloating());
    }

    @Test
    @DisplayName("Test stillFloating with all positions hit")
    void testStillFloating3() {
        frigate.getPositions().forEach(IPosition::shoot);
        assertFalse(frigate.stillFloating());
    }

    @Test
    @DisplayName("Test Frigate boundary getters (Top, Bottom, Left, Right)")
    void testBoundaryGetters() {
        assertEquals(5, frigate.getTopMostPos());
        assertEquals(8, frigate.getBottomMostPos());
        assertEquals(5, frigate.getLeftMostPos());
        assertEquals(5, frigate.getRightMostPos());
    }

    @Test
    @DisplayName("Test Frigate constructor null exception handling")
    void testConstructorInvalidInput() {
        assertThrows(NullPointerException.class, () -> new Frigate(null, new Position(1, 1)));
        assertThrows(NullPointerException.class, () -> new Frigate(Compass.NORTH, null));
    }
}