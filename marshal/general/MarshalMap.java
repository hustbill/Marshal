package general;

import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

public class MarshalMap implements TileBasedMap {

	private TiledMap map;
	private int layerId;
	
	public MarshalMap(TiledMap m, int layerId) {
		map = m;
		this.layerId = layerId;
	}
	
	@Override
	public int getWidthInTiles() {
		// TODO Auto-generated method stub
		return map.getWidth();
	}

	@Override
	public int getHeightInTiles() {
		// TODO Auto-generated method stub
		return map.getHeight();
	}

	@Override
	public void pathFinderVisited(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean blocked(PathFindingContext context, int tx, int ty) {
		// TODO Auto-generated method stub
		return map.getTileId(tx, ty, layerId) != 0;
	}

	@Override
	public float getCost(PathFindingContext context, int tx, int ty) {
		// TODO Auto-generated method stub
		return 1;
	}
	
}
