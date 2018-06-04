package jaicore.graphvisualizer;

public interface IGraphDataSupplier<T> {


    public void receiveEvent(T event);

    public void update(long time, Object event);

    IDataVisualizer getVisualization();
}
