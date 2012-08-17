package client;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 * The GameScreen class runs the UI of the game screen. It uses Double Buffering
 * to ensure good quality.
 * 
 * @author Andy Katz
 * 
 */
public class GameScreen implements Runnable{
	
	static final int TILE_SIZE = 32;
	static final int SPRITE_SIZE = 48;
	static final int INPUT_LINE_SIZE = 37;
	static final int CHAT_WINDOW_TOP = 212;
	static final int LINE_HEIGHT = 20;
	static final int CHAT_WINDOW_LEFT = 10;
	static final int TEXT_MARGIN = 20;
	
	static final int MUTE_TOP = 435;
	static final int MUTE_LEFT = 580;
	static final int MUTE_WIDTH = 47;
	static final int MUTE_HEIGHT = 41;
	
	static final int SCROLL_UP_X1 = 276;
	static final int SCROLL_UP_X2 = 308;
	static final int SCROLL_UP_Y1 = 12;
	static final int SCROLL_UP_Y2 = 43;
	static final int SCROLL_DOWN_X1 = 276;
	static final int SCROLL_DOWN_X2 = 308;
	static final int SCROLL_DOWN_Y1 = 174;
	static final int SCROLL_DOWN_Y2 = 205;
	
	private final int opacityThreshold = 10000;
	private final int chunks = 2;
	private final int scroll = 8;
	
	private Image tile;
	private Image feet;
	private Image chat;
	private Image mute;
	private Image unmute;
	
	private ClientModel model;
	private CanvasDrawer drawer;
	private GameMusic music;
	
	private boolean muted;
	
	private int width;
	private int height;
	private int x;
	private int y;
	
	private BufferedImage overlay;
	float[] scales = {1f, 1f, 1f, 1f};
	float[] offsets = new float[4];
	RescaleOp rescale;
	
	private String input;
	private boolean chatting = true;
	private int chatScroll;
	
	Thread animator = null;
	
	private boolean panUp = false;
	private boolean panDown = false;
	private boolean panLeft = false;
	private boolean panRight = false;
	
