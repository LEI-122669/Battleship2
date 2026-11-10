package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    private IShip ship;

    @BeforeEach
    void setUp() {
        ship = new Barge(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        ship = null;
    }

    @Test
    @DisplayName("Test buildShip factory method for standard ships")
    void testBuildShipStandardTypes() {
        assertInstanceOf(Barge.class, Ship.buildShip("barca", Compass.NORTH, new Position(1, 1)));
        assertInstanceOf(Caravel.class, Ship.buildShip("caravela", Compass.EAST, new Position(1, 1)));
        assertInstanceOf(Carrack.class, Ship.buildShip("nau", Compass.SOUTH, new Position(1, 1)));
    }

    @Test
    @DisplayName("Test buildShip factory method for heavy ships")
    void testBuildShipHeavyTypes() {
        assertInstanceOf(Frigate.class, Ship.buildShip("fragata", Compass.WEST, new Position(1, 1)));
        assertInstanceOf(Galleon.class, Ship.buildShip("galeao", Compass.NORTH, new Position(1, 1)));
    }

    @Test
    @DisplayName("Test buildShip factory method for invalid inputs")
    void testBuildShipInvalidInputs() {
        assertNull(Ship.buildShip("unknown_type", Compass.NORTH, new Position(1, 1)));
        assertNull(Ship.buildShip("", Compass.NORTH, new Position(1, 1)));
    }

    @Test
    @DisplayName("Test constructor and basic attributes")
    void testConstructor() {
        assertNotNull(ship);
        assertEquals("Barca", ship.getCategory());
        assertEquals(Compass.NORTH, ship.getBearing());
        assertEquals(1, ship.getSize());
        assertFalse(ship.getPositions().isEmpty());
    }

    @Test
    @DisplayName("Test constructor null handling")
    void testConstructorNullHandling() {
        assertThrows(NullPointerException.class, () -> new Barge(null, new Position(1, 1)));
        assertThrows(NullPointerException.class, () -> new Barge(Compass.NORTH, null));
    }

    @Test
    @DisplayName("Test getPosition and getPositions methods")
    void testGetPositions() {
        assertNotNull(ship.getPosition());
        assertEquals(5, ship.getPosition().getRow());
        assertEquals(5, ship.getPosition().getColumn());

        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions);
        assertEquals(1, positions.size());
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        String str = ship.toString();
        assertNotNull(str);
        assertTrue(str.contains("Barca"));
    }

    @Test
    @DisplayName("Test boundary getters for single position ship")
    void testSinglePositionBounds() {
        assertEquals(5, ship.getTopMostPos());
        assertEquals(5, ship.getBottomMostPos());
        assertEquals(5, ship.getLeftMostPos());
        assertEquals(5, ship.getRightMostPos());
    }

    @Test
    @DisplayName("Test boundary getters for multi-deck ship")
    void testMultiPositionShipBounds() {
        IShip frigateSouth = new Frigate(Compass.SOUTH, new Position(2, 3));
        assertTrue(frigateSouth.getTopMostPos() <= frigateSouth.getBottomMostPos());
        assertTrue(frigateSouth.getLeftMostPos() <= frigateSouth.getRightMostPos());
    }

    @Test
    @DisplayName("Test stillFloating state transitions")
    void testStillFloatingStates() {
        assertTrue(ship.stillFloating());
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating());
    }

    @Test
    @DisplayName("Test sink method")
    void testSink() {
        ship.sink();
        assertFalse(ship.stillFloating());
    }

    @Test
    @DisplayName("Test shoot on hit and miss")
    void testShootTarget() {
        ship.shoot(new Position(5, 5));
        assertTrue(ship.getPositions().get(0).isHit());

        IShip anotherShip = new Barge(Compass.NORTH, new Position(3, 3));
        anotherShip.shoot(new Position(0, 0));
        assertFalse(anotherShip.getPositions().get(0).isHit());
    }

    @Test
    @DisplayName("Test occupies method")
    void testOccupies() {
        assertTrue(ship.occupies(new Position(5, 5)));
        assertFalse(ship.occupies(new Position(1, 1)));
    }

    @Test
    @DisplayName("Test tooCloseTo with another IShip")
    void testTooCloseToShip() {
        IShip nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        IShip farShip = new Barge(Compass.NORTH, new Position(10, 10));

        assertTrue(ship.tooCloseTo(nearbyShip));
        assertFalse(ship.tooCloseTo(farShip));
    }

    @Test
    @DisplayName("Test tooCloseTo with Position")
    void testTooCloseToPosition() {
        assertTrue(ship.tooCloseTo(new Position(5, 6)));
        assertFalse(ship.tooCloseTo(new Position(7, 7)));
    }

    @Test
    @DisplayName("Test isInsideBoard boundary checks")
    void testIsInsideBoard() {
        assertTrue(ship.isInsideBoard());

        IShip shipOutside = new Barge(Compass.NORTH, new Position(-1, 5));
        assertFalse(shipOutside.isInsideBoard());
    }

    @Test
    @DisplayName("Test getAdjacentPositions")
    void testGetAdjacentPositions() {
        List<IPosition> adjCenter = ship.getAdjacentPositions();
        assertEquals(8, adjCenter.size());

        IShip shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
        assertFalse(shipBorder.getAdjacentPositions().isEmpty());
    }
}