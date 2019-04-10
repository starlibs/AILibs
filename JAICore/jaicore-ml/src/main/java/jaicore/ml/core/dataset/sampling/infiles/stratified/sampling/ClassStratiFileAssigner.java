package jaicore.ml.core.dataset.sampling.infiles.stratified.sampling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jaicore.basic.TempFileHandler;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

public class ClassStratiFileAssigner implements IStratiFileAssigner {

	private TempFileHandler tempFileHandler;
	private String arffHeader;
	private int targetAttribute;
	private Map<String, String> classToStratumMapping;
	private Map<String, Integer> stratiSizes;

	/**
	 * Constructor with a given target attribute.
	 * 
	 * @param targetAttribute Index of the target attribute.
	 */
	public ClassStratiFileAssigner(int targetAttribute) {
		this.targetAttribute = targetAttribute;
		this.classToStratumMapping = new HashMap<>();
		this.stratiSizes = new HashMap<>();
	}

	/**
	 * Constructor without a given target attribute. The last feature will be used.
	 */
	public ClassStratiFileAssigner() {
		this.targetAttribute = -1;
		this.classToStratumMapping = new HashMap<>();
		this.stratiSizes = new HashMap<>();
	}

	@Override
	public void setTempFileHandler(TempFileHandler tempFileHandler) {
		this.tempFileHandler = tempFileHandler;
	}

	@Override
	public void setArffHeader(String arffHeader) {
		this.arffHeader = arffHeader;
	}

	@Override
	public void assignDatapoint(String datapoint) throws AlgorithmException {
		String c;
		String[] features = datapoint.split(",");
		if (this.targetAttribute == -1) {
			c = features[features.length - 1];
		} else {
			c = features[this.targetAttribute];
		}
		String uuid;
		if (!this.classToStratumMapping.containsKey(c)) {
			try {
				uuid = this.createNewStratumFile(c);
			} catch (IOException e) {
				throw new AlgorithmException(e, "Was not able to create a new temporary file for a stratum.");
			}
		} else {
			uuid = this.classToStratumMapping.get(c);
		}
		try {
			this.tempFileHandler.getFileWriterForTempFile(uuid).write(datapoint + "\n");
			this.tempFileHandler.getFileWriterForTempFile(uuid).flush();
			this.stratiSizes.put(uuid, this.stratiSizes.get(uuid) + 1);
		} catch (IOException e) {
			throw new AlgorithmException(e, "Was not able to write the datapoint into the corresponding stratum file.");
		}
	}

	@Override
	public Map<String, Integer> getAllCreatedStrati() {
		return stratiSizes;
	}

	private String createNewStratumFile(String c) throws IOException {
		String uuid = this.tempFileHandler.createTempFile();
		this.tempFileHandler.getFileWriterForTempFile(uuid).write(this.arffHeader);
		this.tempFileHandler.getFileWriterForTempFile(uuid).flush();
		this.classToStratumMapping.put(c, uuid);
		this.stratiSizes.put(uuid, 0);
		return uuid;
	}

}
