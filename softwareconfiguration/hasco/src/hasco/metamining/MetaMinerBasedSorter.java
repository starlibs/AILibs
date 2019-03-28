package hasco.metamining;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;

/**
 * A Comparator for {@link TFDNode}s that sorts based on meta information about
 * the underlying {@link ComponentInstance} of the node and possibly application
 * context.
 * 
 * @author Helena Graf
 *
 */
public class MetaMinerBasedSorter implements Comparator<TFDNode> {

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
			System.err.println("No Components in sorter!");
		}
		this.components = components;
		this.metaminer = metaminer;
	}

	@Override
	public int compare(TFDNode o1, TFDNode o2) {
		if (convertToComponentInstance(o1) == null || convertToComponentInstance(o2) == null) {
			System.err.println("Cannot compare pipelines when one is null.");
			return 0;
		}
		if (o1.equals(o2)) {
			System.err.println("Comparing two nodes which are the same.");
			return 0;
		}
		
		double score1 = metaminer.score(convertToComponentInstance(o1));
		double score2 = metaminer.score(convertToComponentInstance(o2));
		
		System.out.println("Node " + o1);
		System.out.println("Converted to " + convertToComponentInstance(o1).getPrettyPrint());
		System.out.println("Node " + o2);
		System.out.println("Converted to " + convertToComponentInstance(o2).getPrettyPrint());
		System.out.println("Comparing nodes with scores: " + score1 + " vs " + score2);
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
	
	public static void main (String [] args) {
		List<Integer> list = Arrays.asList(3,5,4,7,1,2,6,8,9);
		System.out.println(list.stream().sorted(new Comparator<Integer>() {
			
			@Override
			public int compare(Integer o1, Integer o2) {
				System.out.println("Comparing " + o1 + " vs " + o2);
				return (int) Math.signum(o1-o2);
			}
		}).collect(Collectors.toList()));
	}
}
