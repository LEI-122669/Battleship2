package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Ship.
 *
 * Author: LEI124359
 * Date: 2026-11-06
 *
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - buildShip: 6
 * - getCategory: 1
 * - getSize: 1
 * - getBearing: 1
 * - getPositions: 1
 * - getPosition: 1
 * - getAdjacentPositions: 3
 * - stillFloating: 2
 * - shoot: 2
 * - occupies: 2
 * - tooCloseTo (IShip): 2
 * - tooCloseTo (IPosition): 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 * - isInsideBoard: 2
 * - sink: 1
 * - toString: 1
 */
public class ShipTest {

    private Ship ship;
    private Ship shipBorder;

    @BeforeEach
    void setUp() {
        // Instantiate using concrete subclass Barge
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        ship = null;
        shipBorder = null;
    }

    // ==========================================
    // 1. Factory Method Tests (buildShip)
    // ==========================================

    @Test
    @DisplayName("Test buildShip factory method for all ship types and default case")
    void testBuildShipAllTypes() {
        assertAll("Factory method buildShip tests",
                () -> assertTrue(Ship.buildShip("barca", Compass.NORTH, new Position(1, 1)) instanceof Barge, "Should build Barge"),
                () -> assertTrue(Ship.buildShip("caravela", Compass.EAST, new Position(1, 1)) instanceof Caravel, "Should build Caravel"),
                () -> assertTrue(Ship.buildShip("nau", Compass.SOUTH, new Position(1, 1)) instanceof Carrack, "Should build Carrack"),
                () -> assertTrue(Ship.buildShip("fragata", Compass.WEST, new Position(1, 1)) instanceof Frigate, "Should build Frigate"),
                () -> assertTrue(Ship.buildShip("galeao", Compass.NORTH, new Position(1, 1)) instanceof Galleon, "Should build Galleon"),
                () -> assertNull(Ship.buildShip("unknown_type", Compass.NORTH, new Position(1, 1)), "Should return null for unknown ship kind"),
                () -> assertNull(Ship.buildShip("", Compass.NORTH, new Position(1, 1)), "Should return null for empty ship kind")
        );
    }

    // ==========================================
    // 2. Basic Getters, Constructor & ToString
    // ==========================================

    @Test
    @DisplayName("Test for the constructor and basic attributes")
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
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
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    @Test
    @DisplayName("Test for getSize method")
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    @Test
    @DisplayName("Test for getBearing method")
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    @Test
    @DisplayName("Test for getPosition method")
    void testGetPosition() {
        assertNotNull(ship.getPosition(), "Error: Position should not be null.");
        assertEquals(5, ship.getPosition().getRow(), "Error: Row should be 5.");
        assertEquals(5, ship.getPosition().getColumn(), "Error: Column should be 5.");
    }

