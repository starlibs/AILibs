package jaicore.graphvisualizer.events.misc;

/**
 * An Event which contains a html-string
 * 
 * @author jkoepe
 *
 */
public class HTMLEvent {

	String text;

	public HTMLEvent(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
