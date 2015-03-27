package general;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jig.Entity;

public class Player extends Entity {
	
	public static final int INITIAL_MONEY = 15000;
	
	public static final int NORMAL_MODE = 0;
	public static final int REPAIR_MODE = 1;
	public static final int UPGRADE_MODE = 2;
	public static final int BUILD_WALL_MODE = 3;
	public static final int BUILD_FACTORY_MODE = 4;
	public static final int PRODUCE_TANK_MODE = 5;
	
	private Headquarter headquarter;
	private String username;
	public Set<Tank> tanks;
	public ArrayList<Wall> walls;
	public ArrayList<TankFactory> factories;
	public float cX , cY;
	
	private int money;
	private int mode;
	
	private boolean player0;
	
	public Player(boolean player0) {
		headquarter = new Headquarter();
		tanks = new HashSet<Tank>();
		walls = new ArrayList<Wall>();
		factories = new ArrayList<TankFactory>();
		
		money = Player.INITIAL_MONEY;
		mode = NORMAL_MODE;
		this.player0 = player0;
		setUsername(null);
	}
	
	public Player(String name, int spawnAt) {
		// TODO Create new player at spawnAt location
		username = name;
	}
	
	public boolean isPlayer0() {
		return player0;
	}

	public void setMoney(int m) {
		money = m;
	}
	
	public int getMoney() {
		return money;
	}
	
	public void earnMoney(int m) {
		money += m;
	}
	
	public void setHeadquarter(int xId, int yId) {
		headquarter = new Headquarter(xId, yId);
	}
	
	public Headquarter getHeadquarter() {
		return headquarter;
	}
	
	public Set<Tank> getTanks() {
		return tanks;
	}
	
	public ArrayList<Wall> getWalls() {
		return walls;
	}
	
	public ArrayList<TankFactory> getTankFactories() {
		return factories;
	}
	
	public void addTank(Tank tank) {
		tanks.add(tank);
	}
	
	public void addTankFactory(TankFactory factory) {
		factories.add(factory);
	}
	
	public void addWall(Wall wall) {
		walls.add(wall);
	}
	
	public void addInitialTanks() {
		int x = this.headquarter.getXId();
		int y = this.headquarter.getYId();
		
		this.addTank(new LevelOneTank(x - 1, y - 1));
		this.addTank(new LevelOneTank(x - 1, y));
		this.addTank(new LevelOneTank(x - 1, y + 1));
		this.addTank(new LevelOneTank(x, y - 1));
		this.addTank(new LevelOneTank(x, y + 1));
		this.addTank(new LevelOneTank(x + 1, y - 1));
		this.addTank(new LevelOneTank(x + 1, y));
		this.addTank(new LevelOneTank(x + 1, y + 1));
	}
	
	public int getMode() {
		return mode;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
