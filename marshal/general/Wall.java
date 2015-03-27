package general;

public class Wall extends MarshalEntity {
	
	public static final int ACTIVE = 0;
	public static final int DESTROYED = 1;
	public static final int INACTIVE = 2;
	
	//public static final int WALL_TILE_ID = 0;
	
	public Wall(int xId, int yId, float d, int m, int l) {
		super(xId, yId, d, Wall.ACTIVE, m, l);
	}
	
	public boolean isAlive() {
		return getState() == Wall.ACTIVE;
	}
	
	public boolean isActive() {
		return getState() != Wall.INACTIVE;
	}
	
	public void update(final int delta) {
		if (getHealth() <= 0) {
			setState(Wall.DESTROYED);
		}
		
		switch(getState()) {
			case Wall.ACTIVE:
				
				break;
			case Wall.DESTROYED:
				// play animation for the destroyed object
				dyingTimer -= delta;
				if (dyingTimer <= 0) {
					setState(Wall.INACTIVE);
				}
				break;
			case Wall.INACTIVE:
				// remove all images and animations for this object
				break;
			default:
				break;
		}
	}
}
