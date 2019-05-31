package autofe.experiments;

import java.io.File;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import weka.core.Instances;

public class BenchmarkRankExperimentEvaluator implements IExperimentSetEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRankExperimentEvaluator.class);

    private final IBenchmarkRankConfig config = ConfigCache.getOrCreate(IBenchmarkRankConfig.class);

    private static final int MAX_COUNT_VARIATIONS = 10;

    private final SQLAdapter adapter;

    public BenchmarkRankExperimentEvaluator(final SQLAdapter adapter) {
        super();
        this.adapter = adapter;
    }

    @Override
    public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
        String dataSetFolder = config.getDatasetFolder();
        if (dataSetFolder == null || !(new File(dataSetFolder).exists())) {
            throw new IllegalArgumentException("Data set folder must exist!");
        }

        Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();

        // Get benchmark function
        String benchmark = description.get("benchmark");
        Function<Instances, Double> benchmarkFunction = EvaluationUtils.getBenchmarkFunctionByName(benchmark);

        Map<String, Object> results = new HashMap<>();

        int seed = Integer.parseInt(description.get("seed"));
        String dataSet = description.get("dataset");

        // Read prior ranking
        try {
            ResultSet mlPlanScores = adapter.getResultsOfQuery("SELECT score FROM mlplanRanking WHERE seed = " + seed + " and dataset = \"" + dataSet + "\" ORDER BY variation ASC");

            // Retrieve prior ranking from data base
            double[] priorRanking = new double[MAX_COUNT_VARIATIONS];
            for (int i = 0; i < priorRanking.length; i++) {
                mlPlanScores.next();
                double varScore = mlPlanScores.getDouble(1);
                priorRanking[i] = varScore;

            }

            if (logger.isDebugEnabled()) {
                logger.debug("Prior ranking: {}", Arrays.toString(priorRanking));
            }

            // Compute score
            double[] scores = new double[MAX_COUNT_VARIATIONS];
            for (int i = 0; i < priorRanking.length; i++) {
                String filePath = dataSetFolder + File.separator + dataSet + "_" + seed + "_" + i + ".arff";
                Instances variation = FileUtils.readSingleInstances(filePath);
                scores[i] = benchmarkFunction.apply(variation);
            }

            // Calculate Kendall's Tau result
            double kendallsTau = EvaluationUtils.rankKendallsTau(priorRanking, scores);

            results.put("kendallsTau", kendallsTau);
            results.put("benchmarkRanking", Arrays.toString(scores));
            results.put("mlplanRanking", Arrays.toString(priorRanking));
            processor.processResults(results);
        } catch (Exception e) {
            throw new ExperimentEvaluationFailedException(e);
        }
    }
}
