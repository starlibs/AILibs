package de.upb.crc901.mlplan.metamining.databaseconnection;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.SupervisedFilterSelector;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentNotFoundException;
import jaicore.basic.SQLAdapter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;

/**
 * A worker that gets a range of rows from a database with entries containing
 * String representations of MLPipelines. These representations are converted
 * back to ComponentInstances by the thread. Currently only pipelines that
 * exclusively contain elements from the autoweka-all configuration can be
 * parsed. All the setters have to used before the thread is run.
 * 
 * @author Helena Graf
 *
 */
public class ComponentInstanceDatabaseGetter extends Thread {

	private List<ComponentInstance> pipelines;
	private List<HashMap<String, List<Double>>> pipelinePerformances;

	private int offset;
	private int limit;
	private SQLAdapter adapter;
	private MLPipelineComponentInstanceFactory factory;
	private boolean finishedSuccessfully = false;

	@Override
	public void run() {
		// TODO adapt this query so that it includes the parameters of preprocessors
		// String query = "SELECT pipeline, GROUP_CONCAT( CONCAT (dataset, ',',
		// errorRate) SEPARATOR ';') AS results FROM (SELECT pipeline, errorRate, run_id
		// FROM evaluations) AS A INNER JOIN (SELECT run_id, dataset FROM runs WHERE
		// dataset NOT LIKE \"testrsc/all/autoUnivau6750.arff\") AS B ON A.run_id =
		// B.run_id WHERE pipeline NOT LIKE
		// '%jaicore.ml.classification.multiclass.reduction.MCTreeNodeReD%' GROUP BY
		// pipeline ORDER BY pipeline LIMIT "
		/// + limit + " OFFSET " + offset;
		String query = "SELECT searcher, evaluator, classifier, GROUP_CONCAT( CONCAT (dataset_id, ':', dataset_origin, ',', error_rate) SEPARATOR ';') AS results FROM basePipelineEvals GROUP BY searcher, evaluator, classifier ORDER BY searcher, evaluator, classifier LIMIT "
				+ limit + " OFFSET " + offset;

		try {
			pipelines = new ArrayList<ComponentInstance>(limit);
			pipelinePerformances = new ArrayList<HashMap<String, List<Double>>>(limit);

			ResultSet resultSet = adapter.getResultsOfQuery(query);
			System.out.println(
					"ComponentInstanceDatabaseGetter: Thread " + this.getId() + " got pipelines from data base.");

			while (resultSet.next()) {
				try {
					// Get pipeline
					ComponentInstance CI;
					if (resultSet.getString("searcher") != null && resultSet.getString("evaluator") != null) {
						CI = factory.convertToComponentInstance(
								new MLPipeline(ASSearch.forName(resultSet.getString("searcher"), null),
										ASEvaluation.forName(resultSet.getString("evaluator"), null),
										AbstractClassifier.forName(resultSet.getString("classifier"), null)));
					} else {
						CI = factory
								.convertToComponentInstance(new MLPipeline(new ArrayList<SupervisedFilterSelector>(),
										AbstractClassifier.forName(resultSet.getString("classifier"), null)));
					}

					// Get pipeline performance values (errorRate,dataset array)
					String[] results = resultSet.getString("results").split(";");
					HashMap<String, List<Double>> datasetPerformances = new HashMap<String, List<Double>>();
					for (int j = 0; j < results.length; j++) {
						String[] errorRate_performance = results[j].split(",");
						if (!datasetPerformances.containsKey(errorRate_performance[0])) {
							datasetPerformances.put(errorRate_performance[0], new ArrayList<Double>());
						}

						if (errorRate_performance.length > 1) {
							datasetPerformances.get(errorRate_performance[0])
									.add(Double.parseDouble(errorRate_performance[1]));
						}

					}

					pipelines.add(CI);
					pipelinePerformances.add(datasetPerformances);
				} catch (ComponentNotFoundException e) {
					// Could not convert pipeline - component not in loaded configuration
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}

		finishedSuccessfully = true;
	}

	/**
	 * Set the row of the table at which this thread should start.
	 * 
	 * @param offset
	 *            The offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Set the limit of how many rows this thread shall get.
	 * 
	 * @param limit
	 *            The limit
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Set the adapter this thread uses to get the data from the data base. It has
	 * to have an open connection.
	 * 
	 * @param adapter
	 *            The used adapter
	 */
	public void setAdapter(SQLAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Set the factory used to convert the MLPipelines instantiated from the String
	 * representation in the database to ComponentInstances.
	 * 
	 * @param factory
	 *            The converter factory
	 */
	public void setFactory(MLPipelineComponentInstanceFactory factory) {
		this.factory = factory;
	}

	/**
	 * Get the converted pipelines the thread collected from the data base.
	 * 
	 * @return The list of converted pipelines
	 */
	public List<ComponentInstance> getPipelines() {
		return pipelines;
	}

	/**
	 * Get the performances of the pipelines on the database for which they are
	 * values present.
	 * 
	 * @return A list of mappings of data set ids to a list of performance values in
	 *         the same order as the returned list of pipelines
	 */
	public List<HashMap<String, List<Double>>> getPipelinePerformances() {
		return pipelinePerformances;
	}

	/**
	 * Find out whether the thread finished successfully or aborted with an error.
	 * 
	 * @return Whether the execution of the thread was successful
	 */
	public boolean isFinishedSuccessfully() {
		return finishedSuccessfully;
	}

}
