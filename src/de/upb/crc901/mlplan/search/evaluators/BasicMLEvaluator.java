package de.upb.crc901.mlplan.search.evaluators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MLUtil;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("serial")
public class BasicMLEvaluator implements Serializable {
	
	private final static Logger logger = LoggerFactory.getLogger(BasicMLEvaluator.class);
	
	private final Random rand;
	private final File statfolder;
	private final File statfile;
	private final BlockingQueue<LearningStats> results = new LinkedBlockingQueue<>();
	private boolean canceled;
	private transient final ResultWriter resultWriterThread; // no results will be written if the process is outsourced!
	private final EventBus measurementEventBus = new EventBus();
	
	private class LearningStats {
		Instances data, train;
		MLPipeline plCopy;
		long overallExecutionTimeOfPreprocessor;
		long overallExecutionTimeOfClassifier;
		double error;

		public LearningStats(Instances data, Instances train, MLPipeline plCopy, long overallExecutionTimeOfPreprocessor, long overallExecutionTimeOfClassifier, double error) {
			super();
			this.data = data;
			this.train = train;
			this.plCopy = plCopy;
			this.overallExecutionTimeOfPreprocessor = overallExecutionTimeOfPreprocessor;
			this.overallExecutionTimeOfClassifier = overallExecutionTimeOfClassifier;
			this.error = error;
		}
	}

	private class ResultWriter extends Thread {
		public void run() {
			try {
				while (!Thread.interrupted() && !canceled) {
					LearningStats stats = results.take();
					try {
						StringBuilder sb = new StringBuilder();
						int code = WekaUtil.toJAICoreInstances(stats.data).hashCode();
						File dataFile = new File(statfolder + File.separator + code + ".arff");
						if (!dataFile.exists()) {
							try (FileWriter fw = new FileWriter(dataFile)) {
								fw.write(stats.data.toString());
							}
						}
						sb.append(code);
						sb.append(", ");
						sb.append(MLUtil.serializePipelineToJson(stats.plCopy));
						sb.append(", ");
						sb.append(Arrays.toString(WekaUtil.getIndicesOfContainedInstances(stats.data, stats.train)));
						sb.append(", ");
						sb.append(stats.plCopy.getTimeForTrainingPreprocessor());
						sb.append(", ");
						sb.append(stats.plCopy.getTimeForTrainingClassifier());
						sb.append(", ");
						sb.append(stats.overallExecutionTimeOfPreprocessor);
						sb.append(", ");
						sb.append(stats.overallExecutionTimeOfClassifier);
						sb.append(", ");
						sb.append(Math.round(stats.error * 100) / 10000f);
						sb.append("\n");
						try (FileWriter fw = new FileWriter(statfile, true)) {
							fw.write(sb.toString());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Shutting down result writer.");
			}
		}
	}

	public BasicMLEvaluator(Random r) throws IOException {
		super();
		this.rand = r;
//		IBasicMLEvaluatorConf conf = ConfigCache.getOrCreate(IBasicMLEvaluatorConf.class);
//		this.statfolder = conf.getStatDir();
		this.statfolder = null;
		if (statfolder == null) {
//			logger.warn("Property \"statdir\" not set in configuration file or file does not exist (conf/BasicMLEvaluator.properties)");
			statfile = null;
			resultWriterThread = null;
		}
		else {
			if (!statfolder.exists())
				statfolder.mkdirs();
			statfile = new File(statfolder + File.separator + "stats.csv");
			
			resultWriterThread = new ResultWriter();
			resultWriterThread.start();
		}
	}

	public double getErrorRateForRandomSplit(MLPipeline pl, Instances data, double splitSize) throws Exception {
		List<Instances> split = WekaUtil.getStratifiedSplit(data, rand, splitSize);
		Instances train = split.get(0);
		Instances test = split.get(1);
		return getErrorRateForSplit(pl, train, test);
	}
	
	public double getErrorRateForSplit(MLPipeline pl, Instances train, Instances test) throws Exception {
		MLPipeline plCopy = pl.clone();
		plCopy.buildClassifier(train);
		int mistakes = 0;
		logger.info("Split size is {}/{}", train.size(), test.size());
		
		for (Instance i : test) {
			if (i.classValue() != plCopy.classifyInstance(i))
				mistakes++;
		}
		double error = mistakes * 100f / test.size();
		Instances data = new Instances(train);
		data.addAll(test);
		if (resultWriterThread != null)
			results.put(new LearningStats(data, train, plCopy, (int)plCopy.getTimeForExecutingPreprocessor().getSum(), (int)plCopy.getTimeForExecutingClassifier().getSum(), error));
		measurementEventBus.post(new PipelineMeasurementEvent<Double>(plCopy, error));
		
		/* write stats */
		return error;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public EventBus getMeasurementEventBus() {
		return measurementEventBus;
	}
}
