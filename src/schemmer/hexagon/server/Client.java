package schemmer.hexagon.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import schemmer.hexagon.game.Main;
import schemmer.hexagon.map.HexTypeInt;
import schemmer.hexagon.map.Hexagon;
import schemmer.hexagon.processes.MapFactory;

public class Client implements Runnable{
	private Main main;
	private ClientFunctions clientFunctions;
	
	private ArrayList<String> messages = new ArrayList<String>();
	private Selector selector;

	private SocketChannel channel;

	public Client(Main main){
		String serverName = "localhost";
		int port = 5555;

		this.main = main;
		clientFunctions = new ClientFunctions(this);

		try
		{
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);

			channel.register(selector, SelectionKey.OP_CONNECT);
			channel.connect(new InetSocketAddress(serverName, port));
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()){

				selector.select(1000);

				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

				while (keys.hasNext()){
					SelectionKey key = keys.next();
					keys.remove();

					if (!key.isValid()) continue;

					if (key.isConnectable()){
						System.out.println("CLIENT: I am connected to the server");
						connect(key);
					}   
					if (key.isWritable()){
						write(key);
					}
					if (key.isReadable()){
						read(key);
					}
				}   
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			close();
		}
	}

	private void close(){
		try {
			selector.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void read (SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(1000);
		readBuffer.clear();
		int length;
		try{
			length = channel.read(readBuffer);
		} catch (IOException e){
			System.out.println("CLIENT: Reading problem, closing connection");
			key.cancel();
			channel.close();
			return;
		}
		if (length == -1){
			System.out.println("CLIENT: Nothing was read from server");
			channel.close();
			key.cancel();
			return;
		}
		readBuffer.flip();
		byte[] buff = new byte[1024];
		readBuffer.get(buff, 0, length);
		
		clientFunctions.handleMessage(new String(buff));
		System.out.println("CLIENT: Server said: "+new String(buff));
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		if(!messages.isEmpty()){
			String message = messages.get(0);
			channel.write(ByteBuffer.wrap(message.getBytes()));
		}

		channel.register(selector, SelectionKey.OP_READ);
	}

	private void connect(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		if (channel.isConnectionPending()){
			channel.finishConnect();
		}
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
	}
	
	public void sendMessage(String s){
		try {
			while(!channel.isConnected()){}
			channel.write(ByteBuffer.wrap(s.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void nextPlayer() {
		clientFunctions.sendNextPlayer();
	}

	public int getCurrentRound() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void receivedPlayers(){
		try{
			sendMessage("clientReady");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Deprecated
	private void setHexType(Hexagon hex, char c){
		hex.setType(HexTypeInt.TYPE_FIELD.getValue());
		if(c >= 20){
			hex.setType(HexTypeInt.TYPE_HILL.getValue());
			c = (char) (c - 20);
		}
		switch(c){
		case 0:
			hex.setBiome(MapFactory.desert);
			break;
		case 1:
			hex.setBiome(MapFactory.forest);
			break;
		case 2:
			hex.setBiome(MapFactory.grassDesert);
			break;
		case 3:
			hex.setBiome(MapFactory.rainForest);
			break;
		case 4:
			hex.setBiome(MapFactory.savanna);
			break;
		case 5:
			hex.setBiome(MapFactory.seasonalForest);
			break;
		case 6:
			hex.setBiome(MapFactory.swamp);
			break;
		case 7:
			hex.setBiome(MapFactory.taiga);
			break;
		case 8:
			hex.setBiome(MapFactory.tundra);
			break;
		case 9:
			hex.setType(HexTypeInt.TYPE_DEEPWATER.getValue());
			break;
		case 10:
			hex.setType(HexTypeInt.TYPE_WATER.getValue());
			break;
		case 11:
			hex.setType(HexTypeInt.TYPE_MOUNTAIN.getValue());
			break;
		}
	}

	public void attack(Hexagon field, Hexagon fieldEnemy){
		try{
			String s = "attack,";
			s += field.getX()+",";
			s += field.getY()+",";
			s += fieldEnemy.getX()+",";
			s += fieldEnemy.getY();
			clientFunctions.flush(s);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void moveTo(Hexagon before, Hexagon after){
		try{
			if(before != null && after != null){
				String s = "move,";
				s += before.getX()+",";
				s += before.getY()+",";
				s += after.getX()+",";
				s += after.getY();
				clientFunctions.flush(s);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Main getMain(){
		return main;
	}

	
}
