package client;

import general.ClientMessage;
import general.ClientPlayingState;
import general.GameState;
import general.MarshalGame;
import general.PlayingState;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class Client {

	String host;
	int port;
	ObjectInputStream in;
	ObjectOutputStream out;
	
	private Socket clientSocket;
	private GameState gs; 

	public Client(String host, int port){
		this.host = host;
		this.port = port;
		
		try {
			clientSocket = new Socket(host, port);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
				
			start();

		} catch (EOFException e) {
		
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() throws IOException{
		new Thread() {		
			@Override
			public void run() {
				while (true){
					try{
						gs = (GameState)in.readObject();
						if (gs.equals(null)){
							continue;
						}
						if (gs.close())
						{
								in.close();
								out.close();
								clientSocket.close();
						}
						else{
							ClientPlayingState.updateGame(gs);
						}

					}catch(EOFException e){
						}
					catch(ClassNotFoundException e){
						System.out.println(e);
					}catch(IOException e){
						System.out.println(e);
					}
				}
			}
		}.start();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	public Socket getClientSocket() {
		return clientSocket;
	}

	public void sendMessage(ClientMessage message){
		try {
			//System.out.println("Sending " + message);
			//if(message.getSelectionRectangle() != null)
				//MarshalGame.app.destroy();
			System.out.println(message.username);
			out.reset(); // VERY IMPORTANT !!!!!!
			out.writeObject(message); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
