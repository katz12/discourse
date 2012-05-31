package server;

import java.util.ArrayList;

import shared.Room;

/**
 * ServerModel holds the model that the server uses as the master copy of the world
 * state.
 * 
 * @author Andy Katz
 *
 */
public class ServerModel {
	public final int WORLD_SIZE = 10;
	public final int NUM_TYPES = 2;
	public final int width = 640;
	public final int height = 480;
	
	
	private Room[][] world;
	private ArrayList<Player> players;
	
	public ServerModel(){
		world = new Room[WORLD_SIZE][WORLD_SIZE];
		players = new ArrayList<Player>();
		
		for (int i = 0; i < WORLD_SIZE; i++){
			for (int j = 0; j < WORLD_SIZE; j++){
				world[i][j] = new Room(width, height, i, j);
			}
		}
	}
	
	/**
	 * getRoom() returns the room with the given x,y coordinate.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the room
	 */
	public Room getRoom(int x, int y){
		if(x < 0 || x >= WORLD_SIZE || y < 0 || y >= WORLD_SIZE)
			return null;
		
		return world[x][y];
	}
	
	/**
	 * getPlayers() gets the players in the game.
	 * 
	 * @return the players as an ArrayList
	 */
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	/**
	 * addPlayer() adds a player to the list of players.
	 * This function is synchronized as to not create race conditions.
	 * 
	 * @param p the player to add
	 */
	public synchronized void addPlayer(Player p){
		players.add(p);
	}
	
	/**
	 * removePlayer() removes a player from the list of players.
	 * This function is synchronized as to not create race conditions.
	 * 
	 * @param p the player to add
	 */
	public synchronized void removePlayer(Player p){
		players.remove(p);
	}
}
