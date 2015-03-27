package general;

import java.awt.Font;

import jig.ResourceManager;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

public class MenuState extends BasicGameState{

	private Image background = null;
	private Font font;
	private TrueTypeFont ttf; 
	private static final int PLAY_BUTTON = 0;
	private static final int SERVER_BUTTON = 1;
	private static final int QUIT_BUTTON = 4;
	private static final int LEADERBOARD_BUTTON = 2;
	private static final int SOUND_BUTTON = 3;
	private int buttonHovered = -1;
	private int fontSize = 35;
	
	private Sound mouseover = null;
	private boolean mouseSound = false;
	private int lastButton = -1;
	private Sound intro;
	
	public MenuState() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		background = ResourceManager.getImage(MarshalGame.BACK_TITLE_IMAGE);
		font = new Font("Verdana", Font.BOLD, 20);
	    ttf = new TrueTypeFont(font, true);
	   mouseover = ResourceManager.getSound(MarshalGame.MOUSE_OVER_SOUND);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
			Input input = container.getInput();
			float mX = input.getMouseX();
			float mY = input.getMouseY();
			
			if(mX >= 200 && mX <= 470){
				lastButton = buttonHovered;
				buttonHovered = (int)(mY - 320)/(int)(1.25*fontSize); //determines which button was hovered
			}
			if(buttonHovered < PLAY_BUTTON || buttonHovered > QUIT_BUTTON)
				mouseSound = false;
			if(input.isMousePressed(Input.MOUSE_LEFT_BUTTON)){
				switch(buttonHovered){
				case PLAY_BUTTON:
					//intro.stop();
					game.enterState(MarshalGame.WAITSTATE);
					break;
				case SERVER_BUTTON:
					if(MarshalGame.serverMode)
						MarshalGame.serverMode = false;
					else 
						MarshalGame.serverMode = true;
					break;
				case LEADERBOARD_BUTTON:
					//TODO
					game.enterState(MarshalGame.LEADERBOARD , new FadeOutTransition() , new FadeInTransition());
					break;
			//	case SOUND_BUTTON:
				//	if(intro.playing())
					//	intro.stop();
					//else
						//intro.play();
					//break;
				case QUIT_BUTTON:
					MarshalGame.app.destroy();
					break; 
				}
			}
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		background.draw(0,0);
		ttf.drawString(200, 320, "Play" , Color.black);
		if(MarshalGame.serverMode)
			ttf.drawString(200, 360, "ServerMode: ON" , Color.black);
		else 
			ttf.drawString(200, 360 , "ServerMode: OFF" , Color.black);
		ttf.drawString(200, 400 , "LEADERBOARD" , Color.black);
		//ttf.drawString( 200, 440 , "SOUND" , Color.black);
		ttf.drawString( 200, 480 , "QUIT" , Color.black);
		
		switch(buttonHovered){
			case PLAY_BUTTON:
				ttf.drawString(200, 320, "Play" , Color.white);
				if(!mouseover.playing() &&(lastButton != buttonHovered ||mouseSound == false)){
					mouseover.play();
					mouseSound = true;
				}
				break;
			case SERVER_BUTTON:
				if(MarshalGame.serverMode)
					ttf.drawString(200, 360, "ServerMode: ON" , Color.white);
				else 
					ttf.drawString(200, 360 , "ServerMode: OFF" , Color.white);
				if(!mouseover.playing()&& (lastButton != buttonHovered ||mouseSound == false)){
					mouseover.play();
					mouseSound = true;
				}
				break;
			case LEADERBOARD_BUTTON:
				ttf.drawString(200, 400 , "LEADERBOARD" , Color.white);
				if(!mouseover.playing()&& (lastButton != buttonHovered ||mouseSound == false)){
					mouseover.play();
					mouseSound = true;
				}
				break;
			case SOUND_BUTTON:
				//ttf.drawString(200, 440 , "SOUND" , Color.white);
				if(!mouseover.playing()&& (lastButton != buttonHovered ||mouseSound == false)){
					mouseover.play();
					mouseSound = true;
				}
				break;
			case QUIT_BUTTON:
				ttf.drawString( 200, 480 , "QUIT" , Color.white);
				System.out.println(lastButton + " " + buttonHovered);
				if(!mouseover.playing()&& (lastButton != buttonHovered ||mouseSound == false)){
					mouseover.play();
					mouseSound = true;
				}
				break;
		}
	}	
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		background = ResourceManager.getImage(MarshalGame.BACK_TITLE_IMAGE);
		font = new Font("Franklin Gothic Book", Font.BOLD, fontSize);
	    ttf = new TrueTypeFont(font, true);
	    mouseover = ResourceManager.getSound(MarshalGame.MOUSE_OVER_SOUND);
	    //intro = ResourceManager.getSound(MarshalGame.INTRO_SOUND);
	    //intro.play();
	}
	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return MarshalGame.MENUSTATE;
	}

}
