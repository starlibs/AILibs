package ai.libs.hasco.examples.visualization;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import ai.libs.jaicore.components.exceptions.UnresolvableRequiredInterfaceException;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import ai.libs.jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;

public class HASCOModelStatisticsObserverPluginExample {

	public static final String pathToExamples = "../../../JAICore/jaicore-components/";

	public static void main(final String[] args) throws UnresolvableRequiredInterfaceException, IOException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(new File(pathToExamples + "testrsc/simpleproblemwithtwocomponents.json"), "IFace", n -> System.currentTimeMillis() * 1.0);
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBlindSearch().getAlgorithm();
		hasco.setNumCPUs(1);
		hasco.registerListener(new Object() {

			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<?> solutionEvent) {
				System.out.println(new ComponentSerialization().serialize(solutionEvent.getSolutionCandidate().getComponentInstance()));

			}
		});
		AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(hasco);
		window.withMainPlugin(new GraphViewPlugin());
		window.withPlugin(new NodeInfoGUIPlugin(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())), new HASCOModelStatisticsPlugin());
		hasco.call();
	}
}
