package jaicore.basic.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;

public class CartesianProductComputer<T> extends AAlgorithm<List<Collection<T>>, List<List<T>>> {

	private final List<T> itemsOfFolder;
	private final CartesianProductComputer<T> subsolutionComputer;
	
	private int currentIndex = 0;
	private List<T> currentSubSolutionTuple;
	

	public CartesianProductComputer(List<Collection<T>> collections) {
		this(collections, 0);
	}

	private CartesianProductComputer(List<Collection<T>> collections, int folder) {
		super(collections);
		this.itemsOfFolder = collections.get(folder) instanceof List ? (List<T>)collections.get(folder) : new ArrayList<>(collections.get(folder));
		if (folder < getInput().size() - 1) {
			subsolutionComputer = new CartesianProductComputer<>(getInput(), folder + 1);
		}
		else
			subsolutionComputer = null;
	}

	@Override
	public AlgorithmEvent nextWithException() {

		/* if there is just one fold, just return the (reformatted) set */
		if (subsolutionComputer == null) {
			if (currentIndex == itemsOfFolder.size())
				return terminate();
			List<T> tupleOfSize1 = new ArrayList<>();
			tupleOfSize1.add(itemsOfFolder.get(currentIndex++));
			return new TupleFoundEvent<>(tupleOfSize1);
		}

		/* otherwise work with solutions from the subsolution computer. If there is no current such tuple availble */
		if (currentSubSolutionTuple == null) {
			TupleFoundEvent<T> nextTupleEventOfSubSolver = subsolutionComputer.nextTupleEvent();
			if (nextTupleEventOfSubSolver == null) {
				return terminate();
			}
			currentSubSolutionTuple = nextTupleEventOfSubSolver.getTuple();
		}
		
		/* form new tuple with the current element */
		List<T> newTuple = new ArrayList<>();
		newTuple.add(itemsOfFolder.get(currentIndex ++));
		newTuple.addAll(currentSubSolutionTuple);
		
		/* if all items of this folder have been combined with the sub-solution, reset the sub-solution */
		if (currentIndex == itemsOfFolder.size()) {
			currentSubSolutionTuple = null;
			currentIndex = 0;
		}
		
		/* add the current value to th */
		return new TupleFoundEvent<>(newTuple);
	}

	@SuppressWarnings("unchecked")
	public TupleFoundEvent<T> nextTupleEvent() {
		AlgorithmEvent e = nextWithException();
		while (!(e instanceof TupleFoundEvent)) {
			if (e instanceof AlgorithmFinishedEvent)
				return null;
			e = nextWithException();
		}
		return (TupleFoundEvent<T>)e;
	}

	@Override
	public List<List<T>> call() {
		List<List<T>> product = new ArrayList<>();
		while (hasNext()) {
			TupleFoundEvent<T> nextEvent = nextTupleEvent();
			if (nextEvent == null)
				return product;
			product.add(nextEvent.getTuple());
			System.out.println(product.size());
		}
		return product;
	}
	
	public void reset() {
		this.currentIndex = 0;
		this.currentSubSolutionTuple = null;
		if (subsolutionComputer != null)
			this.subsolutionComputer.reset();
	}
}
