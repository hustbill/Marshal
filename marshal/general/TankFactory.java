package general;

import org.newdawn.slick.tiled.TiledMap;

import jig.Vector;

public class TankFactory extends MarshalEntity {
	
	public static final int ACTIVE = 0;
	public static final int DESTROYED = 1;
	public static final int INACTIVE = 2;

	public TankFactory(int xId, int yId, float d, int m, int l) {
		super(xId, yId, d, TankFactory.ACTIVE, m, l);
	}
	
	public boolean isAlive() {
		return getState() == TankFactory.ACTIVE;
	}
	
	public boolean isActive() {
		return getState() != TankFactory.INACTIVE;
	}
	
	public Vector getNewTankIndices(TiledMap map) {
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
				return new Vector(-1, -1);
			}
		}
		
		return new Vector(tXId, tYId);
	}
	
	private int tileManhattanDistance(int xId, int yId) {
		int dx = xId > this.xId ? (xId - this.xId) : (this.xId - xId);
		int dy = yId > this.yId ? (yId - this.yId) : (this.yId - yId);
		return dx + dy;
	}
	
	public void update(final int delta) {
		if (getHealth() <= 0) {
			setState(TankFactory.DESTROYED);
		}
		
		switch(getState()) {
			case TankFactory.ACTIVE:
				
				break;
			case TankFactory.DESTROYED:
				// play animation for the destroyed object
				dyingTimer -= delta;
				if (dyingTimer <= 0) {
					setState(TankFactory.INACTIVE);
				}
				break;
			case TankFactory.INACTIVE:
				// remove all images and animations for this object
				break;
			default:
				break;
		}
	}
}
