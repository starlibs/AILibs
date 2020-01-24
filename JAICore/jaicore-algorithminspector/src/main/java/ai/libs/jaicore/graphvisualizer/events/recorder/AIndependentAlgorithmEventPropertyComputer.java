package ai.libs.jaicore.graphvisualizer.events.recorder;

import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;

public abstract class AIndependentAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	@Override
	public List<AlgorithmEventPropertyComputer> getRequiredPropertyComputers() {
		return Arrays.asList();
	}

	@Override
	public void overwriteRequiredPropertyComputer(final AlgorithmEventPropertyComputer computer) {
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does not rely on other property computers, so overwriting makes no sense.");
	}
}
