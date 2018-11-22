package de.upb.crc901.mlpipeline_evaluation;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Optional;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.SQLAdapter;
import jaicore.ml.cache.ReproducibleInstances;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.RandomForest;

public class PerformanceDBAdapterTest {
	@Test
	public void test() {
		SQLAdapter adapter = new SQLAdapter("isys-db.cs.upb.de", "pgotfml", "automl2018", "pgotfml_jmhansel");
        PerformanceDBAdapter pAdapter = new PerformanceDBAdapter(adapter, "performance_cache_test");
		try {
			ComponentLoader loader = new ComponentLoader(
					new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"));
			MLPipelineComponentInstanceFactory factory = new MLPipelineComponentInstanceFactory(loader.getComponents());
			ComponentInstance composition1 = factory.convertToComponentInstance(
					new MLPipeline(new Ranker(), new OneRAttributeEval(), new RandomForest()));

			ReproducibleInstances reproducibleInstances1 = ReproducibleInstances.fromOpenML("40983",
					"4350e421cdc16404033ef1812ea38c01");
			double score = Math.PI / 5.0;

			// Store the first sample
			pAdapter.store(composition1, reproducibleInstances1, score);

			// These should have no entry in the db, assuming it was empty when the test was
			// started
			ComponentInstance composition2 = factory.convertToComponentInstance(
					new MLPipeline(new Ranker(), new OneRAttributeEval(), new MultilayerPerceptron()));
			ReproducibleInstances reproducibleInstances2 = ReproducibleInstances.fromOpenML("181",
					"4350e421cdc16404033ef1812ea38c01");

			Optional<Double> shouldntExist1 = pAdapter.exists(composition1, reproducibleInstances2);
			Optional<Double> shouldntExist2 = pAdapter.exists(composition2, reproducibleInstances1);
			Optional<Double> shouldntExist3 = pAdapter.exists(composition2, reproducibleInstances2);
			Optional<Double> shouldExist1 = pAdapter.exists(composition1, reproducibleInstances1);

			assertFalse(shouldntExist1.isPresent());
			assertFalse(shouldntExist2.isPresent());
			assertFalse(shouldntExist3.isPresent());
			assertTrue(shouldExist1.isPresent());

			pAdapter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
