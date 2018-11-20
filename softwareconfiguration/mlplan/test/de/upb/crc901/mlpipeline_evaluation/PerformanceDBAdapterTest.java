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
		SQLAdapter adapter = new SQLAdapter("host", "user", "password", "database");
		PerformanceDBAdapter pAdapter = new PerformanceDBAdapter(adapter, "performance_cache");
		try {
			ComponentLoader loader = new ComponentLoader(
					new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"));
			MLPipelineComponentInstanceFactory factory = new MLPipelineComponentInstanceFactory(loader.getComponents());
			ComponentInstance composition1 = factory.convertToComponentInstance(
					new MLPipeline(new Ranker(), new OneRAttributeEval(), new RandomForest()));

			ReproducibleInstances reproducibleInstances1 = ReproducibleInstances.fromOpenML("40983",
					"4350e421cdc16404033ef1812ea38c01");
			double score = Math.PI / 10.0;

			// Store the first sample
			pAdapter.store(composition1, reproducibleInstances1, score);

			ComponentInstance composition2 = factory.convertToComponentInstance(
					new MLPipeline(new Ranker(), new OneRAttributeEval(), new MultilayerPerceptron()));
			ReproducibleInstances reproducibleInstances2 = ReproducibleInstances.fromOpenML("181",
					"4350e421cdc16404033ef1812ea38c01");

//			Optional<Double> shouldntExist1 = pAdapter.exists(composition1, reproducibleInstances2);
//			Optional<Double> shouldntExist2 = pAdapter.exists(composition2, reproducibleInstances1);
//			Optional<Double> shouldntExist3 = pAdapter.exists(composition2, reproducibleInstances2);
			Optional<Double> shouldExist1 = pAdapter.exists(composition1, reproducibleInstances1);

			// ObjectMapper mapper1 = new ObjectMapper();
			//
			// String cs1 = mapper1.writeValueAsString(composition1);
			// String cs2 = mapper1.writeValueAsString(composition2);
			//
			// ObjectMapper mapper2 = new ObjectMapper();
			//
			// String cs3 = mapper2.writeValueAsString(composition1);
			// String cs4 = mapper2.writeValueAsString(composition2);
			//
			// assertEquals(cs1, cs2);
			// assertEquals(cs2, cs3);
			// assertEquals(cs3, cs4);

			// assertFalse(shouldntExist1.isPresent());
			// assertFalse(shouldntExist2.isPresent());
			// assertFalse(shouldntExist3.isPresent());
			 assertTrue(shouldExist1.isPresent());

		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
