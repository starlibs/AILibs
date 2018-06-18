package jaicore.graphvisualizer.events.controlEvents;

import java.io.File;

public class FileEvent implements ControlEvent {

    private boolean load;
    private File file;

    public FileEvent(boolean load, File file){
        this.load = load;
        this.file = file;
    }

    public boolean isLoad() {
        return load;
    }

    public File getFile() {
        return file;
    }
}
