package general;

import org.newdawn.slick.Animation;

import jig.Entity;
import jig.ResourceManager;
import jig.Vector;

public class Fireball extends Entity {
	
	public static final int MOVE = 0;
	public static final int INACTIVE = 1;
	
	public static final int MOVEDURATION = 20;
	
	private Animation moving;
	
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int UP = 3;
	public static final int DOWN = 4;
	public static final int UPLEFT = 5;
	public static final int UPRIGHT = 6;
	public static final int DOWNLEFT = 7;
	public static final int DOWNRIGHT = 8;
	
	private int state;
	
	private Vector velocity;
	private Vector source;
	private Vector goal;
	private int direction;
	private boolean sent;

	public Fireball(Vector source, Vector goal, int direction) {
		state = Fireball.MOVE;
		velocity = goal.subtract(source).scale(0.125f / MOVEDURATION);
		this.setSource(source); 
		this.setGoal(goal);
		this.setDirection(direction);
		sent = false;
		setPosition(source);
		
		switch (direction) {
			case LEFT:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,0,7,0, true, MOVEDURATION, true);
				break;
			case RIGHT:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,4,7,4, true, MOVEDURATION, true);
				break;
			case UP:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,2,7,2, true, MOVEDURATION, true);
				break;
			case DOWN:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,6,7,6, true, MOVEDURATION, true);
				break;
			case UPLEFT:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,1,7,1, true, MOVEDURATION, true);
				break;
			case UPRIGHT:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,3,7,3, true, MOVEDURATION, true);
				break;
			case DOWNLEFT:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,7,7,7, true, MOVEDURATION, true);
				break;
			case DOWNRIGHT:
				moving = new Animation(ResourceManager.getSpriteSheet(MarshalGame.fireballSheetPath, 64, 64), 
						0,5,7,5, true, MOVEDURATION, true);
				break;
			default:
				break;
		}
		addAnimation(moving);
		moving.setLooping(false);
		moving.start();
	}
	
	public boolean isActive() {
		return state != Fireball.INACTIVE;
	}
	
	public void update(final int delta) {
		if (state == MOVE) {
			if (moving.isStopped()) {
				state = INACTIVE;
				removeAnimation(moving);
			}
			translate(velocity.scale(delta));
		}
	}
	public Vector getSource() {
		return source;
	}
	public Animation getMoving(){
		return moving;
	}
	public void setSource(Vector source) {
		this.source = source;
	}

	public Vector getGoal() {
		return goal;
	}

	public void setGoal(Vector goal) {
		this.goal = goal;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	public boolean getSent() {
		return sent;
	}
	public void setSent(boolean value){
		sent = value;
	}
}
