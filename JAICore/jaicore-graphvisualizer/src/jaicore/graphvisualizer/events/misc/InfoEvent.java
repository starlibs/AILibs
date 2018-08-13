package jaicore.graphvisualizer.events.misc;

/**
 * An event which contains information over the recorder.
 * This information is used to sync the participating parts.
 * @author jkoepe
 *
 */
public class InfoEvent {
    private int maxIndex;
    private long maxTime;
    private int numberOfDataSupplier;
    private boolean updateIndex;

    public InfoEvent(int maxIndex, long maxTime, int numberOfDataSupplier ){
        this(maxIndex, maxTime, numberOfDataSupplier, false);
    }

    public InfoEvent(int maxIndex, long maxTime, int numberOfDataSupplier, boolean updateIndex){
        this.maxIndex = maxIndex;
        this.maxTime = maxTime;
        this.numberOfDataSupplier = numberOfDataSupplier;
        this.updateIndex = updateIndex;

    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public boolean updateIndex() {
        return updateIndex;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public int getNumberOfDataSupplier(){return numberOfDataSupplier;}
}
