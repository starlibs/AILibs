package jaicore.graphvisualizer.events.controlEvents;

/**
 * A ControlEvent which is triggerd by pushing a node. 
 * @author jkoepe
 *
 */
public class NodePushed implements ControlEvent{

    Object node;
    /**
     * A new NodePushed event
     * @param node
     * 		The pushed node.
     */
    public NodePushed(Object node){
        this.node = node;
    }

    public Object getNode() {
        return node;
    }
}
