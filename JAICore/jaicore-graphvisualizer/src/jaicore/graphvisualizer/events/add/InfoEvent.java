package jaicore.graphvisualizer.events.add;

public class InfoEvent {
    private int maxIndex;
    private long maxTime;

    public InfoEvent(int maxIndex, long maxTime ){
        this.maxIndex = maxIndex;
        this.maxTime = maxTime;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public long getMaxTime() {
        return maxTime;
    }
}
