package de.upb.crc901.mlplan.metamining.databaseconnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataHandling.mySQL.MetaDataDataBaseConnection;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import hasco.model.ComponentInstance;
import jaicore.basic.SQLAdapter;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Manages a connection to experiment data of pipelines on dataset in a data
 * base.
 * 
 * @author Helena Graf
 *
 */
public class ExperimentRepository {
	private SQLAdapter adapter;
	private String host;
	private String user;
	private String password;
	private MLPipelineComponentInstanceFactory factory;
	private int CPUs;
	private String metaFeatureSetName;
	private String datasetSetName;
	private Integer limit;

	private List<HashMap<String, List<Double>>> pipelinePerformances = new ArrayList<HashMap<String, List<Double>>>();
	private MetaDataDataBaseConnection metaDataBaseConnection;

	public ExperimentRepository(String host, String user, String password, MLPipelineComponentInstanceFactory factory,
			int CPUs, String metaFeatureSetName, String datasetSetName) {
		this.user = user;
		this.password = password;
		this.host = host;
		this.factory = factory;
		this.CPUs = CPUs;
		this.metaDataBaseConnection = new MetaDataDataBaseConnection(host, user, password, "hgraf");
		this.metaFeatureSetName = metaFeatureSetName;
		this.datasetSetName = datasetSetName;
	}

	public List<ComponentInstance> getDistinctPipelines() throws Exception {		
		connect();
		System.out.println("ExperimentRepository: Get distinct pipelines.");
		// TODO also adapt query here to change of including hyperparameters of
		// preprocessors (STATEMENT BELOW DOESN'T HAVE ACTUAL COLUMN NAMES
		String query = "SELECT (COUNT(DISTINCT searcher, evaluator, classifier)) FROM basePipelineEvals";
		// String query = "SELECT COUNT(DISTINCT pipeline, searcherParameter,
		// evaluatorParameter) FROM evaluations";
		ResultSet resultSet = adapter.getResultsOfQuery(query);
		resultSet.next();
		int distinctPipelineCount = resultSet.getInt("(COUNT(DISTINCT searcher, evaluator, classifier))");
		distinctPipelineCount = limit == null ? distinctPipelineCount : limit;
		System.out.println(distinctPipelineCount + " distinct pipelines will be downloaded.");

		int chunkSize = Math.floorDiv(distinctPipelineCount, CPUs);
		int lastchunkSize = distinctPipelineCount - (chunkSize * (CPUs - 1));
		System.err.println(chunkSize * (CPUs - 1) + lastchunkSize == distinctPipelineCount);

		System.out.println("ExperimentRepository: Allocate Getter-Threads.");
		ComponentInstanceDatabaseGetter[] threads = new ComponentInstanceDatabaseGetter[CPUs];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ComponentInstanceDatabaseGetter();
			threads[i].setAdapter(adapter);
			threads[i].setOffset(i * chunkSize);
			threads[i].setLimit(i == (threads.length - 1) ? lastchunkSize : chunkSize);
			threads[i].setFactory(factory);
			threads[i].start();
		}

		List<ComponentInstance> pipelines = new ArrayList<ComponentInstance>();
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
			pipelines.addAll(threads[i].getPipelines());
			pipelinePerformances.addAll(threads[i].getPipelinePerformances());
		}

		for (int i = 0; i < threads.length; i++) {
			System.out.println(
					"Threads " + threads[i].getId() + " finished succesfully: " + threads[i].isFinishedSuccessfully());
		}

		disconnect();

		return pipelines;
	}

	public Instances getDatasetCahracterizations() throws SQLException {
		// get data set characterizations
		System.out.println("ExperimentRepository: Downloading dataset characterizations.");
		Instances metaData = metaDataBaseConnection.getMetaDataSetForDataSetSet(datasetSetName, metaFeatureSetName);
		//TODO remove this!!!!
		//metaData.add(new DenseInstance(metaData.numAttributes()));
		//metaData.add(new DenseInstance(metaData.numAttributes()));
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
		System.out.println("ExperimentRepository: Get inidividual pipeline results.");

		// Get order of datasets
		System.out.println(datasetSetName);
		List<String> datasets = metaDataBaseConnection.getMembersOfDatasetSet(datasetSetName);

		System.out.println(datasets);

		// Organize results into matrix
		double[][][] results = new double[datasets.size()][pipelinePerformances.size()][];

		for (int j = 0; j < datasets.size(); j++) {
			String dataset = datasets.get(j);
			for (int i = 0; i < pipelinePerformances.size(); i++) {
				// Does the pipeline have a result for the dataset
				List<Double> datasetResults = pipelinePerformances.get(i).get(dataset);
				if (datasetResults != null && datasetResults.size() > 0) {
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
