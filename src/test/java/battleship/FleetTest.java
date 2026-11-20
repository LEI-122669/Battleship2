package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Fleet.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - addShip: 3
 * - getShips: 1
 * - getShipsLike: 2
 * - getFloatingShips: 2
 * - shipAt: 2
 * - isInsideBoard: 3
 * - colisionRisk: 2
 */
	public class FleetTest {

		private Fleet fleet;

		@BeforeEach
		void setUp() {
			fleet = new Fleet();
		}

		@AfterEach
		void tearDown() {
			fleet = null;
		}





		@Test
		void testCreateRandom(){
				IFleet r = Fleet.createRandom();
				assertNotNull(r, "Error: createRandom should return a non-null fleet instance.");
				assertEquals(Fleet.FLEET_SIZE, r.getShips().size(), "Error: createRandom should create a fleet with the correct number of ships.");
		}

		/**
		 * Test for the addShip method (all conditions true).
		 * Cyclomatic Complexity: 3
		 */
		@Test
		void testAddShip1() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
		}

		/**
		 * Test for the addShip method (fleet size limit reached).
		 */
		@Test
		void testAddShip2() {
			for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
				fleet.addShip(new Barge(Compass.NORTH, new Position(i, 0)));
			}
			IShip anotherShip = new Barge(Compass.NORTH, new Position(10, 10));
			assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
		}

		/**
		 * Test for the addShip method (ship outside the board).
		 */
		@Test
		void testAddShip3() {
			IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
			assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
		}

		/**
		 * Test for the addShip method (collision risk).
		 */
		@Test
		void testAddShip4() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);
			assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
		}

		/**
		 * Test for the getShips method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testGetShips() {
			assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
			assertEquals(ship, fleet.getShips().getFirst(), "Error: Fleet's first ship should match the added ship.");
		}

		/**
		 * Test for the getShipsLike method (ships of specific category).
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetShipsLike() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(2, 1));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> barges = fleet.getShipsLike("Barca");
			assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
			assertEquals(ship1, barges.getFirst(), "Error: The ship of category 'Barca' does not match.");
		}

		/**
		 * Test for the getFloatingShips method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testGetFloatingShips() {
			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
			fleet.addShip(ship1);
			fleet.addShip(ship2);

			List<IShip> floatingShips = fleet.getFloatingShips();
			assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

			ship1.getPositions().getFirst().shoot();  // Sink ship1
			floatingShips = fleet.getFloatingShips();
			assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
			assertEquals(ship2, floatingShips.getFirst(), "Error: The floating ship should match the expected result.");
		}

		/**
		 * Test for the shipAt method.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testShipAt() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);

			assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
			assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
		}


		/**
		 * Test for private method colisionRisk.
		 * Cyclomatic Complexity: 2
		 */
		@Test
		void testColisionRisk() throws Exception {
			var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
			method.setAccessible(true);

			IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
			IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
			fleet.addShip(ship1);

			assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
			assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
					"Error: Ships at non-overlapping positions should not have a collision risk.");
		}

		/**
		 * Test for the printStatus method.
		 * Cyclomatic Complexity: 1
		 */
		@Test
		void testPrintStatus() {
			IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
			fleet.addShip(ship);
			assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
		}
	/**
	 * Test for adding a null ship.
	 * Covers the exception branch in addShip.
	 */
	@Test
	void testAddShip_Null() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> fleet.addShip(null));
		assertEquals("Ship cannot be null.", exception.getMessage());
	}

	/**
	 * Test for the getSunkShips method.
	 */
	@Test
	void testGetSunkShips() {
		IShip ship = new Barge(Compass.NORTH, new Position(5, 5));
		fleet.addShip(ship);

		assertTrue(fleet.getSunkShips().isEmpty(), "Error: Fleet should have no sunk ships initially.");

		// Afundar o navio disparando em todas as suas posições
		for (IPosition p : ship.getPositions()) {
			p.shoot();
		}

		assertEquals(1, fleet.getSunkShips().size(), "Error: Fleet should have exactly 1 sunk ship after it is destroyed.");
	}

	/**
	 * Test for all remaining print methods to reach 100% Method coverage.
	 */
	@Test
	void testRemainingPrintMethods() {
		IShip ship = new Barge(Compass.NORTH, new Position(8, 8));
		fleet.addShip(ship);

		assertDoesNotThrow(() -> {
			fleet.printShips(fleet.getShips());
			fleet.printShipsByCategory("Barca");
			fleet.printFloatingShips();
			fleet.printAllShips();
		}, "Error: None of the print methods should throw an exception.");
	}
	/**
	 * Cobre os casos em que a frota está vazia.
	 * Isso força as condições dos laços 'for' a serem avaliadas como FALSO logo de início.
	 */
	@Test
	void testEmptyFleetLoops() {
		Fleet emptyFleet = new Fleet();
		assertTrue(emptyFleet.getFloatingShips().isEmpty());
		assertTrue(emptyFleet.getSunkShips().isEmpty());
		assertTrue(emptyFleet.getShipsLike("Barca").isEmpty());
		assertNull(emptyFleet.shipAt(new Position(1, 1)));
		assertDoesNotThrow(() -> {
			emptyFleet.printAllShips();
			emptyFleet.printFloatingShips();
		});
	}

	/**
	 * Cobre o laço do colisionRisk avaliando tooCloseTo como FALSO (navios distantes).
	 */
	@Test
	void testColisionRisk_NoCollisionLoop() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship1);

		// O segundo navio está longe, força o loop de colisionRisk a rodar sem dar return true
		IShip ship2 = new Barge(Compass.NORTH, new Position(8, 8));
		assertTrue(fleet.addShip(ship2), "Error: Navios distantes devem ser adicionados sem risco de colisão.");
	}

	/**
	 * Cobre a falha de encontrar o navio (ramificação false do if no laço).
	 */
	@Test
	void testSearchMisses() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		// Procura por categoria que não existe (equals == false)
		assertTrue(fleet.getShipsLike("Galeao").isEmpty());
		// Procura por posição que o navio não ocupa (occupies == false)
		assertNull(fleet.shipAt(new Position(9, 9)));
	}

	/**
	 * Cobre as ramificações de 'assert var != null' presentes no código.
	 * Usa try-catch genérico, pois o comportamento depende de a flag -ea estar ativa no IntelliJ.
	 */
	@Test
	void testAssertNullBranches() {
		try { fleet.shipAt(null); } catch (Throwable ignored) {}
		try { fleet.getShipsLike(null); } catch (Throwable ignored) {}
		try { fleet.printShipsByCategory(null); } catch (Throwable ignored) {}
		try { fleet.printShips(null); } catch (Throwable ignored) {}
	}

	@Test
	void printShipsRejectsNull() {
		assertThrows(
				NullPointerException.class,
				() -> fleet.printShips(null)
		);
	}
	}