package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JApplet;
import javax.swing.JTextField;

public class ConnectionTestApplet extends JApplet {
	
	public void init(){
		try{
			
			Socket socket = null;
			socket = new Socket("localhost", 2219);
			
			System.out.println("Connected to server");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			int newPort = Integer.parseInt(in.readLine());
			System.out.println("Port: " + newPort);
			
			socket = null;
			socket = new Socket("localhost", newPort);
			
			System.out.println("Connection established");
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			while(true){
				System.out.println(in.readLine());
			}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
}
	}

}
