package client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import shared.Game;
import shared.Room;

/**
 * RoomWindow holds a grid of Rooms to service the CanvasDrawer
 * in navigating Rooms.
 * 
 * @author Andy Katz
 *
 */
public class RoomWindow {
	private static final int width = Game.width;
	private static final int height = Game.height;

	public Room[][] window;
	private RoomDownloader downloader;
	private int centerX;
	private int centerY;
	
	public RoomWindow(int xPos, int yPos, Socket serverSocket, Semaphore sem){
		//3x3 allows for pre-loading all adjacent rooms
		window = new Room[3][3];
		
		centerX = xPos / width;
		centerY = yPos / height;
		
		downloader= new RoomDownloader(serverSocket, sem);
		
		//Download initial state
		for(int x = 0; x < 3; x++){
			for(int y = 0; y < 3; y++){
				window[y][x] = downloader.getRoom(centerX + x-1, centerY + y-1);
			}
		}	
	}
	
	/**
	 * Used for testing. Allows for insertion of Rooms.
	 * @param down
	 * @param w
	 */
	public RoomWindow(RoomDownloader down, Room[][] w){
		window = w;
		downloader= down;
	}
	
	/**
	 * updateRoom() updates the correct room in the window with the given room.
	 * 
	 * @param newRoom the new Room
	 */
	public void updateRoom(Room newRoom) {
		if(Math.abs(newRoom.getGridX() - centerX) > 1)
			return;
		
		if(Math.abs(newRoom.getGridY() - centerY) > 1)
			return;
		
		int windowX = 1 + (newRoom.getGridX() - centerX);
		int windowY = 1 + (newRoom.getGridY() - centerY); 
		
		window[windowY][windowX] = newRoom;
	}
	
	/**
	 * isInWindow() tells if the given coordinate is inside the window.
	 * 
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return true if it is
	 */
	public boolean isInWindow(int x, int y){
		int centerXCoord = centerX * width;
		int centerYCoord = centerY * height;
		
		if (x > centerXCoord + width * 1.5 || x < centerXCoord - width * 1.5)
			return false;
		
		if (y > centerYCoord + height * 1.5 || y < centerYCoord - height * 1.5)
			return false;
		
		return true;
	}
	
	
	
	
	//---------------------------------
	//Footstep functions
	//---------------------------------
	
	/**
	 * updateFootstep() updates the footstep information of the given x,y cell in the
	 * current Room.
	 * 
	 * @param x the x-coordinate of the cell
 	 * @param y the y-coordinate of the cell
	 * @param currentTimeMillis the footstep information
	 * 
	 * @return true if it updates
	 */
	public boolean updateFootstep(int x, int y, long currentTimeMillis) {
		return window[1][1].updateTime(x, y, currentTimeMillis);
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
		if(Math.abs(gridX - centerX) > 1)
			return;
		
		if(Math.abs(gridY - centerY) > 1)
			return;
		
		int windowX = 1 + (gridX - centerX);
		int windowY = 1 + (gridY - centerY);
		
		window[windowY][windowX].updateTime(x, y, time);
	}
	
	/**
	 * getFootstep() gets the footstep information for the given cell.
	 * 
	 * @param x the x-coordinate of the cell in the world
	 * @param y the y-coordinate of the cell in the world
	 * @return the footstep information
	 */
	public long getFootstep(int x, int y){
		int gridX = x / (width / Room.TILE_SIZE);
		int gridY = y / (height / Room.TILE_SIZE);
		
		if(Math.abs(gridX - centerX) > 1)
			return 0;
		
		if(Math.abs(gridY - centerY) > 1)
			return 0;
		
		int virtX = x % (width / Room.TILE_SIZE);
		int virtY = y % (height / Room.TILE_SIZE); 
		
		int windowX = 1 + (gridX - centerX);
		int windowY = 1 + (gridY - centerY);
		try{
			return window[windowY][windowX].getTime(virtX, virtY);
		} catch (NullPointerException e){
			System.err.println("windowX: " + windowX + " windowY: " + windowY
					+ " virtX: " + virtX + " virtY: " + virtY);
			return 0;
		}
		
	}
	
	
	
	
	//---------------------------------
	//Chat functions
	//---------------------------------
	
