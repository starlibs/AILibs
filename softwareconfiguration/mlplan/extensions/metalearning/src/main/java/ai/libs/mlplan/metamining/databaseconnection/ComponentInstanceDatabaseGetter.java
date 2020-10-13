package ai.libs.mlplan.metamining.databaseconnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.exceptions.ComponentNotFoundException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import ai.libs.mlplan.weka.weka.MLPipelineComponentInstanceFactory;
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

	private Logger logger = LoggerFactory.getLogger(ComponentInstanceDatabaseGetter.class);

	private List<ComponentInstance> pipelines;
	private List<HashMap<String, List<Double>>> pipelinePerformances;

	private int offset;
	private int limit;
	private IDatabaseAdapter adapter;
	private MLPipelineComponentInstanceFactory factory;
	private boolean finishedSuccessfully = false;

	@Override
	public void run() {
		String query = "SELECT searcher, evaluator, classifier, GROUP_CONCAT( CONCAT (dataset_id, ':', dataset_origin, ',', error_rate) SEPARATOR ';') AS results FROM basePipelineEvals GROUP BY searcher, evaluator, classifier ORDER BY searcher, evaluator, classifier LIMIT "
				+ this.limit + " OFFSET " + this.offset;

		try {
			this.pipelines = new ArrayList<>(this.limit);
			this.pipelinePerformances = new ArrayList<>(this.limit);

			List<IKVStore> resultSet = this.adapter.getResultsOfQuery(query);
			this.logger.debug("ComponentInstanceDatabaseGetter: Thread {} got pipelines from data base.", this.getId());

			for (IKVStore store : resultSet) {
				this.next(store);
			}

		} catch (Exception e1) {
			this.logger.error("Thread {} could not finish getting all pipelines. Cause: {}", this.getId(), e1.getMessage());
			return;
		}

		this.finishedSuccessfully = true;
	}

	private void next(final IKVStore resultSet) throws Exception {
		try {
			// Get pipeline
			ComponentInstance ci;
			if (resultSet.getAsString("searcher") != null && resultSet.getAsString("evaluator") != null) {
				ci = this.factory.convertToComponentInstance(
						new MLPipeline(ASSearch.forName(resultSet.getAsString("searcher"), null), ASEvaluation.forName(resultSet.getAsString("evaluator"), null), AbstractClassifier.forName(resultSet.getAsString("classifier"), null)));
			} else {
				ci = this.factory.convertToComponentInstance(new MLPipeline(new ArrayList<>(), AbstractClassifier.forName(resultSet.getAsString("classifier"), null)));
			}

			// Get pipeline performance values (errorRate,dataset array)
			String[] results = resultSet.getAsString("results").split(";");
			HashMap<String, List<Double>> datasetPerformances = new HashMap<>();
			for (int j = 0; j < results.length; j++) {
				String[] errorRatePerformance = results[j].split(",");
				if (!datasetPerformances.containsKey(errorRatePerformance[0])) {
					datasetPerformances.put(errorRatePerformance[0], new ArrayList<>());
				}

				if (errorRatePerformance.length > 1) {
					datasetPerformances.get(errorRatePerformance[0]).add(Double.parseDouble(errorRatePerformance[1]));
				}

			}

			this.pipelines.add(ci);
			this.pipelinePerformances.add(datasetPerformances);
		} catch (ComponentNotFoundException e) {
			// Could not convert pipeline - component not in loaded configuration
			this.logger.warn("Could not convert component due to {}", LoggerUtil.getExceptionInfo(e));
		}
	}

	/**
	 * Set the row of the table at which this thread should start.
	 *
	 * @param offset
	 *            The offset
	 */
	public void setOffset(final int offset) {
		this.offset = offset;
	}

	/**
	 * Set the limit of how many rows this thread shall get.
	 *
	 * @param limit
	 *            The limit
	 */
	public void setLimit(final int limit) {
		this.limit = limit;
	}

	/**
	 * Set the adapter this thread uses to get the data from the data base. It has
	 * to have an open connection.
	 *
	 * @param adapter
	 *            The used adapter
	 */
	public void setAdapter(final IDatabaseAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Set the factory used to convert the MLPipelines instantiated from the String
	 * representation in the database to ComponentInstances.
	 *
	 * @param factory
	 *            The converter factory
	 */
	public void setFactory(final MLPipelineComponentInstanceFactory factory) {
		this.factory = factory;
	}

	/**
	 * Get the converted pipelines the thread collected from the data base.
	 *
	 * @return The list of converted pipelines
	 */
	public List<ComponentInstance> getPipelines() {
		return this.pipelines;
	}

	/**
	 * Get the performances of the pipelines on the database for which they are
	 * values present.
	 *
	 * @return A list of mappings of data set ids to a list of performance values in
	 *         the same order as the returned list of pipelines
	 */
	public List<HashMap<String, List<Double>>> getPipelinePerformances() {
		return this.pipelinePerformances;
	}

	/**
	 * Find out whether the thread finished successfully or aborted with an error.
	 *
	 * @return Whether the execution of the thread was successful
	 */
	public boolean isFinishedSuccessfully() {
		return this.finishedSuccessfully;
	}

}
