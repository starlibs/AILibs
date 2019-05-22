package hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.Subscribe;

import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.events.HASCOSolutionEvent;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import hasco.gui.statsplugin.HASCOSolutionCandidateRepresenter;
import hasco.serialization.CompositionSerializer;
import hasco.serialization.UnresolvableRequiredInterfaceException;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class HASCOModelStatisticsObserverPluginExample {
	public static void main(final String[] args) throws UnresolvableRequiredInterfaceException, IOException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		HASCOViaFDAndBestFirstFactory<Double> hascoFactory = new HASCOViaFDAndBestFirstFactory<>(n -> 0.0);
		Random r = new Random();
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/simpleproblemwithtwocomponents.json"), "IFace", n -> System.currentTimeMillis() * 1.0);
		hascoFactory.setProblemInput(problem);
		// RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/tinylogparamproblem.json"), "IFace", n ->
		// r.nextDouble());
		// hascoFactory.setProblemInput(problem);
		hascoFactory.withDefaultAlgorithmConfig();
		HASCOViaFDAndBestFirst<Double> hasco = hascoFactory.getAlgorithm();
		hasco.setNumCPUs(1);
		hasco.registerListener(new Object() {

			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<?> solutionEvent) {
				System.out.println(CompositionSerializer.serializeComponentInstance(solutionEvent.getSolutionCandidate().getComponentInstance()));

			}
		});
		new JFXPanel();
		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer,
				new NodeDisplayInfoAlgorithmEventPropertyComputer<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new RolloutInfoAlgorithmEventPropertyComputer(nodeInfoAlgorithmEventPropertyComputer),
				new ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(new HASCOSolutionCandidateRepresenter()));
		Platform.runLater(new AlgorithmVisualizationWindow(hasco, algorithmEventPropertyComputers, new GraphViewPlugin(), new NodeInfoGUIPlugin(), new HASCOModelStatisticsPlugin()));
		hasco.call();
	}
}
