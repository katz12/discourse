package client;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The LoginScreen class runs the UI of the login screen. It uses Double Buffering
 * to ensure good quality.
 * 
 * @author Andy Katz
 * 
 */
public class LoginScreen{
	private static final long serialVersionUID = 1L;

	private static final int TEXT_MARGIN_X = 106;
	private static final int TEXT_MARGIN_Y = 230;
	private static final int LINE_HEIGHT = 80;
	private static final int CENTER_TEXT = 145;
	private static final int TILE_SIZE = 32;
	
	private static final int NAME = 0;
	private static final int CONNECT = 1;
	
	private CanvasDrawer drawer;
	private Image tile;
	private Image login;
	private int width;
	private int height;
	private String name;
	private int state;

	public LoginScreen(int myWidth, int myHeight, CanvasDrawer myDrawer){
		width = myWidth;
		height = myHeight;
		drawer = myDrawer;
		name = "";
		state = NAME;
		
		File src = new File("tilenoborder-oof.png");
		try {
			tile = ImageIO.read(src);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		src = new File("login.png");
		try {
			login = ImageIO.read(src);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * paint() paints the login screen to the given Graphics2D object.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	public void paint(Graphics2D bufferGraphics){
		drawBackground(bufferGraphics);
		
		if(state == NAME){
			drawLogin(bufferGraphics);
		}
		else if(state == CONNECT){
			drawConnectingDialog(bufferGraphics);
		}
	}
	
	/**
	 * drawConnectingDialog() paints the overlayed connecting dialog
	 * screen to the given Graphics2D object.
	 * @param bufferGraphics 
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	private void drawConnectingDialog(Graphics2D bufferGraphics) {
		bufferGraphics.drawImage(login, 0, 0, drawer);
		
		String output = "Connecting ...";
		bufferGraphics.setFont(new Font("Courier New", Font.PLAIN, 18));
		bufferGraphics.drawString(output, TEXT_MARGIN_X + CENTER_TEXT, TEXT_MARGIN_Y);
	}

	/**
	 * drawLogin() paints the overlayed login screen to the given Graphics2D object.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	private void drawLogin(Graphics2D bufferGraphics) {
		bufferGraphics.drawImage(login, 0, 0, drawer);
		
		String output = "Name: " + name + "_";
		bufferGraphics.setFont(new Font("Courier New", Font.PLAIN, 18));
		bufferGraphics.drawString(output, TEXT_MARGIN_X, TEXT_MARGIN_Y);
		
		if(name.length() > 0){
			output = "Press ENTER";
			bufferGraphics.drawString(output, TEXT_MARGIN_X + CENTER_TEXT, TEXT_MARGIN_Y + LINE_HEIGHT);
		}
	}

	/**
	 * drawBackground() paints the background for login screen to the given
	 * Graphics2D object.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	private void drawBackground(Graphics2D bufferGraphics) {
		for(int i = 0; i < width; i+=TILE_SIZE ){
			for(int j = 0; j < height; j+=TILE_SIZE){
				bufferGraphics.drawImage(tile, i, j, drawer);
			}
		}
	}
	
	/**
	 * changeToConnecting() tells the login to show the "Connecting ..." dialog.
	 */
	public void changeToConnecting() {
		state = CONNECT;
	}
	
	/**
	 * appendChar() adds a character to the input for the name, if it is not already too
	 * large.
	 * 
	 * @param append the char to append
	 */
	public void appendChar(char append){
		if(name.length() < 12)
			name += append;
	}
	
	/**
	 * removeChar() removes one character from the end of the input string.
	 */
	public void removeChar(){
		name = (name.length() > 0) ? name.substring(0, name.length()-1) : name;
	}

	/**
	 * getPlayerName() gets the name of the player that was inputted.
	 * 
	 * @return the name
	 */
	public String getPlayerName() {
		return name;
	}

}
