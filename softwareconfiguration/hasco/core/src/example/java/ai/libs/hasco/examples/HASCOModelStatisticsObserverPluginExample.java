package ai.libs.hasco.examples;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import ai.libs.hasco.serialization.CompositionSerializer;
import ai.libs.hasco.serialization.UnresolvableRequiredInterfaceException;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;

public class HASCOModelStatisticsObserverPluginExample {
	public static void main(final String[] args) throws UnresolvableRequiredInterfaceException, IOException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		HASCOViaFDAndBestFirstFactory<Double> hascoFactory = new HASCOViaFDAndBestFirstFactory<>(n -> 0.0);
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/simpleproblemwithtwocomponents.json"), "IFace", n -> System.currentTimeMillis() * 1.0);
		hascoFactory.setProblemInput(problem);
		hascoFactory.withDefaultAlgorithmConfig();
		HASCOViaFDAndBestFirst<Double> hasco = hascoFactory.getAlgorithm();
		hasco.setNumCPUs(1);
		hasco.registerListener(new Object() {

			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<?> solutionEvent) {
				System.out.println(CompositionSerializer.serializeComponentInstance(solutionEvent.getSolutionCandidate().getComponentInstance()));

			}
		});
		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(hasco);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new HASCOModelStatisticsPlugin());
		hasco.call();
	}
}
