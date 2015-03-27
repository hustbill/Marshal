package general;

import java.io.Serializable;

import org.newdawn.slick.geom.Rectangle;

import client.Client;
import jig.Vector;


public class ClientMessage implements Serializable{

	private static final long serialVersionUID = 8184037616721571463L;
	private int button = -1; //mouse button
	private Vector mouse = null; //mouse click coordinates
	private boolean close = false; 
	private Rectangle selectionRectangle = null;
	private String text;
	private Vector cameraPos = null; //client camera position
	private int mode = Player.NORMAL_MODE;
	public String username;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String s) {
		this.username = s;
	}
	
	public void buttonPressed(int key){
		setButton(key);
		setMouse(null);
		setSelectionRectangle(null);
	}
	public void mousePressed(Vector mouse){
		this.setMouse(mouse);
		System.out.println(getMouse());
		setButton(-1);
		setSelectionRectangle(null);
	}
	public boolean close(){
		return close;
	}
	public int getButton() {
		return button;
	}
	public void setButton(int button) {
		this.button = button;
	}
	public Vector getMouse() {
		return mouse;
	}
	public void setMouse(Vector mouse) {
		this.mouse = mouse;
	}
	public String toString(){
		if(getSelectionRectangle() != null)
			return "Area(" + getSelectionRectangle().getMinX() + "," + getSelectionRectangle().getMinY() + "," + getSelectionRectangle().getMaxX() + "," +getSelectionRectangle().getMaxY() + ")" + "Which button  = " + getButton() + " Mouse indices = " + getMouse() + "TEXT : " + getText();
		else 
			return "Which button  = " + getButton() + " Mouse indices = " + getMouse() + "TEXT : " + getText();

	}
	public boolean equals(Object o) {
		if (this == o) 
			return true;
		if (o == null || getClass() != o.getClass()) 
			return false;
		ClientMessage msg = (ClientMessage) o;
		if (button != msg.getButton()) 
			return false;
		if (mouse != msg.getMouse()) 
			return false;
		return true;
	}
	public Rectangle getSelectionRectangle() {
		return selectionRectangle;
	}
	public void setSelectionRectangle(Rectangle selectionRectangle) {
		this.selectionRectangle = selectionRectangle;
	}
	public boolean isMousePressed(){
		if(getMouse()==null)
			return false;
		return true;
	}
	public boolean isAreaSelected(){
		if(getSelectionRectangle() == null)
			return false;
		return true;
	}
	public boolean isButtonPressed(){
		if(getButton() == -1)
			return false;
		return true;
	}
	public String getText() {
		return text;
	}
	public void areaSelected(Rectangle selectionRectangle) {
		this.setSelectionRectangle(selectionRectangle);
		setMouse(null);
		setButton(-1);
	}
	public void setText(String text) {
		this.text = text;
	}
	public Vector getCameraPos() {
		return cameraPos;
	}
	public void setCameraPos(Vector cameraPos) {
		this.cameraPos = cameraPos;
	}
	public void sendView(Client client, Vector cameraPos) {
		setMouse(null);
		setButton(-1);
		setSelectionRectangle(null);
		setCameraPos(cameraPos);
		client.sendMessage(this);
	}
	public boolean isView(){
		if(!isButtonPressed() && !isMousePressed() && !isAreaSelected() && getCameraPos() != null)
			return true;
		else 
			return false;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
}
