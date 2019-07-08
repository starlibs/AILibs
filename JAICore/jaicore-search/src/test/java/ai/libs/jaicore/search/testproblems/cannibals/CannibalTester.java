package ai.libs.jaicore.search.testproblems.cannibals;

import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import ai.libs.jaicore.search.algorithms.standard.astar.AStar;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.testproblems.cannibals.CannibalProblem;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class CannibalTester {


	public static void main(final String[] args) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		CannibalProblem p = new CannibalProblem(true, 3, 3, 0, 0);



		GraphSearchWithSubpathEvaluationsInput<CannibalProblem, String, Integer> prob = new GraphSearchWithSubpathEvaluationsInput<>(new CannibalGraphGenerator(p), n -> n.externalPath().size());
		StandardBestFirst<CannibalProblem, String, Integer> rs = new StandardBestFirst<>(prob);

		AStar<CannibalProblem, String> astar = new AStar<>(new GraphSearchWithNumberBasedAdditivePathEvaluation<>(prob.getGraphGenerator(), (n1,n2) -> 1, n -> 1.0 * n.getPoint().getCannibalsOnLeft() + n.getPoint().getMissionariesOnLeft()));
		new JFXPanel();
		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer, new NodeDisplayInfoAlgorithmEventPropertyComputer<>(n -> n.toString()));

		Platform.runLater(new AlgorithmVisualizationWindow(astar, algorithmEventPropertyComputers, new GraphViewPlugin(), new NodeInfoGUIPlugin()));

		System.out.println(astar.nextSolutionCandidate().getNodes().size() - 1);
		//		rs.cancel();
		//		for (int i = 0; i < 20 && rs.hasNext(); i++) {
		//			System.out.println(rs.next());
		//		}

		System.out.println("ready");
	}
}