	/**
	 * getHistory() gets the history for the current room.
	 * 
	 * @return the history as an ArrayList of Strings
	 */
	public ArrayList<String> getHistory() {
		return window[1][1].getHistory();
	}
	
	/**
	 * inputMessage() inputs a message into a given room.
	 * 
	 * @param gridX the x-coordinate of the Room
	 * @param gridY the y-coordinate of the Room
	 * @param line the message
	 */
	public void inputMessage(int gridX, int gridY, String line) {
		if(Math.abs(gridX - centerX) > 1)
			return;
		
		if(Math.abs(gridY - centerY) > 1)
			return;
		
		int windowX = 1 + (gridX - centerX);
		int windowY = 1 + (gridY - centerY);
		
		window[windowY][windowX].addLine(line);
	}
	
	
	
	//-------------------------------------
	//Panning functions
	//-------------------------------------
	
	/**
	 * shiftRight() shifts the window to the right and
	 * gets new Rooms.
	 */
	public void shiftRight(){
		for(int y = 0; y < 3; y++){
			window[y][0] = window[y][1];
			window[y][1] = window[y][2];
			
			window[y][2] = null;
			downloader.getRoom(centerX + 2, centerY + y-1);
		}
		
		centerX++;
	}
	
	/**
	 * canShiftRight() tells if the window can shift right.
	 * 
	 * @return true if it can shift
	 */
	public boolean canShiftRight() {
		return window[1][2] != null;
	}
	
	/**
	 * shiftLeft() shifts the window to the left and
	 * gets new Rooms.
	 */
	public void shiftLeft(){
		for(int y = 0; y < 3; y++){
			window[y][2] = window[y][1];
			window[y][1] = window[y][0];
			
			window[y][0] = null;
			downloader.getRoom(centerX - 2, centerY + y-1);
		}
		centerX--;
	} 
	
	/**
	 * canShiftLeft() tells if the window can shift left.
	 * 
	 * @return true if it can shift
	 */
	public boolean canShiftLeft() {
		return window[1][0] != null;
	}
	
	/**
	 * shiftUp() shifts the window up and
	 * gets new Rooms.
	 */
	public void shiftUp(){
		for(int x = 0; x < 3; x++){
			window[2][x] = window[1][x];
			window[1][x] = window[0][x];
			
			window[0][x] = null;
			downloader.getRoom(centerX + x-1, centerY - 2);
		}
		centerY--;
	}
	
	/**
	 * canShiftUp() tells if the window can shift up.
	 * 
	 * @return true if it can shift
	 */
	public boolean canShiftUp() {
		return window[0][1] != null;
	}
	
	/**
	 * shiftDown() shifts the window up and
	 * gets new Rooms.
	 */
	public void shiftDown(){
		for(int x = 0; x < 3; x++){
			window[0][x] = window[1][x];
			window[1][x] = window[2][x];
			
			window[2][x] = null;
			downloader.getRoom(centerX + x-1, centerY + 2);;
		}
		centerY++;
	}
	
	/**
	 * canShiftDown() tells if the window can shift down.
	 * 
	 * @return true if it can shift
	 */
	public boolean canShiftDown() {
		return window[2][1] != null;
	}
	
	/**
	 * toString() makes a string version of the window. Used for debugging.
	 * 
	 * @return the string encoding
	 */
	public String toString(){
		String output = "";
		for(int x = 0; x < 3; x++){
			for(int y = 0; y < 3; y++){
				output += "(" + window[y][x].getGridX() + "," + window[y][x].getGridY() + ") ";
			}
			output += "\n";
		}
		return output;
	}
}
