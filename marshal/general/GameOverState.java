package general;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import jig.ResourceManager;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.state.transition.HorizontalSplitTransition;


/**
 * This state is active when the Game is over. In this state, the tank is
 * neither drawn nor updated; and a gameover banner is displayed. A timer
 * automatically transitions back to the StartUp State.
 * 
 * Transitions From PlayingState
 * 
 * Transitions To StartUpState
 */
class GameOverState extends BasicGameState {
	
	String string = null;
	
	private int timer;
	private int score; // the user's score, to be displayed, but not updated.
	private int loserScore;
	private String message = null;

	private String loserName;
	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		
		
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		timer = 6000;
		try {
			 FileInputStream fin = new FileInputStream("leaders.ser");
			try{
			 ObjectInputStream ois = new ObjectInputStream(fin);
			 string = (String) ois.readObject();
			 ois.close();
			}catch(EOFException ex){
				string = "";
			}   
			string += MarshalGame.username + " - " + score + "\n";
			string += loserName + " - " + getLoserScore() + "\n";
			
		    FileOutputStream fout = new FileOutputStream("leaders.ser");
		    ObjectOutputStream oos = new ObjectOutputStream(fout);
		    oos.writeObject(string);
		    oos.close();
		    }
		   catch (Exception e) { e.printStackTrace(); }
	}

	public void setUserScore(int score) {
		this.score = score;
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game,
			Graphics g) throws SlickException {

		MarshalGame bg = (MarshalGame)game;
		g.drawString("Score: " + score, 10, 30);

		g.drawImage(ResourceManager.getImage(MarshalGame.GAMEOVER_BANNER_RSC), 225,
				270);
		g.drawString(message, 10, 50);
		g.drawString(string , 10 , 100);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {
		MarshalGame mg = (MarshalGame)game;
		
		timer -= delta;
		if (timer <= 0){
			game.enterState(MarshalGame.MENUSTATE, new FadeOutTransition(), new FadeInTransition() );
			MarshalGame.server = null;
			MarshalGame.serverSocket = null;
			MarshalGame.client = null;
			MarshalGame.msg = null;
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

	}

	@Override
	public int getID() {
		return MarshalGame.GAMEOVERSTATE;
	}

	public void setMessage(String string) {
		this.message  = string;
	}

	public void setLoserName(String username) {
		loserName = username;
	}
	public String getLoserName(){
		return loserName;
	}
	public int getLoserScore() {
		return loserScore;
	}
	
	public void setLoserScore(int loserScore) {
		this.loserScore = loserScore;
	}
}