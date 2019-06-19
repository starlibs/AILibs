package ai.libs.jaicore.experiments;

import java.io.File;
import java.io.IOException;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.IDatabaseConfig;
import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterSQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;

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
		IDatabaseConfig conf = (IDatabaseConfig)ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFile(new File("testrsc/dbconfig.properties"));
		IExperimentDatabaseHandle handle = new ExperimenterSQLHandle(new SQLAdapter(conf.getDBHost(), conf.getDBUsername(), conf.getDBPassword(), conf.getDBDatabaseName(), conf.getDBSSL()), "resulttable");
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
