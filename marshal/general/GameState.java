package general;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameState implements Serializable{
	/**
	 * serialVersionUID helps clients and server serialize same class
	 * This UID is auto-generated
	 */
	private static final long serialVersionUID = 3761514747794126937L;
	private ArrayList<String> tanks = null;
	private ArrayList<String> walls = null;
	private ArrayList<String> factories = null;
	private ArrayList<String> fireballs = null;
	private Set<String> explosions = null;
	private ArrayList<String> health = null;
	private float[] hqHealth;
	private int clientMode;
	private int money;
	
	private boolean close;
	private boolean playSound;
	private int winner;
	public String otherName;
	public int otherScore;
	
	public GameState(){
		tanks = new ArrayList<String>();
		walls = new ArrayList<String>();
		factories = new ArrayList<String>();
		fireballs = new ArrayList<String>();
		explosions = new HashSet<String>();
		health = new ArrayList<String>();
		close = false;
		hqHealth = new float[2];
		setPlaySound(false);
		setWinner(-1);
	}
	public String getOtherName(){
		return otherName;
	}
	public int getOtherScore(){
		return otherScore;
	}
	public void setOtherName(String s){
		otherName = s;
	}
	public void setOtherScore(int s){
		otherScore  = s;
	}
	public boolean close(){
		return close;
	}
	public ArrayList<String> getTanks(){
		return tanks;
	}
	public ArrayList<String> getWalls(){
		return walls;
	}
	public ArrayList<String> getFactories(){
		return factories;
	}
	public Set<String> getExplosions(){
		return explosions;
	}
	public ArrayList<String> getFireballs(){
		return fireballs;
	}
	public void addTank(Tank tank, int isPlayer0){
		tanks.add(getInfo(tank , isPlayer0));
	}	
	public void addWall(Wall w , int t){
		walls.add(getInfo(w , t));
	}
	public void addFactory(TankFactory f , int t){
		factories.add(getInfo(f , t));
	}
	public void addFireball(Fireball f){
		fireballs.add(getInfo(f));
	}
	public void addExplosion(Explosion e){
		explosions.add(getInfo(e));
	}
	public void addHealth(final float h , final  float x , final float y){
		health.add(getInfo(h , x , y));
	}
	private String getInfo(float health, float x, float y) {
		String info = "";
		//info += MarshalGame.TYPE_HEALTH + ","+ health + "," + x + "," + y ;
		info = health + "," + x + "," + y ;
		return info;
	}
	private String getInfo(Tank t, int isPlayer0) {
		String info = "";
		info = t.getX() + "," + t.getY() +"," + t.getDirection() + "," + t.getLevel() + "," + t.getHealth() + "," + t.getState() + "," + isPlayer0;
		return info;
	}
	private String getInfo(TankFactory t , int player) {
		String info = "";
		info = t.getX() + "," + t.getY() + "," + t.getLevel() + "," + t.getHealth() + "," + player;
		return info;
	}
	private String getInfo(Wall t , int player) {
		String info = "";
		info = t.getX() + "," + t.getY() + "," + t.getLevel() + "," + t.getHealth() + "," + player;
		return info;
	}
	private String getInfo(Fireball f) {
		String info = "";
		info = f.getSource().getX() + "," + f.getSource().getY()  + "," + f.getGoal().getX()  + "," + f.getGoal().getY() + "," + f.getDirection();
		return info;
	}
	private String getInfo(Explosion e) {
		String info = "";
		info = e.getPosition().getX() + "," + e.getPosition().getY();
		return info;
	}
	public float getHqHealth(int i) {
		return hqHealth[i];
	}
	public void setHqHealth(float hqHealth, int i) {
		this.hqHealth[i] = hqHealth;
	}
	public void clearLists(){
		tanks.clear();
		walls.clear();
		factories.clear();
		fireballs.clear();
		explosions.clear();
	}
	public int getClientMode() {
		return clientMode;
	}
	public void setClientMode(int clientMode) {
		this.clientMode = clientMode;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public void setPlaySound(boolean playSoundClient) {
		this.playSound = playSoundClient;
	}
	public boolean getPlaySound(){
		return playSound;
	}
	public void setWinner(int i) {
		winner = i;
	}
	public int getWinner(){
		return winner;
	}
}
