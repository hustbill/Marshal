package general;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Sound;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

public class Explosion extends Entity {
	
	public static final int EXPLODE = 0;
	public static final int INACTIVE = 1;
	
	public static final int KABOOMDURATION = 30;
	private Animation kaboom;
	private Sound bang;
	
	private int xId;
	private int yId;
	private int state;

	public Explosion(Vector pos) {
		super(pos);
		xId = -1;
		yId = -1;
		state = Explosion.EXPLODE;
		
		kaboom = new Animation(ResourceManager.getSpriteSheet(MarshalGame.explosionSheetPath, 256, 128), 
				KABOOMDURATION);
		addAnimation(kaboom);
		kaboom.setLooping(false);
		kaboom.start();
		
		bang = ResourceManager.getSound(MarshalGame.BANG_EXPLOSIONSND_RSC);
		bang.play();
	}
	
	public boolean isActive() {
		return state != Explosion.INACTIVE;
	}
	
	public void update(final int delta) {
		if (state == EXPLODE) {
			if (kaboom.isStopped()) {
				removeAnimation(kaboom);
				state = INACTIVE;
			}
		}
	}

	public int getxId() {
		return xId;
	}

	public void setxId(int xId) {
		this.xId = xId;
	}

	public int getyId() {
		return yId;
	}

	public void setyId(int yId) {
		this.yId = yId;
	}
}
