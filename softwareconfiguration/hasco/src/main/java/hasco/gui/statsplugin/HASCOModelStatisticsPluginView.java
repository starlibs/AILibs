package hasco.gui.statsplugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hasco.model.UnparametrizedComponentInstance;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import jaicore.graphvisualizer.plugin.IGUIPluginView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.FlowPane;

/**
 * 
 * @author fmohr
 *
 * @param <N>
 *            The node class
 */
public class HASCOModelStatisticsPluginView extends ASimpleMVCPluginView<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginController> {

	private Map<UnparametrizedComponentInstance, Histogram> histograms = new HashMap<>();
	private FlowPane root = new FlowPane();
    private final int n = 100;

    public HASCOModelStatisticsPluginView(HASCOModelStatisticsPluginModel model) {
		super(model);
	}	

	@Override
	public Node getNode() {
		return root;
	}

	@Override
	public void update() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("<ul>");
		Map<UnparametrizedComponentInstance, DescriptiveStatistics> stats = getModel().getPerformanceStatisticsPerComposition();
		for (UnparametrizedComponentInstance comp : stats.keySet()) {
			if (!histograms.containsKey(comp)) {
				Histogram newHist = new Histogram(n);
				histograms.put(comp, newHist);
				Platform.runLater(() -> {
					root.getChildren().add(newHist);
				});
			}
//			sb.append("<li>");
//			sb.append(comp);
//			sb.append(": ");
//			sb.append(stats.get(comp));
//			sb.append("</li>");
//		}
//		sb.append("</ul>");
			
		
        
//        barChart.setCategoryGap(0);
//        barChart.setBarGap(0);
        
        
//        xAxis.setLabel("Range");       
//        yAxis.setLabel("Population");
        
//		Histogram hist = ;
        Platform.runLater(() -> {
        	histograms.get(comp).update(getHistogram(stats.get(comp), n));
		});
		}
		
	}
     
    //count data population in groups
    private int[] getHistogram(DescriptiveStatistics stats, int numBars){
    	int[] histogram = new int[numBars];
    	double[] values = stats.getValues();
    	double min = stats.getMin();
    	double stepSize = (stats.getMax() - min) / numBars;
        for(int i = 0; i < values.length; i++){
        	for (int j = 0; j < numBars; j++) {
        		if (values[i] <= min + (j * stepSize)) {
        			histogram[j] ++;
        			break;
        		}
        	}
        }
        return histogram;
    }

	@Override
	public String getTitle() {
		return "HASCO Model Statistics";
	}

}
