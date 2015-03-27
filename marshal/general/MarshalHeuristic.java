package general;

import org.newdawn.slick.util.pathfinding.AStarHeuristic;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

public class MarshalHeuristic implements AStarHeuristic {

	public MarshalHeuristic() {
		
	}
	
	@Override
	public float getCost(TileBasedMap map, Mover mover, int x, int y, int tx,
			int ty) {
		// TODO Auto-generated method stub
		int dx = tx > x ? (tx - x) : (x - tx);
		int dy = ty > y ? (ty - y) : (y - ty);
		return dx + dy;
	}
	
}
