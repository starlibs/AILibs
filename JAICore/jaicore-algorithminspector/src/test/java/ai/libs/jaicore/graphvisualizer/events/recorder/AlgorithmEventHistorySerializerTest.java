package ai.libs.jaicore.graphvisualizer.events.recorder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.algorithm.AlgorithmCanceledEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInterruptedEvent;
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

class AlgorithmEventHistorySerializerTest {

	@Test
	void testAlgorithmEventSerializationAndDeserializationWithEasyEvents() throws IOException, InterruptedException {

		NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
		List<AlgorithmEventPropertyComputer> algorithmEventPropertyComputers = Arrays.asList(nodeInfoAlgorithmEventPropertyComputer, new NodeDisplayInfoAlgorithmEventPropertyComputer<>(n -> n.toString()),
				new ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer());

		AlgorithmEventHistoryRecorder recorder = new AlgorithmEventHistoryRecorder(algorithmEventPropertyComputers);

		IAlgorithm<?, ?> dummyAlg = new AAlgorithm<Object, Object>(null) {

			@Override
			public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
				return null;
			}

			@Override
			public Object call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
				return null;
			}
		};

		recorder.handleAlgorithmEvent(new AlgorithmCanceledEvent(dummyAlg));
		recorder.handleAlgorithmEvent(new AlgorithmFinishedEvent(dummyAlg));
		recorder.handleAlgorithmEvent(new AlgorithmInitializedEvent(dummyAlg));
		recorder.handleAlgorithmEvent(new AlgorithmFinishedEvent(dummyAlg));
		recorder.handleAlgorithmEvent(new AlgorithmInterruptedEvent(dummyAlg));
		recorder.handleAlgorithmEvent(new GraphInitializedEvent<>(dummyAlg, "root"));
		recorder.handleAlgorithmEvent(new NodeAddedEvent<>(dummyAlg, "root", "n1", "cool"));
		recorder.handleAlgorithmEvent(new NodeAddedEvent<>(dummyAlg, "root", "n2", "very_cool"));
		recorder.handleAlgorithmEvent(new NodeParentSwitchEvent<>(dummyAlg, "n1", "root", "n2"));
		recorder.handleAlgorithmEvent(new NodeRemovedEvent<>(dummyAlg, "n1"));
		recorder.handleAlgorithmEvent(new NodeTypeSwitchEvent<>(dummyAlg, "n2", "cool"));
		recorder.handleAlgorithmEvent(new TupleFoundEvent<>(dummyAlg, Arrays.asList("a", "b")));
		recorder.handleAlgorithmEvent(new TupleOfCartesianProductFoundEvent<>(dummyAlg, Arrays.asList("a", "b")));

		AlgorithmEventHistorySerializer serializer = new AlgorithmEventHistorySerializer();
		String serializedAlgorithmEventHistory = serializer.serializeAlgorithmEventHistory(recorder.getHistory());

		AlgorithmEventHistory deserializedEventHistory = serializer.deserializeAlgorithmEventHistory(serializedAlgorithmEventHistory);

		assertEquals(recorder.getHistory(), deserializedEventHistory);
	}

}
