package general;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jig.ResourceManager;
import jig.Vector;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
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

public class ClientPlayingState extends BasicGameState {
	
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
	
	private TrueTypeFont levelFont;
	
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
	
	private Sound spendSound;
	
	public static final int NORMAL_MODE = 0;
	public static final int REPAIR_MODE = 1;
	public static final int UPGRADE_MODE = 2;
	public static final int BUILD_WALL_MODE = 3;
	public static final int BUILD_FACTORY_MODE = 4;
	public static final int PRODUCE_TANK_MODE = 5;
	
	public int mode;
	
	public static final int FACTORY_BUILD_COST = 1500;
	public static final int WALL_BUILD_COST = 1000;
	public static final int TANK_BUILD_COST = 1500;
	
	public int  finalScore = 1000;
	
	private Player me;
	private Client client = null;
	private static String host = "localhost";
	private static int port = 3333;
	public static boolean tank_selected = false;
	private static int clientButton;
	private static Vector clientMouse;
	private static List<Tank> myTanks = null;
	private static GameState gameState = null;
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
	private ClientMessage msg = null;
	private boolean updateGameState = true;
	private SpriteSheet tankImages;
	private Image tankImage;
	private boolean disableScrolling = false;
	public static final int KABOOMDURATION = 30;
	
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
	private Image factory0;
	private Image wallImage;
	private Animation kaboom;
	private Set<Fireball> fireballs;
	private Sound bang = null;
	private Sound spend_sound;
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		
		//createClient();
		spend_sound = ResourceManager.getSound(MarshalGame.SPEND_MONEY_SOUND);
		factory0 = ResourceManager.getImage(MarshalGame.FACTORY_IMAGE);
		wallImage = ResourceManager.getImage(MarshalGame.WALL_IMAGE);
		disableScrolling = false;
		state = PlayingState.START;
		map = new TiledMap("marshal/resource/third_map.tmx");
		cameraPos = new Vector(0, 0);
		myTanks = new ArrayList<Tank>();
		MarshalGame mg = (MarshalGame)game;
		for(Tank t : mg.player0.getTanks()){
			myTanks.add(t);
			//System.out.println("INIT SIZE: " + myTanks.size());
		}
		fireballs = new HashSet<Fireball>();
		int tx = map.getTileWidth(); 
		int ty = map.getTileHeight();
		kaboom = new Animation(ResourceManager.getSpriteSheet(MarshalGame.explosionSheetPath, 256, 128), 
				KABOOMDURATION);
		bang  = ResourceManager.getSound(MarshalGame.BANG_EXPLOSIONSND_RSC);
		
		uiPanel = ResourceManager.getImage(MarshalGame.UI_IMAGE);
		
		cameraMaxX = map.getWidth() * tx  + tx - MarshalGame.SCREEN_WIDTH + uiPanel.getWidth(); 
		cameraMaxY = map.getHeight() * ty + ty - MarshalGame.SCREEN_HEIGH;
		cameraMinX = -tx;
		cameraMinY = -ty;  
		
		mh = new MarshalHeuristic();
		
		Font tempFont = new Font("Times New Roman", Font.BOLD, MONEY_SIZE);
		moneyFont = new TrueTypeFont(tempFont, false);
		
		tempFont = new Font("Verdana", Font.BOLD, 20);
		controlFont = new TrueTypeFont(tempFont, true);
		
		tempFont = new Font("Verdana", Font.BOLD, 14);
		levelFont = new TrueTypeFont(tempFont, true);
		
		mode = Player.NORMAL_MODE;
		
		spendSound = new Sound(MarshalGame.SPEND_MONEY_SOUND);
		
		clientButton = -1;
		clientMouse = null;
		
		this.container = container;
		
