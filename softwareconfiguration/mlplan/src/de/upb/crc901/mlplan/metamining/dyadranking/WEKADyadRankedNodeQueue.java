package de.upb.crc901.mlplan.metamining.dyadranking;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.search.DyadRankedNodeQueue;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.model.travesaltree.Node;

public class WEKADyadRankedNodeQueue extends DyadRankedNodeQueue<TFDNode, Double> {

	public WEKADyadRankedNodeQueue(Vector contextCharacterization) {
		super(contextCharacterization);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Vector characterize(Node<TFDNode, Double> node) {
		// TODO Auto-generated method stub
		return null;
	}
}
