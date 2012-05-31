package client;
/**
 * The CanvasDrawer class runs the UI of the game. It uses Double Buffering
 * to ensure good quality.
 * 
 * @author Andy Katz
 * 
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


public class CanvasDrawer extends Canvas implements KeyListener, MouseListener{
	private static final long serialVersionUID = 1L;
	public static final int LOGIN = 0;
	public static final int CONNECTING = 1;
	public static final int PLAYING = 2;
	
	private final int delay = 100;
	
	private int state;
	
	private BufferedImage offscreen;
	private Graphics2D bufferGraphics;
	
	private ClientModel model;
	private LoginScreen login;
	private GameScreen game;
	
	private int width;
	private int height;
	

	
	public CanvasDrawer(int myWidth, int myHeight, ClientModel myModel){
		width = myWidth;
		height = myHeight;
		model = myModel;
		
		state = LOGIN;
		
		login = new LoginScreen(width, height, this);
		game = null;
		
		offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		bufferGraphics = (Graphics2D) offscreen.getGraphics();
		
		
		addKeyListener(this);
		addMouseListener(this);
	}
	
	/**
	 * paint() draws all of the parts of the UI onto the Canvas.
	 */
	public void paint(Graphics g){		
		bufferGraphics.clearRect(0, 0, width, height);
		
		if(state == LOGIN || state == CONNECTING){
			login.paint(bufferGraphics);
		}
		else if(state == PLAYING){
			game.paint(bufferGraphics);
		}

		g.drawImage(offscreen, 0, 0, width, height, 0, 0, width, height, this);
	}

	/**
	 * update() updates the Canvas. It is called every time repaint() is called.
	 */
	public void update(Graphics g){
		paint(g);
	}
	
	/**
	 * changeToPlayingState() changes the game into play mode.
	 */
	private void changeToPlayingState() {
		login.changeToConnecting();
		state = CONNECTING;
		
		while(model == null || model.isLoading()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		game = new GameScreen(width, height, model, this);
		state = PLAYING;
	}
	
	/**
	 * setModel() allows for creating the model after creating the CanvasDrawer.
	 * 
	 * @param myModel the model
	 */
	public void setModel(ClientModel myModel){
		model = myModel;
	}
	
	/**
	 * getState() gets the state of the drawer.
	 * 
	 * @return the state as an int (constant)
	 */
	public int getState(){
		return state;
	}
	
	/**
	 * getPlayerName() gets the input name for the player.
	 * 
	 * @return the name
	 */
	public String getPlayerName() {
		return login.getPlayerName();
	}
	
	
	
	//-----------------------------------------------
	//Input handling
	//-----------------------------------------------
	
	/**
	 * keyPressed() handles input in the form of a key being pressed down.
	 * When the user presses down one of the arrow keys, it sets the player Sprite
	 * moving.
	 */
	@Override
	public void keyPressed(KeyEvent k) {
		
		if(state == PLAYING){
		
			//Don't handle input while animating
			if(game.isAnimating())
				return;
			
			if (k.getKeyCode() == KeyEvent.VK_LEFT){
				if(!model.isPlayerMoving(Sprite.DIRECTION_LEFT))
					model.startMoving(Sprite.DIRECTION_LEFT);
			}
			
			if (k.getKeyCode() == KeyEvent.VK_RIGHT){
				if(!model.isPlayerMoving(Sprite.DIRECTION_RIGHT))
					model.startMoving(Sprite.DIRECTION_RIGHT);
			}	
			
			if (k.getKeyCode() == KeyEvent.VK_UP){
				if(!model.isPlayerMoving(Sprite.DIRECTION_UP))
					model.startMoving(Sprite.DIRECTION_UP);
			}
			
			if (k.getKeyCode() == KeyEvent.VK_DOWN){
				if(!model.isPlayerMoving(Sprite.DIRECTION_DOWN))
					model.startMoving(Sprite.DIRECTION_DOWN);
			}
		}
	}
	
	/**
	 * KeyReleased() handles input when a key stops being pressed.
	 * When the user releases an arrow key, the player Sprite stops moving.
	 */
	@Override
	public void keyReleased(KeyEvent k) {
		
		if(state == PLAYING){
		
			if (k.getKeyCode() == KeyEvent.VK_LEFT){
				game.movingLeft = false;
				model.stopPlayerMoving(Sprite.DIRECTION_LEFT);
			}
			else if (k.getKeyCode() == KeyEvent.VK_RIGHT){
				game.movingRight = false;
				model.stopPlayerMoving(Sprite.DIRECTION_RIGHT);
			}
			else if (k.getKeyCode() == KeyEvent.VK_UP){
				game.movingUp = false;
				model.stopPlayerMoving(Sprite.DIRECTION_UP);
			}
			else if (k.getKeyCode() == KeyEvent.VK_DOWN){
				game.movingDown = false;
				model.stopPlayerMoving(Sprite.DIRECTION_DOWN);
			}
		}
	}

	/**
	 * KeyTyped() handles input when a key has been typed.
	 * Used for chat.
	 */
	@Override
	public void keyTyped(KeyEvent k) {
		
		if(state == LOGIN){
			if(k.getKeyChar() == '\b'){
				login.removeChar();
			}
			else if(k.getKeyChar() == '\n'){
				changeToPlayingState();
			}
			else{
				login.appendChar(k.getKeyChar());
			}
			repaint();
		}
		else if(state == PLAYING){
			if(!model.canChat())
				return;
			
			if(k.getKeyChar() == '\b'){
				game.removeChar();
			}
			else if(k.getKeyChar() == '\n'){
				game.sendMessage();
			}
			else{
				game.appendChar(k.getKeyChar());
			}
			repaint();
		}
		
	}

	/**
	 * mouseClicked() handles mouse click input from the user.
	 * It is used for scrolling the chat window.
	 */
	@Override
	public void mouseClicked(MouseEvent m) {
		
		if(state == PLAYING){
			
			if(!model.canChat())
				return;
			
			int xPos = m.getX();
			int yPos = m.getY();
			
			if(xPos > GameScreen.CHAT_WINDOW_LEFT + GameScreen.SCROLL_UP_X1 && xPos < GameScreen.CHAT_WINDOW_LEFT + GameScreen.SCROLL_UP_X2 &&
			   yPos > GameScreen.CHAT_WINDOW_TOP + GameScreen.SCROLL_UP_Y1 && yPos < GameScreen.CHAT_WINDOW_TOP + GameScreen.SCROLL_UP_Y2)
				game.chatScrollUp();
			
			if(xPos > GameScreen.CHAT_WINDOW_LEFT + GameScreen.SCROLL_DOWN_X1 && xPos < GameScreen.CHAT_WINDOW_LEFT + GameScreen.SCROLL_DOWN_X2 &&
			   yPos > GameScreen.CHAT_WINDOW_TOP + GameScreen.SCROLL_DOWN_Y1 && yPos < GameScreen.CHAT_WINDOW_TOP + GameScreen.SCROLL_DOWN_Y2)
				game.chatScrollDown();
			
			repaint();
		}
	}
	
	
	
	
	//---------------------------------------
	//unused methods
	//---------------------------------------

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	

}
