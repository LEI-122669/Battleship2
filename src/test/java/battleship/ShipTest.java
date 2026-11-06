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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("Test buildShip factory method for all ship types and default case")
    void testBuildShipAllTypes() {
        assertAll("Factory method buildShip tests",
                () -> assertTrue(Ship.buildShip("barca", Compass.NORTH, new Position(1, 1)) instanceof Barge),
                () -> assertTrue(Ship.buildShip("caravela", Compass.EAST, new Position(1, 1)) instanceof Caravel),
                () -> assertTrue(Ship.buildShip("nau", Compass.SOUTH, new Position(1, 1)) instanceof Carrack),
                () -> assertTrue(Ship.buildShip("fragata", Compass.WEST, new Position(1, 1)) instanceof Frigate),
                () -> assertTrue(Ship.buildShip("galeao", Compass.NORTH, new Position(1, 1)) instanceof Galleon),
                () -> assertNull(Ship.buildShip("unknown_type", Compass.NORTH, new Position(1, 1))),
                () -> assertNull(Ship.buildShip("", Compass.NORTH, new Position(1, 1)))
        );
    }

    @Test
    @DisplayName("Test for the constructor and basic attributes")
    void testConstructor() {
        assertNotNull(ship);
        assertEquals("Barca", ship.getCategory());
        assertEquals(Compass.NORTH, ship.getBearing());
        assertEquals(1, ship.getSize());
        assertFalse(ship.getPositions().isEmpty());
    }

    @Test
    @DisplayName("Test Constructor Exception Handling")
    void testConstructorNullHandling() {
        assertAll("Constructor Null Checks",
                () -> assertThrows(NullPointerException.class, () -> new Barge(null, new Position(1, 1))),
                () -> assertThrows(NullPointerException.class, () -> new Barge(Compass.NORTH, null))
        );
    }

    @Test
    @DisplayName("Test for getCategory method")
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory());
    }

    @Test
    @DisplayName("Test for getSize method")
    void testGetSize() {
        assertEquals(1, ship.getSize());
    }

    @Test
    @DisplayName("Test for getBearing method")
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing());
    }

    @Test
    @DisplayName("Test for getPosition method")
    void testGetPosition() {
        assertNotNull(ship.getPosition());
        assertEquals(5, ship.getPosition().getRow());
        assertEquals(5, ship.getPosition().getColumn());
    }

    @Test
    @DisplayName("Test for getPositions method")
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions);
        assertEquals(1, positions.size());
        assertEquals(5, positions.get(0).getRow());
        assertEquals(5, positions.get(0).getColumn());
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
    @DisplayName("Test boundary getters for multi-deck ship (SOUTH & EAST)")
    void testMultiPositionShipBounds() {
        IShip frigateSouth = new Frigate(Compass.SOUTH, new Position(2, 3));
        assertTrue(frigateSouth.getTopMostPos() <= frigateSouth.getBottomMostPos());
        assertTrue(frigateSouth.getLeftMostPos() <= frigateSouth.getRightMostPos());

        IShip frigateEast = new Frigate(Compass.EAST, new Position(4, 4));
        assertEquals(4, frigateEast.getTopMostPos());
        assertEquals(4, frigateEast.getLeftMostPos());
    }

    @Test
    @DisplayName("Test boundary getters for WEST and NORTH bearings")
    void testAllBearingsBoundaries() {
        IShip shipWest = new Frigate(Compass.WEST, new Position(5, 5));
        assertEquals(5, shipWest.getTopMostPos());
        assertEquals(5, shipWest.getLeftMostPos());

        IShip shipNorth = new Frigate(Compass.NORTH, new Position(5, 5));
        assertEquals(5, shipNorth.getTopMostPos());
        assertEquals(5, shipNorth.getLeftMostPos());
    }

    @Test
    @DisplayName("Test stillFloating when intact")
    void testStillFloatingIntact() {
        assertTrue(ship.stillFloating());
    }

    @Test
    @DisplayName("Test stillFloating when fully hit")
    void testStillFloatingSunk() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating());
    }

    @Test
    @DisplayName("Test stillFloating when partially hit")
    void testStillFloatingPartiallyHit() {
        IShip frigate = new Frigate(Compass.EAST, new Position(2, 3));
        frigate.shoot(new Position(2, 3));
        assertTrue(frigate.stillFloating());
    }

    @Test
    @DisplayName("Test sink method")
    void testSink() {
        ship.sink();
        assertFalse(ship.stillFloating());
    }

    @Test
    @DisplayName("Test shoot on valid target position")
    void testShootHit() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit());
    }

    @Test
    @DisplayName("Test shoot on miss position")
    void testShootMiss() {
        Position target = new Position(0, 0);
        ship.shoot(target);
        assertFalse(ship.getPositions().get(0).isHit());
    }

    @Test
    @DisplayName("Test occupies with occupied position")
    void testOccupiesTrue() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos));
    }

    @Test
    @DisplayName("Test occupies with non-occupied position")
    void testOccupiesFalse() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos));
    }

    @Test
    @DisplayName("Test tooCloseTo with another IShip (close and far)")
    void testTooCloseToShip() {
        IShip nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        IShip farShip = new Barge(Compass.NORTH, new Position(10, 10));

        assertTrue(ship.tooCloseTo(nearbyShip));
        assertFalse(ship.tooCloseTo(farShip));
    }

    @Test
    @DisplayName("Test tooCloseTo with IPosition (adjacent and far)")
    void testTooCloseToPosition() {
        Position closePos = new Position(5, 6);
        Position farPos = new Position(7, 7);

        assertTrue(ship.tooCloseTo(closePos));
        assertFalse(ship.tooCloseTo(farPos));
    }

    @Test
    @DisplayName("Test tooCloseTo diagonal and edge cases")
    void testTooCloseToEdgeCases() {
        IShip diagShip = new Barge(Compass.NORTH, new Position(4, 4));
        assertTrue(ship.tooCloseTo(diagShip));
    }

    @Test
    @DisplayName("Test isInsideBoard inside and outside boundaries")
    void testIsInsideBoard() {
        assertTrue(ship.isInsideBoard());

        IShip shipOutsideTop = new Barge(Compass.NORTH, new Position(-1, 5));
        IShip shipOutsideRight = new Barge(Compass.NORTH, new Position(5, Game.BOARD_SIZE + 2));

        assertFalse(shipOutsideTop.isInsideBoard());
        assertFalse(shipOutsideRight.isInsideBoard());
    }

    @Test
    @DisplayName("Test getAdjacentPositions for center and border ships")
    void testGetAdjacentPositions() {
        List<IPosition> adjCenter = ship.getAdjacentPositions();
        assertEquals(8, adjCenter.size());

        IShip shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
        assertFalse(shipBorder.getAdjacentPositions().isEmpty());

        IShip shipBottomRight = new Barge(Compass.NORTH, new Position(Game.BOARD_SIZE - 1, Game.BOARD_SIZE - 1));
        assertFalse(shipBottomRight.getAdjacentPositions().isEmpty());
    }

    @Test
    @DisplayName("Test tooCloseTo with another IShip when not close")
    void testTooCloseToShipFalse() {
        IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
        IShip ship2 = new Barge(Compass.NORTH, new Position(5, 5));
        assertFalse(ship1.tooCloseTo(ship2));
    }

    @Test
    @DisplayName("Test isInsideBoard for left and bottom boundaries out of bounds")
    void testIsInsideBoardAllBoundaries() {
        IShip shipLeftOut = new Barge(Compass.NORTH, new Position(5, -1));
        IShip shipBottomOut = new Barge(Compass.NORTH, new Position(Game.BOARD_SIZE + 2, 5));

        assertFalse(shipLeftOut.isInsideBoard());
        assertFalse(shipBottomOut.isInsideBoard());
    }
}