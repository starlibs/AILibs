package ai.libs.jaicore.experiments;
public abstract class AExperimentDecoder<I, A> implements IExperimentDecoder<I, A> {
	private final IExperimentSetConfig config;
	private final ExperimentSetAnalyzer analyzer;

	public AExperimentDecoder(final IExperimentSetConfig config) {
		super();
		this.config = config;
		this.analyzer = new ExperimentSetAnalyzer(config);
	}

	public void checkThatAllKeyFieldsInExperimentAreDefined(final Experiment experiment) {
		if (!this.analyzer.isExperimentInLineWithSetup(experiment)) {
			throw new IllegalArgumentException("Experiment " + experiment.getValuesOfKeyFields() + " is not in line with the experiment set configuration!");
		}
	}

	public IExperimentSetConfig getConfig() {
		return this.config;
	}
}