	public boolean movingUp = false;
	public boolean movingDown = false;
	public boolean movingLeft = false;
	public boolean movingRight = false;

	
	private final int speed = 6;
	private final int delay = 150;

	
	public GameScreen(int myWidth, int myHeight, ClientModel myModel, CanvasDrawer myDrawer){
		width = myWidth;
		height = myHeight;
		model = myModel;
		drawer = myDrawer;
		
		x = model.getPlayerX();
		y = model.getPlayerY();
		
		try{
			File src = new File("tilenoborder.png");
			tile = ImageIO.read(src);
			
			src = new File("chat window.png");
			chat = ImageIO.read(src);
			
			src = new File("tilenoborder-lit2.png");
			feet = ImageIO.read(src);
			
			src = new File("sound-icon.png");
			mute = ImageIO.read(src);
			
			src = new File("sound-icon-off.png");
			unmute = ImageIO.read(src);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		overlay = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
		overlay.getGraphics().drawImage(feet, 0, 0, drawer);
		
		//menu
		chatScroll = 0;
		input = "";
		
		music = new GameMusic(model.isPlayerNearMe());
		muted = false;
		
		ActionListener taskPerformer = new ActionListener() {
			  public void actionPerformed(ActionEvent evt) {
				if(model == null)
					  return;
				
				
			    if(getVirtualX(model.getPlayerX()) > width && model.getPlayerDirection() == Sprite.DIRECTION_RIGHT){
			    	if(model.canPan(Sprite.DIRECTION_RIGHT)){
				    	panRight = true;
				    	start();
			    	}
			    }
			    else if(getVirtualX(model.getPlayerX()) < 0 && model.getPlayerDirection() == Sprite.DIRECTION_LEFT){
			    	if(model.canPan(Sprite.DIRECTION_LEFT)){
				    	panLeft = true;
				    	start();
			    	}
			    }
			    else if(getVirtualY(model.getPlayerY()) > height && model.getPlayerDirection() == Sprite.DIRECTION_DOWN){
			    	if(model.canPan(Sprite.DIRECTION_DOWN)){
				    	panDown = true;
				    	start();
			    	}
			    }
			    else if(getVirtualY(model.getPlayerY()) < 0 && model.getPlayerDirection() == Sprite.DIRECTION_UP){
			    	if(model.canPan(Sprite.DIRECTION_UP)){
				    	panUp = true;
				    	start();
			    	}
			    }
			    else{
					model.movePlayer(speed);
					updateFootsteps();
				    drawer.repaint();
			    }
			    
			    music.updateMusic(model.isPlayerNearMe());
			  }
			};

			new Timer(delay, taskPerformer).start();
	}
	
	/**
	 * updateFootsteps() is a private helper function that sets the footstep value of
	 * the square that the current player is on.
	 */
	private void updateFootsteps(){
		if(Math.abs(getVirtualX(model.getPlayerX()) - 0) < 10 || Math.abs(getVirtualX(model.getPlayerX()) - width) < 10)
			return;
		if(Math.abs(getVirtualY(model.getPlayerY()) - 0) < 10 || Math.abs(getVirtualY(model.getPlayerY()) - height) < 10)
			return;
		
		int xCell = ( (model.getPlayerX() % width) / TILE_SIZE);
		int yCell = ( (model.getPlayerY() % height) / TILE_SIZE);
		model.updateFootstep(xCell, yCell, System.currentTimeMillis());
	}
	
	/**
	 * paint() paints the game screen to the given Graphics2D object.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	public void paint(Graphics2D bufferGraphics){
		drawBackground(bufferGraphics);
		drawSprites(bufferGraphics);
		drawMuteButton(bufferGraphics);
		
		if(model.canChat() && animator == null)
			drawChatWindow(bufferGraphics);
	}
	
	/**
	 * drawBackground() is a private helper that draws the background
	 * of the screen.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	private void drawBackground(Graphics2D bufferGraphics) {
		long now = System.currentTimeMillis();
		
		//used for animation
		int xdif = (width/2 - x) % TILE_SIZE;
		int ydif = (height/2 - y) % TILE_SIZE;
		if(xdif > 0) xdif = -TILE_SIZE + xdif;
		if(ydif > 0) ydif = -TILE_SIZE + ydif;
		
		for(int i = 0; i < width; i+=TILE_SIZE ){
			for(int j = 0; j < height; j+=TILE_SIZE){
				bufferGraphics.drawImage(tile, i+xdif, j+ydif, drawer);
				
				if(i < width-1 && j < height-1){
					int iStep = i + x - width/2;
					int jStep = j + y - height/2;
					long elapsed = now - model.getFootsteps(iStep / TILE_SIZE, jStep / TILE_SIZE);
					setOpacity(elapsed);
					bufferGraphics.drawImage(overlay, rescale, i+xdif, j+ydif);
				}
			}
		}
		
		/*for(int i = x - width/2; i < x + width/2; i+=TILE_SIZE ){
			for(int j = y - height/2; j < y + height/2; j+=TILE_SIZE){
				long elapsed = now - footsteps[i/TILE_SIZE][j/TILE_SIZE];
				setOpacity(elapsed);
				bufferGraphics.drawImage(overlay, rescale, getVirtualX(i)+xdif, getVirtualY(j)+ydif);
			}
		}*/
	}
	
