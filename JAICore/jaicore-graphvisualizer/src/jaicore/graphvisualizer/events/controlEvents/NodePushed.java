package jaicore.graphvisualizer.events.controlEvents;

public class NodePushed implements ControlEvent{

    Object node;

    public NodePushed(Object node){
        this.node = node;
    }

    public Object getNode() {
        return node;
    }
}
