package client;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AePlayWave extends Thread {

	private String filename;

	private Position curPosition;

	private boolean play;
	private Thread player;
	
	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

	public float volumeLevel;

	enum Position {
		LEFT, RIGHT, NORMAL
	};

	public AePlayWave(String wavfile) {
		filename = wavfile;
		curPosition = Position.NORMAL;
	}

	public AePlayWave(String wavfile, Position p) {
		filename = wavfile;
		curPosition = p;
	}

	public void startPlaying() {
		play = true;
		player = new Thread(this);
		player.start();
	}

	@SuppressWarnings("deprecation")
	public void stopPlaying() {
		play = false;
		player.stop();
	}
	
	public void mute(){
		volumeLevel = -100.0f;
	}
	
	public void unmute(){
		volumeLevel = 0f;
	}

	private AudioInputStream getStream(File soundFile) {
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		return audioInputStream;
	}

	public void run() {
		File soundFile = new File(filename);
		if (!soundFile.exists()) {
			System.err.println("Wave file not found: " + filename);
			return;
		}

		AudioInputStream audioInputStream = getStream(soundFile);
		if (audioInputStream == null) {
			return;
		}

		AudioFormat format = audioInputStream.getFormat();
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (auline.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
			if (curPosition == Position.RIGHT)
				pan.setValue(1.0f);
			else if (curPosition == Position.LEFT)
				pan.setValue(-1.0f);
		}
		
		FloatControl volume = (FloatControl) auline.getControl(FloatControl.Type.MASTER_GAIN);

		auline.start();
		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

		try {
			while (play) {
				if (nBytesRead == -1) {
					if ((audioInputStream = getStream(soundFile)) == null) {
						return;
					}
				}
				volume.setValue(volumeLevel);
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0)
					auline.write(abData, 0, nBytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			auline.close();
		}
	}
}
