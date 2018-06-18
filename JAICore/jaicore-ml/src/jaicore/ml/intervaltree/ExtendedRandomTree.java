package jaicore.ml.intervaltree;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map.Entry;

import jaicore.ml.core.Interval;
import weka.classifiers.trees.RandomTree;

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
		//
		ArrayList<Integer> list = new ArrayList<>();
		while(stack.peek() != null) {
			Entry<Interval[], Tree> toProcess = stack.pop();
			Tree nextTree = toProcess.getValue();
			double threshold = nextTree.getSplitPoint();
			int attribute = nextTree.getAttribute();
		}
	}
	
	
	
	/** 
	 * Helper class to access member variables of the Tree class.
	 * @author elppa
	 *
	 */
	protected class InnerTree extends Tree{
		
	}
	
}
