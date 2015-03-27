package server;

import general.PlayingState;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
	Set<ServerClient> clients;
	private Queue<String> queue; 
	private boolean gameIsRunning;
	
	
	public Server(){
		gameIsRunning = true;
		clients = new HashSet<ServerClient>();
		queue = new ConcurrentLinkedQueue<String>();
	}

	public Set<ServerClient> getClients() {
		return clients;
	}
	public void setClients(Set<ServerClient> clients) {
		this.clients = clients;
	}
	
	/***
	 * Broadcast function
	 * */
	public void sendMessageToAll(String info){ 
		for (ServerClient sc : this.clients){
			//sc.sendMessage(info);
			
		}
	}
	/***
	 * Process all information in the queue
	 * */
	public void coordinator(){
		if (queue.peek() != null && gameIsRunning){
			sendMessageToAll(queue.poll());
		}
	}
	
	public void addInfoToQueue () {
		String s="";
		this.queue.add(s);
	}

	public Queue<String> getQueue() {
		return queue;
	}
	
	public void endGame(){
		this.gameIsRunning = false;
		sendMessageToAll("end");
	}

	public static void calculateGameState(String s) {
		if(s.startsWith("tank")){
			PlayingState.tank_selected = true;
		}
		else 
			PlayingState.tank_selected = false;
	}
}
