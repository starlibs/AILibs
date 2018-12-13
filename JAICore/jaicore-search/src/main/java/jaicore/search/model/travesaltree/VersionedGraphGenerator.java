package jaicore.search.model.travesaltree;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * Class which wraps up a normal GraphGenerator and is adding a id to every node
 * @author jkoepe
 *
 */
public class VersionedGraphGenerator<T,A> implements VersionedGraphGeneratorInterface<VersionedDomainNode<T>,A> {

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
	public SingleRootGenerator<VersionedDomainNode<T>> getRootGenerator() {
		return () -> {
			SingleRootGenerator<T> rootGenerator = (SingleRootGenerator<T>) gen.getRootGenerator();
			T root = (T) rootGenerator.getRoot();
			return new VersionedDomainNode<T>(root, this.getNextID());
		};
	}
	
	public SingleRootGenerator<VersionedDomainNode<T>> getSingleRootGenerator(){
		return () -> {
			SingleRootGenerator<T> rootGenerator = (SingleRootGenerator<T>) gen.getRootGenerator();
			T root = (T) rootGenerator.getRoot();
			return new VersionedDomainNode<T>(root, this.getNextID());
		};
	}
	
	public MultipleRootGenerator<VersionedDomainNode<T>> getMultipleRootGenerator(){
		return () -> {
			MultipleRootGenerator<T> rootGenerator = (MultipleRootGenerator<T>) gen.getRootGenerator();
			Collection<VersionedDomainNode<T>> vRoots = new ArrayList<VersionedDomainNode<T>>();
			Collection<T> roots = rootGenerator.getRoots();
			
			roots.stream().forEach(
					n -> vRoots.add(new VersionedDomainNode<T>(n, this.getNextID()))
					);			
			return (Collection<VersionedDomainNode<T>>) vRoots;
		};
	}


	@Override
	public SuccessorGenerator<VersionedDomainNode<T>, A> getSuccessorGenerator() {
		return nodeToExpand ->{
			SuccessorGenerator<T,A> successorGenerator = (SuccessorGenerator<T, A>) gen.getSuccessorGenerator();
			Collection<NodeExpansionDescription<T,A>> successorDescriptions = successorGenerator.generateSuccessors(nodeToExpand.getNode());
			
			List<NodeExpansionDescription<VersionedDomainNode<T>,A>> versionedDescriptions = new ArrayList<>();
			
			successorDescriptions.stream().forEach(description->
						versionedDescriptions.add(new NodeExpansionDescription<>(nodeToExpand, new VersionedDomainNode<>(description.getTo(), this.getNextID()), description.getAction(), description.getTypeOfToNode()))
					);
			return versionedDescriptions;
		};
	}



	@Override
	public NodeGoalTester<VersionedDomainNode<T>> getGoalTester() {
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
	public NodeGoalTester<VersionedDomainNode<T>> getNodeGoalTester(){
		return n -> {
			NodeGoalTester<T> goalTester = (NodeGoalTester<T>) gen.getGoalTester();
			return goalTester.isGoal(n.getNode());
		};
	}
	
	
	/**
	 * Method which redirects the pathgoaltester from versioned<T> nodes to simple t nodes.
	 * This method does currently not work as it is not implemented to extract the path from a versioned node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PathGoalTester<VersionedDomainNode<T>> getPathGoalTester(){
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