		tankImages = ResourceManager.getSpriteSheet(MarshalGame.PLAYER_TANKS, 
				MarshalEntity.TILE_WIDTH, MarshalEntity.TILE_HEIGHT);
		tankImage = tankImages.getSprite(0, 0);
	}

	public void enter(GameContainer container, StateBasedGame game) 
			throws SlickException {
		disableScrolling = false;
		this.msg = MarshalGame.msg;
		this.client = MarshalGame.client;
		msg.setText("Hey server!");
		client.sendMessage(msg);
		
		clientButton = -1;
		clientMouse = null;
		
		state = PlayingState.START;
		map = new TiledMap("marshal/resource/third_map.tmx");
		cameraPos = new Vector(0, 0);
		
		mh = new MarshalHeuristic();
		
		Font tempFont = new Font("Times New Roman", Font.BOLD, MONEY_SIZE);
		moneyFont = new TrueTypeFont(tempFont, false);
		
		tempFont = new Font("Verdana", Font.BOLD, 20);
		controlFont = new TrueTypeFont(tempFont, true);
		
		tempFont = new Font("Verdana", Font.BOLD, 14);
		levelFont = new TrueTypeFont(tempFont, true);
		
		mode = Player.NORMAL_MODE;
		
		spendSound = ResourceManager.getSound(MarshalGame.SPEND_MONEY_SOUND);
		
		uiPanel = ResourceManager.getImage(MarshalGame.UI_IMAGE);

		int tx = map.getTileWidth();
		int ty = map.getTileHeight();
		cameraMaxX = map.getWidth() * tx  + tx - MarshalGame.SCREEN_WIDTH + uiPanel.getWidth(); 
		cameraMaxY = map.getHeight() * ty + ty - MarshalGame.SCREEN_HEIGH;
		cameraMinX = -tx;
		cameraMinY = -ty;
		//Networking init
		MarshalGame mg = (MarshalGame)game;
		//init myTanks
		
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
		
		for(Tank t : mg.player0.getTanks()){
			myTanks.add(t);
			//System.out.println(myTanks.size());
		}
		
		// test factory and wall. should remove later
		/*mg.player0.addTankFactory(new LevelOneFactory(6, 6));
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
		mg.player1.addWall(new LevelOneWall(12, 7));
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
		
		/*if(StartUpState.getConnect()){
			msg = new ClientMessage();
			this.client =  new Client(host, port);
			int n = r.nextInt(1000) + 1;
			players = new CopyOnWriteArrayList<Player>();
			String name = n + "";
			System.out.println(name);
			me = new Player(name, 0);
			players.add(me);
		}*/
		this.container = container;
	}
	
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		Input in = container.getInput();
		MarshalGame mg = (MarshalGame)game;
		int tx = map.getTileWidth();
		int ty = map.getTileHeight();
		
		Vector translateVec = new Vector(-cameraPos.getX(), -cameraPos.getY());
		map.render((int) translateVec.getX(), (int) translateVec.getY());
		//g.drawString(getMouseTileIndices(in.getMouseX(), in.getMouseY()).toString(), in.getMouseX(), in.getMouseY());
		
		Vector maxIndices = getMouseTileIndices(MarshalGame.SCREEN_WIDTH, MarshalGame.SCREEN_HEIGH);
		Vector minIndices = getMouseTileIndices(0, 0);
		int maxX = (int) maxIndices.getX();
		int maxY = (int) maxIndices.getY();
		int minX = (int) minIndices.getX();
		int minY = (int) minIndices.getY();
		if(gameState != null){
			mg.player0.getHeadquarter().setHealth(gameState.getHqHealth(0));
			mg.player1.getHeadquarter().setHealth(gameState.getHqHealth(1));
			mode = gameState.getClientMode();
		}
		if (inView(maxX, maxY, minX, minY, mg.player0.getHeadquarter())) {
			renderHealthBar(g, mg.player0.getHeadquarter(), tx, ty, cameraPos, mg.player0.isPlayer0());
		}
		if (inView(maxX, maxY, minX, minY, mg.player1.getHeadquarter())) {
			renderHealthBar(g, mg.player1.getHeadquarter(), tx, ty, cameraPos, mg.player1.isPlayer0());
		}
		int panelW = uiPanel.getWidth();
		int panelH = uiPanel.getHeight();
		
		int panelX = MarshalGame.SCREEN_WIDTH - panelW;
		int panelY = MarshalGame.SCREEN_HEIGH - panelH;
		
		//if(gameState != null)
			//mode = gameState.getClientMode();
		
		if (mode != NORMAL_MODE) {
			g.setColor(Color.red);
			switch (mode) {
				case REPAIR_MODE:
					g.drawString("REPAIR MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case UPGRADE_MODE:
					g.drawString("UPGRADE MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case BUILD_WALL_MODE:
					g.drawString("WALL BUILDING MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case BUILD_FACTORY_MODE:
					g.drawString("FACTORY BUILDING MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				case PRODUCE_TANK_MODE:
					g.drawString("TANK MANUFACTURE MODE", MarshalGame.SCREEN_WIDTH / 3, 25);
					break;
				default:
					break;
			}
			g.setColor(Color.white);
		}
		
		if(gameState != null){
			//render tanks
			g.setFont(levelFont);
			for(String t: gameState.getTanks()){
				String[] tank = t.split(",");
				float x = Float.parseFloat(tank[0]) - map.getTileWidth()/2;
				float y = Float.parseFloat(tank[1]) - map.getTileHeight()/2;
				
				int direction = Integer.parseInt(tank[2]);
				Vector newPos = (new Vector(x , y)).add(translateVec);
				
				switch(direction){
					case Tank.UP:
						tankImage = tankImages.getSprite(18, 0);
						break;
					case Tank.DOWN:
						tankImage = tankImages.getSprite(0, 0);
						break;
					case Tank.LEFT:
						tankImage = tankImages.getSprite(9, 0);
						tankImage = tankImage.getFlippedCopy(true, false);
						break;
					case Tank.RIGHT:
						tankImage = tankImages.getSprite(9, 0);
						break;
					case Tank.UPLEFT:
						tankImage = tankImages.getSprite(15, 0);
						tankImage = tankImage.getFlippedCopy(true, false);
						break;
					case Tank.UPRIGHT:
						tankImage = tankImages.getSprite(15, 0);
						break;
					case Tank.DOWNRIGHT:
						tankImage = tankImages.getSprite(3, 0);
						break;
					case Tank.DOWNLEFT:
						tankImage = tankImages.getSprite(3, 0);
						tankImage = tankImage.getFlippedCopy(true, false);
						break;
				}
				g.drawImage(tankImage, newPos.getX(), newPos.getY());
				float health = Float.parseFloat(tank[4]);
				Vector pos = new Vector(Float.parseFloat(tank[0]) , Float.parseFloat(tank[1]));
				int state = Integer.parseInt(tank[5]);
				int level = Integer.parseInt(tank[3]);
				int player = Integer.parseInt(tank[6]);
				//tank.renderFireball(g, cameraPos);
				//if (inView(maxX, maxY, minX, minY, tank)) {
					renderHealthBar(g, health, pos, tx, ty, cameraPos, player);
					renderLevel(g, pos , level , tx, ty, cameraPos);
					if (state == Tank.TARGET && player == 0) {
						renderTargetingTank(g, pos, tx, ty, cameraPos);
					}
				//}
					
				//System.out.println(x + " " + y);
			}
			//render factories
			
			for (String f: gameState.getFactories()) {
				String[] factory = f.split(",");
				float health = Float.parseFloat(factory[3]);
				int level = Integer.parseInt(factory[2]);
				Vector pos = new Vector(Float.parseFloat(factory[0]) , Float.parseFloat(factory[1]));
				Vector newPos = pos.add(translateVec);
				//Vector posInd = getMouseTileIndices((int)pos.getX() , (int)pos.getY());
				//map.setTileId((int)posInd.getX(), (int)posInd.getY(), objectsLayer, PLAYER_FACTORY);
				g.drawImage(factory0, newPos.getX() - map.getTileWidth()/2, newPos.getY() - map.getTileHeight()/2);
				//if (inView(maxX, maxY, minX, minY, factory)) {
					renderHealthBar(g, health, pos , tx , ty, cameraPos, Integer.parseInt(factory[4]));
					renderLevel(g, pos , level , tx, ty, cameraPos);
				//}
			}
			
			for (String w : gameState.getWalls()) {
				String[] wall = w.split(",");
				float health = Float.parseFloat(wall[3]);
				int level = Integer.parseInt(wall[2]);
				Vector pos = new Vector(Float.parseFloat(wall[0]), Float.parseFloat(wall[1]));
				Vector newPos = pos.add(translateVec);
				//Vector posInd = getMouseTileIndices((int)pos.getX() , (int)pos.getY());
				//map.setTileId((int)posInd.getX(), (int)posInd.getY(), objectsLayer, PLAYER_WALL);
				g.drawImage(wallImage, newPos.getX() - map.getTileWidth()/2, newPos.getY() - map.getTileHeight()/2);
				//if (inView(maxX, maxY, minX, minY, wall)) {
					renderHealthBar(g, health , pos, tx, ty, cameraPos,  Integer.parseInt(wall[4]));
					renderLevel(g, pos , level, tx, ty, cameraPos);
				//}
			}
			for (String w : gameState.getExplosions()) {
				String[] explosion = w.split(",");
				//Weird offset value
				//Does not work without them
				float x = Float.parseFloat(explosion[0]) - 3*map.getTileWidth();
				float y = Float.parseFloat(explosion[1]) - map.getTileHeight();
				Vector newPos = (new Vector(x , y)).add(translateVec);
				g.drawAnimation(kaboom, newPos.getX(), newPos.getY());
				if(bang != null && bang.playing() == false){
					bang.play();
				}
				//System.out.println("EXPLOSION : " + x + "," + y + ":" + translateVec + ":" + newPos);
			}
			for (String w : gameState.getFireballs()) {
				String[] fireball = w.split(",");
				//Weird offset value
				//Does not work without them
				float sx = Float.parseFloat(fireball[0]);
				float sy = Float.parseFloat(fireball[1]);
				float gx = Float.parseFloat(fireball[2]);
				float gy = Float.parseFloat(fireball[3]);
				int dir = Integer.parseInt(fireball[4]);
				
				Fireball f = new Fireball(new Vector(sx , sy) , new Vector(gx , gy) , dir);
				fireballs.add(f);
				System.out.println("Fireball added ");
			}
			if(gameState.getPlaySound() && spend_sound.playing() == false){
				spend_sound.play();
			}
		}
		
		for(Fireball f: fireballs){
			f.translate(translateVec);
			f.render(g);
			f.translate(translateVec.scale(-1));
		}
		/*
		//TEST CODE 
		if(tank_selected)
			g.drawString("TANK IS SELECTED", 120, 10);
		else 
			g.drawString("TANK IS NOT SELECTED", 120, 10);

		if(WaitOpponent.isServer ==true && MarshalGame.serverMode == true)
			g.drawString("I am SERVER", 400, 10);
		else
			g.drawString("I am CLIENT", 400, 10);	*/
		g.drawImage(uiPanel, panelX, panelY);
		
		g.setFont(moneyFont);
		g.setColor(Color.yellow);
		
		if(gameState != null)
			g.drawString("$ " + gameState.getMoney(), 
					panelX + panelW - MONEY_IND_X_OFFSET, 
					panelY + panelH - MONEY_IND_Y_OFFSET);
		else 
			g.drawString("" + mg.player0.getMoney(), 
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
	}

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
	private void renderHealthBar(Graphics g, float health, Vector me, 
			int tileWidth, int tileHeight, Vector cameraPos, int player) {
		
		float xLeft = me.getX() - map.getWidth() / 2 + PlayingState.HEALTH_BAR_X_OFFSET - cameraPos.getX();
		float xMid = xLeft + health * PlayingState.HEALTH_BAR_WIDTH;
		float yUp = me.getY() - map.getHeight() / 2 + PlayingState.HEALTH_BAR_Y_OFFSET - cameraPos.getY();
		
		if (player == 0) {
			g.setColor(new Color(0, 0, 255));
		} else {
			g.setColor(new Color(255, 0, 0));
		}
		g.fillRect(xLeft, yUp, 
				health * PlayingState.HEALTH_BAR_WIDTH, PlayingState.HEALTH_BAR_HEIGHT);
		g.setColor(new Color(255, 255, 255));
		g.fillRect(xMid, yUp, 
				(1 - health) * PlayingState.HEALTH_BAR_WIDTH, PlayingState.HEALTH_BAR_HEIGHT);
	}
	
	private void renderTargetingTank(Graphics g, Tank tank, int tileWidth, int tileHeight, Vector cameraPos) {
		Vector upperLeftCornerPos = tank.getUpperLeftCornerPos(tileWidth, tileHeight);
		
		float xLeft = upperLeftCornerPos.getX() - cameraPos.getX();
		float yUp = upperLeftCornerPos.getY() - cameraPos.getY();
		
		g.setColor(new Color(255, 0, 0));
		g.drawRect(xLeft, yUp, tileWidth, tileHeight);
	}
	private void renderLevel(Graphics g, Vector me , int level, int tileWidth, int tileHeight, Vector cameraPos) {
		float x = me.getX() - map.getWidth() / 2 + PlayingState.HEALTH_BAR_X_OFFSET - cameraPos.getX();
		float y = me.getY() + map.getHeight() / 2 - 3 * PlayingState.HEALTH_BAR_HEIGHT - cameraPos.getY();
		g.setColor(new Color(192, 206, 250));
		g.drawString("L " + level, x, y);
	}
	private void renderTargetingTank(Graphics g, Vector pos, int tileWidth, int tileHeight, Vector cameraPos) {
		
		float xLeft = pos.getX() - cameraPos.getX() - tileWidth/2;
		float yUp = pos.getY() - cameraPos.getY() - tileHeight/2;
		
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
		msg.setUsername(MarshalGame.username);
		
		Headquarter hq0 = mg.player0.getHeadquarter();
		Headquarter hq1 = mg.player1.getHeadquarter();
		
		int mouseX = input.getMouseX();
		int mouseY = input.getMouseY();
		
		int panelW = uiPanel.getWidth();
		int panelX = MarshalGame.SCREEN_WIDTH - panelW;
		
		int objectsLayer = map.getLayerIndex("Objects");
		MarshalMap mm = new MarshalMap(map, objectsLayer);
		if(gameState != null){
			if(gameState.getWinner() == 0){
				finalScore = gameState.getMoney();
				String m = MarshalGame.username;
				int s = finalScore;
				finalScore = gameState.otherScore;
				MarshalGame.username = gameState.otherName;
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setUserScore(finalScore);
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setMessage("You WIN!");
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserName(m);
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserScore(s);
				game.enterState(MarshalGame.GAMEOVERSTATE, new FadeOutTransition(), new FadeInTransition());
			}
			else if(gameState.getWinner() == 1){
				finalScore = gameState.getMoney();
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setUserScore(finalScore);
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setMessage("You LOSE!");
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserName(gameState.otherName);
				((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setLoserScore(gameState.otherScore);
				game.enterState(MarshalGame.GAMEOVERSTATE, new FadeOutTransition(), new FadeInTransition());
			}
		}
		/***Needed input from clients: mouse click tile indices
		 * client: 
		 * 	computes mouse click tile indices using local mouse click positions and camera positions
		 * 	=>
		 * 	sends mouse click tile indices to server
		 * server: 
		 * 	processes control
		 */	
		mode = mg.player0.getMode();
		msg.setCameraPos(cameraPos);
		msg.setMode(mode);
		// Player 0 tank control
		//Client sends message only when mouse button was clicked. IT does not send cameraPos repeatedly. 
		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
			Vector mousePos = new Vector(mouseX, mouseY);

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
			}	
			mode = mg.player0.getMode();
			msg.setMode(mode);
			
			if(msg != null){
					msg.setButton(Input.MOUSE_LEFT_BUTTON);
					msg.setMouse(mousePos);
					msg.setSelectionRectangle(null);
					client.sendMessage(msg);
			}
		}
		else{
			msg.sendView(client, cameraPos);
		}
		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
			// test code
			for (Tank tank : mg.player0.getTanks()) {
				System.out.println(tank.getState());
			}
			for (Tank tank : mg.player1.getTanks()) {
				System.out.println(tank.getState());
			}
			System.out.println(mode);
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
		// Add game over state
		if (hq0.isActive() && !hq1.isActive()) {  // play1's headquarters was destroyed 
			finalScore = mg.player0.getMoney();
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setUserScore(finalScore);
			game.enterState(MarshalGame.GAMEOVERSTATE);
		}
		if (!hq0.isActive() && hq1.isActive()) {  // play0's headquarters was destroyed 
			finalScore = mg.player1.getMoney();
			((GameOverState) game.getState(MarshalGame.GAMEOVERSTATE)).setUserScore(finalScore);
			game.enterState(MarshalGame.GAMEOVERSTATE);
		}
		for(Iterator<Fireball> i = fireballs.iterator(); i.hasNext(); ){
			if(!i.next().isActive())
				i.remove();
		}
		for(Iterator<Fireball> i = fireballs.iterator(); i.hasNext(); ){
			Fireball f = i.next();
			if(f.isActive())
				f.update(delta);
		}
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
	
	@Override
    public final void mouseReleased(final int button, final int x, final int y) {
		if (button == Input.MOUSE_LEFT_BUTTON  && this.selectionRectVisible) {
			this.selectionRectVisible = false;
			
			if (this.selectionRectangle.getWidth() * this.selectionRectangle.getHeight() >=1) {
				msg.areaSelected(selectionRectangle);
				msg.setCameraPos(cameraPos);
				client.sendMessage(msg);
				//Select objects 
				massSelection = true;
				ArrayList<Tank> selectedTanks = selectMovableEntitiesInsideBox(this.selectionRectangle);
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
						//client.sendMessage("@");
				}
			}
			//System.out.println("MouseReleased");
		}
    }
	private ArrayList<Tank> selectMovableEntitiesInsideBox(Rectangle s) {
		ArrayList<Tank> tanksInsideBox = new ArrayList<Tank>();
		
		int startXId = (int)s.getMinX();
		int startYId = (int)s.getMinY();
		int endXId = (int)s.getMaxX();
		int endYId = (int)s.getMaxY();
		
		for (Tank tank : mgame.player0.getTanks()) {
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
		return MarshalGame.CLIENTPLAYINGSTATE;
	}

	public String getCurrentInfo() {
		// TODO Prepare information to be sent to the server
		return null;
	}
	public static void updateGame(GameState gs) {
		gameState  = gs;
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
}
