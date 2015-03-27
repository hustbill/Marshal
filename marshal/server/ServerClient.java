package server;

import general.ClientMessage;
import general.GameState;
import general.PlayingState;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ServerClient extends Thread {

	Socket socket;
	ObjectInputStream in;
	ObjectOutputStream out;
	ClientMessage m = null;
	PlayingState ps = null;
	
	//TODO: Declare private variables here

	public ServerClient(Socket socket){
		this.socket = socket;
	}
	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			
			while (true){
				try{
					m = (ClientMessage)in.readObject();
				}catch(SocketException e){
						continue;
				}
				catch(EOFException e){
					if(m == null)
						continue;
				}
				if (m.close())
				{
						in.close();
						out.close();
						socket.close();
				}
				else{
					PlayingState.updateGame(m);
				}
			}
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void send(GameState gameState){
		try {
			out.reset(); //VERY IMPORTANT
			if(gameState.getPlaySound())
				System.out.println(gameState.getClientMode());
			out.writeObject(gameState);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
