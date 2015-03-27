package general;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jig.ResourceManager;
import jig.Vector;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;

import client.Client;

public class PlayingState extends BasicGameState {
	
	// states for a round of the game
	public static final int START = 0;
	public static final int PLAYING = 1;
	public static final int END = 2;
	
	public static final int HEALTH_BAR_WIDTH = 40;
	public static final int HEALTH_BAR_HEIGHT = 5;
	public static final int HEALTH_BAR_X_OFFSET = 4;
	public static final int HEALTH_BAR_Y_OFFSET = 5;
	
	public static final int SELECTED_WIDTH = 2;
	
	public static final int TANK_TILE_ID = 257;
	public static final int PLAYER_HEAD = 258;
	public static final int PLAYER_FACTORY = 259;
	public static final int PLAYER_WALL = 260;
	
	private int state; // current state
	
	private TiledMap map; // This will be the tiled map of the round of the game. 
	
	private Vector cameraPos;
	public static final float CAMERA_SPEED = 0.35f;
	// distance from the window boundary in which camera moves are in effect
	public static final float CAMERA_MOVE_RANGE = 100;
	
	private float cameraMaxX;
	private float cameraMaxY;
	private float cameraMinX;
	private float cameraMinY;
	
	private MarshalHeuristic mh;
	
	private Image uiPanel;
	
	public static final int MONEY_IND_X_OFFSET = 100;
	public static final int MONEY_IND_Y_OFFSET = 90;
	public static final int MONEY_SIZE = 30;
	
	public static final int HEAD_X_OFFSET = 4;
	public static final int HEAD_Y_OFFSET = 4;
	
	private TrueTypeFont controlFont;
	private TrueTypeFont moneyFont;
	private TrueTypeFont levelFont;
	
	private Sound spendSound;
	
	public static final int FACTORY_BUILD_COST = 1000;
	public static final int WALL_BUILD_COST = 50;
	public static final int TANK_BUILD_COST = 200;
	public static final int FACTORY_UPGRADE_COST = 3000;
	public static final int WALL_UPGRADE_COST = 50;
	
	public int  finalScore = 1000;
	
	private Player me;
	private Client client = null;
	private static String host = "localhost";
	private static int port = 3333;
	public static boolean tank_selected = false;
	private static int clientButton;
	private static Vector clientMouse;
	private CopyOnWriteArrayList<Player> players;
	private Random r = new Random();
	private GameContainer container;
	
	private Rectangle selectionRectangle = new Rectangle( 0 , 0 , 0 , 0);
	private boolean selectionRectVisible;
	private Color selectionFillColor = new Color(0, 0, 0, 64);
	private Point mouseFrom = new Point( 0 , 0);
	private Point mouseTo = new Point(0, 0);
	private boolean massSelection = false;
	private MarshalGame mgame;
	public boolean serverStart = false;
	private static ClientMessage msg = null;
	
	private boolean clientAreaUpdate = true;
	private static PlayingState currentState;
	private static boolean clientArea = false;
	
	private GameState gameState = null;
	private boolean disableScrolling = false;
	private boolean playSoundClient;
	private static boolean clientMouseUpdate;
	private static String clientName;
	
	private void createClient() {
		if(MarshalGame.createClient){
			ClientMessage msg = new ClientMessage();
			this.client = new Client(host, port);
			MarshalGame.createClient = false;
		}
	}

	public void setState(int s) {
		state = s;
	}
	
	public int getState() {
		return state;
	}
	
	public void setMap(String m) 
			throws SlickException {
		map = new TiledMap(m);
	}
	
	public TiledMap getMap() {
		return map;
	}
	
	public void setCameraPos(float x, float y) {
		cameraPos = new Vector(x, y);
	}
	
	public void setCameraPos(Vector pos) {
		cameraPos = new Vector(pos);
	}
	
	public Vector getCameraPos() {
		return cameraPos;
	}
	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.host = MarshalGame.host;
		this.port = MarshalGame.port;
		disableScrolling = false;
		state = PlayingState.START;
		currentState = (PlayingState)(game.getState(MarshalGame.PLAYINGSTATE));
		
		map = new TiledMap("marshal/resource/third_map.tmx");
		cameraPos = new Vector(0, 0);
		
		int tx = map.getTileWidth(); 
		int ty = map.getTileHeight();
		
		uiPanel = ResourceManager.getImage(MarshalGame.UI_IMAGE);
		
		cameraMaxX = map.getWidth() * tx  + tx - MarshalGame.SCREEN_WIDTH + uiPanel.getWidth(); 
		cameraMaxY = map.getHeight() * ty + ty - MarshalGame.SCREEN_HEIGH;
		cameraMinX = -tx;
		cameraMinY = -ty;  
		
		mh = new MarshalHeuristic();
		
		Font tempFont = new Font("Times New Roman", Font.BOLD, MONEY_SIZE);
		moneyFont = new TrueTypeFont(tempFont, true);
		
		tempFont = new Font("Verdana", Font.BOLD, 20);
		controlFont = new TrueTypeFont(tempFont, true);
		
		tempFont = new Font("Verdana", Font.BOLD, 14);
		levelFont = new TrueTypeFont(tempFont, true);
		
		spendSound = new Sound(MarshalGame.SPEND_MONEY_SOUND);
		
		clientButton = -1;
		clientMouse = null;
		
