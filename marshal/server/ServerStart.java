package server;

import general.MarshalGame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerStart {
	public static volatile boolean serverStart = false;
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		
		//Automatically start one java program 
		new Thread() {		
			public void run() {
				MarshalGame.main(new String[0]);
			}
		}.start();
		
		ServerSocket serverSocket;
		
		serverSocket = new ServerSocket(3333);
		
		System.out.println("Server Started!");
		ServerClient client = null;
		
		while (true){
				Socket connectionSocket = serverSocket.accept();
				System.out.println("New Client! IP:" + connectionSocket.getInetAddress());
				client = new ServerClient(connectionSocket);
				client.start();
		}
	}
}
