package jaicore.experiments;

import java.util.Map;

import jaicore.basic.MySQLAdapter;

public interface ISingleExperimentConductor {
	
	public Map<String,Object> conduct(ExperimentDBEntry experimentEntry, MySQLAdapter adapter) throws Exception;
}
