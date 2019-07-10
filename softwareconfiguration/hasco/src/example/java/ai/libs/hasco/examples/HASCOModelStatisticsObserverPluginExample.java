package ai.libs.hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import ai.libs.hasco.gui.statsplugin.HASCOSolutionCandidateRepresenter;
import ai.libs.hasco.serialization.CompositionSerializer;
import ai.libs.hasco.serialization.UnresolvableRequiredInterfaceException;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
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
