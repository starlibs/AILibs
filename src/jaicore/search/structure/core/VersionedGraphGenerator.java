package jaicore.search.structure.core;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * Class which wraps up a normal GraphGenerator and is adding a id to every node
 * @author jkoepe
 *
 */
public class VersionedGraphGenerator<T,A> implements VersionedGraphGeneratorInterface<VersionedT<T>,A> {

	//variables 
	GraphGenerator<T, A> gen;
	boolean nodeNumbering;
	Random rnd;
	
	public VersionedGraphGenerator(GraphGenerator<T,A> gen) {
		this.gen = gen;
		nodeNumbering = true;
		rnd = new Random();
	}
	
	/**
	 * Retrieves the next id
	 * @return
	 * 		returns a unique id if numbering is enable, otherwise -1
	 */
	public int getNextID() {
		if(nodeNumbering)
			return rnd.nextInt(Integer.MAX_VALUE);
		else
			return -1;	
	}


	@Override
	public SingleRootGenerator<VersionedT<T>> getRootGenerator() {
		return () -> {
			SingleRootGenerator<T> rootGenerator = (SingleRootGenerator<T>) gen.getRootGenerator();
			T root = (T) rootGenerator.getRoot();
			return new VersionedT<T>(root, this.getNextID());
		};
	}
	
	public SingleRootGenerator<VersionedT<T>> getSingleRootGenerator(){
		return () -> {
			SingleRootGenerator rootGenerator = (SingleRootGenerator) gen.getRootGenerator();
			T root = (T) rootGenerator.getRoot();
			return new VersionedT<T>(root, this.getNextID());
		};
	}
	
	public MultipleRootGenerator<VersionedT<T>> getMultipleRootGenerator(){
		return () -> {
			MultipleRootGenerator rootGenerator = (MultipleRootGenerator) gen.getRootGenerator();
			Collection<VersionedT<T>> vRoots = new ArrayList<VersionedT<T>>();
			Collection roots = rootGenerator.getRoots();
			
			roots.stream().forEach(
					n -> vRoots.add(new VersionedT(n, this.getNextID()))
					);			
			return (Collection<VersionedT<T>>) vRoots;
		};
	}


	@Override
	public SuccessorGenerator<VersionedT<T>, A> getSuccessorGenerator() {
		return nodeToExpand ->{
			SuccessorGenerator<T,A> successorGenerator = (SuccessorGenerator<T, A>) gen.getSuccessorGenerator();
			Collection<NodeExpansionDescription<T,A>> successorDescriptions = successorGenerator.generateSuccessors(nodeToExpand.getNode());
			
			Collection<NodeExpansionDescription<VersionedT<T>,A>> versionedDescriptions = new ArrayList<>();
			
			successorDescriptions.stream().forEach(description->
						versionedDescriptions.add(new NodeExpansionDescription(nodeToExpand, new VersionedT(description.getTo(), this.getNextID()), description.getAction(), description.getTypeOfToNode()))
					);
			return versionedDescriptions;
		};
	}



	@Override
	public NodeGoalTester<VersionedT<T>> getGoalTester() {
		// TODO Auto-generated method stub
		return n -> {
			NodeGoalTester<T> goalTester = (NodeGoalTester<T>) gen.getGoalTester();
			return goalTester.isGoal(n.getNode());
		};
	}
	
	/**
	 * A method which redirects the NodeGoalTester from VersionedT<T> to T
	 * @return
	 */
	public NodeGoalTester<VersionedT<T>> getNodeGoalTester(){
		return n -> {
			NodeGoalTester<T> goalTester = (NodeGoalTester<T>) gen.getGoalTester();
			return goalTester.isGoal(n.getNode());
		};
	}
	
	
	/**
	 * Method which redirects the pathgoaltester from versioned<T> nodes to simple t nodes.
	 * This method does currently not work as it is not implemented to extract the path from a verioned node
	 * @return
	 */
	public PathGoalTester<VersionedT<T>> getPathGoalTester(){
		return n ->{
			PathGoalTester<T> goalTester = (PathGoalTester<T>)gen.getGoalTester();
			return goalTester.isGoal((List<T>) n);
		};
	}


	@Override
	public boolean isSelfContained() {
	
		return gen.isSelfContained();
	}


	@Override
	public void setNodeNumbering(boolean numbering) {
		this.nodeNumbering = numbering;
	}




}
