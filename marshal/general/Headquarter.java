package general;

public class Headquarter extends MarshalEntity {
	
	public static final int ACTIVE = 0;
	public static final int DESTROYED = 1;
	public static final int INACTIVE = 2;
	
	//public static final int HEAD_TILE_ID = 0;
	
	public static final float HEADQUARTER_DEFENSE = 0.0251f; // need to change this
	
	// not yet built headquarter
	public Headquarter() {
		this(-1, -1);
	}
	
	public Headquarter(int xId, int yId) {
		super(xId, yId, Headquarter.HEADQUARTER_DEFENSE);
	}
	
	public boolean isAlive() {
		return getState() == Headquarter.ACTIVE;
	}
	
	public boolean isActive() {
		return getState() != Headquarter.INACTIVE;
	}
	
	public void update(final int delta) {
		if(this.getHealth() <= 0)
			this.setState(Headquarter.INACTIVE);
		switch(getState()) {
			case Headquarter.ACTIVE:
				
				break;
			case Headquarter.DESTROYED:
				// play animation for the destroyed object
				break;
			case Headquarter.INACTIVE:
				// remove all images and animations for this object
				break;
			default:
				break;
		}
	}
}
