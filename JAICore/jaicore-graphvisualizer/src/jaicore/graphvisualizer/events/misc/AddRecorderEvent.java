package jaicore.graphvisualizer.events.misc;

import jaicore.graphvisualizer.guiOld.Recorder;
/**
 * A event which is used to add a new Recorder to the receiving objects.
 * @author jkoepe
 *
 */
public class AddRecorderEvent {
	//TODO has to be deleted later on
	private Recorder<?> rec;
	
	public AddRecorderEvent(Recorder<?> rec) {
		this.rec = rec;
	}
	
	public Recorder<?> getRecorder() {
		return this.rec;
	}

}
