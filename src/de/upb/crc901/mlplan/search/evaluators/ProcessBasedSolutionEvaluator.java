package de.upb.crc901.mlplan.search.evaluators;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.SolutionEvaluator;
import jaicore.basic.FileUtil;
import jaicore.processes.SubProcessWrapper;
import weka.core.Instances;

@SuppressWarnings("serial")
public class ProcessBasedSolutionEvaluator implements SolutionEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(ProcessBasedSolutionEvaluator.class);
	private final static String FILE_DATA = "data.ser";
	private final SolutionEvaluator wrappedSolutionEvaluator;
	private final int memoryOverheadPerProcessInMB;
	private final int memoryInMB;
	private Instances data;
	private boolean canceled = false;
	private int timeout;
	private File tmpDir;
	private final int id = (int)System.currentTimeMillis();

	public ProcessBasedSolutionEvaluator(SolutionEvaluator wrappedSolutionEvaluator, int memoryInMB, int memoryOverheadPerProcessInMB) {
		super();
		this.wrappedSolutionEvaluator = wrappedSolutionEvaluator;
		this.memoryInMB = memoryInMB;
		this.memoryOverheadPerProcessInMB = memoryOverheadPerProcessInMB;
	}

	public static Integer evalPipeline(SolutionEvaluator evaluator, MLPipeline pl, String dataFile) throws Exception {
		Instances data = (Instances) FileUtil.unserializeObject(dataFile);
		evaluator.setData(data);
		return evaluator.getSolutionScore(pl);
	}

	@Override
	public Integer getSolutionScore(MLPipeline pl) throws Exception {

		/* check whether we have data available */
		if (data == null)
			throw new IllegalStateException("Cannot compute f-values since data have not been set!");

		/* if data has not been serialized, do this now */
		if (!(new File(getDataFile()).exists()))
			serializeData(data, getDataFile());

		/* invoke process */
		SubProcessWrapper wrapper = new SubProcessWrapper();
		int assignedMemory = (int)(memoryInMB - memoryOverheadPerProcessInMB);
		if (assignedMemory <= 10)
			throw new IllegalStateException("Cannot run f-computation with " + assignedMemory + "MB of memory, which result from a budget of " + memoryInMB + " minus the process overhead of " + memoryOverheadPerProcessInMB);
		wrapper.setMemory(assignedMemory + "M");
		wrapper.setTmpDir(tmpDir);
		Optional<?> result = (Optional<?>) wrapper.runWithTimeout(this.getClass().getName(), "evalPipeline", null, timeout, wrappedSolutionEvaluator, pl, getDataFile());
		return result.isPresent() ? (Integer)result.get() : null;
	}

	@Override
	public void setData(Instances data) {
		if (this.data != null)
			throw new IllegalStateException("Cannot reset the data");
		this.data = data;
		serializeData(data, getDataFile());
	}

	private void serializeData(Instances data, String filename) {

		/* serialize pipeline */
		try {
			FileUtil.serializeObject(data, filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancel() {
		logger.info("Received cancel signal.");
		canceled = true;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getDataFile() {
		if (tmpDir == null)
			throw new IllegalStateException("No tmp directory has been set, so we cannot give the path for the data file!");
		return getTmpDir().getAbsolutePath() + File.separator + id + "_" + FILE_DATA;
	}
	
	public File getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(File tmpDir) {
		this.tmpDir = tmpDir;
	}

	public boolean isCanceled() {
		return canceled;
	}
}
