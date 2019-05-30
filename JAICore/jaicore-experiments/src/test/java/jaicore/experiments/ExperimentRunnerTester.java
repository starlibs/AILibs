package jaicore.experiments;

import java.io.IOException;

import org.aeonbits.owner.ConfigCache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.basic.SQLAdapter;
import jaicore.experiments.databasehandle.ExperimenterSQLHandle;
import jaicore.experiments.exceptions.ExperimentEvaluationFailedException;

public class ExperimentRunnerTester implements IExperimentSetEvaluator {

	public static class Generator implements IExperimentJSONKeyGenerator {

		@Override
		public int getNumberOfValues() {
			return 2;
		}

		@Override
		public ObjectNode getValue(final int i) {
			ObjectMapper om = new ObjectMapper();
			ObjectNode node = om.createObjectNode();
			node.put("number", i);
			return node;
		}

		@Override
		public boolean isValueValid(final String value) {
			ObjectMapper om = new ObjectMapper();
			JsonNode node;
			try {
				node = om.readTree(value);
			} catch (IOException e) {
				return false;
			}
			if (!(node instanceof ObjectNode)) {
				return false;
			}
			ObjectNode castedNode = (ObjectNode)node;
			return castedNode.size() == 1 && castedNode.has("number") && castedNode.get("number").asInt() <= 2;
		}
	}

	public static void main(final String[] args) throws Exception {
		//		IExperimentDatabaseHandle handle = new ExperimenterFileDBHandle(new File("testrsc/experiments.db"));
		IExperimentDatabaseHandle handle = new ExperimenterSQLHandle(new SQLAdapter("isys-db.cs.upb.de", "results", "Hallo333!", "test", false), "resulttable");
		IExperimentSetConfig config = ConfigCache.getOrCreate(IExperimentTesterConfig.class);
		IExperimentSetEvaluator evaluator = new ExperimentRunnerTester();

		ExperimentRunner runner = new ExperimentRunner(config, evaluator, handle);
		runner.randomlyConductExperiments(false);
	}


	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
		System.out.println("Running " + experimentEntry);
	}

}
