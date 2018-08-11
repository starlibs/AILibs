package jaicore.graphvisualizer.guiOld.dataVisualizer;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.misc.HTMLEvent;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import javax.swing.*;

/**
 * The HTML-Visualizer is able to display text written in HTML.
 * @author jkoepe
 *
 */
public class HTMLVisualizer implements IVisualizer {

    SwingNode node;
    JLabel label;

    /**
     * Creates a new HTMLVisualizer
     */
    public HTMLVisualizer(){
        label = new JLabel();
        label.setText("<html></html>");
        node = new SwingNode();
        createSwingNode( node);
    }


    private void createSwingNode(SwingNode node){
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(label);
        SwingUtilities.invokeLater(()->{
            node.setContent(pane);
        });
    }

    @Override
    public Node getVisualization() {
        return node;
    }


    @Subscribe
    public void receiveData(HTMLEvent html){
        StringBuilder sb = new StringBuilder();
//						sb.append("<html><div style='padding: 5px; background: #ffffcc; border: 1px solid black;'>");
        sb.append("<html><div style='padding: 5px;'>");
        sb.append(html.getText());
        sb.append("</div></html>");

        label.setText(sb.toString());
    }
}
