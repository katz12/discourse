package shared;
import static org.junit.Assert.*;

import org.junit.Test;


public class RoomTest {

	/**
	 * Tests that UpdateTime() only updates.
	 * 
	 * @throws Exception
	 */
	@Test
	public void UpdateTime() throws Exception{
		Room room = new Room(640, 480, 0, 0);
		room.updateTime(0, 0, 5);
		assertEquals(5, room.getTime(0, 0));
		
		room.updateTime(0, 0, 3);
		assertEquals(5, room.getTime(0, 0));
		
		room.updateTime(0, 0, 10);
		assertEquals(10, room.getTime(0, 0));
	}
	
	/**
	 * Tests that UpdateTime() only updates coordinates in bounds.
	 * 
	 * @throws Exception
	 */
	@Test
	public void OutOfBounds() throws Exception{
		Room room = new Room(640, 480, 0, 0);
		room.updateTime(641, 0, 5);
		assertEquals(0, room.getTime(0, 641));
		
		room.updateTime(0, 481, 3);
		assertEquals(0, room.getTime(0, 481));
	}

}
