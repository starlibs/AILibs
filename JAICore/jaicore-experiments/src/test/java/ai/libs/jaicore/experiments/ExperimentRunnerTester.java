package ai.libs.jaicore.experiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

import ai.libs.jaicore.basic.IDatabaseConfig;
import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.experiments.exceptions.IllegalExperimentSetupException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
			ObjectNode castedNode = (ObjectNode) node;
			return castedNode.size() == 1 && castedNode.has("number") && castedNode.get("number").asInt() <= 2;
		}
	}

	private IDatabaseConfig conf = (IDatabaseConfig) ConfigFactory.create(IDatabaseConfig.class).loadPropertiesFromFile(new File("testrsc/dbconfig.properties"));
	private IExperimentDatabaseHandle handle = new ExperimenterMySQLHandle(new SQLAdapter(this.conf.getDBHost(), this.conf.getDBUsername(), this.conf.getDBPassword(), this.conf.getDBDatabaseName(), this.conf.getDBSSL()), "resulttable");
	private IExperimentSetConfig config = ConfigCache.getOrCreate(IExperimentTesterConfig.class);

	@Test
	public void test1ThatAllExperimentsAreCreated() throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException, ExperimentAlreadyExistsInDatabaseException {

		int n = 6;

		/* check that running the experiments works */
		ExperimentRunner runner = new ExperimentRunner(this.config, this, this.handle);
		Collection<ExperimentDBEntry> experimentDBEntries = runner.createExperiments();
		assertEquals(n, experimentDBEntries.size());

		Collection<ExperimentDBEntry> experiments = this.handle.getAllExperiments();
		assertEquals(n, experiments.size());
	}

	@Test
	public void test2ThatAllExperimentsAreConducted() throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException {

		/* check that running the experiments works */
		ExperimentRunner runner = new ExperimentRunner(this.config, this, this.handle);
		runner.randomlyConductExperiments();
		Collection<ExperimentDBEntry> conductedExperiments = this.handle.getConductedExperiments();
		assertEquals(6, conductedExperiments.size());

		/* check results for every experiment */
		for (ExperimentDBEntry entry : conductedExperiments) {
			assertEquals(this.computeExperimentValue(entry.getExperiment()), entry.getExperiment().getValuesOfResultFields().get("C"));
		}
	}

	@Test
	public void test3ThatErasureOfSingleExperimentsWorks() throws ExperimentDBInteractionFailedException {

		/* erase all experiments */
		this.handle.setup(this.config);
		int n = this.handle.getConductedExperiments().size();
		assertEquals(6, n); // check that all experiments are still there
		for (ExperimentDBEntry entry : this.handle.getConductedExperiments()) {
			this.handle.deleteExperiment(entry);
			assertEquals(n - 1, this.handle.getConductedExperiments().size());
			n--;
		}
	}

	@Test
	public void test4Deletion() throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {
		this.handle.setup(this.config);
		this.handle.deleteDatabase();
		boolean correctExceptionObserved = false;
		try {
			this.handle.createAndGetExperiment(new Experiment(0, 0, new HashMap<>()));
		}
		catch (ExperimentDBInteractionFailedException e) {
			correctExceptionObserved = (e.getCause() instanceof MySQLSyntaxErrorException) && ((MySQLSyntaxErrorException)e.getCause()).getMessage().equals("Table '" + this.conf.getDBDatabaseName() + ".resulttable' doesn't exist");
		}
		assertTrue(correctExceptionObserved);
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
		Map<String, Object> results = new HashMap<>();
		results.put("C", this.computeExperimentValue(experimentEntry.getExperiment()));
		processor.processResults(results);
	}

	private String computeExperimentValue(final Experiment exp) {
		return exp.getValuesOfKeyFields().get("A") + "/" + exp.getValuesOfKeyFields().get("B");
	}
}
