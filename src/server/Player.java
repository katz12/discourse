package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Player represents a player of the game.
 * 
 * @author Andy Katz
 *
 */
public class Player {
	static int nextID = 0;

	private int x;
	private int y;
	private int type;
	private int id;
	OutputStream out;
	
	public Player(int xCoord, int yCoord, int t, OutputStream o){
		x = xCoord;
		y = yCoord;
		type = t;
		out = o;
		id = nextID++;
	}
	
	/**
	 * setX() sets the x coordinate of the Player.
	 * @param xCoord the x coordinate
	 */
	public void setX(int xCoord){
		x = xCoord;
	}
	
	/**
	 * setY() sets the y coordinate of the Player.
	 * @param yCoord the y coordinate
	 */
	public void setY(int yCoord){
		y = yCoord;
	}
	
	/**
	 * getX() gets the x coordinate of the Player.
	 * 
	 * @return the x coordinate
	 */
	public int getX(){
		return x;
	}
	
	/**
	 * getY() gets the y coordinate of the Player.
	 * 
	 * @return the y coordinate
	 */
	public int getY(){
		return y;
	}
	
	/**
	 * getType() gets the type of the Player.
	 * 
	 * @return the type
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * getID() gets the id of the Player.
	 * 
	 * @return the id
	 */
	public int getID(){
		return id;
	}
	
	/**
	 * getStrean() gets the stream for communicating with the Player.
	 * 
	 * @return the stream
	 */
	public OutputStream getStream(){
		return out;
	}
	
	/**
	 * sendInitializationInfo() sends the information to the client
	 * so that it can initialize its sprite information.
	 * @param objectPort 
	 */
	public void sendInitializationInfo(int objectPort) {
		DataOutputStream dataOut = new DataOutputStream(out);
		try {
			dataOut.writeBytes(id + "\n" + x + "\n" + y + "\n" + type + "\n" + objectPort + "\n");
			dataOut.flush();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
