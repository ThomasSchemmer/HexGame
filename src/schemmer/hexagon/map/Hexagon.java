package schemmer.hexagon.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import schemmer.hexagon.biomes.Biome;
import schemmer.hexagon.game.Screen;
import schemmer.hexagon.utils.Conv;
import schemmer.hexagon.utils.Cube;
import schemmer.hexagon.utils.Point;

public class Hexagon {
	private final static double CORNERS = 6;	// # Corners
	private static int SIZE = 50;			// in Pixel
	private static float widthFactor = (float) (Math.sqrt(3)/2f);
	
	private HexType type;
	private Biome biome;
	
	private Point center;
	private Cube coords;
	
	private BufferedImage image;
	private BufferedImage addition;
	
	public Hexagon(Cube c){
		this.coords = c;
		this.center = Conv.cubeToPixel(c);
		center = new Point(center.x + Screen.WIDTH/2, center.y + Screen.HEIGHT/2);
	}
	
	public Point hexCorner(double i){
		double angleDeg = 360/CORNERS * i + 30;
		double angleRad = Math.PI /180 * angleDeg;
		return new Point(center.x + SIZE * Math.cos(angleRad), center.y + SIZE * Math.sin(angleRad));
	}
	
	public void draw(Graphics2D g2d, int offX, int offY){
		recalculateCenter();
		g2d.setColor(type.getColor());
		for (int i = 0; i < CORNERS; i++){
			g2d.drawLine((int)(hexCorner(i).x)-offX, (int)(hexCorner(i).y)-offY, (int)(hexCorner((i+1)%CORNERS).x)-offX, (int)(hexCorner((i+1)%CORNERS).y)-offY);
		}
		//g2d.drawString(""+(int)this.coords.getV()[0] + "|" + (int)this.coords.getV()[1]+"|"+(int)this.coords.getV()[2], (int)center.x-offX, (int)center.y-offY);
	}
	
	public void drawPicture(Graphics2D g2d, int offX, int offY){
		recalculateCenter();
		float OSF = SIZE/50f; 			//Offset Scaling Factor
		if(image == null)
			createPicture();
		g2d.drawImage(image, (int) (center.x-offX-SIZE+7*OSF), (int)center.y-offY-SIZE, (int) (SIZE*Math.sqrt(3) +1*OSF), (int)(SIZE*2 + 1 * OSF), null);
		if(addition == null)
			createAddition();
		if(type.getIndex() == HexTypeInt.TYPE_MOUNTAIN.getValue() || type.getIndex() == HexTypeInt.TYPE_HILL.getValue())
			g2d.drawImage(addition, (int)(center.x-offX-SIZE + 15*OSF), (int)(center.y-offY-SIZE ), (int) (SIZE*Math.sqrt(3) - 15 * OSF), (int)(SIZE*2 - 15*OSF), null);
		else 
			g2d.drawImage(addition, (int) (center.x-offX-SIZE+7*OSF), (int)center.y-offY-SIZE, (int) (SIZE*Math.sqrt(3) +1*OSF), (int)(SIZE*2 + 1 * OSF), null);
	}
	
	private void createPicture(){
		String filepath = "/png/images/tile";
		if(biome != null){
			filepath += biome.getImage();
			filepath += type.getImage();
		} else{
			if (type.getIndex() == HexTypeInt.TYPE_WATER.getValue())
				filepath += "Water_tile";
			if (type.getIndex() == HexTypeInt.TYPE_DEEPWATER.getValue())
				filepath += "Deepwater_tile";
			if (type.getIndex() == HexTypeInt.TYPE_MOUNTAIN.getValue())
				filepath += "Snow";
		}
		filepath += ".png";
		try{
			image = ImageIO.read(this.getClass().getResourceAsStream(filepath));
		} catch (Exception e){
			System.out.println("Couldn't load picture \""+filepath+"\"");
		}
	}
	
	private void createAddition(){
		String filepath = "/png/additions/";
		if(biome != null){
			filepath += "add";
			filepath += biome.getAddition();
		} else 
			filepath += type.getAddition();
		filepath += ".png";
		try{
			addition = ImageIO.read(this.getClass().getResourceAsStream(filepath));
		} catch (Exception e){
			System.out.println("Couldn't load picture \""+filepath+"\"");
		}
	}
	
	public void fill(Graphics2D g2d, int offX, int offY){
		recalculateCenter();
		g2d.setColor(type.getColor());
		int xs[] = new int [(int) CORNERS];
		int ys[] = new int [(int) CORNERS];
		
		for (int i = 0; i < xs.length; i++){
			xs[i] = (int) hexCorner(i).x - offX;
			ys[i] = (int) hexCorner(i).y - offY;
		}
		
		g2d.fillPolygon(xs, ys, (int) CORNERS);
		
		
	}
	
	public void drawOutline(Graphics2D g2d, int offX, int offY){
		g2d.setColor(Color.GRAY);
		for (int i = 0; i < CORNERS; i++){
			g2d.drawLine((int)(hexCorner(i).x)-offX, (int)(hexCorner(i).y)-offY, (int)(hexCorner((i+1)%CORNERS).x)-offX, (int)(hexCorner((i+1)%CORNERS).y)-offY);
		}
		//if (biome != null)
			//g2d.drawString(this.getBiome().getName(), (int)center.x-offX, (int)center.y-offY);
	}
	
	public static double getSize(){
		return SIZE;
	}
	
	public String printCoords(){
		return "Coords: "+coords.printCube()+"\n";
	}
	
	public String printCenter(){
		return "Center @"+center.x+" | "+center.y+"\n";
	}
	
	public void draw(Graphics2D g2d, Color c, Stroke s, int offX, int offY){
		g2d.setColor(c);
		g2d.setStroke(s);
		recalculateCenter();
		for (int i = 0; i < CORNERS; i++){
			g2d.drawLine((int)(hexCorner(i).x)-offX, (int)(hexCorner(i).y)-offY, (int)(hexCorner((i+1)%CORNERS).x)-offX, (int)(hexCorner((i+1)%CORNERS).y)-offY);
		}
	}
	
	public Cube getCoords(){
		return coords;
	}
	
	public Point getCenter(){
		return center;
	}
	
	public void recalculateCenter(){
		this.center = Conv.cubeToPixel(this.coords);
		center = new Point(center.x + Screen.WIDTH/2, center.y + Screen.HEIGHT/2);
	}
	
	public static void zoomIn(){
		if (SIZE < 80) SIZE ++;
	}
	
	public static void zoomOut(){
		if (SIZE > 20) SIZE --;
	}
	
	public HexType getType(){
		return type;
	}
	
	public void setType(int i){
		type = new HexType(i);
	}
	
	public void setBiome(Biome b){
		biome = b;
	}
	
	public Biome getBiome(){
		return biome;
	}
}

