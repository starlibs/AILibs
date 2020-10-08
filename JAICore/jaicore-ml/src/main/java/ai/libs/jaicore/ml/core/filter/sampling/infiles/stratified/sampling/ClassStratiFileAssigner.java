package ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.api4.java.algorithm.exceptions.AlgorithmException;

import ai.libs.jaicore.basic.TempFileHandler;

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
	public ClassStratiFileAssigner(final int targetAttribute) {
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
	public void setTempFileHandler(final TempFileHandler tempFileHandler) {
		this.tempFileHandler = tempFileHandler;
	}

	@Override
	public void setArffHeader(final String arffHeader) {
		this.arffHeader = arffHeader;
	}

	@Override
	public void assignDatapoint(final String datapoint) throws AlgorithmException {
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
				throw new AlgorithmException("Was not able to create a new temporary file for a stratum.", e);
			}
		} else {
			uuid = this.classToStratumMapping.get(c);
		}
		try {
			this.tempFileHandler.getFileWriterForTempFile(uuid).write(datapoint + "\n");
			this.tempFileHandler.getFileWriterForTempFile(uuid).flush();
			this.stratiSizes.put(uuid, this.stratiSizes.get(uuid) + 1);
		} catch (IOException e) {
			throw new AlgorithmException("Was not able to write the datapoint into the corresponding stratum file.", e);
		}
	}

	@Override
	public Map<String, Integer> getAllCreatedStrati() {
		return this.stratiSizes;
	}

	private String createNewStratumFile(final String c) throws IOException {
		String uuid = this.tempFileHandler.createTempFile().getName();
		this.tempFileHandler.getFileWriterForTempFile(uuid).write(this.arffHeader);
		this.tempFileHandler.getFileWriterForTempFile(uuid).flush();
		this.classToStratumMapping.put(c, uuid);
		this.stratiSizes.put(uuid, 0);
		return uuid;
	}

}
