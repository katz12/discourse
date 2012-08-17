package client;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class GameMusic {
	
	static final int MUSIC_WALKING = 0;
	static final int MUSIC_TALKING = 1;
	
	private int curMusic;
	private AePlayWave walkin;
	private AePlayWave talkin;

	public GameMusic(boolean playerNearMe) {
		walkin = new AePlayWave("walkin.wav");
		talkin = new AePlayWave("talkin.wav");
		
		
		if(playerNearMe){
			talkin.startPlaying();
			curMusic = MUSIC_TALKING;
		}
		else{
			walkin.startPlaying();
			curMusic = MUSIC_WALKING;
		}
	}
	
	public void muteAll(){
		talkin.mute();
		walkin.mute();
	}
	
	public void unmuteAll(){
		talkin.unmute();
		walkin.unmute();
	}

	public void updateMusic(boolean playerNearMe) {
		if(playerNearMe && curMusic == MUSIC_WALKING){
			walkin.stopPlaying();
			talkin.startPlaying();
			curMusic = MUSIC_TALKING;
		}
		else if(!playerNearMe && curMusic == MUSIC_TALKING){
			talkin.stopPlaying();
			walkin.startPlaying();
			curMusic = MUSIC_WALKING;
		}
		
	}

}
