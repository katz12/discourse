package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import shared.Game;
import shared.Room;

/**
 * ServerRunner is the main class for the server.
 * 
 * @author Andy Katz
 *
 */
public class ServerRunner implements Runnable{

	static final int SERVER_PORT = 2219;
	
	static int nextPort = SERVER_PORT + 1;
	static ServerModel model;
	
	private int threadPort;

	private ObjectOutputStream objOut;



	public static void main(String[] args) {
		model = new ServerModel();
		
		//Create server socket
		ServerSocket server = null;
		try {
			server = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		//Loop on listening
		//Spawn new thread on connection
		while(true){
			try {
				Socket client = server.accept();
				
				System.out.println("Connection recieved");
			
				//Get and send port for new connection
				int port = nextPort++;
				DataOutputStream out = new DataOutputStream(client.getOutputStream());
				out.writeBytes("" + port);
			
				client.close();
			
				//Start new thread to handle connection
				Runnable r = new ServerRunner(port);
				new Thread(r).start();
				
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				continue;
			}
		}
	}
	
	
	public ServerRunner(int port) {
		threadPort = port;
	}


	@Override
	public void run() {
		//Create server socket
		ServerSocket server = null;
		Socket client = null;
		try {
			server = new ServerSocket(threadPort);
			client = server.accept();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		System.out.println("Client connection established");
		
		Player player = generatePlayer(client);
		model.addPlayer(player);
		
		int objectPort = nextPort++;
		player.sendInitializationInfo(objectPort);
		
		try {
			ServerSocket objectServer = new ServerSocket(objectPort);
			Socket objectSender = objectServer.accept();
			objOut = new ObjectOutputStream(objectSender.getOutputStream());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		String message = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
			e1.printStackTrace();
			return;
		}
		while(true){
			try {
				message = in.readLine();
				
				if("REQUEST ROOM".equals(message)){
					int gridX = Integer.parseInt(in.readLine());
					int gridY = Integer.parseInt(in.readLine());
					sendRoom(gridX, gridY, player.getStream());
				}
				else if("FOOTSTEP".equals(message)){
					int gridX = Integer.parseInt(in.readLine());
					int gridY = Integer.parseInt(in.readLine());
					int x = Integer.parseInt(in.readLine());
					int y = Integer.parseInt(in.readLine());
					long time = Long.parseLong(in.readLine());
					int id = player.getID();
					int type = player.getType();
					
					model.getRoom(gridX, gridY).updateTime(x, y, time);
					player.setX(gridX * Game.width + x * Game.TILE_SIZE);
					player.setY(gridY * Game.width + y * Game.TILE_SIZE);
					
					for (Player p : model.getPlayers()){
						if(p != player && isInWindow(p, gridX, gridY)){
							sendFootstep(gridX, gridY, x, y, time, id, type, p.getStream());
						}
					}
				}
				else if("MESSAGE".equals(message)){
					String line = in.readLine();
					
					int gridX = getGridX(player.getX());
					int gridY = getGridY(player.getY());
					model.getRoom(gridX, gridY).addLine(line);
					
					for (Player p : model.getPlayers()){
						if(p != player && isInWindow(p, gridX, gridY)){
							sendMessage(gridX, gridY, line, p.getStream());
						}
					}
				}
				
				
			} catch (IOException e) {				
				model.removePlayer(player);
				return;
			}
		}
		
	}

	/**
	 * isInWindow() is a private helper that decides if the given room coordinates
	 * are in the player's window.
	 * 
	 * @param p the player to look at 
	 * @param gridX the x coordinate of the room
	 * @param gridY the y coordinate of the room
	 * @return true if the room is in the player's window
	 */
	private boolean isInWindow(Player p, int gridX, int gridY) {
		int pGridX = getGridX(p.getX());
		int pGridY = getGridY(p.getY());
		
		return (Math.abs(gridX - pGridX) <= 1 && Math.abs(gridY - pGridY) <= 1);
		
	}


	private int getGridY(int y) {
		return y / model.height;
	}


	private int getGridX(int x) {
		return x / model.width;
	}
	
	/**
	 * sendMessage() is a private helper that sends a message to a client for updating
	 * room history.
	 * 
	 * @param gridX the x coordinate of the room
	 * @param gridY the y coordinate of the room
	 * @param line the message
	 * @param stream the stream to send on
	 * @throws IOException 
	 */
	private void sendMessage(int gridX, int gridY, String line, OutputStream stream) throws IOException {
		DataOutputStream out = new DataOutputStream(stream);
		String message = "MESSAGE" + "\n" + gridX + "\n" + gridY + "\n" + line + "\n";
		out.writeBytes(message);
	}

	/**
	 * sendFootstep() is a private helper that sends footstep information to the
	 * given client.
	 * 
	 * @param gridX the x coordinate of the room
	 * @param gridY the y coordinate of the room
	 * @param x the x coordinate of the location in the room
	 * @param y the y coordinate of the location in the room
	 * @param time the time of the new footstep information
	 * @param id 
	 * @param out the stream to send on
	 * @throws IOException 
	 */
	private void sendFootstep(int gridX, int gridY, int x, int y, long time, int id, int type, OutputStream stream) throws IOException {
		DataOutputStream out = new DataOutputStream(stream);
		String message = "FOOTSTEP" + "\n" + gridX + "\n" + gridY + "\n" + x + "\n"
				+ y + "\n" + time + "\n" + id + "\n" + type + "\n";
		out.writeBytes(message);
		out.flush();
	}

	/**
	 * sendRoom() is a private helper method that sends a room to a client.
	 * 
	 * @param gridX the x coordinate of the room
	 * @param gridY the y coordinate of the room
	 * @param stream the stream to send over
	 * @throws IOException 
	 */
	private void sendRoom(int gridX, int gridY, OutputStream stream) throws IOException {
		DataOutputStream out = new DataOutputStream(stream);
		String message = "ROOM" + "\n";
		out.writeBytes(message);
		out.flush();
		
		//ObjectOutputStream obj = new ObjectOutputStream(stream);
		Room room = model.getRoom(gridX, gridY);
		objOut.writeObject(room);
		objOut.flush();
	}


	/**
	 * generatePlayer() is a private helper method to create a new player.
	 * 
	 * @param client the socket that the player is connected to
	 * @return the player
	 */
	private Player generatePlayer(Socket client){
		int gridX = 0;//(int)(model.WORLD_SIZE * Math.random());
		int gridY = 0;//(int)(model.WORLD_SIZE * Math.random());
		int x = model.width * gridX + model.width/2;
		int y = model.height * gridY + model.height/2;
		int type = (int)(model.NUM_TYPES * Math.random());
		OutputStream out;
		try {
			out = client.getOutputStream();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		return new Player(x, y, type, out);
	}

}