	/**
	 * drawSprites() is a private helper that draws the sprites onto the
	 * screen.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	private void drawSprites(Graphics2D bufferGraphics) {
		int xDraw = getVirtualX(model.getPlayerX() - SPRITE_SIZE/2);
		int yDraw = getVirtualY(model.getPlayerY() - SPRITE_SIZE);
		
		model.canChat = false;
		
		if (xDraw >= 0-SPRITE_SIZE && xDraw <= width && yDraw >= 0-SPRITE_SIZE && yDraw <= height)
			bufferGraphics.drawImage(model.getPlayerImage(), xDraw, yDraw, SPRITE_SIZE, SPRITE_SIZE, drawer);
		
		for(Sprite s : model.getOthers()){
			xDraw = getVirtualX(s.getX() - SPRITE_SIZE/2);
			yDraw = getVirtualY(s.getY() - SPRITE_SIZE);
			
			if (xDraw >= 0-SPRITE_SIZE && xDraw <= width && yDraw >= 0-SPRITE_SIZE && yDraw <= height){
				bufferGraphics.drawImage(s.getImage(), xDraw, yDraw, SPRITE_SIZE, SPRITE_SIZE, drawer);
				model.canChat = true;
			}
		}
	}
	
	/**
	 * drawChatWindow() is a private helper that draws the chat UI
	 * onto the screen.
	 * 
	 * @param bufferGraphics the Graphics2D to be painted to
	 */
	private void drawChatWindow(Graphics2D bufferGraphics) {
		bufferGraphics.drawImage(chat, CHAT_WINDOW_LEFT, CHAT_WINDOW_TOP, drawer);
		bufferGraphics.setFont(new Font("Courier New", Font.PLAIN, 12));
		
		int startLine = CHAT_WINDOW_TOP + 10 * LINE_HEIGHT;
		int curLine = model.getHistory().size() - chatScroll -1;
		for (int i = 0; i < 10; i++){
			if(curLine < 0)
				break;
			
			String line = model.getHistory().get(curLine);
			bufferGraphics.drawString(line, TEXT_MARGIN, startLine);
			startLine -= LINE_HEIGHT;
			curLine -= 1;
		}
		
		int offset = (input.length() < INPUT_LINE_SIZE) ? 0 : input.length() - INPUT_LINE_SIZE;
		String output = "> " + input.substring(offset) + "_";
		bufferGraphics.drawString(output, TEXT_MARGIN, CHAT_WINDOW_TOP + 11 * LINE_HEIGHT);
	}
	
	private void drawMuteButton(Graphics2D bufferGraphics){
		Image muteButton = (muted) ? unmute : mute;
		bufferGraphics.drawImage(muteButton, MUTE_LEFT, MUTE_TOP, drawer);
	}
	
	public void muteToggle(){
		if (muted){
			muted = false;
			music.unmuteAll();
		}
		else{
			muted = true;
			music.muteAll();
		}
	}

	/**
	 * getVirtualX() returns the given coordinate in the x-axis
	 * relative to the center of the window.
	 * 
	 * Will return numbers outside of the window.
	 * 
	 * @param coord the real coordinate in the x-axis
	 * @return the virtual coordinate in the x-axis
	 */
	private int getVirtualX(int coord) {
		int xDiff = coord - x;
		return width/2 + xDiff;
	}
	
	/**
	 * getVirtualY() returns the given coordinate in the y-axis
	 * relative to the center of the window.
	 * 
	 * Will return numbers outside of the window.
	 * 
	 * @param coord the real coordinate in the y-axis
	 * @return the virtual coordinate in the y-axis
	 */
	private int getVirtualY(int coord) {
		int yDiff = coord - y;
		return height/2 + yDiff;
	}
	
	/**
	 * setOpacity() sets the current opacity to be used in drawing a transparent image.
	 * The elapsed time parameter allows for a gradual fading of an image.
	 * 
	 * @param elapsed the time elapsed since the image was created
	 */
	private void setOpacity(long elapsed){
		float opacity = 1f * (opacityThreshold - elapsed) / opacityThreshold;
		if(opacity < 0)
			opacity = 0;
		scales[3] = opacity;
		rescale = new RescaleOp(scales, offsets, null);
	}
	
	/**
	 * chatScrollUp() scrolls the chat window up one line.
	 */
	public void chatScrollUp(){
		if(chatScroll < model.getHistory().size() - 10)
			chatScroll ++;
	}
	
	/**
	 * chatScrollDown() scrolls chat window down one line.
	 */
	public void chatScrollDown(){
		if(chatScroll > 0)
			chatScroll --;
	}
	
	/**
	 * removeChar() removes one character from the end of the input string.
	 */
	public void removeChar(){
		input = (input.length() > 0) ? input.substring(0, input.length()-1) : input;
	}
	
	/**
	 * appendChar() adds a character to the input for chatting.
	 * 
	 * @param append the char to append
	 */
	public void appendChar(char append){
			input += append;
	}
	
