package general;

import jig.Entity;
import jig.Vector;

public class MarshalEntity extends Entity {
	
	public static final int REPAIR_PAY_RATE = 25;
	
	public static final int TILE_WIDTH = 48;
	public static final int TILE_HEIGHT = 48;
	
	public static final int DYING_TIME = 100;
	
	protected int xId;
	protected int yId;
	
	private float health; // normalized health
	private float defense; // percentage of damage received
	
	private int state; // used in child classes
	
	private int money;
	
	protected int dyingTimer; // time after being destroyed but before being inactive
	
	private int level;
	
	protected boolean exploded;
	
	public void setXId(int xId) {
		this.xId = xId;
	}
	
	public int getXId() {
		return xId;
	}
	
	public void setYId(int yId) {
		this.yId = yId;
	}
	
	public int getYId() {
		return yId;
	}
	
	public void setIndex(int xId, int yId) {
		this.xId = xId;
		this.yId = yId;
	}
	
	public float getHealth() {
		return health;
	}
	public void setHealth(float health) {
		this.health = health;
	}
	public void setState(int s) {
		state = s;
	}
	
	public int getState() {
		return state;
	}
	
	/***
	 * Create an invalid Marshal game entity as an empty Marshal game entity. 
	 */
	public MarshalEntity() {
		this(-1, -1, -1);
	}
	
	/***
	 * Create a Marshal game entity. 
	 * @param xId x index of the object in the tiled map
	 * @param yId y index of the object in the tiled map
	 * @param d defense of the object
	 */
	public MarshalEntity(int xId, int yId, float d) {
		this(xId, yId, d, 0, 0, 0);
	}
	
	public MarshalEntity(int xId, int yId, float d, int s, int m, int l) {
		this.xId = xId;
		this.yId = yId;
		this.defense = d;
		this.health = 1;
		this.state = s;
		this.money = m;
		this.level = l;
		
		setX(xId * MarshalEntity.TILE_WIDTH + MarshalEntity.TILE_WIDTH / 2);
		setY(yId * MarshalEntity.TILE_HEIGHT + MarshalEntity.TILE_HEIGHT / 2);
		
		dyingTimer = MarshalEntity.DYING_TIME;
		
		exploded = false;
	}
	
	public int getLevel() {
		return level;
	}
	
	/***
	 * Handle the case wherein the object receives damage. 
	 * @param d damage amount received
	 */
	public void receiveDamage(float d) {
		float newHealth = health - defense * d;
		health = newHealth >= 0 ? newHealth : 0;
	}
	
	public void repair() {
		float newHealth = health + defense;
		health = newHealth > 1 ? 1 : newHealth;
	}
	
	public int getRepairCost() {
		float newHealth = health + defense;
		int repairCost = newHealth > 1 ? (int) Math.ceil(REPAIR_PAY_RATE * (1 - health) / defense) : 
			(int) Math.ceil(REPAIR_PAY_RATE);
		return repairCost;
	}
	
	public Vector getUpperLeftCornerPos(int tileWidth, int tileHeight) {
		int x = getXId() * tileWidth;
		int y = getYId() * tileHeight;
		return new Vector(x, y);
	}
	
	public boolean isAlive() {
		return false;
	}
	
	public Vector getIndices() {
		return new Vector(xId, yId);
	}
	
	public int getMoney() {
		return money;
	}
	
	public void setMoney(int m) {
		money = m;
	}
	
	public boolean getExploded() {
		return exploded;
	}
	
	public void setExploded() {
		exploded = true;
	}
}
