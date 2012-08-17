package client;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import shared.Game;
import shared.Room;

/**
 * ClientModel holds the model that the client uses to keep track of its state.
 * 
 * @author Andy Katz
 *
 */
public class ClientModel {
	
	private RoomWindow window;
	private Sprite player;
	private ArrayList<Sprite> others;
	private int xPos;
	private int yPos;
	public boolean canChat;
	
	private Socket server;
	private DataOutputStream out;
	
	private Semaphore sem;
	
	public ClientModel(int x, int y, Sprite myPlayer, Socket serverSocket){
		xPos = x;
		yPos = y;
		canChat = false;
		
		player = myPlayer;
		others = new ArrayList<Sprite>();
		
		server = serverSocket;
		sem = new Semaphore(1);
		window = new RoomWindow(xPos, yPos, server, sem);
		
		try {
			out = new DataOutputStream(server.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * isLoading() tells if the model is still loading its initial state.
	 * 
	 * @return true if it is still loading
	 */
	public boolean isLoading() {
		return window.window[1][1] == null;
	}
	
	//--------------------------------------
	//Footstep functions
	//--------------------------------------
	
	/**
	 * updateFootstep() updates the footstep information of the given x,y cell in the
	 * current Room.
	 * 
	 * @param x the x-coordinate of the cell
 	 * @param y the y-coordinate of the cell
	 * @param currentTimeMillis the footstep information
	 */
	public void updateFootstep(int x, int y, long currentTimeMillis) {
		boolean updated = window.updateFootstep(x, y, currentTimeMillis);
		if (updated){
			try{
				sem.acquire();
				int gridX = xPos / Game.width;
				int gridY = yPos / Game.height;
				
				out.writeBytes("FOOTSTEP" + "\n" + gridX + "\n" + gridY + "\n" + x + "\n" + y + "\n"
						+ currentTimeMillis + "\n");
				out.flush();
				sem.release();
			} catch (Exception e){
				return;
			}
		}
	}
	
	/**
	 * updateFootstep() updates the footstep information of the given x,y cell in the
	 * given Room.
	 * 
	 * @param gridX the x-coordinate of the Room
	 * @param gridY the y-coordinate of the Room
	 * @param x the x-coordinate of the cell
 	 * @param y the y-coordinate of the cell
	 * @param currentTimeMillis the footstep information
	 */
	public void updateFootstep(int gridX, int gridY, int x, int y, long time) {
		window.updateFootstep(gridX, gridY, x, y, time);
		
	}
	
	/**
	 * getFootsteps() gets the footstep information for the given (x,y) coordinate.
	 * 
	 * @param x the x-coordinate of the cell
	 * @param y the y-coordinate of the cell
	 * @return the footstep information as a long
	 */
	public long getFootsteps(int x, int y){
		return window.getFootstep(x, y);
	}
	
	
	
	
	//------------------------------------------
	//Chat functions
	//------------------------------------------
	
	/**
	 * getHistory() gets the history for the current room.
	 * 
	 * @return the history as an ArrayList of Strings
	 */
	public ArrayList<String> getHistory() {
		return window.getHistory();
	}
	
	/**
	 * sendMessage() sends a message to the server for chatting.
	 * Stores in the model.
	 * 
	 * @param input the message
	 */
	public void sendMessage(String input) {
		int gridX = xPos / Game.width;
		int gridY = yPos / Game.height;
		inputMessage(gridX, gridY, input);
		
		try{
			sem.acquire();
			
			out.writeBytes("MESSAGE" + "\n" + input + "\n");
			out.flush();
			sem.release();
		} catch (Exception e){
			return;
		}
		
	}
	
	/**
	 * inputMessage() inputs a message into a given room.
	 * 
	 * @param gridX the x-coordinate of the Room
	 * @param gridY the y-coordinate of the Room
	 * @param line the message
	 */
	public void inputMessage(int gridX, int gridY, String line) {
		window.inputMessage(gridX, gridY, line);
		
	}
	
	/**
	 * canChat() tells whether the player can chat right now.
	 * 
	 * @return true if the player can chat
	 */
	public boolean canChat() {
		return canChat;
	}
	
	
	
	
	
	//---------------------------------
	//Window functions
	//---------------------------------
	
	/**
	 * canPan() tells whether the window can pan in the specified direction.
	 * 
	 * @param dir the direction to pan
	 * @return true if it can pan
	 */
	public boolean canPan(int dir) {
		if(dir == Sprite.DIRECTION_LEFT){
			return window.canShiftLeft();
		}
		else if(dir == Sprite.DIRECTION_RIGHT){
			return window.canShiftRight();
		}
		else if(dir == Sprite.DIRECTION_UP){
			return window.canShiftUp();
		}
		else if(dir == Sprite.DIRECTION_DOWN){
			return window.canShiftDown();
		}
		else if(dir == Sprite.DIRECTION_ANY){
			return(window.canShiftLeft() || window.canShiftRight()
					|| window.canShiftUp() || window.canShiftDown());
		}
		else
			return false;
	}
	
	/**
	 * getCenterX() gets the x-coordinate of the middle of the RoomWindow.
	 * @return returns the x-coordinate
	 */
	public int getCenterX(){
		return xPos;
	}
	
	/**
	 * getCenterY() gets the y-coordinate of the middle of the RoomWindow.
	 * @return returns the y-coordinate
	 */
	public int getCenterY(){
		return yPos;
	}
	
	/**
	 * shiftLeft() shifts the window to the left.
	 */
	public void shiftLeft(){
		window.shiftLeft();
		xPos -= Game.width;
	}
	
	/**
	 * shiftRight() shifts the window to the right.
	 */
	public void shiftRight(){
		window.shiftRight();
		xPos += Game.width;
	}
	
	/**
	 * shiftUp() shifts the window to the up.
	 */
	public void shiftUp(){
		window.shiftUp();
		yPos -= Game.height;
	}
	
	/**
	 * shiftDown() shifts the window to the down.
	 */
	public void shiftDown(){
		window.shiftDown();
		yPos += Game.height;
	}

	/**
	 * updateRoom() updates the correct room in the window with the given room.
	 * 
	 * @param newRoom the new Room
	 */
	public void updateRoom(Room newRoom) {
		window.updateRoom(newRoom);
	}
	
	
	
	
	//------------------------------------------
	//Player functions
	//-------------------------------------------
	
	/**
	 * movePlayer() moves the player the specified amount.
	 * 
	 * @param speed the amount to move the player
	 */
	public void movePlayer(int speed) {
		player.move(speed);
	}
	
	/**
	 * isPlayerMoving() tells if the player is moving in the specified direction.
	 * 
	 * @param dir the direction
	 * @return true if moving in the direction
	 */
	public boolean isPlayerMoving(int dir) {
		return player.isMoving(dir);
	}
	
	/**
	 * startMoving() starts the player moving in the specified direction.
	 * 
	 * @param dir the direction
	 */
	public void startMoving(int dir) {
		player.startMoving(dir);
	}
	
	/**
	 * stopMoving() stops the player from moving in the specified direction.
	 * 
	 * @param dir the direction
	 */
	public void stopPlayerMoving(int dir) {
		player.stopMoving(dir);
	}

	/**
	 * getPlayerX() returns the players x-coordinate.
	 * @return the x -coordinate
	 */
	public int getPlayerX(){
		return player.getX();
	}
	
	/**
	 * getPlayerY() returns the players y-coordinate.
	 * @return the y -coordinate
	 */
	public int getPlayerY(){
		return player.getY();
	}
	
	/**
	 * getPlayerName() returns the players name.
	 * @return the name
	 */
	public String getPlayerName() {
		return player.getName();
	}
	
	/**
	 * getPlayerImage() gets the current image associated with the player.
	 * @return the image as a BufferedImage
	 */
	public BufferedImage getPlayerImage(){
		return player.getImage();
	}
	
	/**
	 * getDirection() gets the current direction of the Sprite.
	 * 
	 * @return	the direction as one of the Sprite direction constants
	 */
	public int getPlayerDirection() {
		return player.getDirection();
	}
	
	
	
	
	//-----------------------------------
	//Other players functions
	//-----------------------------------
	
	/**
	 * isPlayerNearMe() checks to see if another player is near the player.
	 * @return true if there is
	 */
	public boolean isPlayerNearMe(){
		ArrayList<Sprite> others = getOthers();
		for (Sprite s: others){
			if(window.isInWindow(s.getX(), s.getY()))
				return true;
		}
		return false;
	}
	
	/**
	 * getOthers() fetches an ArrayList of other players in the area.
	 * @return the ArrayList of other players
	 */
	public ArrayList<Sprite> getOthers(){
		return others;
	}

	/**
	 * updatePlayerPosition() updates the position of the given player.
	 * 
	 * @param gridX the x-coordinate of the room the player is in
	 * @param gridY the y-coordinate of the room the player is in
	 * @param x	the x-coordinate of the cell the player is on
	 * @param y the y-coordinate of the cell the player is on
	 * @param id the player's id
	 */
	public void updatePlayerPosition(int gridX, int gridY, int x, int y, int id, int type) {
		boolean exists= false;
		int xReal = gridX * Game.width + x * Game.TILE_SIZE;
		int yReal = gridY * Game.height + y * Game.TILE_SIZE;
		for(Sprite s : others){
			if(s.getID() == id){
				exists = true;
				s.setX(xReal);
				s.setY(yReal);
			}
		}
		if(!exists){
			Sprite other = new Sprite(xReal, yReal, "", type, id);
			others.add(other);
		}
	}
}
