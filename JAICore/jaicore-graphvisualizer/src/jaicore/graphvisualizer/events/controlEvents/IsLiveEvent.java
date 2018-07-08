package jaicore.graphvisualizer.events.controlEvents;

public class IsLiveEvent implements ControlEvent {

    boolean isLive;

    public IsLiveEvent(boolean isLive){
        this.isLive = isLive;
    }

    public boolean isLive(){
        return this.isLive;
    }
}
