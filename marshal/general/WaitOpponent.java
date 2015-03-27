package general;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import client.Client;
import server.Server;
import server.ServerClient;

public class WaitOpponent extends BasicGameState {
	
	ServerSocket serverSocket = null;
	String host; 
	Server server = null;
	public static boolean isServer = false;
	private int port;
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		isServer = false;
		this.host = MarshalGame.host;
		this.port = MarshalGame.port;
	}
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if(!MarshalGame.serverMode){
			game.enterState(MarshalGame.PLAYINGSTATE);
		}
		else{
			try {
				if(host.equals("localhost")){
					if(MarshalGame.serverSocket == null){
						try {
							MarshalGame.serverSocket = new ServerSocket(3333);
							System.out.println("Socket created");
						} catch (IOException e) {
							 isServer = false;
							 MarshalGame.msg = new ClientMessage();
							 MarshalGame.client = new Client(host, port);
							 game.enterState(MarshalGame.CLIENTPLAYINGSTATE); 
							e.printStackTrace();
						}
					}
					else{
						try {
							 Socket connectionSocket = null; 
							 connectionSocket = MarshalGame.serverSocket.accept();
							 System.out.println("New Client! IP:" + connectionSocket.getInetAddress());
							 MarshalGame.server = new ServerClient(connectionSocket);
							 isServer = true;
							 MarshalGame.server.start();
							 game.enterState(MarshalGame.PLAYINGSTATE);
						 } catch (IOException e) {
							 e.printStackTrace();
						 }
					}
				}
				else{
					if(myIP().equals(host)){ //am i server
						if(MarshalGame.serverSocket == null){
							try {
								MarshalGame.serverSocket = new ServerSocket(3333);
								System.out.println("Socket created");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else{
							try {
								 Socket connectionSocket = null; 
								 String p = "";
								 do{
										 connectionSocket = MarshalGame.serverSocket.accept();
										 p = connectionSocket.getInetAddress().getHostName();
								 }while(p.equals(host));
								 System.out.println("New Client! IP:" + connectionSocket.getInetAddress());
								 MarshalGame.server = new ServerClient(connectionSocket);
								 isServer = true;
								 MarshalGame.server.start();
								 
								 game.enterState(MarshalGame.PLAYINGSTATE);
							 } catch (IOException e) {
								 e.printStackTrace();
							 }
						}
					}
				 else{
					 isServer = false;
					 MarshalGame.msg = new ClientMessage();
					 MarshalGame.client = new Client(host, port);
					 game.enterState(MarshalGame.CLIENTPLAYINGSTATE); 
				 }
			   }
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	private String myIP() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
		
	}
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {	
		if(MarshalGame.serverSocket != null)
			g.drawString("SERVER STARTED: WAITING FOR THE OPPONENT", 100, 100);
		else 
			g.drawString("I AM A CLIENT WAITING TO START GAME", 100, 100);
	}
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		server = null;
		serverSocket = null;
		host = MarshalGame.host;
	}
	public int getID() {
		return MarshalGame.WAITSTATE; // State id must be non-zero
	}
}
