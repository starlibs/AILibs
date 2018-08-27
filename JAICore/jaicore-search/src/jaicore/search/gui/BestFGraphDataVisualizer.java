package jaicore.search.gui;

import jaicore.graphvisualizer.IDataVisualizer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class BestFGraphDataVisualizer implements IDataVisualizer{

    LineChart<Number, Number> chart;
    XYChart.Series series;

    //TODO

    public BestFGraphDataVisualizer(){

        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        y.setLabel("Best f");
        x.setLabel("Time");
        series =  new XYChart.Series();
        series.setName("Best f");
        this.chart = new LineChart<Number, Number>(x,y);
        chart.getData().add(series);
    }

    public void update(Number time, Number bestf){
        Platform.runLater(()->{
            series.getData().add(new XYChart.Data(time, bestf));
        });


    }

    @Override
    public Node getVisualization() {
        return chart;
    }
}
