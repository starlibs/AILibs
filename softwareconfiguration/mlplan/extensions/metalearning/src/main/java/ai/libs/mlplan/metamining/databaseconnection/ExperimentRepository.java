package ai.libs.mlplan.metamining.databaseconnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.db.sql.SQLAdapter;
import ai.libs.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import dataHandling.mySQL.MetaDataDataBaseConnection;
import weka.core.Instances;

/**
 * Manages a connection to experiment data of pipelines on dataset in a data
 * base.
 *
 * @author Helena Graf
 *
 */
public class ExperimentRepository {

	private Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

	private SQLAdapter adapter;
	private String host;
	private String user;
	private String password;
	private MLPipelineComponentInstanceFactory factory;
	private int cpus;
	private String metaFeatureSetName;
	private String datasetSetName;
	private Integer limit;

	private List<HashMap<String, List<Double>>> pipelinePerformances = new ArrayList<>();
	private MetaDataDataBaseConnection metaDataBaseConnection;

	public ExperimentRepository(final String host, final String user, final String password, final MLPipelineComponentInstanceFactory factory, final int cpus, final String metaFeatureSetName, final String datasetSetName) {
		this.user = user;
		this.password = password;
		this.host = host;
		this.factory = factory;
		this.cpus = cpus;
		this.metaDataBaseConnection = new MetaDataDataBaseConnection(host, user, password, "hgraf");
		this.metaFeatureSetName = metaFeatureSetName;
		this.datasetSetName = datasetSetName;
	}

	public List<ComponentInstance> getDistinctPipelines() throws SQLException, InterruptedException {
		this.connect();
		String query = "SELECT (COUNT(DISTINCT searcher, evaluator, classifier)) FROM basePipelineEvals";
		List<IKVStore> resultSet = this.adapter.getResultsOfQuery(query);
		int distinctPipelineCount = resultSet.get(0).getAsInt("(COUNT(DISTINCT searcher, evaluator, classifier))");
		distinctPipelineCount = this.limit == null ? distinctPipelineCount : this.limit;
		this.logger.info("Getting {} distinct pipelines.", distinctPipelineCount);

		int chunkSize = Math.floorDiv(distinctPipelineCount, this.cpus);
		int lastchunkSize = distinctPipelineCount - (chunkSize * (this.cpus - 1));

		this.logger.debug("ExperimentRepository: Allocate Getter-Threads.");
		ComponentInstanceDatabaseGetter[] threads = new ComponentInstanceDatabaseGetter[this.cpus];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ComponentInstanceDatabaseGetter();
			threads[i].setAdapter(this.adapter);
			threads[i].setOffset(i * chunkSize);
			threads[i].setLimit(i == (threads.length - 1) ? lastchunkSize : chunkSize);
			threads[i].setFactory(this.factory);
			threads[i].start();
		}

		List<ComponentInstance> pipelines = new ArrayList<>();
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
			pipelines.addAll(threads[i].getPipelines());
			this.pipelinePerformances.addAll(threads[i].getPipelinePerformances());
		}

		boolean allSuccessful = true;
		for (int i = 0; i < threads.length; i++) {
			this.logger.debug("Thread {} finished succuesfully: {}", threads[i].getId(), threads[i].isFinishedSuccessfully());
			if (!threads[i].isFinishedSuccessfully()) {
				allSuccessful = false;
			}
		}
		if (!allSuccessful) {
			this.logger.error("Not all threads finished the download successfully!");
		} else {
			this.logger.info("All threads finished successfully.");
		}

		this.disconnect();

		return pipelines;
	}

	public Instances getDatasetCahracterizations() throws SQLException {
		// get data set characterizations
		this.logger.info("Downloading dataset characterizations.");
		Instances metaData = this.metaDataBaseConnection.getMetaDataSetForDataSetSet(this.datasetSetName, this.metaFeatureSetName);
		metaData.deleteAttributeAt(0);
		return metaData;
	}

	/**
	 * Gets all the pipeline results for the distinct pipelines from
	 * {@link #getDistinctPipelines()}, thus has to be called after that method.
	 *
	 * @return The results of pipelines on datasets: rows: data sets, columns:
	 *         pipelines, entries: array of results of pipeline on data set
	 * @throws SQLException
	 *             If something goes wrong while connecting to the database
	 */
	public double[][][] getPipelineResultsOnDatasets() throws SQLException {
		this.logger.info("Downloading pipeline results for datasets.");

		// Get order of datasets
		List<String> datasets = this.metaDataBaseConnection.getMembersOfDatasetSet(this.datasetSetName);

		// Organize results into matrix
		double[][][] results = new double[datasets.size()][this.pipelinePerformances.size()][];

		for (int j = 0; j < datasets.size(); j++) {
			String dataset = datasets.get(j);
			for (int i = 0; i < this.pipelinePerformances.size(); i++) {
				// Does the pipeline have a result for the dataset
				List<Double> datasetResults = this.pipelinePerformances.get(i).get(dataset);
				if (datasetResults != null && !datasetResults.isEmpty()) {
					results[j][i] = datasetResults.stream().mapToDouble(value -> value).toArray();
				}
			}
		}

		return results;
	}

	private void connect() {
		this.adapter = new SQLAdapter(this.host, this.user, this.password, "pgotfml_hgraf");
	}

	private void disconnect() {
		this.adapter.close();
	}

	public void setLimit(final Integer limit) {
		this.limit = limit;
	}
}
