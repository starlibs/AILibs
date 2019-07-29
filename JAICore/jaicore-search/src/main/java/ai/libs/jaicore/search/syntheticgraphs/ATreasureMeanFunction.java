package ai.libs.jaicore.search.syntheticgraphs;

import java.util.function.Function;

public abstract class ATreasureMeanFunction implements Function<Long, Double> {

	private final long totalNumberOfIslands;
	private final long numberOfTreasures;

	public ATreasureMeanFunction(final long totalNumberOfIslands, final long numberOfTreasures) {
		super();
		this.totalNumberOfIslands = totalNumberOfIslands;
		this.numberOfTreasures = numberOfTreasures;
	}

	public long getTotalNumberOfIslands() {
		return this.totalNumberOfIslands;
	}

	public long getNumberOfTreasures() {
		return this.numberOfTreasures;
	}
}