	/**
	 * sendMessage() makes the current typed line a message and sends it
	 * to the chat log for the current room.
	 */
	public void sendMessage() {
		input = model.getPlayerName() + ": " + input;
		while(input.length() > 0){
			String line = input.substring(0, Math.min(INPUT_LINE_SIZE, input.length()));
			model.sendMessage(line);
			input = (input.length() > INPUT_LINE_SIZE) ? input.substring(INPUT_LINE_SIZE) : "";
		}
	}

	
	
	
	
	
	
	//----------------------------------
	//Animation methods
	//----------------------------------
	
	/**
	 * stopMoving() is a private helper method that stops the sprite from moving.
	 */
	private void stopMoving() {
		model.stopPlayerMoving(Sprite.DIRECTION_DOWN);
		model.stopPlayerMoving(Sprite.DIRECTION_UP);
		model.stopPlayerMoving(Sprite.DIRECTION_LEFT);
		model.stopPlayerMoving(Sprite.DIRECTION_RIGHT);
	}
	
	/**
	 * panLeft() pans the screen to the left.
	 */
	public void panLeft(){
		for(int i = 0; i < width/scroll; i++){
			scrollLeft(scroll);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * scrollLeft() is a private helper function that scrolls the 
	 * screen left the specified amount.
	 * 
	 * @param scroll the amount by which to scroll
	 */
	private void scrollLeft(int scroll){
		x -= scroll;
		drawer.repaint();
	}
	
	/**
	 * panRight() pans the screen to the right.
	 */
	public void panRight(){
		for(int i = 0; i < width/scroll; i++){
			scrollRight(scroll);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {}
		}
	}

	/**
	 * scrollRight() is a private helper function that scrolls the 
	 * screen right the specified amount.
	 * 
	 * @param scroll the amount by which to scroll
	 */
	private void scrollRight(int scroll){
		x += scroll;
		drawer.repaint();
	
	}
	
	/**
	 * panUp() pans the screen up.
	 */
	public void panUp(){
		for(int i = 0; i < height/scroll; i++){
			scrollUp(scroll);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * scrollUp() is a private helper function that scrolls the 
	 * screen up the specified amount.
	 * 
	 * @param scroll the amount by which to scroll
	 */
	private void scrollUp(int scroll){
		y -= scroll;
		drawer.repaint();
		
	}
	
	/**
	 * panDown() pans the screen down.
	 */
	public void panDown(){
		for(int i = 0; i < height/scroll; i++){
			scrollDown(scroll);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * scrollDown() is a private helper function that scrolls the 
	 * screen down the specified amount.
	 * 
	 * @param scroll the amount by which to scroll
	 */
	private void scrollDown(int scroll){
		y += scroll;
		drawer.repaint();
		
	}

	/**
	 * isAnimating() tells if the screen in currently animating.
	 * 
	 * @return true if it is animating.
	 */
	public boolean isAnimating() {
		return (animator != null);
	}
	
	
	
	
	//-----------------------------------
	//Thread methods
	//-----------------------------------
	
	/**
	 * start() starts the animator thread.
	 * Used for panning.
	 */
	public void start(){
		if (animator == null){
			animator = new Thread(this);
			animator.start();
		}
	}
	
	/**
	 * run() is called by the thread when it is started.
	 * Calls the necessary functions to pas the screen.
	 */
	public void run(){
		Thread thisThread = Thread.currentThread();
		stopMoving();
		while(animator == thisThread){
			if(panUp){
				panUp();
				model.shiftUp();
			}
			else if(panDown){
				panDown();
				model.shiftDown();
			}
			else if(panLeft){
				panLeft();
				model.shiftLeft();
			}
			else if(panRight){
				panRight();
				model.shiftRight();
			}
			stop();
			panUp = false;
			panDown = false;
			panLeft = false;
			panRight = false;
		}
	}
	
	/**
	 * stop() stops the animator thread.
	 * Must be called when you are done animating.
	 */
	public void stop(){
		if (animator != null)
			animator = null;
	}



}
