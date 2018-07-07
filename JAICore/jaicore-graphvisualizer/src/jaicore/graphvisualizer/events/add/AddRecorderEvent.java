package jaicore.graphvisualizer.events.add;

import jaicore.graphvisualizer.gui.Recorder;

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
