package client;
/**
 * The Sprite class defines a character entity on the game grid.
 * It has a sprite sheet that it uses to serve the images that
 * make the character move.
 * 
 * @author Andy Katz
 * 
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Sprite {
	
	private BufferedImage sheet;
	
	//number of steps in the sprite animation
	private final int NUM_STEPS = 3;
	
	static final int DIRECTION_ANY = 4;
	static final int DIRECTION_RIGHT = 3;
	static final int DIRECTION_LEFT = 2;
	static final int DIRECTION_UP = 1;
	static final int DIRECTION_DOWN = 0;
	
	private int direction;
	private int step;
	
	private boolean movingRight;
	private boolean movingLeft;
	private boolean movingUp;
	private boolean movingDown;
	
	private boolean moving;
	
	private int x;
	private int y;
	private int type;
	private int id;
	private String name;
	
	public Sprite(int xInit, int yInit, String myName, int myType, int myId){		
		x = xInit;
		y = yInit;
		name = myName;
		type = myType;
		id = myId;
		
		try {
			sheet = ImageIO.read(new File("sheet" + type + ".png"));
		} catch (IOException e) {}
		
		movingRight = false;
		movingLeft = false;
		movingUp = false;
		movingDown = false;
		
		moving = false;
		
		direction = DIRECTION_RIGHT;
	}
	
	/**
	 * startMoving() initiates movement of the sprite.
	 * Only call when the Sprite is not moving.
	 * 
	 * @param dir the direction in which to move
	 */
	public void startMoving(int dir){
		direction = dir;
		moving = true;
		step = 1;
	}
	
	/**
	 * isMoving() returns whether or not the sprite is moving.
	 * Used to decide whether or not to startMoving().
	 * 
	 * @param dir the direction of motion intended
	 * @return true if it is moving
	 */
	public boolean isMoving(int dir){
		return moving && (dir == DIRECTION_ANY || dir == direction);
	}
	
	/**
	 * move() moves the sprite the specified amount.
	 * @param amount the amount to move
	 */
	public void move(int amount){
		if(!isMoving(DIRECTION_ANY))
			return;
		
		switch(direction){
			case DIRECTION_RIGHT:
				x += amount;
				break;
			case DIRECTION_LEFT:
				if(x > amount)
					x -= amount;
				break;
			case DIRECTION_UP:
				if (y > amount)
					y -= amount;
				break;
			case DIRECTION_DOWN:
				y += amount;
				break;
			default:
				break;
		}
		
		System.out.println("x = " + x + " y = " + y);
	}
	
	/**
	 * stopMoving() stops movement of the sprite.
	 * Necessary to stop movement animation.
	 * 
	 * @param dir the direction in which to stop moving
	 */
	public void stopMoving(int dir){
		if (dir == direction)
			moving = false;
	}
	
	/**
	 * getDirection() gets the current direction of the Sprite.
	 * 
	 * @return	the direction as one of the Sprite direction constants
	 */
	public int getDirection(){
		return direction;
	}
	
	/**
	 * getX() gets the current x coordinate of the sprite
	 * 
	 * @return	the x-coordinate
	 */
	public int getX(){
		return x;
	}
	
	/**
	 * getY() gets the current y coordinate of the sprite
	 * 
	 * @return	the y-coordinate
	 */
	public int getY(){
		return y;
	}
	
	/**
	 * setX() sets the current x-coordinate of the sprite 
	 * @param xPos the new x-coordinate
	 */
	public void setX(int xPos){
		x = xPos;
	}
	
	/**
	 * setY() sets the current y-coordinate of the sprite 
	 * @param yxPos the new y-coordinate
	 */
	public void setY(int yPos){
		y = yPos;
	}
	
	/**
	 * getName() gets the name of the sprite
	 * 
	 * @return	the name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * getID() gets the id of the sprite
	 * 
	 * @return	the id
	 */
	public int getID(){
		return id;
	}
	
	/**
	 * getImage() picks the correct image to display depending on
	 * direction and movement.
	 * 
	 * @return a BufferedImage of the Sprite
	 */
	public BufferedImage getImage(){
		int xCoord;
		
		//direction corresponds to the row on the sprite sheet
		int yCoord = 32 * direction;
		
		if (moving){
			//step corresponds to the column on the sprite sheet
			xCoord = 32 * step;
		}
		else{
			xCoord = 0;
		}
		
		step = (step + 1) % NUM_STEPS;
		
		return sheet.getSubimage(xCoord, yCoord, 32, 32);
	}
}
