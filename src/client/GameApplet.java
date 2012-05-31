package client;
/**
 * The GameApplet class runs the applet that contains the game.
 * This is necessary for deploying to a web browser.
 * 
 * @author Andy Katz
 * 
 */
import java.awt.Dimension;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JApplet;
import javax.swing.JFrame;

import shared.Game;
import shared.Room;


public class GameApplet extends JApplet implements Runnable{
	private static final long serialVersionUID = 1L;
	
	private final String SERVER_NAME = "localhost";
	private final int SERVER_PORT = 2219;
	
	private final int width = Game.width+1;
	private final int height = Game.height+1;
	
	private Socket server;
	private BufferedReader in;
	private Socket object;
	private ObjectInputStream objIn;
	
	private ClientModel model;
	private CanvasDrawer canvas;
	
	
	public void init(){
		setSize(new Dimension(width,height));
		
		model = null;
		
		canvas = new CanvasDrawer(width, height, model);
		add(canvas);
		
		Thread connectionHandler = new Thread(this);
		connectionHandler.start();
	}
	
	/**
	 * run() is the function that the connectionHandler thread runs.
	 */
	@Override
	public void run() {
		//Wait until it is time to connect
		while( !(canvas.getState() == CanvasDrawer.CONNECTING) ){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		try{
			int port = initializeConnection();
			getInitializationInfo(port);
			
			while(true){
				String message = in.readLine();
				handleMessage(message);
			}
		} catch (Exception e){
			System.err.println("Connection failed " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	/**
	 * initializeConnection() opens a session with the server and gets the port
	 * to be used for communication.
	 * 
	 * @return the port for communication
	 */
	private int initializeConnection() throws Exception{
		Socket server = new Socket(SERVER_NAME, SERVER_PORT);
		BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
		
		int port = Integer.parseInt(in.readLine());
		
		return port;
	}
	
	/**
	 * getInitializationInfo() gets the necessary information to initialize the game
	 * from the server.
	 * 
	 * @throws an exception if the connection fails
	 */
	private void getInitializationInfo(int port) throws Exception{
			server = new Socket(SERVER_NAME, port);
			in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			int id = Integer.parseInt(in.readLine());
			int x = Integer.parseInt(in.readLine());
			int y = Integer.parseInt(in.readLine());
			int type = Integer.parseInt(in.readLine());
			
			int objectPort = Integer.parseInt(in.readLine());
			object = new Socket(SERVER_NAME, objectPort);
			ObjectOutputStream objOut = new ObjectOutputStream(object.getOutputStream());
			objOut.flush();
			objIn = new ObjectInputStream(object.getInputStream());
			
			
			Sprite player = new Sprite(x, y, canvas.getPlayerName(), type, id);
			model = new ClientModel(x, y, player, server);
			canvas.setModel(model);
	}
	
	/**
	 * handleMessage handles a message from the server.
	 * 
	 * @param message the message to be handled
	 */
	private void handleMessage(String message) {
		if ("ROOM".equals(message)){
			try {
				Room newRoom = (Room) objIn.readObject();
				if(newRoom != null){
					model.updateRoom(newRoom);
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(model.isLoading()){
			return;
		}
		else if ("FOOTSTEP".equals(message)){
			try {
				int gridX = Integer.parseInt(in.readLine());
				int gridY = Integer.parseInt(in.readLine());
				int x = Integer.parseInt(in.readLine());
				int y = Integer.parseInt(in.readLine());
				long time = Long.parseLong(in.readLine());
				int id = Integer.parseInt(in.readLine());
				int type = Integer.parseInt(in.readLine());
				
				model.updateFootstep(gridX, gridY, x, y, time);
				model.updatePlayerPosition(gridX, gridY, x, y, id, type);
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if ("MESSAGE".equals(message)){
			try {
				int gridX = Integer.parseInt(in.readLine());
				int gridY = Integer.parseInt(in.readLine());
				String line = in.readLine();
				
				model.inputMessage(gridX, gridY, line);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
