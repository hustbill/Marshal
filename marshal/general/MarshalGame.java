package general;

import java.net.ServerSocket;
import java.util.ArrayList;

import jig.Entity;
import jig.ResourceManager;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import client.Client;
import server.ServerClient;


public class MarshalGame extends StateBasedGame {
	
	public static final String GAMENAME = "Marshal";
	public static final String GAMEOVER_BANNER_RSC = "marshal/resource/GameOver.png";
	public static final String BANG_EXPLOSIONIMG_RSC ="marshal/resource/explosion.png";
	public static final String BANG_EXPLOSIONSND_RSC = "marshal/resource/explosion.wav";
	public static final String groundSheetPath   = "marshal/resource/tileset_desert.png";
	public static final String fireballSheetPath = "marshal/resource/fireball.png";
	public static final String explosionSheetPath   = "marshal/resource/explosion.png";
	public static final String PLAY0_TANKFactorys = "marshal/resource/manufactory0.png";
	public static final String PLAY1_TANKFactorys = "marshal/resource/manufactory1.png";
	
	public static final String PLAYER_TANKS = "marshal/resource/tank_images.png";
	public static final String YES_SIR_SOUND = "marshal/resource/soviet_yes_sir.wav";
	public static final String WAITING_ORDER_SOUND = "marshal/resource/soviet_waiting_order.wav";
	
	public static final String MOUSE_OVER_SOUND = "marshal/resource/multimedia_rollover_030.wav";
	public static final String INTRO_SOUND = "marshal/resource/panem_anthem.wav";
	
	//public static final String EARN_MONEY_SOUND = "";
	public static final String SPEND_MONEY_SOUND = "marshal/resource/sell_buy_item.wav";
	
	public static final String UI_IMAGE = "marshal/resource/UI_long0.png";
	public static final String BACK_IMAGE = "marshal/resource/marshal.jpg";
	public static final String BACK_TITLE_IMAGE = "marshal/resource/marshal_title.png";
	public static final String WALL_IMAGE = "marshal/resource/wall_48.png";
	public static final String FACTORY_IMAGE = "marshal/resource/factory_48.png";
	public static final String MINI = "marshal/resource/mini.png";
	
	public static final int SCREEN_WIDTH = 1200;
	public static final int SCREEN_HEIGH = 800;
	
	public static final int NEAREST_EMPTY_TILE_SEARCH_LIMIT = 5;
	
//	public static final int STARTUPSTATE = 0;
	public static final int ENDSTATE = 1;
	public static final int GAMEOVERSTATE = 2;
	public static final int CONFIGSTATE = 3;
	public static final int WAITSTATE = 4;
	public static final int PLAYINGSTATE = 10;
	public static final int MENUSTATE = 5;
	public static final int CLIENTPLAYINGSTATE = 12;
	
	public static int TYPE_TANK = 0;
	public static int TYPE_WALL = 1;
	public static int TYPE_FIREBALL = 2;
	public static int TYPE_FACTORY = 3;
	public static int TYPE_EXPLOSION = 4;
	public static int TYPE_HEALTH = 5;
	
	public static int tileSize = 16;
	public final int ScreenWidth;
	public final int ScreenHeight;
	
	public static boolean createClient = false;
	public static boolean serverMode = false;
	public static String host = "localhost"; //172.16.32.45 replace with IP of the computer you want to connect to
	public static int port = 3333; // can be replaced with another port number
	public static String clientId = "";
	
	Player player0;
	Player player1;
	ArrayList<Explosion> explosions;
	
	public static AppGameContainer app = null;
	public static Client client = null;
	public static ClientMessage msg  = null;
	public static ServerSocket serverSocket = null;
	public static ServerClient server = null;
	public static String username;
	public static final int LEADERBOARD = 33;
	public static final int NAMEPROMPT = 66;
	

	public MarshalGame(String title, int width, int height) {
		super(title);
		ScreenHeight = height;
		ScreenWidth = width;
		
		Entity.setCoarseGrainedCollisionBoundary(Entity.CIRCLE);
		
		player0 = new Player(true);
		player1 = new Player(false);
		explosions = new ArrayList<Explosion>();
		
		serverMode = false;
		
	}
	public static void main(String[] args) {
		try {
			app = new AppGameContainer(new MarshalGame(GAMENAME, SCREEN_WIDTH, SCREEN_HEIGH));
			app.setAlwaysRender(true);
			app.setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGH, false);
			app.setVSync(true);
			app.start();
			
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		addState(new NamePrompt());
		addState(new MenuState());
		addState(new WaitOpponent());
//		addState(new StartUpState());
		addState(new GameOverState());
		addState(new EndState());
		addState(new PlayingState());
		addState(new ClientPlayingState());
		addState(new Leaderboard());
			
		ResourceManager.loadImage(MINI);
		
		ResourceManager.loadImage(PLAYER_TANKS);
		ResourceManager.loadImage(PLAY0_TANKFactorys);
		ResourceManager.loadImage(PLAY1_TANKFactorys);
		
		
		ResourceManager.loadImage(UI_IMAGE);
		ResourceManager.loadImage(BACK_IMAGE);
		ResourceManager.loadImage(BACK_TITLE_IMAGE);
	
		ResourceManager.loadImage(GAMEOVER_BANNER_RSC);		
		//ResourceManager.loadImage(BANG_EXPLOSIONIMG_RSC);

		ResourceManager.loadImage(groundSheetPath);
		ResourceManager.loadImage(fireballSheetPath);
		ResourceManager.loadImage(explosionSheetPath);

		ResourceManager.loadImage(MarshalGame.FACTORY_IMAGE);
		ResourceManager.loadImage(MarshalGame.WALL_IMAGE);
		
		ResourceManager.loadSound(BANG_EXPLOSIONSND_RSC);
		ResourceManager.loadSound(YES_SIR_SOUND);
		ResourceManager.loadSound(WAITING_ORDER_SOUND);
		
		ResourceManager.loadSound(MOUSE_OVER_SOUND);
		ResourceManager.loadSound(INTRO_SOUND);
		ResourceManager.loadSound(SPEND_MONEY_SOUND);
	}
}
