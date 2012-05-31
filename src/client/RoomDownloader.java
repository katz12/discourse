package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import shared.Room;

public class RoomDownloader implements Runnable{ 
	
	private Socket server;
	private Semaphore sem;
	
	private int gridX;
	private int gridY;
	
	private Room getRoom = null;

	public RoomDownloader(Socket serverSocket, Semaphore mySem) {
		server = serverSocket;
		sem = mySem;
	}

	/**
	 * getRoom() launches a thread to retrieve the specified room.
	 * 
	 * @param x the x-coordinate of the Room
	 * @param y the y-coordinate of the Room
	 * @return a reference to the Room being downloaded
	 */
	public Room getRoom(int x, int y) {
		gridX = x;
		gridY = y;
		
		Thread t = new Thread(this);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return getRoom;
	}

	@Override
	public void run() {
		try {
			DataOutputStream out = new DataOutputStream(server.getOutputStream());
			sem.acquire();
			out.writeBytes("REQUEST ROOM" + "\n" + gridX + "\n" + gridY +"\n");
			out.flush();
			sem.release();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
