package jaicore.basic.algorithm.events;

public class AAlgorithmEvent implements AlgorithmEvent {
	
	private final long timestamp = System.currentTimeMillis();
	private final String algorithmId;

	public AAlgorithmEvent(String algorithmId) {
		super();
		this.algorithmId = algorithmId;
	}

	@Override
	public String getAlgorithmId() {
		return algorithmId;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
