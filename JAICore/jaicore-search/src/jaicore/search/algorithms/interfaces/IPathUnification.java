package jaicore.search.algorithms.interfaces;

import java.util.List;
import java.util.Map;

public interface IPathUnification<T> {
	public List<T> getSubsumingKnownPathCompletion(Map<List<T>, List<T>> knownPathCompletions, List<T> path) throws InterruptedException;
}
