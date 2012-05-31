package shared;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Room contains the information for footsteps in a given room.
 * 
 * @author Andy Katz
 *
 */
public class Room implements Serializable{
	public static final int TILE_SIZE = 32;

	private long[][] footsteps;
	ArrayList<String> chatHistory;
	int gridX;
	int gridY;
	
	public Room(int width, int height, int x, int y){
		footsteps = new long[width / TILE_SIZE][height / TILE_SIZE];
		chatHistory = new ArrayList<String>();
		
		gridX = x;
		gridY = y;
	}
	
	public Room() {
	}

	/**
	 * updateTime() updates the time of the given x,y coordinate.
	 * 
	 * @param x the x-coordinate in the Room
	 * @param y the y-coordinate in the Room
	 * @param time	the new time
	 * 
	 * @return true if updated
	 */
	public boolean updateTime(int x, int y, long time){
		if(x >= footsteps.length || y >= footsteps[0].length || x < 0 || y < 0)
			return false;
		
		if (footsteps[x][y] < time){
			footsteps[x][y] = time;
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * getTime() returns the time of the given x,y coordinate.
	 * 
	 * @param x the x-coordinate in the Room
	 * @param y the y-coordinate in the Room
	 * @return	the time of the x,y
	 */
	public long getTime(int x, int y){
		if(x >= footsteps.length || y >= footsteps[0].length || x < 0 || y < 0)
			return 0;
		
		return footsteps[x][y];
	}
	
	/**
	 * addLine() adds a line to the chat history for the Room.
	 * 
	 * @param line the String to be added
	 */
	public void addLine(String line){
		chatHistory.add(line);
	}
	
	/**
	 * getHistory() fetches the chat history of the Room.
	 * 
	 * @return the history as an ArrayList
	 */
	public ArrayList<String> getHistory(){
		return chatHistory;
	}
	
	/**
	 * getGridX() gets the x coordinate  of this Room in the whole world.
	 * 
	 * @return the x coordinate
	 */
	public int getGridX(){
		return gridX;
	}
	
	/**
	 * getGridY() gets the y coordinate  of this Room in the whole world.
	 * 
	 * @return the y coordinate
	 */
	public int getGridY(){
		return gridY;
	}
	
	/**
	 * getWidth() gets the width of the Room.
	 * 
	 * @return the width
	 */
	public int getWidth(){
		return footsteps.length;
	}
	
	/**
	 * getHeight() gets the height of the Room.
	 * 
	 * @return the height
	 */
	public int getHeight(){
		return footsteps[0].length;
	}
}
