package general;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.pathfinding.Mover;

import jig.ResourceManager;
import jig.Vector;


public class Tank extends MarshalEntity implements Mover {
	
	public static final int TARGET = 0;
	public static final int ATTACK = 1;
	public static final int ATTACKING = 2;
	public static final int MOVE = 3;
	public static final int STANDBY = 4;
	public static final int DESTROYED = 5;
	public static final int INACTIVE = 6;
	
	// Tank directions
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int UP = 2;
	public static final int DOWN = 3;
	public static final int UPLEFT = 4;
	public static final int UPRIGHT = 5;
	public static final int DOWNLEFT = 6;
	public static final int DOWNRIGHT = 7;
	
	private int direction;
	
	// -1 if not set yet
	private MarshalEntity targeted; // targeted entity for attacking
	private int finalTargetXId; // destination x index for non-attack moving
	private int finalTargetYId; // destination y index for non-attack moving
	private int currentTargetXId; // current intermediate target x index
	private int currentTargetYId; // current intermediate target x index
	private int nextTargetXId; // next intermediate target y index
	private int nextTargetYId; // next intermediate target y index
	
	private int attackTimer; // a unit of time to attack the target
	private int moveTimer; // time for moving from tile to tile
	
	private int attackTime; // interval from one attack to another
	private int tileTime; // time taken to move from one tile to another
	// fire power of the tank, which is the amount of damage the tank can make in a unit of time
	private float power;
	private float range; // attack range of the tank in tile distance
	
	protected SpriteSheet tankImages;
	protected Image tankImage;
	
	private int tankId;
	
	private boolean commandedStandby;
	
	private Fireball fireball;
	private boolean fireballLaunched;

	public Tank(int xId, int yId, float d, int a, int t, float p, float r , int id, int m, int l) {
		super(xId, yId, d, Tank.STANDBY, m, l);
		
		targeted = new MarshalEntity();
		currentTargetXId = xId;
		currentTargetYId = yId;
		nextTargetXId = xId;
		nextTargetYId = yId;
		
		attackTime = a;
		tileTime = t;
		
		power = p;
		range = r;
		
		tankImages = ResourceManager.getSpriteSheet(MarshalGame.PLAYER_TANKS, 
				MarshalEntity.TILE_WIDTH, MarshalEntity.TILE_HEIGHT);
		tankImage = tankImages.getSprite(0, 0);
		addImageWithBoundingBox(tankImage);
		
		commandedStandby = false;
		
		fireballLaunched = false;
		
		direction = 3;
		
		tankId = id;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int d) {
		direction = d;
	}
	
	public void setCurrentTarget(int targetXId, int targetYId) {
		currentTargetXId = targetXId;
		currentTargetYId = targetYId;
	}
	
	public Vector getCurrentTarget() {
		return new Vector(currentTargetXId, currentTargetYId);
	}
	
	public void setNextTarget(int targetXId, int targetYId) {
		nextTargetXId = targetXId;
		nextTargetYId = targetYId;
	}
	
	public Vector getNextTarget() {
		return new Vector(nextTargetXId, nextTargetYId);
	}
	
	public boolean targetInRange() {
		int dx = targeted.getXId() - getXId();
		int dy = targeted.getYId() - getYId();
		int dSquare = dx * dx + dy * dy;
		return dSquare <= range * range;
	}
	
	public boolean arriveAtDestination() {
		return finalTargetXId == getXId() && finalTargetYId == getYId();
	}
	
	public float getPower() {
		return power;
	}
	
	public boolean isAlive() {
		return getState() != Tank.DESTROYED && getState() != Tank.DESTROYED;
	}
	
	public boolean isActive() {
		return getState() != Tank.INACTIVE;
	}
	
	public void waitForTarget() {
		setState(Tank.TARGET);
	}
	
	public void setAttackTarget(MarshalEntity t) {
		targeted = t;
		moveTimer = tileTime;
		setState(Tank.ATTACK);
	}
	
	public void attackTarget() {
		targeted.receiveDamage(power);
	}
	
	public void setMoveDestination(int xId, int yId) {
		currentTargetXId = this.xId;
		currentTargetYId = this.yId;
		nextTargetXId = this.xId;
		nextTargetYId = this.yId;
		finalTargetXId = xId;
		finalTargetYId = yId;
		moveTimer = tileTime;
		setState(Tank.MOVE);
	}
	
	public void standby() {
		if (getState() == Tank.MOVE || getState() == Tank.ATTACK) {
			commandedStandby = true;
		} else {
			setState(Tank.STANDBY);
		}
	}
	
	private int tileManhattanDistance(int xId, int yId) {
		int dx = xId > this.xId ? (xId - this.xId) : (this.xId - xId);
		int dy = yId > this.yId ? (yId - this.yId) : (this.yId - yId);
		return dx + dy;
	}
	
	public Vector getTargetIndices(TiledMap map) {
		int xId = targeted.getXId();
		int yId = targeted.getYId();
		
		Vector nearestIndices = getNearestNonblockedTileIndices(map, xId, yId);
		return nearestIndices;
	}
	
