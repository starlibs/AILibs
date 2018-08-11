package jaicore.graphvisualizer.guiOld.dataVisualizer;

import javafx.scene.Node;

/**
 * An Interface, which describes Visualizer, which can be added to the gui.
 * @author jkoepe
 *
 */

public interface IVisualizer {

	/**
	 * Returns a javafx-node, which is later shown in the visualizer.
	 * @return
	 * 		javafx.node which contains the visualization.
	 */
    public Node getVisualization();

    /**
     * This method determines, which supplier adds the events to this visualizer.
     * To add a supplier, the class name of the supplier is needed.
     * @return
     * 		Simpleclass name of the event supplier
     */
    default String getSupplier() {
        return "";
    }

    /**
     * This method is used to get the title of the visualizer.
     * The main application is to set the title which is shown in the coorsponding tab in the main frame
     * @return
     * 		The title of the visualizer
     */
    default String getTitle(){
        return "";
    }
}
