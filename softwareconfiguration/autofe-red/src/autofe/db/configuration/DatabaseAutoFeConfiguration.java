package autofe.db.configuration;

public class DatabaseAutoFeConfiguration {

	private final int randomCompletionPathLength;

	private final String evaluationFunction;

	private final long seed;

	private final int timeoutInMs;

	public DatabaseAutoFeConfiguration(int randomCompletionPathLength, String evaluationFunction, long seed,
			int timeoutInMs) {
		this.randomCompletionPathLength = randomCompletionPathLength;
		this.evaluationFunction = evaluationFunction;
		this.seed = seed;
		this.timeoutInMs = timeoutInMs;
	}

	public int getRandomCompletionPathLength() {
		return randomCompletionPathLength;
	}

	public String getEvaluationFunction() {
		return evaluationFunction;
	}

	public long getSeed() {
		return seed;
	}

	public int getTimeoutInMs() {
		return timeoutInMs;
	}

	@Override
	public String toString() {
		return "DatabaseAutoFeConfiguration [randomCompletionPathLength=" + randomCompletionPathLength
				+ ", evaluationFunction=" + evaluationFunction + ", seed=" + seed + ", timeoutInMs=" + timeoutInMs
				+ "]";
	}

}