	public Vector getDestination(TiledMap map) {
		int objectsLayer = map.getLayerIndex("Objects");
		if (map.getTileId(finalTargetXId, finalTargetYId, objectsLayer) == 0) {
			return new Vector(finalTargetXId, finalTargetYId);
		} else {
			Vector nearestIndices = getNearestNonblockedTileIndices(map, finalTargetXId, finalTargetYId);
			return nearestIndices;
		}
	}
	
	private Vector getNearestNonblockedTileIndices(TiledMap map, int xId, int yId) {
		int w = map.getWidth();
		int h = map.getHeight();
		
		int tXId = this.xId;
		int tYId = this.yId;
		
		int offset = 1;
		boolean nearestFound = false;
		
		int objectsLayer = map.getLayerIndex("Objects");
		int minDistance = Integer.MAX_VALUE;
		
		while (!nearestFound) {
			// left
			int tempXId = xId - offset;
			int tempYId = yId;
			if (tempXId >= 0 && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// right
			tempXId = xId + offset;
			tempYId = yId;
			if (tempXId < w && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// upper
			tempXId = xId;
			tempYId = yId - offset;
			if (tempYId >= 0 && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// lower
			tempXId = xId;
			tempYId = yId + offset;
			if (tempYId < h && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// upper left
			tempXId = xId - offset;
			tempYId = yId - offset;
			if (tempXId >= 0 && tempYId >= 0 && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// upper right
			tempXId = xId + offset;
			tempYId = yId - offset;
			if (tempXId < w && tempYId >= 0 && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// lower left
			tempXId = xId - offset;
			tempYId = yId + offset;
			if (tempXId >= 0 && tempYId < h && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			// lower right
			tempXId = xId + offset;
			tempYId = yId + offset;
			if (tempXId < w && tempYId < h && map.getTileId(tempXId, tempYId, objectsLayer) == 0) {
				int tempDistance = tileManhattanDistance(tempXId, tempYId);
				if (tempDistance < minDistance) {
					tXId = tempXId;
					tYId = tempYId;
					minDistance = tempDistance;
				}
				nearestFound = true;
			}
			offset += 1;
			if (offset > MarshalGame.NEAREST_EMPTY_TILE_SEARCH_LIMIT) {
				standby();
			}
		}
		if (minDistance + 1 == tileManhattanDistance(xId, yId) && getState() == Tank.MOVE) {
			standby();
		}
		return new Vector(tXId, tYId);
	}

	public void renderMoveImage() {
		if (getState() == Tank.MOVE || getState() == Tank.ATTACK) {
			if (currentTargetXId > xId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(9, 0);
				addImageWithBoundingBox(tankImage);
				direction = RIGHT;
			} else if (currentTargetXId < xId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(9, 0);
				tankImage = tankImage.getFlippedCopy(true, false);
				addImageWithBoundingBox(tankImage);
				direction = LEFT;
			} else if (currentTargetYId > yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(0, 0);
				addImageWithBoundingBox(tankImage);
				direction = DOWN;
			} else if (currentTargetYId < yId){
				removeImage(tankImage);
				tankImage = tankImages.getSprite(18, 0);
				addImageWithBoundingBox(tankImage);
				direction = UP;
			}
		}
	}
	
	public void renderAttackingImage() {
		if (targeted.getXId() > xId) {
			if (targeted.getYId() > yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(3, 0);
				addImageWithBoundingBox(tankImage);
				direction = DOWNRIGHT;
			} else if (targeted.getYId() < yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(15, 0);
				addImageWithBoundingBox(tankImage);
				direction = UPRIGHT;
			} else {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(9, 0);
				addImageWithBoundingBox(tankImage);
				direction = RIGHT;
			}
		} else if (targeted.getXId() < xId) {
			if (targeted.getYId() > yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(3, 0);
				tankImage = tankImage.getFlippedCopy(true, false);
				addImageWithBoundingBox(tankImage);
				direction = DOWNLEFT;
			} else if (targeted.getYId() < yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(15, 0);
				tankImage = tankImage.getFlippedCopy(true, false);
				addImageWithBoundingBox(tankImage);
				direction = UPLEFT;
			} else {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(9, 0);
				tankImage = tankImage.getFlippedCopy(true, false);
				addImageWithBoundingBox(tankImage);
				direction = LEFT;
			}
		} else {
			if (targeted.getYId() > yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(0, 0);
				addImageWithBoundingBox(tankImage);
				direction = DOWN;
			} else if (targeted.getYId() < yId) {
				removeImage(tankImage);
				tankImage = tankImages.getSprite(18, 0);
				addImageWithBoundingBox(tankImage);
				direction = UP;
			}
		}
	}
	
	public void update(final int delta) {
		if (getHealth() <= 0) {
			setState(Tank.DESTROYED);
		}
		
		if (getState() != Tank.MOVE && getState() != Tank.ATTACK) {
			setX(xId * MarshalEntity.TILE_WIDTH + MarshalEntity.TILE_WIDTH / 2);
			setY(yId * MarshalEntity.TILE_HEIGHT + MarshalEntity.TILE_HEIGHT / 2);
		}
		
		if (fireball != null) {
			fireball.update(delta);
		}
		
		renderMoveImage();
		switch(getState()) {
			case Tank.TARGET:
				
				break;
			case Tank.ATTACK:
				moveTimer -= delta;
				
				if (targeted == null || !targeted.isAlive()) {
					standby();
				}
				
				if (targeted != null && targeted.isAlive() && targetInRange()) {
					attackTimer = attackTime;
					setState(Tank.ATTACKING);
				} else if (moveTimer <= 0) {
					moveTimer = tileTime;
					
					setXId(currentTargetXId);
					setYId(currentTargetYId);
					
					currentTargetXId = nextTargetXId;
					currentTargetYId = nextTargetYId;
					
					if (commandedStandby) {
						setState(Tank.STANDBY);
						commandedStandby = false;
					}
				}
				setX(xId * MarshalEntity.TILE_WIDTH + MarshalEntity.TILE_WIDTH / 2 + 
						(int) ((currentTargetXId - xId) * (1 - 1.0 * moveTimer / tileTime) * MarshalEntity.TILE_WIDTH));
				setY(yId * MarshalEntity.TILE_HEIGHT + MarshalEntity.TILE_HEIGHT / 2 + 
						(int) ((currentTargetYId - yId) * (1 - 1.0 * moveTimer / tileTime) * MarshalEntity.TILE_HEIGHT));
				break;
			case Tank.ATTACKING:
				renderAttackingImage();
				
				attackTimer -= delta;
				if (targeted == null || !targeted.isAlive()) {
					standby();
				} else if (attackTimer <= 8 * Fireball.MOVEDURATION 
						&& !fireballLaunched) {
					fireball = launchFireball();
					fireballLaunched = true;
				} else if (attackTimer <= 0) {
					attackTimer = attackTime;
					
					attackTarget();
					fireballLaunched = false;
				}
				break;
			case Tank.MOVE:
				moveTimer -= delta;
				
				if (arriveAtDestination()) {
					setState(Tank.STANDBY);
				} else if (moveTimer <= 0) {
					
					moveTimer = tileTime;
					
					setXId(currentTargetXId);
					setYId(currentTargetYId);
					
					currentTargetXId = nextTargetXId;
					currentTargetYId = nextTargetYId;
					
					if (commandedStandby) {
						setState(Tank.STANDBY);
						commandedStandby = false;
					}
				}
				setX(xId * MarshalEntity.TILE_WIDTH + MarshalEntity.TILE_WIDTH / 2 + 
						(int) ((currentTargetXId - xId) * (1 - 1.0 * moveTimer / tileTime) * MarshalEntity.TILE_WIDTH));
				setY(yId * MarshalEntity.TILE_HEIGHT + MarshalEntity.TILE_HEIGHT / 2 + 
						(int) ((currentTargetYId - yId) * (1 - 1.0 * moveTimer / tileTime) * MarshalEntity.TILE_HEIGHT));
				break;
			case Tank.STANDBY:
				
				break;
			case Tank.DESTROYED:
				// play animation for the destroyed object
				dyingTimer -= delta;
				if (dyingTimer <= 0) {
					setState(Tank.INACTIVE);
				}
				break;
			case Tank.INACTIVE:
				// remove all images and animations for this object
				break;
			default:
				break;
		}
	}
	
	private int getFireballDirection() {
		int direction = -1;
		if (xId == targeted.getXId()) {
			if (yId < targeted.getYId()) {
				direction = Fireball.DOWN;
			} else if (yId > targeted.getYId()) {
				direction = Fireball.UP;
			}
		} else if (xId < targeted.getXId()) {
			if (yId == targeted.getYId()) {
				direction = Fireball.RIGHT;
			} else if (yId < targeted.getYId()) {
				direction = Fireball.DOWNRIGHT;
			} else {
				direction = Fireball.UPRIGHT;
			}
		} else {
			if (yId == targeted.getYId()) {
				direction = Fireball.LEFT;
			} else if (yId < targeted.getYId()) {
				direction = Fireball.DOWNLEFT;
			} else {
				direction = Fireball.UPLEFT;
			}
		}
		
		return direction;
	}
	public Fireball getFireball(){
		return fireball;
	}
	public boolean isFireballLaunched(){
		return fireballLaunched;
	}
	public Fireball launchFireball() {
		Vector source = this.getPosition();
		Vector goal = targeted.getPosition();
		return new Fireball(source, goal, getFireballDirection());
	}
	
	public void renderFireball(Graphics g, Vector cameraPos) {
		if (fireball != null && fireball.isActive()) {
			Vector translateVec = new Vector(-cameraPos.getX(), -cameraPos.getY());
			fireball.translate(translateVec);
			fireball.render(g);
			fireball.translate(translateVec.scale(-1));
		}
	}
}