		this.container = container;
	}

	public void enter(GameContainer container, StateBasedGame game) 
			throws SlickException {
		this.host = MarshalGame.host;
		this.port = MarshalGame.port;
		disableScrolling = false;
		clientButton = -1;
		clientMouse = null;
		clientMouseUpdate = true;
		gameState = new GameState();
		currentState = (PlayingState)(game.getState(MarshalGame.PLAYINGSTATE));
		
		state = PlayingState.START;
		map = new TiledMap("marshal/resource/third_map.tmx");
		cameraPos = new Vector(0, 0);
		
		mh = new MarshalHeuristic();
		
		Font tempFont = new Font("Times New Roman", Font.BOLD, MONEY_SIZE);
		moneyFont = new TrueTypeFont(tempFont, true);
		
		tempFont = new Font("Verdana", Font.BOLD, 20);
		controlFont = new TrueTypeFont(tempFont, true);
		
		tempFont = new Font("Verdana", Font.BOLD, 14);
		levelFont = new TrueTypeFont(tempFont, true);
		
		spendSound = ResourceManager.getSound(MarshalGame.SPEND_MONEY_SOUND);
		
		uiPanel = ResourceManager.getImage(MarshalGame.UI_IMAGE);

		int tx = map.getTileWidth();
		int ty = map.getTileHeight();
		cameraMaxX = map.getWidth() * tx  + tx - MarshalGame.SCREEN_WIDTH + uiPanel.getWidth(); 
		cameraMaxY = map.getHeight() * ty + ty - MarshalGame.SCREEN_HEIGH;
		cameraMinX = -tx;
		cameraMinY = -ty;
		
		MarshalGame mg = (MarshalGame)game;
		mgame = mg;
		int mw = map.getWidth();
		int mh = map.getHeight();
		mw /= 4;
		mh /= 4;
		mg.player0.setHeadquarter(mw + HEAD_X_OFFSET, mh + HEAD_Y_OFFSET);
		mg.player1.setHeadquarter(map.getWidth() - 1 - mw - HEAD_X_OFFSET, 
				map.getHeight() - 1 - mh - HEAD_Y_OFFSET);
		
		mg.player0.addInitialTanks();
		mg.player1.addInitialTanks();
		/*
		// test factory and wall. should remove later
		mg.player1.addTankFactory(new LevelOneFactory(6, 6));
		mg.player1.addTankFactory(new LevelTwoFactory(7, 6));
		mg.player0.addWall(new LevelOneWall(6, 7));
		mg.player1.addWall(new LevelTwoWall(6, 8));
		mg.player1.addWall(new LevelOneWall(6, 9));
		mg.player1.addWall(new LevelOneWall(6, 10));
		mg.player1.addWall(new LevelOneWall(6, 11));
		mg.player1.addWall(new LevelOneWall(6, 12));
		mg.player0.addWall(new LevelOneWall(7, 7));
		mg.player1.addWall(new LevelTwoWall(8, 7));
		mg.player1.addWall(new LevelOneWall(9, 7));
		mg.player1.addWall(new LevelOneWall(10, 7));
		mg.player1.addWall(new LevelOneWall(11, 7));
		mg.player0.addTank(new LevelTwoTank(12, 7));
		mg.player1.addTank(new LevelOneTank(5, 7));
		mg.player1.addTank(new LevelOneTank(5, 8));
		mg.player1.addTank(new LevelOneTank(5, 9));
		mg.player1.addTank(new LevelOneTank(5, 10));
		mg.player1.addTank(new LevelOneTank(5, 11));
		mg.player1.addTank(new LevelOneTank(5, 12));
		mg.player1.addTank(new LevelOneTank(8, 8));
		mg.player1.addTank(new LevelOneTank(9, 9));*/
		
		int objectsLayer = map.getLayerIndex("Objects");
		
		Headquarter hq0 = mg.player0.getHeadquarter();
		Headquarter hq1 = mg.player1.getHeadquarter();
		
		map.setTileId(hq0.getXId(), hq0.getYId(), objectsLayer, PLAYER_HEAD);
		map.setTileId(hq1.getXId(), hq1.getYId(), objectsLayer, PLAYER_HEAD);
		
		for (Tank tank : mg.player0.getTanks()) {
			map.setTileId(tank.getXId(), tank.getYId(), objectsLayer, TANK_TILE_ID);
		}
		
		for (TankFactory factory : mg.player0.getTankFactories()) {
			map.setTileId(factory.getXId(), factory.getYId(), objectsLayer, PLAYER_FACTORY);
		}
		
		for (Wall wall : mg.player0.getWalls()) {
			map.setTileId(wall.getXId(), wall.getYId(), objectsLayer, PLAYER_WALL);
		}
		
		for (Tank tank : mg.player1.getTanks()) {
			map.setTileId(tank.getXId(), tank.getYId(), objectsLayer, TANK_TILE_ID);
		}
		
		for (TankFactory factory : mg.player1.getTankFactories()) {
			map.setTileId(factory.getXId(), factory.getYId(), objectsLayer, PLAYER_FACTORY);
		}
		
		for (Wall wall : mg.player1.getWalls()) {
			map.setTileId(wall.getXId(), wall.getYId(), objectsLayer, PLAYER_WALL);
		}
		
//		if(StartUpState.getConnect()){
//			msg = new ClientMessage();
//			this.client =  new Client(host, port);
//			int n = r.nextInt(1000) + 1;
//			players = new CopyOnWriteArrayList<Player>();
//			String name = n + "";
//			System.out.println(name);
//			me = new Player(name, 0);
//			players.add(me);
//		}
		this.container = container;
	}
	
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		
		MarshalGame mg = (MarshalGame)game;
		int tx = map.getTileWidth();
		int ty = map.getTileHeight();
		
		Vector translateVec = new Vector(-cameraPos.getX(), -cameraPos.getY());
		map.render((int) translateVec.getX(), (int) translateVec.getY());
		
		Vector maxIndices = getMouseTileIndices(MarshalGame.SCREEN_WIDTH, MarshalGame.SCREEN_HEIGH);
		Vector minIndices = getMouseTileIndices(0, 0);
		int maxX = (int) maxIndices.getX();
		int maxY = (int) maxIndices.getY();
		int minX = (int) minIndices.getX();
		int minY = (int) minIndices.getY();
		
		if (inView(maxX, maxY, minX, minY, mg.player0.getHeadquarter())) {
			renderHealthBar(g, mg.player0.getHeadquarter(), tx, ty, cameraPos, mg.player0.isPlayer0());
		}
		if (inView(maxX, maxY, minX, minY, mg.player1.getHeadquarter())) {
			renderHealthBar(g, mg.player1.getHeadquarter(), tx, ty, cameraPos, mg.player1.isPlayer0());
		}

		for (Tank tank : mg.player0.getTanks()) {
			if (inView(maxX, maxY, minX, minY, tank)) {
				tank.translate(translateVec);
				tank.render(g);
				tank.translate(translateVec.scale(-1));
			}
		}
		
		for (Tank tank : mg.player1.getTanks()) {
			if (inView(maxX, maxY, minX, minY, tank)) {
				tank.translate(translateVec);
				tank.render(g);
				tank.translate(translateVec.scale(-1));
			}
		}
		
		g.setFont(levelFont);
		for (TankFactory factory : mg.player0.getTankFactories()) {
			if (inView(maxX, maxY, minX, minY, factory)) {
				renderHealthBar(g, factory, tx, ty, cameraPos, mg.player0.isPlayer0());
				renderLevel(g, factory, tx, ty, cameraPos);
			}
		}
		
		for (TankFactory factory : mg.player1.getTankFactories()) {
			if (inView(maxX, maxY, minX, minY, factory)) {
				renderHealthBar(g, factory, tx, ty, cameraPos, mg.player1.isPlayer0());
				renderLevel(g, factory, tx, ty, cameraPos);
			}
		}
		
		for (Wall wall : mg.player0.getWalls()) {
			if (inView(maxX, maxY, minX, minY, wall)) {
				renderHealthBar(g, wall, tx, ty, cameraPos, mg.player0.isPlayer0());
				renderLevel(g, wall, tx, ty, cameraPos);
			}
		}
		
		for (Wall wall : mg.player1.getWalls()) {
			if (inView(maxX, maxY, minX, minY, wall)) {
				renderHealthBar(g, wall, tx, ty, cameraPos, mg.player1.isPlayer0());
				renderLevel(g, wall, tx, ty, cameraPos);
			}
		}
		
		for (Explosion explosion : mg.explosions) {
			explosion.translate(translateVec);
			explosion.render(g);
			explosion.translate(translateVec.scale(-1));
		}
		
		for (Tank tank : mg.player0.getTanks()) {
			tank.renderFireball(g, cameraPos);
			if (inView(maxX, maxY, minX, minY, tank)) {
				renderHealthBar(g, tank, tx, ty, cameraPos, mg.player0.isPlayer0());
				renderLevel(g, tank, tx, ty, cameraPos);
				if (tank.getState() == Tank.TARGET) {
					//renderTargetingTank(g, tank, tx, ty, cameraPos);
				}
			}
		}
		
		for (Tank tank : mg.player1.getTanks()) {
			tank.renderFireball(g, cameraPos);
			if (inView(maxX, maxY, minX, minY, tank)) {
				renderHealthBar(g, tank, tx, ty, cameraPos, mg.player1.isPlayer0());
				renderLevel(g, tank, tx, ty, cameraPos);
				if (tank.getState() == Tank.TARGET) {
					renderTargetingTank(g, tank, tx, ty, cameraPos);
				}
			}
		}
		g.resetFont();

		int panelW = uiPanel.getWidth();
		int panelH = uiPanel.getHeight();
		
		int panelX = MarshalGame.SCREEN_WIDTH - panelW;
		int panelY = MarshalGame.SCREEN_HEIGH - panelH;
		
		g.drawImage(uiPanel, panelX, panelY);
		
		g.setFont(moneyFont);
		g.setColor(Color.yellow);
		g.drawString("$ " + mg.player1.getMoney(), 
				panelX + panelW - MONEY_IND_X_OFFSET, 
				panelY + panelH - MONEY_IND_Y_OFFSET);
		g.resetFont();
		g.setColor(Color.white);
		
		g.setFont(controlFont);
		g.setColor(Color.white);
		g.drawString("Repair Mode", panelX + 75, panelY + 40);
		g.drawString("Upgrade Mode", panelX + 68, panelY + 140);
		g.drawString("Wall Building", panelX + 70, panelY + 230);
		g.drawString("Mode", panelX + 115, panelY + 255);
		g.drawString("Factory Building", panelX + 58, panelY + 330);
		g.drawString("Mode", panelX + 115, panelY + 355);
		g.drawString("Tank Manufacture", panelX + 45, panelY + 430);
		g.drawString("Mode", panelX + 115, panelY + 455);
		g.drawString("Back to", panelX + 100, panelY + 530);
		g.drawString("Normal Mode", panelX + 70, panelY + 555);
		g.resetFont();
		g.setColor(Color.white);
		
		int mode0 = mg.player1.getMode();
		if (mode0 != Player.NORMAL_MODE) {
			g.setColor(Color.red);
			switch (mode0) {
				case Player.REPAIR_MODE:
					g.drawString("REPAIR MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case Player.UPGRADE_MODE:
					g.drawString("UPGRADE MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case Player.BUILD_WALL_MODE:
					g.drawString("WALL BUILDING MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case Player.BUILD_FACTORY_MODE:
					g.drawString("FACTORY BUILDING MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case Player.PRODUCE_TANK_MODE:
					g.drawString("TANK MANUFACTURE MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				default:
					break;
			}
			g.setColor(Color.white);
		}
		/*if(msg != null){
			int mode = msg.getMode();
			if (mode != Player.NORMAL_MODE) {
				g.setColor(Color.blue);
				switch (mode) {
					case Player.REPAIR_MODE:
						g.drawString("CLIENT MODE: REPAIR MODE", MarshalGame.SCREEN_WIDTH / 3 - 300, 25);
						break;
					case Player.UPGRADE_MODE:
						g.drawString("CLIENT MODE: UPGRADE MODE", MarshalGame.SCREEN_WIDTH / 3- 300, 25);
						break;
					case Player.BUILD_WALL_MODE:
						g.drawString("CLIENT MODE: WALL BUILDING MODE", MarshalGame.SCREEN_WIDTH / 3 - 300, 25);
						break;
					case Player.BUILD_FACTORY_MODE:
						g.drawString("CLIENT MODE: FACTORY BUILDING MODE", MarshalGame.SCREEN_WIDTH / 3 - 300, 25);
						break;
					case Player.PRODUCE_TANK_MODE:
						g.drawString("CLIENT MODE: TANK MANUFACTURE MODE", MarshalGame.SCREEN_WIDTH / 3 - 300, 25);
						break;
					default:
						break;
				}
				g.setColor(Color.white);
			}
		}*/
		if (this.selectionRectVisible) {
		    g.setLineWidth(2);
		    g.setColor(Color.white);
		    //Rectangle r is the one drawn on screen. selectionRectangle is the important one used in game logic.
		    Rectangle r = new Rectangle( 0, 0 , 0, 0);
		    r.setX(mouseFrom.getX());
		    r.setY(mouseFrom.getY());
		    r.setWidth(mouseTo.getX() - mouseFrom.getX());
		    r.setHeight(mouseTo.getY() - mouseFrom.getY());
		    g.draw(r);

		    g.setColor(this.selectionFillColor);
		    g.fillRect(r.getX() + 2, r.getY()+ 2, r.getWidth() - 2, r.getHeight() - 2);
		}
		/*
		//TEST CODE 
		if(tank_selected)
			g.drawString("TANK IS SELECTED", 120, 10);
		else 
			g.drawString("TANK IS NOT SELECTED", 120, 10);*/
//		if(StartUpState.getConnect()){
//			/*if(circle != null){
//				
//				g.fill(circle);
//				g.draw(circle);
//				
//				client.sendMessage(getCircleInfo());
//				
//				g.setColor(Color.red);
//				g.drawString("Current circle---> PLAYER: " + me.getUsername()  + "cX : " + circle.getCenterX() + " cY : " + circle.getCenterY() , 500 , 100);
//				g.setColor(Color.white);
//			}
//			g.setColor(Color.green);
//			
//			for(Player p: this.getPlayers()){
//				Circle cir = new Circle(p.cX , p.cY , 30);
//				g.fill(cir);
//				g.draw(cir);
//			}*/
//			g.setColor(Color.white);
//		};
		/*if(WaitOpponent.isServer ==true && MarshalGame.serverMode == true)
			g.drawString("I am SERVER", 400, 10);
		else
			g.drawString("I am CLIENT", 400, 10);	*/
	}
	/*public String getCircleInfo(){
		String msg = me.getUsername() + "@" + circle.getCenterX() + "@" + circle.getCenterY();
		return msg;
	}*/
	private void renderHealthBar(Graphics g, MarshalEntity me, 
			int tileWidth, int tileHeight, Vector cameraPos, boolean player0) {
		
		float xLeft = me.getX() - map.getWidth() / 2 + PlayingState.HEALTH_BAR_X_OFFSET - cameraPos.getX();
		float xMid = xLeft + me.getHealth() * PlayingState.HEALTH_BAR_WIDTH;
		float yUp = me.getY() - map.getHeight() / 2 + PlayingState.HEALTH_BAR_Y_OFFSET - cameraPos.getY();
		
		if (player0) {
			g.setColor(new Color(0, 0, 255));
		} else {
			g.setColor(new Color(255, 0, 0));
		}
		g.fillRect(xLeft, yUp, 
				me.getHealth() * PlayingState.HEALTH_BAR_WIDTH, PlayingState.HEALTH_BAR_HEIGHT);
		g.setColor(new Color(255, 255, 255));
		g.fillRect(xMid, yUp, 
				(1 - me.getHealth()) * PlayingState.HEALTH_BAR_WIDTH, PlayingState.HEALTH_BAR_HEIGHT);
	}
	
	private void renderLevel(Graphics g, MarshalEntity me, int tileWidth, int tileHeight, Vector cameraPos) {
		float x = me.getX() - map.getWidth() / 2 + PlayingState.HEALTH_BAR_X_OFFSET - cameraPos.getX();
		float y = me.getY() + map.getHeight() / 2 - 3 * PlayingState.HEALTH_BAR_HEIGHT - cameraPos.getY();
		g.setColor(new Color(192, 206, 250));
		g.drawString("L " + me.getLevel(), x, y);
	}
	
	private void renderTargetingTank(Graphics g, Tank tank, int tileWidth, int tileHeight, Vector cameraPos) {
		Vector upperLeftCornerPos = tank.getUpperLeftCornerPos(tileWidth, tileHeight);
		
		float xLeft = upperLeftCornerPos.getX() - cameraPos.getX();
		float yUp = upperLeftCornerPos.getY() - cameraPos.getY();
		
		g.setColor(new Color(255, 0, 0));
		g.drawRect(xLeft, yUp, tileWidth, tileHeight);
	}
	
	private boolean inView(int maxX, int maxY, int minX, int minY, MarshalEntity me) {
		int xId = me.getXId();
		int yId = me.getYId();
		return xId >= minX && xId <= maxX && yId >= minY && yId <= maxY;
	}
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		Input input = container.getInput();
		MarshalGame mg = (MarshalGame)game;
	
		Headquarter hq0 = mg.player0.getHeadquarter();
		Headquarter hq1 = mg.player1.getHeadquarter();
		
		int mouseX = input.getMouseX();
		int mouseY = input.getMouseY();
		
		int panelW = uiPanel.getWidth();
		int panelX = MarshalGame.SCREEN_WIDTH - panelW;
		
		int objectsLayer = map.getLayerIndex("Objects");
		MarshalMap mm = new MarshalMap(map, objectsLayer);
		AStarPathFinder pf = new AStarPathFinder(mm, 30, false, mh);
		
		int upgradeOffset = 0;
		boolean wallUpgraded = true;
		int upgradeXId = -1;
		int upgradeYId = -1;
		
		int upgradeOffset2 = 0;
		boolean wallUpgraded2 = true;
		int upgradeXId2 = -1;
		int upgradeYId2 = -1;
		
		/***Needed input from clients: mouse click tile indices
		 * client: 
		 * 	computes mouse click tile indices using local mouse click positions and camera positions
		 * 	=>
		 * 	sends mouse click tile indices to server
		 * server: 
		 * 	processes control
		 */
		//Client mouse control
		//only render new mouse clicks
		//if(clientMouseUpdate && clientMouse != null){
		
		if(input.isKeyDown(Input.KEY_ENTER))
			hq0.setState(Headquarter.INACTIVE);
		if(!clientMouseUpdate || clientMouse==null){
			playSoundClient = false;
		}
		else if(clientMouseUpdate && clientMouse != null){
					clientMouseUpdate = false;
			int modeClient = msg.getMode();
			Vector clientMouseTile = getMouseTileIndices(clientMouse);
			int mouseXId = (int)clientMouseTile.getX();
			int mouseYId = (int)clientMouseTile.getY();
			
			if (mouseX >= panelX) {
				if (mouseY < 100) {
					mg.player0.setMode(Player.REPAIR_MODE);
				} else if (mouseY < 200) {
					mg.player0.setMode(Player.UPGRADE_MODE);
				} else if (mouseY < 300) {
					mg.player0.setMode(Player.BUILD_WALL_MODE);
				} else if (mouseY < 400) {
					mg.player0.setMode(Player.BUILD_FACTORY_MODE);
				} else if (mouseY < 500) {
					mg.player0.setMode(Player.PRODUCE_TANK_MODE);
				} else if (mouseY < 600) {
					mg.player0.setMode(Player.NORMAL_MODE);
				}
			}else if (mouseXId >= 0 && mouseYId >= 0 && 
					mouseXId < map.getWidth() && mouseYId < map.getHeight() && mouseNotInPanel(clientMouse)) {
				if (modeClient == Player.NORMAL_MODE) {	
					//if there is no object , move the tank to the specified location
					if (map.getTileId(mouseXId, mouseYId, objectsLayer) == 0) {
						for (Tank tank : mg.player0.getTanks()) {
							if (tank.getState() == Tank.TARGET) {
								tank.setMoveDestination(mouseXId, mouseYId);
								Vector destination = tank.getDestination(map);
								Path p = pf.findPath(tank, tank.xId, tank.yId, 
										(int) destination.getX(), (int) destination.getY());
								if (p != null && p.getLength() > 2) {
									Path.Step currentStep = p.getStep(1);
									int currentX = currentStep.getX();
									int currentY = currentStep.getY();
									
									Path.Step nextStep = p.getStep(2);
									int nextX = nextStep.getX();
									int nextY = nextStep.getY();
									
									if (moveTargetConflicts(game, tank, currentX, currentY)) {
										currentX = tank.getXId();
										currentY = tank.getYId();
										nextX = tank.getXId();
										nextY = tank.getYId();
									} else if (moveTargetConflicts(game, tank, nextX, nextY)) {
										nextX = currentX;
										nextY = currentY;
									}
									
									tank.setCurrentTarget(currentX, currentY);
									tank.setNextTarget(nextX, nextY);
								} else if (p != null && p.getLength() == 1) {
									Path.Step currentStep = p.getStep(1);
									int currentX = currentStep.getX();
									int currentY = currentStep.getY();
									if (moveTargetConflicts(game, tank, currentX, currentY)) {
										currentX = tank.getXId();
										currentY = tank.getYId();
									}
									tank.setCurrentTarget(currentX, currentY);
								}
							}
						}
						
					} else {
					//Yup, tank was clicked. Now handle it 
						boolean findClicked = false; //better name: foundClicked
						for (Tank tank : mg.player0.getTanks()) {
							if (tank.getXId() == mouseXId && tank.getYId() == mouseYId) {
								// handle clicked tanks
								if(!massSelection ){ //little sound control
									if(r.nextInt(1000) % 2 == 0)
										ResourceManager.getSound(MarshalGame.YES_SIR_SOUND).play();
									else
										ResourceManager.getSound(MarshalGame.WAITING_ORDER_SOUND).play();
									massSelection = false;	
								}
								// handle tank targeting
								if (tank.getState() == Tank.STANDBY) {
									tank.waitForTarget();
								} else if (tank.getState() == Tank.TARGET || 
										tank.getState() == Tank.MOVE || 
										tank.getState() == Tank.ATTACK || 
										tank.getState() == Tank.ATTACKING) {
									// handle tank standby command
									tank.standby();
								}
								findClicked = true;
								break;
							}
						}
						//If that clicked thing is not a tank
						if (!findClicked) {
							MarshalEntity target = new MarshalEntity();
							if (hq1.getXId() == mouseXId && hq1.getYId() == mouseYId) {
								target = hq1;
								findClicked = true;
							} else {
								for (Tank another : mg.player1.getTanks()) {
									if (another.getXId() == mouseXId && another.getYId() == mouseYId) {
										target = another;
										findClicked = true;
										break;
									}
								}
								if (!findClicked) {
									for (TankFactory factory : mg.player1.getTankFactories()) {
										if (factory.getXId() == mouseXId && factory.getYId() == mouseYId) {
											target = factory;
											findClicked = true;
											break;
										}
									}
								}
								if (!findClicked) {
									for (Wall wall : mg.player1.getWalls()) {
										if (wall.getXId() == mouseXId && wall.getYId() == mouseYId) {
											target = wall;
											findClicked = true;
											break;
										}
									}
								}
							}
							
							if (findClicked) {
								for (Tank tank : mg.player0.getTanks()) {
									if (tank.getState() == Tank.TARGET) {
										tank.setAttackTarget(target);
										
										Vector destination = tank.getTargetIndices(map);
										Path p = pf.findPath(tank, tank.xId, tank.yId, 
												(int) destination.getX(), (int) destination.getY());
										if (p != null && p.getLength() > 2) {
											Path.Step currentStep = p.getStep(1);
											int currentX = currentStep.getX();
											int currentY = currentStep.getY();
											
											Path.Step nextStep = p.getStep(2);
											int nextX = nextStep.getX();
											int nextY = nextStep.getY();
											
											if (moveTargetConflicts(game, tank, currentX, currentY)) {
												currentX = tank.getXId();
												currentY = tank.getYId();
												nextX = tank.getXId();
												nextY = tank.getYId();
											} else if (moveTargetConflicts(game, tank, nextX, nextY)) {
												nextX = currentX;
												nextY = currentY;
											}
											
											tank.setCurrentTarget(currentX, currentY);
											tank.setNextTarget(nextX, nextY);
										} else if (p != null && p.getLength() == 1) {
											Path.Step currentStep = p.getStep(1);
											int currentX = currentStep.getX();
											int currentY = currentStep.getY();
											if (moveTargetConflicts(game, tank, currentX, currentY)) {
												currentX = tank.getXId();
												currentY = tank.getYId();
											}
											tank.setCurrentTarget(currentX, currentY);
										}
									}
								}
							}
						}
					}
				} else if (modeClient == Player.BUILD_FACTORY_MODE) {
					if (mg.player0.getMoney() >= FACTORY_BUILD_COST && 
							map.getTileId(mouseXId, mouseYId, objectsLayer) == 0 && 
							!buildTargetConflicts(game, mouseXId, mouseYId)) {
						mg.player0.addTankFactory(new LevelOneFactory(mouseXId, mouseYId));
						map.setTileId(mouseXId, mouseYId, objectsLayer, PLAYER_FACTORY);
						mg.player0.earnMoney(-FACTORY_BUILD_COST);
						System.out.println("SOUND ON");
						playSoundClient = true;
						//spendSound.play();
					}
					else
						playSoundClient = false;
				} else if (modeClient == Player.BUILD_WALL_MODE) {
					if (mg.player0.getMoney() >= WALL_BUILD_COST && 
							map.getTileId(mouseXId, mouseYId, objectsLayer) == 0 && 
							!buildTargetConflicts(game, mouseXId, mouseYId)) {
						mg.player0.addWall(new LevelOneWall(mouseXId, mouseYId));
						map.setTileId(mouseXId, mouseYId, objectsLayer, PLAYER_WALL);
						mg.player0.earnMoney(-WALL_BUILD_COST);
						playSoundClient = true;
						//spendSound.play();
					}
					else
						playSoundClient = false;
				} else if (modeClient == Player.REPAIR_MODE) {
					if (map.getTileId(mouseXId, mouseYId, objectsLayer) != 0) {
						boolean findClicked = false;
						
						MarshalEntity repaired = new MarshalEntity();
						
						if (hq0.getXId() == mouseXId && hq0.getYId() == mouseYId) {
							repaired = hq0;
							findClicked = true;
						} else {
							for (Tank tank : mg.player0.getTanks()) {
								if (tank.getXId() == mouseXId && tank.getYId() == mouseYId) {
									repaired = tank;
									findClicked = true;
									break;
								}
							}
							if (!findClicked) {
								for (TankFactory factory : mg.player0.getTankFactories()) {
									if (factory.getXId() == mouseXId && factory.getYId() == mouseYId) {
										repaired = factory;
										findClicked = true;
										break;
									}
								}
							}
							if (!findClicked) {
								for (Wall wall : mg.player0.getWalls()) {
									if (wall.getXId() == mouseXId && wall.getYId() == mouseYId) {
										repaired = wall;
										findClicked = true;
										break;
									}
								}
							}
						}
						
						if (findClicked) {
							int repairCost = repaired.getRepairCost();
							if (repairCost > 0 && mg.player0.getMoney() >= repairCost) {
								repaired.repair();
								mg.player0.earnMoney(-repairCost);
								playSoundClient = true;
								//spendSound.play();
							}
						}
						else{
							playSoundClient = false;
						}
					}
				} else if (modeClient == Player.UPGRADE_MODE) {
					boolean findClicked = false;
					if (mg.player0.getMoney() >= FACTORY_UPGRADE_COST) {
						for (TankFactory factory : mg.player0.getTankFactories()) {
							if (factory.getXId() == mouseXId && factory.getYId() == mouseYId 
									&& factory.getLevel() == 1) {
								factory.setState(TankFactory.INACTIVE);
								mg.player0.addTankFactory(new LevelTwoFactory(mouseXId, mouseYId));
								mg.player0.earnMoney(-FACTORY_UPGRADE_COST);
								//spendSound.play();
								playSoundClient = true;
								findClicked = true;
								upgradeOffset = factory.getMoney();
								wallUpgraded = false;
								break;
							}
							else
								playSoundClient |= false;
						}
					}
					if (!findClicked && mg.player0.getMoney() >= WALL_UPGRADE_COST) {
						for (Wall wall : mg.player0.getWalls()) {
							if (wall.getXId() == mouseXId && wall.getYId() == mouseYId 
									&& wall.getLevel() == 1) {
								wall.setState(Wall.INACTIVE);
								mg.player0.addWall(new LevelTwoWall(mouseXId, mouseYId));
								mg.player0.earnMoney(-WALL_UPGRADE_COST);
								playSoundClient = true;
								//spendSound.play();
								findClicked = true;
								upgradeOffset = wall.getMoney();
								break;
							}
							else 
									playSoundClient |= false;
						}
					}
					
					if (findClicked) {
						upgradeXId = mouseXId;
						upgradeYId = mouseYId;
					}
				} else if (modeClient == Player.PRODUCE_TANK_MODE) {
					if (mg.player0.getMoney() >= TANK_BUILD_COST) {
						for (TankFactory factory : mg.player0.getTankFactories()) {
							if (factory.getXId() == mouseXId && factory.getYId() == mouseYId) {
								Vector newTankIndices = factory.getNewTankIndices(map);
								int newX = (int) newTankIndices.getX();
								int newY = (int) newTankIndices.getY();
								if (newX != -1 && newY != -1 && 
										!buildTargetConflicts(game, newX, newY)) {
									if (factory.getLevel() == 1) {
										mg.player0.addTank(new LevelOneTank(newX, newY));
									} else {
										mg.player0.addTank(new LevelTwoTank(newX, newY));
									}
									map.setTileId(newX, newY, objectsLayer, TANK_TILE_ID);
									mg.player0.earnMoney(-TANK_BUILD_COST);
									playSoundClient = true;
									//spendSound.play();
								}
								else
									playSoundClient |= false;
								break;
							}
						}
					}
				}
			}
			mg.player0.setMode(modeClient);
		}
		// Player 0 tank control
		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			
			Vector mouseInd = getMouseTileIndices(mouseX, mouseY);
	
			int mouseXId = (int) mouseInd.getX();
			int mouseYId = (int) mouseInd.getY();
			
			int modeServer = mg.player1.getMode();
			
			if (mouseX >= panelX) {
				if (mouseY < 100) {
					mg.player1.setMode(Player.REPAIR_MODE);
				} else if (mouseY < 200) {
					mg.player1.setMode(Player.UPGRADE_MODE);
				} else if (mouseY < 300) {
					mg.player1.setMode(Player.BUILD_WALL_MODE);
				} else if (mouseY < 400) {
					mg.player1.setMode(Player.BUILD_FACTORY_MODE);
				} else if (mouseY < 500) {
					mg.player1.setMode(Player.PRODUCE_TANK_MODE);
				} else if (mouseY < 600) {
					mg.player1.setMode(Player.NORMAL_MODE);
				}
			} else if (mouseXId >= 0 && mouseYId >= 0 && 
					mouseXId < map.getWidth() && mouseYId < map.getHeight()) {
				if (modeServer == Player.NORMAL_MODE) {				
					if (map.getTileId(mouseXId, mouseYId, objectsLayer) == 0) {
						for (Tank tank : mg.player1.getTanks()) {
							if (tank.getState() == Tank.TARGET) {
								tank.setMoveDestination(mouseXId, mouseYId);
								
								Vector destination = tank.getDestination(map);
								Path p = pf.findPath(tank, tank.xId, tank.yId, 
										(int) destination.getX(), (int) destination.getY());
								if (p != null && p.getLength() > 2) {
									Path.Step currentStep = p.getStep(1);
									int currentX = currentStep.getX();
									int currentY = currentStep.getY();
									
									Path.Step nextStep = p.getStep(2);
									int nextX = nextStep.getX();
									int nextY = nextStep.getY();
									
									if (moveTargetConflicts(game, tank, currentX, currentY)) {
										currentX = tank.getXId();
										currentY = tank.getYId();
										nextX = tank.getXId();
										nextY = tank.getYId();
									} else if (moveTargetConflicts(game, tank, nextX, nextY)) {
										nextX = currentX;
										nextY = currentY;
									}
									
									tank.setCurrentTarget(currentX, currentY);
									tank.setNextTarget(nextX, nextY);
								} else if (p != null && p.getLength() == 1) {
									Path.Step currentStep = p.getStep(1);
									int currentX = currentStep.getX();
									int currentY = currentStep.getY();
									if (moveTargetConflicts(game, tank, currentX, currentY)) {
										currentX = tank.getXId();
										currentY = tank.getYId();
									}
									tank.setCurrentTarget(currentX, currentY);
								}
							}
						}
					} else {
						boolean findClicked = false;
						for (Tank tank : mg.player1.getTanks()) {
							if (tank.getXId() == mouseXId && tank.getYId() == mouseYId) {
								// handle clicked tanks
								if(!massSelection ){
									if(r.nextInt(1000) % 2 == 0)
										ResourceManager.getSound(MarshalGame.YES_SIR_SOUND).play();
									else
										ResourceManager.getSound(MarshalGame.WAITING_ORDER_SOUND).play();
									massSelection = false;	
								}
								// handle tank targeting
								if (tank.getState() == Tank.STANDBY) {
									tank.waitForTarget();
								} else if (tank.getState() == Tank.TARGET || 
										tank.getState() == Tank.MOVE || 
										tank.getState() == Tank.ATTACK || 
										tank.getState() == Tank.ATTACKING) {
									// handle tank standby command
									tank.standby();
								}
								findClicked = true;
								break;
							}
						}
						
						if (!findClicked) {
							MarshalEntity target = new MarshalEntity();
							if (hq0.getXId() == mouseXId && hq0.getYId() == mouseYId) {
								target = hq0;
								findClicked = true;
							} else {
								for (Tank another : mg.player0.getTanks()) {
									if (another.getXId() == mouseXId && another.getYId() == mouseYId) {
										target = another;
										findClicked = true;
										break;
									}
								}
								if (!findClicked) {
									for (TankFactory factory : mg.player0.getTankFactories()) {
										if (factory.getXId() == mouseXId && factory.getYId() == mouseYId) {
											target = factory;
											findClicked = true;
											break;
										}
									}
								}
								if (!findClicked) {
									for (Wall wall : mg.player0.getWalls()) {
										if (wall.getXId() == mouseXId && wall.getYId() == mouseYId) {
											target = wall;
											findClicked = true;
											break;
										}
									}
								}
							}
							
							if (findClicked) {
								for (Tank tank : mg.player1.getTanks()) {
									if (tank.getState() == Tank.TARGET) {
										tank.setAttackTarget(target);
										
										Vector destination = tank.getTargetIndices(map);
										Path p = pf.findPath(tank, tank.xId, tank.yId, 
												(int) destination.getX(), (int) destination.getY());
										if (p != null && p.getLength() > 2) {
											Path.Step currentStep = p.getStep(1);
											int currentX = currentStep.getX();
											int currentY = currentStep.getY();
											
											Path.Step nextStep = p.getStep(2);
											int nextX = nextStep.getX();
											int nextY = nextStep.getY();
											
											if (moveTargetConflicts(game, tank, currentX, currentY)) {
												currentX = tank.getXId();
												currentY = tank.getYId();
												nextX = tank.getXId();
												nextY = tank.getYId();
											} else if (moveTargetConflicts(game, tank, nextX, nextY)) {
												nextX = currentX;
												nextY = currentY;
											}
											
											tank.setCurrentTarget(currentX, currentY);
											tank.setNextTarget(nextX, nextY);
										} else if (p != null && p.getLength() == 1) {
											Path.Step currentStep = p.getStep(1);
											int currentX = currentStep.getX();
											int currentY = currentStep.getY();
											if (moveTargetConflicts(game, tank, currentX, currentY)) {
												currentX = tank.getXId();
												currentY = tank.getYId();
											}
											tank.setCurrentTarget(currentX, currentY);
										}
									}
								}
							}
						}
					}
				} else if (modeServer == Player.REPAIR_MODE) {
					if (map.getTileId(mouseXId, mouseYId, objectsLayer) != 0) {
						boolean findClicked = false;
						
						MarshalEntity repaired = new MarshalEntity();
						
						if (hq0.getXId() == mouseXId && hq0.getYId() == mouseYId) {
							repaired = hq0;
							findClicked = true;
						} else {
							for (Tank tank : mg.player1.getTanks()) {
								if (tank.getXId() == mouseXId && tank.getYId() == mouseYId) {
									repaired = tank;
									findClicked = true;
									break;
								}
							}
							if (!findClicked) {
								for (TankFactory factory : mg.player1.getTankFactories()) {
									if (factory.getXId() == mouseXId && factory.getYId() == mouseYId) {
										repaired = factory;
										findClicked = true;
										break;
									}
								}
							}
							if (!findClicked) {
								for (Wall wall : mg.player1.getWalls()) {
									if (wall.getXId() == mouseXId && wall.getYId() == mouseYId) {
										repaired = wall;
										findClicked = true;
										break;
									}
								}
							}
						}
						
						if (findClicked) {
							int repairCost = repaired.getRepairCost();
							if (repairCost > 0 && mg.player1.getMoney() >= repairCost) {
								repaired.repair();
								mg.player1.earnMoney(-repairCost);
								spendSound.play();
							}
						}
					}
				} else if (modeServer == Player.UPGRADE_MODE) {
					boolean findClicked = false;
					if (mg.player1.getMoney() >= FACTORY_UPGRADE_COST) {
						for (TankFactory factory : mg.player1.getTankFactories()) {
							if (factory.getXId() == mouseXId && factory.getYId() == mouseYId 
									&& factory.getLevel() == 1) {
								factory.setState(TankFactory.INACTIVE);
								mg.player1.addTankFactory(new LevelTwoFactory(mouseXId, mouseYId));
								mg.player1.earnMoney(-FACTORY_UPGRADE_COST);
								spendSound.play();
								findClicked = true;
								upgradeOffset2 = factory.getMoney();
								wallUpgraded2 = false;
								break;
							}
						}
					}
					if (!findClicked && mg.player1.getMoney() >= WALL_UPGRADE_COST) {
						for (Wall wall : mg.player1.getWalls()) {
							if (wall.getXId() == mouseXId && wall.getYId() == mouseYId 
									&& wall.getLevel() == 1) {
								wall.setState(Wall.INACTIVE);
								mg.player1.addWall(new LevelTwoWall(mouseXId, mouseYId));
								mg.player1.earnMoney(-WALL_UPGRADE_COST);
								spendSound.play();
								findClicked = true;
								upgradeOffset2 = wall.getMoney();
								break;
							}
						}
					}
					
					if (findClicked) {
						upgradeXId2 = mouseXId;
						upgradeYId2 = mouseYId;
					}
				} else if (modeServer == Player.BUILD_WALL_MODE) {
					if (mg.player1.getMoney() >= WALL_BUILD_COST && 
							map.getTileId(mouseXId, mouseYId, objectsLayer) == 0 && 
							!buildTargetConflicts(game, mouseXId, mouseYId)) {
						mg.player1.addWall(new LevelOneWall(mouseXId, mouseYId));
						map.setTileId(mouseXId, mouseYId, objectsLayer, PLAYER_WALL);
						mg.player1.earnMoney(-WALL_BUILD_COST);
						spendSound.play();
					}
				} else if (modeServer == Player.BUILD_FACTORY_MODE) {
					if (mg.player1.getMoney() >= FACTORY_BUILD_COST && 
							map.getTileId(mouseXId, mouseYId, objectsLayer) == 0 && 
							!buildTargetConflicts(game, mouseXId, mouseYId)) {
						mg.player1.addTankFactory(new LevelOneFactory(mouseXId, mouseYId));
						map.setTileId(mouseXId, mouseYId, objectsLayer, PLAYER_FACTORY);
						mg.player1.earnMoney(-FACTORY_BUILD_COST);
						spendSound.play();
					}
				} else if (modeServer == Player.PRODUCE_TANK_MODE) {
					if (mg.player1.getMoney() >= TANK_BUILD_COST) {
						for (TankFactory factory : mg.player1.getTankFactories()) {
							if (factory.getXId() == mouseXId && factory.getYId() == mouseYId) {
								Vector newTankIndices = factory.getNewTankIndices(map);
								int newX = (int) newTankIndices.getX();
								int newY = (int) newTankIndices.getY();
								if (newX != -1 && newY != -1 && 
										!buildTargetConflicts(game, newX, newY)) {
									if (factory.getLevel() == 1) {
										mg.player1.addTank(new LevelOneTank(newX, newY));
									} else {
										mg.player1.addTank(new LevelTwoTank(newX, newY));
									}
									map.setTileId(newX, newY, objectsLayer, TANK_TILE_ID);
									mg.player1.earnMoney(-TANK_BUILD_COST);
									spendSound.play();
								}
								break;
							}
						}
					}
				}
			}
		}
		
		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
			// test code
			/*for (Tank tank : mg.player0.getTanks()) {
				System.out.println(tank.getState());
			}*/
			for (Tank tank : mg.player1.getTanks()) {
				System.out.println(tank.getState());
			}
			for (Explosion exp : mg.explosions) {
				System.out.println(exp.isActive());
			}
			System.out.println(mg.explosions.size());
			
			disableScrolling = !disableScrolling;
		}
		if(!disableScrolling){
			// Handle scrolling by moving the camera
			if (mouseX < PlayingState.CAMERA_MOVE_RANGE) {
				moveCameraLeft(delta);
			} else if (mouseX > MarshalGame.SCREEN_WIDTH - PlayingState.CAMERA_MOVE_RANGE) {
				moveCameraRight(delta);
			}
			if (mouseY < PlayingState.CAMERA_MOVE_RANGE) {
				moveCameraUp(delta);
			} else if (mouseY > MarshalGame.SCREEN_HEIGH - PlayingState.CAMERA_MOVE_RANGE) {
				moveCameraDown(delta);
			}
		}
		
		// handle tank pathfinding
		for (Tank tank : mg.player0.getTanks()) {
			
			if (tank.getState() == Tank.MOVE) {
				Vector destination = tank.getDestination(map);
				Vector currentStep = tank.getCurrentTarget();
				int currentX = (int) currentStep.getX();
				int currentY = (int) currentStep.getY();
				Path p = pf.findPath(tank, currentX, currentY, 
						(int) destination.getX(), (int) destination.getY());
				if (p != null && p.getLength() > 1) {
					Path.Step nextStep = p.getStep(1);
					int nextX = nextStep.getX();
					int nextY = nextStep.getY();
					if (moveTargetConflicts(game, tank, nextX, nextY)) {
						nextX = currentX;
						nextY = currentY;
					}
					tank.setNextTarget(nextX, nextY);
				}
			} else if (tank.getState() == Tank.ATTACK) {
				Vector destination = tank.getTargetIndices(map);
				Vector currentStep = tank.getCurrentTarget();
				int currentX = (int) currentStep.getX();
				int currentY = (int) currentStep.getY();
				Path p = pf.findPath(tank, currentX, currentY, 
						(int) destination.getX(), (int) destination.getY());
				if (p != null && p.getLength() > 1) {
					Path.Step nextStep = p.getStep(1);
					int nextX = nextStep.getX();
					int nextY = nextStep.getY();
					if (moveTargetConflicts(game, tank, nextX, nextY)) {
						nextX = currentX;
						nextY = currentY;
					}
					tank.setNextTarget(nextX, nextY);
				}
			}

		}
		for (Tank tank : mg.player1.getTanks()) {
			
			if (tank.getState() == Tank.MOVE) {
				Vector destination = tank.getDestination(map);
				Vector currentStep = tank.getCurrentTarget();
				int currentX = (int) currentStep.getX();
				int currentY = (int) currentStep.getY();
				Path p = pf.findPath(tank, currentX, currentY, 
						(int) destination.getX(), (int) destination.getY());
				if (p != null && p.getLength() > 1) {
					Path.Step nextStep = p.getStep(1);
					int nextX = nextStep.getX();
					int nextY = nextStep.getY();
					if (moveTargetConflicts(game, tank, nextX, nextY)) {
						nextX = currentX;
						nextY = currentY;
					}
					tank.setNextTarget(nextX, nextY);
				}
			} else if (tank.getState() == Tank.ATTACK) {
				Vector destination = tank.getTargetIndices(map);
				Vector currentStep = tank.getCurrentTarget();
				int currentX = (int) currentStep.getX();
				int currentY = (int) currentStep.getY();
				Path p = pf.findPath(tank, currentX, currentY, 
						(int) destination.getX(), (int) destination.getY());
				if (p != null && p.getLength() > 1) {
					Path.Step nextStep = p.getStep(1);
					int nextX = nextStep.getX();
					int nextY = nextStep.getY();
					if (moveTargetConflicts(game, tank, nextX, nextY)) {
						nextX = currentX;
						nextY = currentY;
					}
					tank.setNextTarget(nextX, nextY);
				}
			}

		}
		
		// update tanks
		for (Tank tank : mg.player0.getTanks()) {
			map.setTileId(tank.getXId(), tank.getYId(), objectsLayer, 0);
			tank.update(delta);
			if (tank.isActive()) {
				map.setTileId(tank.getXId(), tank.getYId(), objectsLayer, TANK_TILE_ID);
				if (tank.getState() == Tank.DESTROYED && !tank.getExploded()) {
					tank.setExploded();
					mg.explosions.add(new Explosion(tank.getPosition()));
				}
			} else {
				mg.player1.earnMoney(tank.getMoney());
			}
		}
		for (Tank tank : mg.player1.getTanks()) {
			map.setTileId(tank.getXId(), tank.getYId(), objectsLayer, 0);
			tank.update(delta);
			if (tank.isActive()) {
				map.setTileId(tank.getXId(), tank.getYId(), objectsLayer, TANK_TILE_ID);
				if (tank.getState() == Tank.DESTROYED && !tank.getExploded()) {
					tank.setExploded();
					//System.out.println(tank.getPosition());
					mg.explosions.add(new Explosion(tank.getPosition()));
				}
			} else {
				mg.player0.earnMoney(tank.getMoney());
			}
		}
		
		// remove inactive tanks
		for (Iterator<Tank> i = mg.player0.getTanks().iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		for (Iterator<Tank> i = mg.player1.getTanks().iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		
		// update tank factories
		for (TankFactory factory : mg.player0.getTankFactories()) {
			factory.update(delta);
			if (factory.getState() == TankFactory.DESTROYED && !factory.getExploded()) {
				factory.setExploded();
				mg.explosions.add(new Explosion(factory.getPosition()));
			} else if (!factory.isActive()) {
				map.setTileId(factory.getXId(), factory.getYId(), objectsLayer, 0);
				mg.player1.earnMoney(factory.getMoney());
			}
		}
		for (TankFactory factory : mg.player1.getTankFactories()) {
			factory.update(delta);
			if (factory.getState() == TankFactory.DESTROYED && !factory.getExploded()) {
				factory.setExploded();
				mg.explosions.add(new Explosion(factory.getPosition()));
			} else if (!factory.isActive()) {
				map.setTileId(factory.getXId(), factory.getYId(), objectsLayer, 0);
				mg.player0.earnMoney(factory.getMoney());
			}
		}
		
		// update defense walls
		for (Wall wall : mg.player0.getWalls()) {
			wall.update(delta);
			if (wall.getState() == Wall.DESTROYED && !wall.getExploded()) {
				wall.setExploded();
				mg.explosions.add(new Explosion(wall.getPosition()));
			} else if (!wall.isActive()) {
				map.setTileId(wall.getXId(), wall.getYId(), objectsLayer, 0);
				mg.player1.earnMoney(wall.getMoney());
			}
		}
		for (Wall wall : mg.player1.getWalls()) {
			wall.update(delta);
			if (wall.getState() == Wall.DESTROYED && !wall.getExploded()) {
				wall.setExploded();
				mg.explosions.add(new Explosion(wall.getPosition()));
			} else if (!wall.isActive()) {
				map.setTileId(wall.getXId(), wall.getYId(), objectsLayer, 0);
				mg.player0.earnMoney(wall.getMoney());
			}
		}
		
		// remove inactive tank factories
		for (Iterator<TankFactory> i = mg.player0.getTankFactories().iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		for (Iterator<TankFactory> i = mg.player1.getTankFactories().iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		
		// remove inactive defense walls
		for (Iterator<Wall> i = mg.player0.getWalls().iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		for (Iterator<Wall> i = mg.player1.getWalls().iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		
		// handle explosions
		for (Explosion explosion : mg.explosions) {
			explosion.update(delta);
		}
		for (Iterator<Explosion> i = mg.explosions.iterator(); i.hasNext();) {
			if (!i.next().isActive()) {
				i.remove();
			}
		}
		
		// handle upgraded items
		if (upgradeOffset > 0) {
			mg.player1.earnMoney(-upgradeOffset);
			if (wallUpgraded) {
				map.setTileId(upgradeXId, upgradeYId, objectsLayer, PLAYER_WALL);
			} else {
				map.setTileId(upgradeXId, upgradeYId, objectsLayer, PLAYER_FACTORY);
			}
		}
		
		if (upgradeOffset2 > 0) {
			mg.player0.earnMoney(-upgradeOffset2);
			if (wallUpgraded2) {
				map.setTileId(upgradeXId2, upgradeYId2, objectsLayer, PLAYER_WALL);
			} else {
				map.setTileId(upgradeXId2, upgradeYId2, objectsLayer, PLAYER_FACTORY);
			}
		}
		hq0.update(delta);
		hq1.update(delta);
		// Add game over state

		if (hq0.isActive() && !hq1.isActive()) {  // play1's headquarters was destroyed 
			finalScore = mg.player0.getMoney();
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setUserScore(finalScore);
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setMessage("You LOSE!");
			
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserName(msg.username);
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserScore(mg.player1.getMoney());
			gameState.setOtherName(MarshalGame.username);
			gameState.setOtherScore(mg.player1.getMoney());
			game.enterState(MarshalGame.GAMEOVERSTATE, new FadeOutTransition(), new FadeInTransition());
			gameState.setWinner(0);
		}
		if (!hq0.isActive() && hq1.isActive()) {  // play0's headquarters was destroyed 
			finalScore = mg.player1.getMoney();
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setUserScore(finalScore);
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setMessage("You WIN!");
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserName(msg.username);
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserScore(mg.player0.getMoney());
			gameState.setOtherName(MarshalGame.username);
			gameState.setOtherScore(mg.player1.getMoney());
			game.enterState(MarshalGame.GAMEOVERSTATE, new FadeOutTransition(), new FadeInTransition());
			gameState.setWinner(1);
		}

		
		if(MarshalGame.serverMode)
			sendGameState(mg);
	}
	
	private boolean mouseNotInPanel(Vector mouse) {
		int panelW = uiPanel.getWidth();
		int panelX = MarshalGame.SCREEN_WIDTH - panelW;
		if (mouse.getX() >= panelX)
			return false;
		return true;
	}

	private Vector getMouseTileIndices(Vector clientMouse2) {
		Vector clientMouseTile = null;
		if(msg.getCameraPos() != null)
			clientMouseTile = getMouseTileIndices((int)clientMouse.getX() , (int)clientMouse.getY() , msg.getCameraPos());
		else 
			clientMouseTile = getMouseTileIndices((int)clientMouse.getX() , (int)clientMouse.getY());
		return clientMouseTile;
	}

	private void sendGameState(MarshalGame mg) {
		if(msg == null)
			return;
		gameState.setClientMode(mg.player0.getMode());
		if(gameState.getClientMode()== 0 ){
			gameState.setPlaySound(false);
		}
		else
			gameState.setPlaySound(playSoundClient);
		gameState.setHqHealth(mg.player0.getHeadquarter().getHealth() , 0);
		gameState.setHqHealth(mg.player1.getHeadquarter().getHealth() , 1);
		//tanks, walls, factories, fireballs, explosions
		gameState.clearLists();
		Vector clientCamera;
		if(msg.getCameraPos() != null)
			 clientCamera= msg.getCameraPos();
		else
			 clientCamera = cameraPos;
		Vector translateVec = new Vector(-clientCamera.getX(), -clientCamera.getY());
		Vector maxIndices = getMouseTileIndices(MarshalGame.SCREEN_WIDTH, MarshalGame.SCREEN_HEIGH , clientCamera);
		Vector minIndices = getMouseTileIndices(0, 0 , clientCamera);
		int maxX = (int) maxIndices.getX();
		int maxY = (int) maxIndices.getY();
		int minX = (int) minIndices.getX();
		int minY = (int) minIndices.getY();
		
		for(Tank tank: mg.player0.getTanks()){
			if(inView(maxX , maxY , minX , minY , tank)){
				gameState.addTank(tank , 0);
			}
		}
		for(Tank tank: mg.player1.getTanks()){
			if(inView(maxX , maxY , minX , minY , tank)){
				gameState.addTank(tank , 1);
			}
		}
		for(TankFactory f: mg.player0.getTankFactories()){
			if(inView(maxX , maxY , minX , minY , f)){
				gameState.addFactory(f , 0);
			}
		}
		for(TankFactory f: mg.player1.getTankFactories()){
			if(inView(maxX , maxY , minX , minY , f)){
				gameState.addFactory(f , 1);
			}
		}
		for(Wall w: mg.player0.getWalls()){
			if(inView(maxX , maxY , minX , minY , w)){
				gameState.addWall(w , 0);
			}
		}
		for(Wall w: mg.player1.getWalls()){
			if(inView(maxX , maxY , minX , minY , w)){
				gameState.addWall(w , 1);
			}
		}
		for(Explosion w: mg.explosions){
			//if(w.isActive()){
				gameState.addExplosion(w);
				//Vector ind = getMouseTileIndices((int)w.getPosition().getX() , (int)w.getPosition().getY());
				//w.setxId((int)ind.getX());
				//w.setyId((int)ind.getY());
				//System.out.println(w.getX()/48 + " ---- " + w.getY()/48);
				
			//}
		}
		for(Tank t: mg.player0.getTanks()){
			Fireball f = t.getFireball();
			if(f == null || f.isActive() == false)
				continue;
			if(f.getSent() == true)
				continue;
			else{
				f.setSent(true);
				gameState.addFireball(f);
			}
		}
		for(Tank t: mg.player1.getTanks()){
			Fireball f = t.getFireball();
			if(f == null || f.isActive() == false)
				continue;
			if(f.getSent() == true)
				continue;
			else{
				f.setSent(true);
				gameState.addFireball(f);
			}
		}
		gameState.setMoney(mg.player0.getMoney());
		gameState.setHqHealth(mg.player0.getHeadquarter().getHealth() , 0);
		MarshalGame.server.send(gameState);
	}

	private boolean buildTargetConflicts(StateBasedGame game, int nextX, int nextY) {
		MarshalGame mg = (MarshalGame)game;
		for (Tank other : mg.player0.getTanks()) {
			if (other.getState() != Tank.MOVE && 
					other.getState() != Tank.ATTACK) {
				continue;
			}
			Vector otherTarget = other.getCurrentTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
			otherTarget = other.getNextTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
		}
		for (Tank other : mg.player1.getTanks()) {
			if (other.getState() != Tank.MOVE && 
					other.getState() != Tank.ATTACK) {
				continue;
			}
			Vector otherTarget = other.getCurrentTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
			otherTarget = other.getNextTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean moveTargetConflicts(StateBasedGame game, Tank tank, int nextX, int nextY) {
		MarshalGame mg = (MarshalGame)game;
		for (Tank other : mg.player0.getTanks()) {
			if (tank == other) {
				continue;
			}
			if (other.getState() != Tank.MOVE && 
					other.getState() != Tank.ATTACK) {
				continue;
			}
			Vector otherTarget = other.getCurrentTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
			otherTarget = other.getNextTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
		}
		for (Tank other : mg.player1.getTanks()) {
			if (tank == other) {
				continue;
			}
			if (other.getState() != Tank.MOVE && 
					other.getState() != Tank.ATTACK) {
				continue;
			}
			Vector otherTarget = other.getCurrentTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
			otherTarget = other.getNextTarget();
			if (nextX == (int) otherTarget.getX() && 
					nextY == (int) otherTarget.getY()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final void mousePressed(final int button, final int x, final int y) {
		if (button == Input.MOUSE_LEFT_BUTTON) { 
		    this.mouseFrom.setX(x); 
		    this.mouseFrom.setY(y);
		}
	}
	
	@Override
    public void mouseDragged(final int oldX, final int oldY, final int newX, final int newY) {
		if (container.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
			this.selectionRectVisible = true;
			mouseTo.setX(newX);
			mouseTo.setY(newY);
			Vector start = getMouseTileIndices((int)this.mouseFrom.getX(), (int)this.mouseFrom.getY());
			Vector end = getMouseTileIndices((int)this.mouseTo.getX(), (int)this.mouseTo.getY());
			float s;

			//Swap if necessary
			if (start.getX() > end.getX()) {
			    s = end.getX();
			    end.setX(start.getX());
			    start.setX(s);
			}
	
			if (start.getY() > end.getY()) {
			    s = end.getY();
			    end.setY(start.getY());
			    start.setY(s);
			}
			
			int boxH = (int) (end.getY() - start.getY());
			int boxW = (int) (end.getX() - start.getX());

			this.selectionRectangle.setBounds(start.getX(), start.getY(), boxW, boxH);	
			//System.out.println("MouseDragged");
	    } 
    }
	 public void mouseDragged(final int oldX, final int oldY , final int oldTX , final int oldTY, final int newTX, final int newTY) {
			Vector start = new Vector(oldTX , oldTY);
			Vector end = new Vector(newTX , newTY);
			float s;
			//Swap if necessary
			if (start.getX() > end.getX()) {
			    s = end.getX();
			    end.setX(start.getX());
			    start.setX(s);
			}
	
			if (start.getY() > end.getY()) {
			    s = end.getY();
			    end.setY(start.getY());
			    start.setY(s);
			}
			
			int boxH = (int) (end.getY() - start.getY());
			int boxW = (int) (end.getX() - start.getX());

			this.selectionRectangle.setBounds(start.getX(), start.getY(), boxW, boxH);	
 }
	@Override
    public final void mouseReleased(final int button, final int x, final int y) {
		if (button == Input.MOUSE_LEFT_BUTTON  && selectionRectVisible) {
			this.selectionRectVisible = false;

			if (this.selectionRectangle.getWidth() * this.selectionRectangle.getHeight() >=1) {
				//Select objects 
				massSelection = true;
				ArrayList<Tank> selectedTanks = selectMovableEntitiesInsideBox(this.selectionRectangle , 1);
				for(Tank tank: selectedTanks)
					mgame.player1.addTank(tank);
			
				if (!selectedTanks.isEmpty()) { 
					//if(client != null)
						//client.sendMessage("tank@");
					if(r.nextInt(1000) % 2 == 0)
						ResourceManager.getSound(MarshalGame.YES_SIR_SOUND).play();
					else
						ResourceManager.getSound(MarshalGame.WAITING_ORDER_SOUND).play(); 
				}
				else{
					//if(client != null)
						//client.sendMesfsage("@");
				}
			}
			//System.out.println("MouseReleased");
		}
    }
	 public final void clientMouseReleased(final int button, final int x, final int y) {
			if (button == Input.MOUSE_LEFT_BUTTON) {

				if (this.selectionRectangle.getWidth() * this.selectionRectangle.getHeight() >=1) {
					//Select objects 
					massSelection = true;
					ArrayList<Tank> selectedTanks = selectMovableEntitiesInsideBox(this.selectionRectangle , 0);
					for(Tank tank: selectedTanks)
						mgame.player0.addTank(tank);
				
					if (!selectedTanks.isEmpty()) { 
						//if(client != null)
							//client.sendMessage("tank@");
						if(r.nextInt(1000) % 2 == 0)
							ResourceManager.getSound(MarshalGame.YES_SIR_SOUND).play();
						else
							ResourceManager.getSound(MarshalGame.WAITING_ORDER_SOUND).play(); 
					}
					else{
						//if(client != null)
							//client.sendMesfsage("@");
					}
				}
				//System.out.println("MouseReleased");
			}
	    }
	private ArrayList<Tank> selectMovableEntitiesInsideBox(Rectangle s , int player) {
		ArrayList<Tank> tanksInsideBox = new ArrayList<Tank>();
		
		int startXId = (int)s.getMinX();
		int startYId = (int)s.getMinY();
		int endXId = (int)s.getMaxX();
		int endYId = (int)s.getMaxY();
		Set<Tank> tanks = null;
		if(player == 0)
			tanks = mgame.player0.getTanks();
		else
			tanks = mgame.player1.getTanks();
		for (Tank tank : tanks) {
			if (tank.getXId() >= startXId && tank.getXId() <= endXId) {
				if (tank.getYId() >= startYId && tank.getYId() <= endYId) {
					tanksInsideBox.add(tank);
					
					if (tank.getState() == Tank.STANDBY || tank.getState() == Tank.TARGET) {
						tank.waitForTarget();
					} else if (tank.getState() == Tank.TARGET || 
							tank.getState() == Tank.ATTACK || 
							tank.getState() == Tank.ATTACKING) {
						// handle tank standby command
						tank.standby();
					}
				}
			}	
		}
		return tanksInsideBox;
	}

	/***
	 * Finds the indices of the tile clicked. 
	 * @param x x position of the mouse click
	 * @param y y position of the mouse click
	 * @return
	 */
	public Vector getMouseTileIndices(int x, int y) {
		int xInd = (int) (x + cameraPos.getX()) / map.getTileWidth();
		int yInd = (int) (y + cameraPos.getY()) / map.getTileHeight();
		return new Vector(xInd, yInd);
	}
	public Vector getMouseTileIndices(int x, int y , Vector cameraPos) {
		int xInd = (int) (x + cameraPos.getX()) / map.getTileWidth();
		int yInd = (int) (y + cameraPos.getY()) / map.getTileHeight();
		return new Vector(xInd, yInd);
	}
	public void moveCameraLeft(int delta) {
		float newX = cameraPos.getX() - PlayingState.CAMERA_SPEED * delta;
		//int cameraTile = (int)(cameraPos.getX()/map.getTileWidth());
		//if (newX >= cameraMinX || (cameraTile == 1) || (cameraTile ==0 && newX >= 0)) {
		if (newX >= cameraMinX){	
			cameraPos = cameraPos.setX(newX);
		}
	}
	
	public void moveCameraRight(int delta) {
		float newX = cameraPos.getX() + PlayingState.CAMERA_SPEED * delta;
		if (newX <= cameraMaxX) {
			cameraPos = cameraPos.setX(newX);
		}
	}
	
	public void moveCameraUp(int delta) {
		float newY = cameraPos.getY() - PlayingState.CAMERA_SPEED * delta;
		//int cameraTile = (int)(cameraPos.getY()/map.getTileHeight());
		//if (newY >= cameraMinY || (cameraTile == 1) || (cameraTile ==0 && newY >= 0)) {
		if (newY >= cameraMinY){	
			cameraPos = cameraPos.setY(newY);
		}
	}
	
	public void moveCameraDown(int delta) {
		float newY = cameraPos.getY() + PlayingState.CAMERA_SPEED * delta;
		if (newY <= cameraMaxY) {
			cameraPos = cameraPos.setY(newY);
		}
	}

	@Override
	public int getID() {
		return MarshalGame.PLAYINGSTATE;
	}

	public String getCurrentInfo() {
		// TODO Prepare information to be sent to the server
		return null;
	}
	public CopyOnWriteArrayList<Player> getPlayers() {
		// TODO Get current list of players
		return players;
	}
	public void setPlayers(CopyOnWriteArrayList<Player> players) {
		this.players = players;
	}
	
	public Client getClient(){
		return this.client;
	}
	public Player getMe() { 
		return me;
	}
	public void setMe(Player me) {
		this.me = me;
	}

	public static  void updateGame(ClientMessage m) {
		if(m.isView()){
			msg.setCameraPos(m.getCameraPos());
			return;
		}
			
		clientName = m.username;	

		msg = m;
		
		if(m.isMousePressed()){
			clientMouse = m.getMouse();
			clientMouseUpdate = true;
			//clientButton = -1;
			//clientArea = false;
		}
		if(m.getSelectionRectangle()!= null){
			currentState.clientAreaUpdate = true;
			currentState.mouseDragged( 0 , 0 
					, (int)m.getSelectionRectangle().getMinX() 
					, (int)m.getSelectionRectangle().getMinY()
					, (int)m.getSelectionRectangle().getMaxX()
					, (int)m.getSelectionRectangle().getMaxY());
			currentState.clientMouseReleased(0  
					, (int)m.getSelectionRectangle().getMaxX()
					, (int)m.getSelectionRectangle().getMaxY());
			clientArea = true;
		}
		else{
			//comes into else condition
			//System.out.println(m);
		}
	}
}
