package hasco.gui.statsplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hasco.model.UnparametrizedComponentInstance;
import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graphvisualizer.events.gui.Histogram;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;

/**
 * 
 * @author fmohr
 *
 * @param <N>
 *            The node class
 */
public class HASCOModelStatisticsPluginView extends ASimpleMVCPluginView<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginController> {

	private final Map<UnparametrizedComponentInstance, Histogram> histograms = new HashMap<>();
	private final FlowPane root = new FlowPane();
	private final ComboBox<String> rootComboBox = new ComboBox<>();
	private final int n = 100;

	public HASCOModelStatisticsPluginView(HASCOModelStatisticsPluginModel model) {
		super(model);
		root.getChildren().add(rootComboBox);
		rootComboBox.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				/* determine required interfaces of this choice  */
				List<Pair<String,String>> listOfFiltersOnRequiredInterfaces = new ArrayList<>();
				listOfFiltersOnRequiredInterfaces.add(new Pair<>("", newValue));
				Collection<UnparametrizedComponentInstance> compatibleSolutionClasses = getModel().getSeenUnparametrizedComponentsUnderPath(listOfFiltersOnRequiredInterfaces);
				System.out.println("Matches: ");
				List<String> path = new ArrayList<>();
				compatibleSolutionClasses.forEach(e -> System.out.println("\t" + e));
				Collection<UnparametrizedComponentInstance> compatibleSubSolutions = compatibleSolutionClasses.stream().map(s -> s.getSubComposition(path)).collect(Collectors.toList());
				
				/* get required interfaces and the seen values for each of them */
				Map<String, Set<String>> requiredInterfaces = new HashMap<>();
				for (UnparametrizedComponentInstance composition : compatibleSubSolutions) {
					
				}
				System.out.println("Subsolutions: ");
//				compatibleSubSolutions.forEach(e -> System.out.println("\t" + e));
			}
		});
	}

	@Override
	public Node getNode() {
		return root;
	}

	@Override
	public void update() {
		Map<UnparametrizedComponentInstance, DescriptiveStatistics> stats = getModel().getPerformanceStatisticsPerComposition();
		
		/* determine required interface */
		Set<String> usedComponents = stats.keySet().stream().map(e -> e.getComponentName()).collect(Collectors.toSet());
		Platform.runLater(() -> {
			rootComboBox.getItems().addAll(SetUtil.difference(usedComponents, rootComboBox.getItems()));
		});
		
//		for (UnparametrizedComponentInstance comp : stats.keySet()) {
//			if (!histograms.containsKey(comp)) {
//				Histogram newHist = new Histogram(n);
//				histograms.put(comp, newHist);
//				Platform.runLater(() -> {
//					root.getChildren().add(newHist);
//				});
//			}
//
//			// Histogram hist = ;
//			Platform.runLater(() -> {
//				histograms.get(comp).update(getHistogram(stats.get(comp), n));
//			});
//		}

	}

	// count data population in groups
	private int[] getHistogram(DescriptiveStatistics stats, int numBars) {
		int[] histogram = new int[numBars];
		double[] values = stats.getValues();
		double min = stats.getMin();
		double stepSize = (stats.getMax() - min) / numBars;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < numBars; j++) {
				if (values[i] <= min + (j * stepSize)) {
					histogram[j]++;
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
