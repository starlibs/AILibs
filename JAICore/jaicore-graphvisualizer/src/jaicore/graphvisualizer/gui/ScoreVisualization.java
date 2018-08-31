package jaicore.graphvisualizer.gui;

import jaicore.basic.Score;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.FxViewPanel;

import java.util.ArrayList;
import java.util.List;

public class ScoreVisualization<T> extends GraphVisualization<T> {

    private double bestFValue;
    private double worstFValue;

    private List<Node> nodes;

    boolean first;
    boolean score;

    public ScoreVisualization(){
        super();

        this.graph.clear();
        this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");


        bestFValue = Double.MAX_VALUE;
        worstFValue = Double.MIN_VALUE;
        this.nodes = new ArrayList<>();

        first = true;
        score = true;

    }

    @Override
    protected synchronized Node newNode(final T newNodeExt) {
        if(first){
            checkScore(newNodeExt);
            first = false;
        }
        try {
            Node node = super.newNode(newNodeExt);

            if(score) {
                Score s = (Score) this.int2extNodeMap.get(node);
                double fvalue = s.getScore();

                if (fvalue < bestFValue) {
                    bestFValue = fvalue;
                    update();
                }
                if (fvalue > worstFValue) {
                    worstFValue = fvalue;
                    update();
                }

                if (!roots.contains(newNodeExt)) {
                    colorNode(node, fvalue);
                    nodes.add(node);
                }
            }
            return node;
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
            return null;
        }

    }

    private void checkScore(T node ) {
        if(!(node instanceof Score)){
            this.graph.clear();
            this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
            this.score= false;
        }

    }

    public void update(){
        for(Node n :nodes){
            if(n instanceof Score){
                double fvalue = ((Score) n).getScore();
                colorNode(n, fvalue);
            }
        }

    }

    private void colorNode(Node node, double fvalue) {
        float color = 1;
        float x = (float)(fvalue - bestFValue);
        float y = (float)(worstFValue - bestFValue);
        color = x/y;
        node.setAttribute("ui.color", color);
    }


    @Override
    public void reset() {
        super.reset();
        this.graph.clear();
        this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
        this.nodes.clear();
    }

    @Override
    public javafx.scene.Node getFXNode() {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(this.viewPanel);
        stackPane.getChildren().add(createColorGradient());
        stackPane.setAlignment(Pos.TOP_RIGHT);
        return stackPane;
    }

    protected javafx.scene.Node createColorGradient(){
        Rectangle box = new Rectangle(50,500);
        Stop[] stops = new Stop[]{new Stop(0, Color.BLUE), new Stop(1, Color.RED)};
        LinearGradient lg = new LinearGradient(0,1,0,0, true, CycleMethod.NO_CYCLE, stops);
        box.setFill(lg);
        return box;
    }
}