    @Test
    @DisplayName("Test for getPositions method")
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        String str = ship.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Barca"), "toString should contain ship category");
    }

    // ==========================================
    // 3. Extrema & Boundaries (Top, Bottom, Left, Right)
    // ==========================================

    @Test
    @DisplayName("Test boundary getters for single position ship")
    void testSinglePositionBounds() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    @Test
    @DisplayName("Test boundary getters for multi-deck ship (SOUTH & EAST)")
    void testMultiPositionShipBounds() {
        Ship frigateSouth = new Frigate(Compass.SOUTH, new Position(2, 3));
        assertTrue(frigateSouth.getTopMostPos() <= frigateSouth.getBottomMostPos());
        assertTrue(frigateSouth.getLeftMostPos() <= frigateSouth.getRightMostPos());

        Ship frigateEast = new Frigate(Compass.EAST, new Position(4, 4));
        assertEquals(4, frigateEast.getTopMostPos());
        assertEquals(4, frigateEast.getLeftMostPos());
    }

    @Test
    @DisplayName("Test boundary getters for WEST and NORTH bearings")
    void testAllBearingsBoundaries() {
        Ship shipWest = new Frigate(Compass.WEST, new Position(5, 5));
        assertEquals(5, shipWest.getTopMostPos());
        assertEquals(5, shipWest.getLeftMostPos());

        Ship shipNorth = new Frigate(Compass.NORTH, new Position(5, 5));
        assertEquals(5, shipNorth.getTopMostPos());
        assertEquals(5, shipNorth.getLeftMostPos());
    }

    // ==========================================
    // 4. Floating, Shooting & Sink Tests
    // ==========================================

    @Test
    @DisplayName("Test stillFloating when intact")
    void testStillFloatingIntact() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    @Test
    @DisplayName("Test stillFloating when fully hit")
    void testStillFloatingSunk() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    @Test
    @DisplayName("Test stillFloating when partially hit")
    void testStillFloatingPartiallyHit() {
        Ship frigate = new Frigate(Compass.EAST, new Position(2, 3));
        frigate.shoot(new Position(2, 3));
        assertTrue(frigate.stillFloating(), "Error: Frigate should still float when only partially hit.");
    }

    @Test
    @DisplayName("Test sink method")
    void testSink() {
        ship.sink();
        assertFalse(ship.stillFloating(), "Error: Ship should not float after sink().");
    }

    @Test
    @DisplayName("Test shoot on valid target position")
    void testShootHit() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    @Test
    @DisplayName("Test shoot on miss position")
    void testShootMiss() {
        Position target = new Position(0, 0);
        ship.shoot(target);
        assertFalse(ship.getPositions().get(0).isHit(), "Error: Position should not be marked as hit on miss.");
    }

    // ==========================================
    // 5. Occupies & Proximity Tests
    // ==========================================

    @Test
    @DisplayName("Test occupies with occupied position")
    void testOccupiesTrue() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    @Test
    @DisplayName("Test occupies with non-occupied position")
    void testOccupiesFalse() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    @Test
    @DisplayName("Test tooCloseTo with another IShip (close and far)")
    void testTooCloseToShip() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));

        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    @Test
    @DisplayName("Test tooCloseTo with IPosition (adjacent and far)")
    void testTooCloseToPosition() {
        Position closePos = new Position(5, 6);
        Position farPos = new Position(7, 7);

        assertTrue(ship.tooCloseTo(closePos), "Error: Ship should be too close to adjacent position.");
        assertFalse(ship.tooCloseTo(farPos), "Error: Ship should not be too close to far position.");
    }

    @Test
    @DisplayName("Test tooCloseTo diagonal and edge cases")
    void testTooCloseToEdgeCases() {
        Ship diagShip = new Barge(Compass.NORTH, new Position(4, 4));
        assertTrue(ship.tooCloseTo(diagShip), "Diagonally adjacent ships must return true");
    }

    // ==========================================
    // 6. Board Bounds & Adjacent Positions
    // ==========================================

    @Test
    @DisplayName("Test isInsideBoard inside and outside boundaries")
    void testIsInsideBoard() {
        assertTrue(ship.isInsideBoard(), "Ship at (5,5) should be inside board");

        Ship shipOutsideTop = new Barge(Compass.NORTH, new Position(-1, 5));
        Ship shipOutsideRight = new Barge(Compass.NORTH, new Position(5, Game.BOARD_SIZE + 2));

        assertFalse(shipOutsideTop.isInsideBoard(), "Negative row should not be inside board");
        assertFalse(shipOutsideRight.isInsideBoard(), "Out of bounds column should not be inside board");
    }

    @Test
    @DisplayName("Test getAdjacentPositions for center and border ships")
    void testGetAdjacentPositions() {
        List<IPosition> adjCenter = ship.getAdjacentPositions();
        assertEquals(8, adjCenter.size(), "Center barge should have 8 adjacent positions.");

        List<IPosition> adjBorder = shipBorder.getAdjacentPositions();
        assertFalse(adjBorder.isEmpty(), "Border barge should have valid adjacent positions.");

        Ship shipBottomRight = new Barge(Compass.NORTH, new Position(Game.BOARD_SIZE - 1, Game.BOARD_SIZE - 1));
        assertNotNull(shipBottomRight.getAdjacentPositions(), "Edge ship adjacent positions should not be null");
    }
    // ==========================================
    // 7. Additional Edge Case Tests for 100% Branch Coverage
    // ==========================================

    @Test
    @DisplayName("Test tooCloseTo with another IShip when not close")
    void testTooCloseToShipFalse() {
        Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
        Ship ship2 = new Barge(Compass.NORTH, new Position(5, 5));
        assertFalse(ship1.tooCloseTo(ship2), "Ships far apart should return false");
    }

    @Test
    @DisplayName("Test isInsideBoard for left and bottom boundaries out of bounds")
    void testIsInsideBoardAllBoundaries() {
        Ship shipLeftOut = new Barge(Compass.NORTH, new Position(5, -1));
        Ship shipBottomOut = new Barge(Compass.NORTH, new Position(Game.BOARD_SIZE + 2, 5));

        assertFalse(shipLeftOut.isInsideBoard(), "Negative column should return false");
        assertFalse(shipBottomOut.isInsideBoard(), "Row beyond board size should return false");
    }
}