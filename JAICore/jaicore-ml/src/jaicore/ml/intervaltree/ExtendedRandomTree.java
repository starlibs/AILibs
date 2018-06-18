package jaicore.ml.intervaltree;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map.Entry;

import jaicore.ml.core.Interval;
import weka.classifiers.trees.RandomTree;
import weka.core.Utils;

/**
 * Extension of a classic RandomTree to predict intervals.
 * @author mirkoj
 *
 */
public class ExtendedRandomTree extends RandomTree{
	
	
	
	public void predictInterval(Interval[] queriedInterval) {
		// the stack of elements that still have to be processed.
		Deque<Entry<Interval[], Tree>> stack = new ArrayDeque<>();
		// initially, the root and the queried interval
		stack.push(new AbstractMap.SimpleEntry<Interval[], Tree>(queriedInterval, m_Tree));
		
		//the list of all leaf values
		ArrayList<Double> list = new ArrayList<>();
		
		while(stack.peek() != null) {
			//pick the next node to process
			Entry<Interval[], Tree> toProcess = stack.pop();
			Tree nextTree = toProcess.getValue();
			double threshold = nextTree.getSplitPoint();
			int attribute = nextTree.getAttribute();
			Tree[] children = nextTree.getSuccessors();
			double [] classDistribution = nextTree.getClassDistribution();
			if (attribute == -1) {
				// node is a leaf
		        if (classDistribution == null) {
		            if (this.getAllowUnclassifiedInstances()) {
		              double[] result = new double[m_Info.numClasses()];
		              if (this.m_Info.classAttribute().isNumeric()) {
		                result[0] = Utils.missingValue();
		              }
		              //
		            } else {
		              //return null;
		            
		          }
			}
			}
		}
	}
	
	public void get() {
		Deque<Tree> toProcess = new ArrayDeque<>();
		toProcess.addFirst(m_Tree);
		while (toProcess.peek() != null) {
			Tree node = toProcess.pop();
			if (node.getAttribute() == -1) {
				toProcess.addAll(Arrays.asList(node.getSuccessors()));
			}else {
				System.out.println(Arrays.toString(node.getClassDistribution()));
			}
		}
		m_Tree.getDistribution();
	}
}
