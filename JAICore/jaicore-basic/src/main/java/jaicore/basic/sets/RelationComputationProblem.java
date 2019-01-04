package jaicore.basic.sets;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class RelationComputationProblem<T> {
	private final List<? extends Collection<T>> sets;
	private final Predicate<List<T>> prefixFilter; // decides for a tuple prefix whether any tuple being prefixed with it is part of the relation
	
	public RelationComputationProblem(List<? extends Collection<T>> sets) {
		this (sets, t -> true);
	}
	
	public RelationComputationProblem(List<? extends Collection<T>> sets, Predicate<List<T>> prefixFilter) {
		super();
		this.sets = sets;
		this.prefixFilter = prefixFilter;
	}
	
	public List<? extends Collection<T>> getSets() {
		return sets;
	}
	
	public Predicate<List<T>> getPrefixFilter() {
		return prefixFilter;
	}
}
