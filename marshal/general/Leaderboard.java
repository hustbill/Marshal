package general;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

public class Leaderboard extends BasicGameState{
	String leaders = null;
	ArrayList<String> sorter = null;
	int[][] list;
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		sorter = new ArrayList<String>();
		list = new int[50][2];
	}
	
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		try(
				
				 FileInputStream file = new FileInputStream("leaders.ser");
				 InputStream buffer = new BufferedInputStream(file);
				 ObjectInputStream input = new ObjectInputStream (buffer);
		    ){
			try{
				 String lead = (String)input.readObject();
				 leaders = lead;
				 
				 if(lead == null)
					 return;
				 int i = 0;
				/*for(String leader : leaders){
					String[] a = leader.split("-");
					int num = Integer.parseInt(a[1]);
					sorter.add(a[0]);
					list[i][0] = i;
					list[i][1] = num;
				}*/
				//Arrays.sort(list, new ColumnComparator(0));
					
			}finally{
				input.close();
			}
				
			}
			 catch(ClassNotFoundException ex){
			}
		 catch(EOFException ex){
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
	}
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.drawString("LEADERBOARD", 10, 30);
		g.drawString("RETURN" , 10 , 50);
		g.drawString(leaders , 100 , 100);
		/*if(leaders != null){
			int i = 1;
			for(String leader: leaders){
				g.drawString(leader , 10 , 100 + 30*i);
				i++;
			}
		}*/
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		// TODO Auto-generated method stub
		Input in = container.getInput();
		int mx = in.getMouseX();
		int my = in.getMouseY();
		if(in.isMousePressed(Input.MOUSE_LEFT_BUTTON)){
			if(mx < 100 && mx > 10){
				if(my < 80 && my > 50){
					game.enterState(MarshalGame.MENUSTATE , new FadeOutTransition() , new FadeInTransition() );
				}
			}
		}
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return MarshalGame.LEADERBOARD;
	}

}
