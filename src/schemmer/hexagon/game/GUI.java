package schemmer.hexagon.game;

import javax.swing.JFrame;

public class GUI extends JFrame{
	private static final long serialVersionUID = 105L;
	private int width, height;
	private Screen screen;
	
	public GUI(int width, int height, boolean fullscreen, Main main){
		super();
		this.width = width;
		this.height = height;
		this.setSize(width, height);
		
		if(fullscreen){
			this.setExtendedState(GUI.MAXIMIZED_BOTH);
			this.setUndecorated(true);
		}
		
		this.setTitle("Hexagon");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		screen = new Screen(main);
		screen.setSize(this.getSize());
		screen.setVisible(true);
		this.add(screen);

		this.setVisible(true);
	}
	
	public Screen getScreen(){
		return screen;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
}
