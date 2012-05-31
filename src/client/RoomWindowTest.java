package client;
import static org.junit.Assert.*;

import java.net.Socket;

import org.junit.Test;

import shared.Room;



public class RoomWindowTest {

	/**
	 * Tests shifting Right
	 * @throws Exception
	 */
	@Test
	public void shiftRight() throws Exception {
		Socket socket = new Socket();
		RoomDownloader downloader = new RoomDownloader(socket, null);
		
		Room[][] window = new Room[3][3];
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				window[i][j] = new Room(640, 480, j, i);
			}
		}
		RoomWindow rw = new RoomWindow(downloader, window);
		
		rw.shiftRight();
		
		assertEquals(2, rw.window[1][1].getGridX());
		assertEquals(1, rw.window[1][1].getGridY());
	}
	
	/**
	 * Tests shifting Up
	 * @throws Exception
	 */
	@Test
	public void shiftUp() throws Exception {
		Socket socket = new Socket();
		RoomDownloader downloader = new RoomDownloader(socket, null);
		
		Room[][] window = new Room[3][3];
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				window[i][j] = new Room(640, 480, j, i);
			}
		}
		RoomWindow rw = new RoomWindow(downloader, window);
		
		rw.shiftUp();
		
		assertEquals(1, rw.window[1][1].getGridX());
		assertEquals(0, rw.window[1][1].getGridY());
	}
	
	/**
	 * Tests shifting Left
	 * @throws Exception
	 */
	@Test
	public void shiftLeft() throws Exception {
		Socket socket = new Socket();
		RoomDownloader downloader = new RoomDownloader(socket, null);
		
		Room[][] window = new Room[3][3];
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				window[i][j] = new Room(640, 480, j, i);
			}
		}
		RoomWindow rw = new RoomWindow(downloader, window);
		
		rw.shiftLeft();
		
		assertEquals(0, rw.window[1][1].getGridX());
		assertEquals(1, rw.window[1][1].getGridY());
	}
	
	/**
	 * Tests shifting Down
	 * @throws Exception
	 */
	@Test
	public void shiftDown() throws Exception {
		Socket socket = new Socket();
		RoomDownloader downloader = new RoomDownloader(socket, null);
		
		Room[][] window = new Room[3][3];
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				window[i][j] = new Room(640, 480, j, i);
			}
		}
		RoomWindow rw = new RoomWindow(downloader, window);
		
		rw.shiftDown();
		
		assertEquals(1, rw.window[1][1].getGridX());
		assertEquals(2, rw.window[1][1].getGridY());
	}

}
