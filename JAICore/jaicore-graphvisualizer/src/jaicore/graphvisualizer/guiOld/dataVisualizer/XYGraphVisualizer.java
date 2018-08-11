package jaicore.graphvisualizer.guiOld.dataVisualizer;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.misc.XYEvent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
/**
 * A Visualizer which draws a x-y-Graph.
 * The default-supplier is the BestFSupplier.
 * @author jkoepe
 *
 */
public class XYGraphVisualizer implements IVisualizer {

    LineChart<Number, Number> chart;
    XYChart.Series series;

    public XYGraphVisualizer(){
        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Best f");
        x.setLabel("Time");
        series =  new XYChart.Series();
        series.setName("Best f");
        this.chart = new LineChart<Number, Number>(x,y);
        chart.getData().add(series);
    }

    @Override
    public Node getVisualization() {
        return chart;
    }



    @Subscribe
    public void receiveXYEvent(XYEvent event){
        Platform.runLater(()->{
            series.getData().add(event.getData());
        });
    }

    @Override
    public String getSupplier() {
        return "BestFSupplier";
    }

    @Override
    public String getTitle() {
        return "Best F";
    }
}
