package ai.libs.jaicore.graphvisualizer.events.recorder;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.api4.java.algorithm.events.AlgorithmCanceledEvent;
import org.api4.java.algorithm.events.AlgorithmFinishedEvent;
import org.api4.java.algorithm.events.AlgorithmInitializedEvent;
import org.api4.java.algorithm.events.AlgorithmInterruptedEvent;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.TupleFoundEvent;
import ai.libs.jaicore.basic.sets.TupleOfCartesianProductFoundEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeParentSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeDisplayInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;

public class AlgorithmEventHistorySerializerTest {

	@Test
	public void testAlgorithmEventSerializationAndDeserializationWithEasyEvents() throws IOException, InterruptedException {

		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer, new NodeDisplayInfoAlgorithmEventPropertyComputer<>(n -> n.toString()),
				new ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer());

		AlgorithmEventHistoryRecorder recorder = new AlgorithmEventHistoryRecorder(algorithmEventPropertyComputers);

		recorder.handleAlgorithmEvent(new AlgorithmCanceledEvent("t1"));
		Thread.sleep(20);
		recorder.handleAlgorithmEvent(new AlgorithmFinishedEvent("t2"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new AlgorithmInitializedEvent("t3"));
		Thread.sleep(20);
		recorder.handleAlgorithmEvent(new AlgorithmFinishedEvent("t4"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new AlgorithmInterruptedEvent("t5"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new GraphInitializedEvent<>("t6", "root"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new NodeAddedEvent<>("t7", "root", "n1", "cool"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new NodeAddedEvent<>("t8", "root", "n2", "very_cool"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new NodeParentSwitchEvent<>("t9", "n1", "root", "n2"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new NodeRemovedEvent<>("t10", "n1"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new NodeTypeSwitchEvent<>("t11", "n2", "cool"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new TupleFoundEvent<>("t12", Arrays.asList("a", "b")));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new TupleOfCartesianProductFoundEvent<>("t13", Arrays.asList("a", "b")));

		AlgorithmEventHistorySerializer serializer = new AlgorithmEventHistorySerializer();
		String serializedAlgorithmEventHistory = serializer.serializeAlgorithmEventHistory(recorder.getHistory());

		AlgorithmEventHistory deserializedEventHistory = serializer.deserializeAlgorithmEventHistory(serializedAlgorithmEventHistory);

		assertEquals(recorder.getHistory(), deserializedEventHistory);
	}

}
