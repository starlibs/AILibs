package jaicore.graphvisualizer.events.add;

import javafx.scene.chart.XYChart;

public class XYEvent {
    private XYChart.Data data;

   public XYEvent(XYChart.Data data){
       this.data = data;
   }

    public XYChart.Data getData() {
        return data;
    }
}
