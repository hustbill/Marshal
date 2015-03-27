package general;

import java.awt.event.KeyEvent;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


public class NamePrompt extends BasicGameState{
	String string = "";
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.drawString("ENTER YOUR NAME", 400, 400);
		Input in = container.getInput();
		g.drawString(string, 400, 430);
		
		if(in.isKeyPressed(Input.KEY_ENTER)){
			MarshalGame.username = string;
			game.enterState(MarshalGame.MENUSTATE);
		}
			
	}
	public void keyPressed(int key , char c){
		if(c == Input.KEY_ENTER)
			return;
		string += c;
	}
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return MarshalGame.NAMEPROMPT;
	}

}
