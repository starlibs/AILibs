package de.upb.crc901.taskconfigurator.test;

import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ITimeoutNodeEvaluator;
import jaicore.search.structure.core.Node;

@SuppressWarnings("serial")
public class TimeoutNodeEvaluator implements ITimeoutNodeEvaluator<TFDNode, Integer> {
	
	@Override
	public Integer f(Node<TFDNode, Integer> node) {
		return Integer.MAX_VALUE;
	}
};