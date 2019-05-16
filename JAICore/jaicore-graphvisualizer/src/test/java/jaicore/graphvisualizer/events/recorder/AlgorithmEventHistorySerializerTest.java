package jaicore.graphvisualizer.events.recorder;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import jaicore.basic.algorithm.events.AlgorithmCanceledEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;

public class AlgorithmEventHistorySerializerTest {

	@Test
	public void testAlgorithmEventSerialization() throws IOException, InterruptedException {
		AlgorithmEventHistoryRecorder recorder = new AlgorithmEventHistoryRecorder(Collections.EMPTY_LIST);

		recorder.handleAlgorithmEvent(new AlgorithmCanceledEvent("cool"));
		Thread.sleep(20);
		recorder.handleAlgorithmEvent(new AlgorithmCanceledEvent("cooler"));
		Thread.sleep(40);
		recorder.handleAlgorithmEvent(new AlgorithmCanceledEvent("thecoolest"));
		Thread.sleep(10);
		recorder.handleAlgorithmEvent(new AlgorithmFinishedEvent("therediculuoslycoolest"));

		AlgorithmEventHistorySerializer serializer = new AlgorithmEventHistorySerializer();
		String serializedAlgorithmEventHistory = serializer.serializeAlgorithmEventHistory(recorder.getHistory());
		System.out.println(serializedAlgorithmEventHistory);

		AlgorithmEventHistory eventHistory = serializer.deserializeAlgorithmEventHistory(serializedAlgorithmEventHistory);
		System.out.println("done");

		System.out.println(serializer.serializeAlgorithmEventHistory(eventHistory));

	}
}
