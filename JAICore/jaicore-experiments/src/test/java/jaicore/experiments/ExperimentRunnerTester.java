package jaicore.experiments;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExperimentRunnerTester {

	public class Generator implements IExperimentJSONKeyGenerator {

		@Override
		public int getNumberOfValues() {
			return 0;
		}

		@Override
		public ObjectNode getValue(final int i) {
			return null;
		}

	}


	public static void main(final String[] args) {
		new ExperimentRunner((entry, processor) -> {});
	}

}
