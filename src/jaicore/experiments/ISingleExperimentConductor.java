package jaicore.experiments;

import jaicore.basic.MySQLAdapter;

public interface ISingleExperimentConductor {
	
	public void conduct(ExperimentDBEntry experimentEntry, MySQLAdapter adapter, IExperimentIntermediateResultProcessor processor) throws Exception;
}
