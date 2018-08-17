package jaicore.graphvisualizer.gui;

public interface NodeListener<T> {
	
	public void mouseOver(T node);
	
	public void mouseLeft(T node);
	
	public void buttonReleased(T node);
	
	public void buttonPushed(T node);
}
