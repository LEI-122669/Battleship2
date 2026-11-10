/**
 * 
 */
package battleship;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Fleet.
 */
public class Fleet implements IFleet
{
	private static final String SHIP_TYPE_GALEAO = "galeao";
	private static final String SHIP_TYPE_FRAGATA = "fragata";
	private static final String SHIP_TYPE_NAU = "nau";
	private static final String SHIP_TYPE_CARAVELA = "caravela";
	private static final String SHIP_TYPE_BARCA = "barca";
	/**
	 * Creates a randomly generated fleet containing ships of various predefined types.
	 * Each ship is assigned a random bearing and position. If a ship cannot be added
	 * due to constraints (e.g., collision or boundary issues), it will be retried.
	 *
	 * @return a fully constructed and valid fleet as an instance of IFleet
	 */
	public static IFleet createRandom() {

		IFleet randomFleet = new Fleet();

		// Define the types of ships to be added using constants
		String[] shipTypes = {
				SHIP_TYPE_GALEAO,
				SHIP_TYPE_FRAGATA,
				SHIP_TYPE_NAU, SHIP_TYPE_NAU,
				SHIP_TYPE_CARAVELA, SHIP_TYPE_CARAVELA, SHIP_TYPE_CARAVELA,
				SHIP_TYPE_BARCA, SHIP_TYPE_BARCA, SHIP_TYPE_BARCA, SHIP_TYPE_BARCA
		};

		int fleetSize = 0;

		while (fleetSize < shipTypes.length) {

			// Build the ship
			Ship ship = Ship.buildShip(shipTypes[fleetSize], Compass.randomBearing(), Position.randomPosition());

			// Attempt to add the ship to the fleet
			if (ship != null && randomFleet.addShip(ship)) {
				fleetSize++; // Increment count if ship is successfully added
			}
		}
		return randomFleet;
	}


    // -----------------------------------------------------

	/**
	 * The Ships.
	 */
	private final List<IShip> ships;

	// -----------------------------------------------------ge
	/**
	 * Instantiates a new Fleet.
	 */
	public Fleet()
    {
	ships = new ArrayList<>();
    }

	/**
	 * Gets ships.
	 *
	 * @return the ships
	 */
	@Override
    public List<IShip> getShips()
    {
	return ships;
    }
	
	/**
	 * Add ship boolean.
	 *
	 * @param s the s
	 * @return the boolean
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#addShip(battleship.IShip)
     */
	@Override
	public boolean addShip(IShip ship)
	{
		if (ship == null) {
			throw new IllegalArgumentException("Ship cannot be null.");
		}

		if ((ships.size() <= FLEET_SIZE) && (ship.isInsideBoard()) && (!colisionRisk(ship)))
		{
			ships.add(ship);
			return true;
		}

		return false;
	}

	/**
	 * Gets ships like.
	 *
	 * @param category the category
	 * @return the ships like
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#getShipsLike(java.lang.String)
     */
    @Override
    public List<IShip> getShipsLike(String category)
    {
		assert category != null;

		List<IShip> shipsLike = new ArrayList<IShip>();
		for (IShip s : ships)
			if (s.getCategory().equals(category))
				shipsLike.add(s);

		return shipsLike;
    }

	/**
	 * Gets floating ships.
	 *
	 * @return the floating ships
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#getFloatingShips()
     */
    @Override
    public List<IShip> getFloatingShips()
    {
		List<IShip> floatingShips = new ArrayList<IShip>();
		for (IShip s : ships)
			if (s.stillFloating())
				floatingShips.add(s);

		return floatingShips;
    }

	/**
	 * Gets sunk ships.
	 *
	 * @return the sunk ships
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see battleship.IFleet#getSunkShips()
	 */
	@Override
	public List<IShip> getSunkShips()
	{
		List<IShip> sunkShips = new ArrayList<IShip>();
		for (IShip s : ships)
			if (!s.stillFloating())
				sunkShips.add(s);

		return sunkShips;
	}

	/**
	 * Ship at ship.
	 *
	 * @param pos the pos
	 * @return the ship
	 */
	/*
     * (non-Javadoc)
     * 
     * @see battleship.IFleet#shipAt(battleship.IPosition)
     */
    @Override
    public IShip shipAt(IPosition pos)
    {
		assert pos != null;

		for (IShip ship : ships)
			if (ship.occupies(pos))
				return ship;
		return null;
    }

	/**
	 * Colision risk boolean.
	 *
	 * @param s the s
	 * @return the boolean
	 */
	private boolean colisionRisk(IShip s)
    {
		assert s != null;

		for (int i = 0; i < ships.size(); i++)
		{
			if (ships.get(i).tooCloseTo(s))
				return true;
		}
		return false;
    }

	/**
	 * This operation prints all the given ships
	 *
	 * @param ships The list of ships
	 */
	public void printShips(List<IShip> ships)
	{
		assert ships != null;

		for (IShip ship : ships)
			System.out.println(ship);
	}

	/**
	 * This operation shows the state of a fleet
	 */
	public void printStatus()
    {
		System.out.println("Estado da Frota: " + this.getFloatingShips().size() + " a flutuar, " + this.getSunkShips().size() + " afundados!");
//		printAllShips();
//		printFloatingShips();
//		printShipsByCategory("Galeao");
//		printShipsByCategory("Fragata");
//		printShipsByCategory("Nau");
//		printShipsByCategory("Caravela");
//		printShipsByCategory("Barca");
    }

	/**
	 * This operation prints all the ships of a fleet belonging to a particular
	 * category
	 *
	 * @param category The category of ships of interest
	 */
	public void printShipsByCategory(String category)
    {
		assert category != null;

		printShips(getShipsLike(category));
    }

	/**
	 * This operation prints all the ships of a fleet but not yet shot
	 */
	public void printFloatingShips()
    {
	printShips(getFloatingShips());
    }

	/**
	 * This operation prints all the ships of a fleet
	 */
	void printAllShips()
    {
		printShips(ships);
    }
}