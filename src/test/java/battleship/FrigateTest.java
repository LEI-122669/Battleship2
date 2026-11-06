package battleship;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Frigate class.
 *
 * Author: LEI124359
 * Date: 2026-11-06
 *
 * Cyclomatic Complexity for each method:
 * - Constructor: 5
 * - getSize: 1
 * - stillFloating: 2
 * - getPositions: 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 */
public class FrigateTest {

	private Frigate frigate;

	@BeforeEach
	void setUp() {
		frigate = new Frigate(Compass.NORTH, new Position(5, 5));
	}

	@AfterEach
	void tearDown() {
		frigate = null;
	}

	/**
	 * Test for the constructor with NORTH bearing.
	 */
	@Test
	@DisplayName("Test Frigate constructor with NORTH bearing")
	void testConstructorNorth() {
		assertNotNull(frigate, "Error: Frigate instance should not be null.");
		assertEquals("Fragata", frigate.getCategory(), "Error: Frigate category should be 'Fragata'.");
		assertEquals(Compass.NORTH, frigate.getBearing(), "Error: Frigate bearing is incorrect.");
		assertEquals(4, frigate.getSize(), "Error: Frigate size should be 4.");

		List<IPosition> positions = frigate.getPositions();
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for NORTH.");
		assertEquals(new Position(6, 5), positions.get(1), "Error: Second position is incorrect for NORTH.");
		assertEquals(new Position(7, 5), positions.get(2), "Error: Third position is incorrect for NORTH.");
		assertEquals(new Position(8, 5), positions.get(3), "Error: Fourth position is incorrect for NORTH.");
	}

	/**
	 * Test for the constructor with SOUTH bearing.
	 */
	@Test
	@DisplayName("Test Frigate constructor with SOUTH bearing")
	void testConstructorSouth() {
		Frigate frigateSouth = new Frigate(Compass.SOUTH, new Position(5, 5));
		List<IPosition> positions = frigateSouth.getPositions();

		assertNotNull(frigateSouth, "Error: Frigate instance should not be null.");
		assertEquals(Compass.SOUTH, frigateSouth.getBearing(), "Error: Frigate bearing is incorrect.");
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for SOUTH.");
		assertEquals(new Position(6, 5), positions.get(1), "Error: Second position is incorrect for SOUTH.");
		assertEquals(new Position(7, 5), positions.get(2), "Error: Third position is incorrect for SOUTH.");
		assertEquals(new Position(8, 5), positions.get(3), "Error: Fourth position is incorrect for SOUTH.");
	}

	/**
	 * Test for the constructor with EAST bearing.
	 */
	@Test
	@DisplayName("Test Frigate constructor with EAST bearing")
	void testConstructorEast() {
		frigate = new Frigate(Compass.EAST, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();

		assertNotNull(frigate, "Error: Frigate instance should not be null.");
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for EAST.");
		assertEquals(new Position(5, 6), positions.get(1), "Error: Second position is incorrect for EAST.");
		assertEquals(new Position(5, 7), positions.get(2), "Error: Third position is incorrect for EAST.");
		assertEquals(new Position(5, 8), positions.get(3), "Error: Fourth position is incorrect for EAST.");
	}

	/**
	 * Test for the constructor with WEST bearing.
	 */
	@Test
	@DisplayName("Test Frigate constructor with WEST bearing")
	void testConstructorWest() {
		frigate = new Frigate(Compass.WEST, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();

		assertNotNull(frigate, "Error: Frigate instance should not be null.");
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for WEST.");
		assertEquals(new Position(5, 6), positions.get(1), "Error: Second position is incorrect for WEST.");
		assertEquals(new Position(5, 7), positions.get(2), "Error: Third position is incorrect for WEST.");
		assertEquals(new Position(5, 8), positions.get(3), "Error: Fourth position is incorrect for WEST.");
	}

	/**
	 * Test for the getSize method.
	 */
	@Test
	@DisplayName("Test Frigate getSize method")
	void testGetSize() {
		assertEquals(4, frigate.getSize(), "Error: Frigate size should be 4.");
	}

	/**
	 * Test for the stillFloating method (all positions intact).
	 */
	@Test
	@DisplayName("Test stillFloating with no hits")
	void testStillFloating1() {
		assertTrue(frigate.stillFloating(), "Error: Frigate should still be floating.");
	}

	/**
	 * Test for the stillFloating method (one position hit).
	 */
	@Test
	@DisplayName("Test stillFloating with partial hit")
	void testStillFloating2() {
		frigate.getPositions().get(0).shoot();
		assertTrue(frigate.stillFloating(), "Error: Frigate should still be floating with one hit position.");
	}

	/**
	 * Test for the stillFloating method (all positions hit).
	 */
	@Test
	@DisplayName("Test stillFloating with all positions hit")
	void testStillFloating3() {
		frigate.getPositions().forEach(IPosition::shoot);
		assertFalse(frigate.stillFloating(), "Error: Frigate should not be floating if all positions are hit.");
	}

	/**
	 * Test for boundary position getters.
	 */
	@Test
	@DisplayName("Test Frigate boundary getters (Top, Bottom, Left, Right)")
	void testBoundaryGetters() {
		assertEquals(5, frigate.getTopMostPos(), "Error: The topmost position should be 5.");
		assertEquals(8, frigate.getBottomMostPos(), "Error: The bottommost position should be 8.");
		assertEquals(5, frigate.getLeftMostPos(), "Error: The leftmost position should be 5.");
		assertEquals(5, frigate.getRightMostPos(), "Error: The rightmost position should be 5.");
	}

	/**
	 * Test for the constructor with invalid input (null).
	 */
	@Test
	@DisplayName("Test Frigate constructor null exception handling")
	void testConstructorInvalidInput() {
		assertThrows(NullPointerException.class, () -> new Frigate(null, new Position(1, 1)),
				"Error: NullPointerException should be thrown for null bearing.");
		assertThrows(NullPointerException.class, () -> new Frigate(Compass.NORTH, null),
				"Error: NullPointerException should be thrown for null position.");
	}
}