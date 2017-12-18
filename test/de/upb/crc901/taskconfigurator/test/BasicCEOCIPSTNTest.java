package de.upb.crc901.taskconfigurator.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.upb.crc901.mlplan.core.MLUtil;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.structure.core.GraphGenerator;

public class BasicCEOCIPSTNTest {

	@Test
	public void test() {
		try {
			GraphGenerator<TFDNode, String> graphGenerator = MLUtil.getGraphGenerator(new File("testrsc/simplehtnproblem.searchspace"),
					new File("conf/interpretedpredicates.conf"));
			BestFirst<TFDNode, String> bf = new BestFirst<>(graphGenerator, n -> 0.0);
			new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(new TFDTooltipGenerator());
			bf.nextSolution();
			while (true)
				;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
