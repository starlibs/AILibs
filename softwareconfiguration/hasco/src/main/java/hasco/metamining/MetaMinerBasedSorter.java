package hasco.metamining;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

/**
 * A Comparator for {@link TFDNode}s that sorts based on meta information about
 * the underlying {@link ComponentInstance} of the node and possibly application
 * context.
 * 
 * @author Helena Graf
 *
 */
public class MetaMinerBasedSorter implements Comparator<TFDNode> {
	
	private Logger logger = LoggerFactory.getLogger(MetaMinerBasedSorter.class);

	/**
	 * Components for the current configuration used to convert TFDNodes to
	 * ComponentInstances
	 */
	private Collection<Component> components;

	/**
	 * The "MetaMiner" has access to the meta information of the given
	 * {@link ComponentInstance} and possibly its application context. It is used to
	 * derive a score of a given ComponentInstance, based on which a comparison of
	 * the given {@link TFDNode}s is made.
	 */
	private IMetaMiner metaminer;

	public MetaMinerBasedSorter(IMetaMiner metaminer, Collection<Component> components) {
		if (components==null) {
			logger.warn("No Components in sorter!");
		}
		this.components = components;
		this.metaminer = metaminer;
	}

	@Override
	public int compare(TFDNode o1, TFDNode o2) {
		if (convertToComponentInstance(o1) == null || convertToComponentInstance(o2) == null) {
			logger.warn("Cannot compare pipelines when one is null.");
			return 0;
		}
		if (o1.equals(o2)) {
			logger.info("Comparing two nodes which are the same.");
			return 0;
		}
		
		double score1 = metaminer.score(convertToComponentInstance(o1));
		double score2 = metaminer.score(convertToComponentInstance(o2));
		
		try {
			logger.trace("Node {} converted to {}",o1,convertToComponentInstance(o1).getPrettyPrint());
		} catch (IOException e) {
			logger.error("Logging failed due to {}",e);
		}

		try {
			logger.trace("Node {} converted to {}",o2,convertToComponentInstance(o2).getPrettyPrint());
		} catch (IOException e) {
			logger.error("Logging failed due to {}",e);
		}
		
		logger.debug("Comparing nodes with scores: {} vs {}",score1,score2);
		return (int) Math.signum(score1 - score2);
	}

	/**
	 * Converts the given TFDNode to a ComponentInstance.
	 * 
	 * @param node
	 *            The TFDNode to convert
	 * @return The TFDNode as a ComponentInstance
	 */
	protected ComponentInstance convertToComponentInstance(TFDNode node) {
		return Util.getSolutionCompositionFromState(components, node.getState(), false);
	}

	/**
	 * Gets the {@link IMetaMiner}, which is used to derive a score for a given
	 * {@link TFDNode} based on its attached {@link ComponentInstance}.
	 * 
	 * @return The meta miner
	 */
	public IMetaMiner getMetaminer() {
		return metaminer;
	}

	/**
	 * Sets the {@link IMetaMiner}, which is used to derive a score for a given
	 * {@link TFDNode} based on its attached {@link ComponentInstance}.
	 * 
	 * @param metaminer
	 *            The meta miner
	 */
	public void setMetaminer(IMetaMiner metaminer) {
		this.metaminer = metaminer;
	}
}