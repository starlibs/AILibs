package ai.libs.jaicore.graphvisualizer.events.recorder.property;

public interface PropertyProcessedAlgorithmEventSource {

	public void registerListener(PropertyProcessedAlgorithmEventListener algorithmEventListener);

	public void unregisterListener(PropertyProcessedAlgorithmEventListener algorithmEventListener);
}
