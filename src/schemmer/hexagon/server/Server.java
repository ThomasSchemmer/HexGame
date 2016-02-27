package schemmer.hexagon.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import schemmer.hexagon.game.Main;
import schemmer.hexagon.handler.EntityHandler;
import schemmer.hexagon.handler.MapHandler;
import schemmer.hexagon.handler.RoundHandler;
import schemmer.hexagon.player.Player;

public class Server extends Main{
	private ServerSocket serverSocket;
	private ArrayList<ServerThread> clients = new ArrayList<ServerThread>();
	private int maxPlayerCount;
	private int maxAICount;
	private int clientReady = 0;
	
	private final static int PLAYER_COUNT = 2;
	private final static int AI_COUNT = 0;
	
	private ServerWindow window;

	public Server(int port, int player, int ai) throws IOException
	{
		createUI();
		
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(10000);
		this.maxPlayerCount = player;
		this.maxAICount = ai;

		eh = new EntityHandler();
		mh = new MapHandler(this);
		
		acceptClients();

		rh = new RoundHandler(mh);
		rh.createAllPlayers(player, ai);
		
		sendPlayerCount();
		sendPlayers();
		
		while(clientReady < maxPlayerCount){}		// wait
		rh.startRound();
	}

	
	private void acceptClients(){
		int nr = 0;

		log("Waiting for clients..");
		while(nr < maxPlayerCount){
			try{
				Socket client = serverSocket.accept();
				clients.add(new ServerThread(this, client, nr));
				nr++;
				log("Nr "+nr +" connected!");
			}catch(SocketTimeoutException e){
			}catch(Exception e){
				e.printStackTrace();
				log(e.getMessage());
			}
		}
	}
	
	public void append(String s){
		window.log(s);
	}
	
	public void log(String s){
		window.log(s);
		window.newLine();
	}
	
	public static void main (String [] args) {
		try{
			new Server(5555, PLAYER_COUNT, AI_COUNT);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Deprecated
	private void sendMap(){
		byte message[] = mh.getMapAsByte();
		for(int i = 0; i < clients.size(); i++){
			clients.get(i).send("map");
			clients.get(i).writeInt(mh.RADIUS);
			clients.get(i).writeInt(message.length);
			clients.get(i).write(message);
		}
	}
	
	public void sendPlayers(){
		for(int i = 0; i < clients.size(); i++){
			for(int p = 0; p < (maxPlayerCount + maxAICount); p++){
				clients.get(i).sendPlayer(rh.getPlayer(p), p);
			}
		}
	}
	
	public void sendPlayer(Player pl, int i){
		clients.get(i).sendPlayer(pl, i);
	}
	
	public void sendPlayerCount(){
		for(int i = 0; i < clients.size(); i++){
			sendPlayerCount(i);
		}
	}
	
	public void nextPlayer(){
		//TODO: 
	}
	
	public void sendPlayerCount(int i){
		clients.get(i).send("playerCount");
		clients.get(i).writeInt(maxPlayerCount);
		clients.get(i).writeInt(maxAICount);
	}
	
	public void clientReady(){
		clientReady++;
		log("Client is ready");
	}
	
	private void createUI(){
		window = new ServerWindow();
		window.setSize(800, 640);
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }//windowClosing
        });
        window.setVisible(true);
	}
}
