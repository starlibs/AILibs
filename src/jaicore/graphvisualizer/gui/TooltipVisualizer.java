package jaicore.graphvisualizer.gui;

import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import javax.swing.*;

public class TooltipVisualizer implements IGraphDataVisualizer {


    SwingNode node;
    JLabel label;

    public TooltipVisualizer(){
        node = new SwingNode();
        label = new JLabel();
        label.setText("<html></html>");

        createSwingNode(node);
    }

    private void createSwingNode(SwingNode node){
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(label);
        SwingUtilities.invokeLater(()->{
            node.setContent(pane);
        });
    }

    public String getHTML(String data){
        StringBuilder sb = new StringBuilder();
//						sb.append("<html><div style='padding: 5px; background: #ffffcc; border: 1px solid black;'>");
        sb.append("<html><div style='padding: 5px;'>");
        sb.append(data);
        sb.append("</div></html>");

        return sb.toString();
    }


    public void update(String data){
        label.setText(getHTML(data));
    }


    public Node getVisualization(){
        return node;
    }


}
