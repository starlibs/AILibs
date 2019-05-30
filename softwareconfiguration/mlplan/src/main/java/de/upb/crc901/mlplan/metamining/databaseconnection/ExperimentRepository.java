package de.upb.crc901.mlplan.metamining.databaseconnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dataHandling.mySQL.MetaDataDataBaseConnection;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import hasco.model.ComponentInstance;
import jaicore.basic.SQLAdapter;
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

	public ExperimentRepository(String host, String user, String password, MLPipelineComponentInstanceFactory factory,
			int cpus, String metaFeatureSetName, String datasetSetName) {
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
		connect();
		String query = "SELECT (COUNT(DISTINCT searcher, evaluator, classifier)) FROM basePipelineEvals";
		ResultSet resultSet = adapter.getResultsOfQuery(query);
		resultSet.next();
		int distinctPipelineCount = resultSet.getInt("(COUNT(DISTINCT searcher, evaluator, classifier))");
		distinctPipelineCount = limit == null ? distinctPipelineCount : limit;
		logger.info("Getting {} distinct pipelines.", distinctPipelineCount);

		int chunkSize = Math.floorDiv(distinctPipelineCount, cpus);
		int lastchunkSize = distinctPipelineCount - (chunkSize * (cpus - 1));

		logger.debug("ExperimentRepository: Allocate Getter-Threads.");
		ComponentInstanceDatabaseGetter[] threads = new ComponentInstanceDatabaseGetter[cpus];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ComponentInstanceDatabaseGetter();
			threads[i].setAdapter(adapter);
			threads[i].setOffset(i * chunkSize);
			threads[i].setLimit(i == (threads.length - 1) ? lastchunkSize : chunkSize);
			threads[i].setFactory(factory);
			threads[i].start();
		}

		List<ComponentInstance> pipelines = new ArrayList<>();
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
			pipelines.addAll(threads[i].getPipelines());
			pipelinePerformances.addAll(threads[i].getPipelinePerformances());
		}

		boolean allSuccessful = true;
		for (int i = 0; i < threads.length; i++) {
			logger.debug("Thread {} finished succuesfully: {}",threads[i].getId(),threads[i].isFinishedSuccessfully());
			if (!threads[i].isFinishedSuccessfully()) {
				allSuccessful = false;
			}
		}
		if (!allSuccessful) {
			logger.error("Not all threads finished the download successfully!");
		} else {
			logger.info("All threads finished successfully.");
		}
		
		disconnect();

		return pipelines;
	}

	public Instances getDatasetCahracterizations() throws SQLException {
		// get data set characterizations
		logger.info("Downloading dataset characterizations.");
		Instances metaData = metaDataBaseConnection.getMetaDataSetForDataSetSet(datasetSetName, metaFeatureSetName);
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
		logger.info("Downloading pipeline results for datasets.");

		// Get order of datasets
		List<String> datasets = metaDataBaseConnection.getMembersOfDatasetSet(datasetSetName);

		// Organize results into matrix
		double[][][] results = new double[datasets.size()][pipelinePerformances.size()][];

		for (int j = 0; j < datasets.size(); j++) {
			String dataset = datasets.get(j);
			for (int i = 0; i < pipelinePerformances.size(); i++) {
				// Does the pipeline have a result for the dataset
				List<Double> datasetResults = pipelinePerformances.get(i).get(dataset);
				if (datasetResults != null && !datasetResults.isEmpty()) {
					results[j][i] = datasetResults.stream().mapToDouble(value -> value).toArray();
				}
			}
		}

		return results;
	}

	private void connect() {
		adapter = new SQLAdapter(host, user, password, "pgotfml_hgraf");
	}

	private void disconnect() {
		adapter.close();
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
