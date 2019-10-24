package ai.libs.jaicore.ml.core.dataset.reader;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ArrayUtil;
import ai.libs.jaicore.basic.OptionsParser;
import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.DenseInstance;
import ai.libs.jaicore.ml.core.dataset.SparseInstance;
import ai.libs.jaicore.ml.core.dataset.reader.arff.EArffAttributeType;
import ai.libs.jaicore.ml.core.dataset.reader.arff.EArffItem;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;

public class ArffDatasetAdapter implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArffDatasetAdapter.class);

	public static final String K_RELATION_NAME = "relationName";
	public static final String K_CLASS_INDEX = "classIndex";

	private static final String F_CLASS_INDEX = "C";

	private static final String SEPARATOR_RELATIONNAME = ":";
	private static final String SEPARATOR_ATTRIBUTE_DESCRIPTION = " ";
	private static final String SEPARATOR_DENSE_INSTANCE_VALUES = ",";

	private final boolean sparseMode;
	private File datasetFile = null;

	public ArffDatasetAdapter(final boolean sparseMode, final File datasetFile) {
		this(sparseMode);
		this.datasetFile = datasetFile;
	}

	public ArffDatasetAdapter(final boolean sparseMode) {
		this.sparseMode = sparseMode;
	}

	public ArffDatasetAdapter() {
		this(false);
	}

	@Override
	public ILabeledDataset<ILabeledInstance> deserializeDataset(final File datasetFile) throws DatasetDeserializationFailedException, InterruptedException {
		Objects.requireNonNull(this.datasetFile, "No dataset has been configured.");
		return readDataset(this.sparseMode, datasetFile);
	}

	public ILabeledDataset<ILabeledInstance> deserializeDataset() throws InterruptedException, DatasetDeserializationFailedException {
		return this.deserializeDataset(this.datasetFile);
	}

	/**
	 * Extracts meta data about a relation from a string.
	 *
	 * @param line The line which is to be parsed to extract the necessary information from the relation name.
	 * @return A KVStore containing the parsed meta data.
	 */
	protected static KVStore parseRelation(final String line) {
		KVStore metaData = new KVStore();
		String[] relationNameAndOptions = line.substring(line.indexOf('\'') + 1, line.lastIndexOf('\'')).split(SEPARATOR_RELATIONNAME);

		metaData.put(K_RELATION_NAME, relationNameAndOptions[0].trim());
		if (relationNameAndOptions.length > 1) {
			OptionsParser optParser = new OptionsParser(relationNameAndOptions[1]);
			metaData.put(K_CLASS_INDEX, optParser.get(F_CLASS_INDEX));
		}

		return metaData;
	}

	protected static IAttribute parseAttribute(final String line) throws UnsupportedAttributeTypeException {
		String[] attributeDefinitionSplit = line.substring(EArffItem.ATTRIBUTE.getValue().length() + 1).split(SEPARATOR_ATTRIBUTE_DESCRIPTION);
		String name = attributeDefinitionSplit[0].trim();
		if (name.startsWith("'") && name.endsWith("'")) {
			name = name.substring(1, name.length() - 1);
		}
		String type = attributeDefinitionSplit[1].trim();

		EArffAttributeType attType;
		String[] values = null;
		if (type.startsWith("{") && type.endsWith("}")) {
			values = type.substring(1, type.length() - 1).split(SEPARATOR_DENSE_INSTANCE_VALUES);
			attType = EArffAttributeType.NOMINAL;
		} else {
			attType = EArffAttributeType.valueOf(type.toUpperCase());
		}

		switch (attType) {
		case NUMERIC:
			return new NumericAttribute(name);
		case NOMINAL:
			if (values != null) {
				return new CategoricalAttribute(name, Arrays.stream(values).map(String::trim).collect(Collectors.toList()));
			} else {
				throw new IllegalStateException("Identified a nominal attribute but it seems to have no values.");
			}
		default:
			throw new UnsupportedAttributeTypeException("Can not deal with attribute type " + type);
		}
	}

	protected static Object parseInstance(final boolean sparseData, final List<IAttribute> attributes, final String line) {
		String[] lineSplit = line.split(",");

		if (!sparseData) {
			if (lineSplit.length != attributes.size()) {
				throw new IllegalArgumentException("Cannot parse instance as this is not a sparse instance but has less columns than there are attributes defined.");
			}

			Object[] parsedDenseInstance = new Object[lineSplit.length];
			for (int i = 0; i < lineSplit.length; i++) {
				parsedDenseInstance[i] = attributes.get(i).deserializeAttributeValue(lineSplit[i]);
			}

			return parsedDenseInstance;
		} else {
			Map<Integer, Object> parsedSparseInstance = new HashMap<>();
			for (String sparseValue : lineSplit) {
				int indexOfFirstSpace = sparseValue.indexOf(' ');
				int indexOfAttribute = Integer.parseInt(sparseValue.substring(0, indexOfFirstSpace));
				String attributeValue = sparseValue.substring(indexOfFirstSpace + 1);
				parsedSparseInstance.put(indexOfAttribute, attributes.get(indexOfAttribute).deserializeAttributeValue(attributeValue));
			}
			return parsedSparseInstance;
		}
	}

	protected static ILabeledDataset<ILabeledInstance> createDataset(final KVStore relationMetaData, final List<IAttribute> attributes) {
		if (!relationMetaData.containsKey(K_CLASS_INDEX) || relationMetaData.getAsInt(K_CLASS_INDEX) < 0) {
			throw new IllegalArgumentException("No (valid) class index given!");
		}
		List<IAttribute> attributeList = new LinkedList<>(attributes);
		IAttribute labelAttribute = attributeList.remove((int) relationMetaData.getAsInt(K_CLASS_INDEX));
		ILabeledInstanceSchema schema = new LabeledInstanceSchema(relationMetaData.getAsString(K_RELATION_NAME), attributeList, labelAttribute);
		return new Dataset(schema);
	}

	public static ILabeledDataset<ILabeledInstance> readDataset(final File datasetFile) throws DatasetDeserializationFailedException, InterruptedException {
		return readDataset(false, datasetFile);
	}

	public static ILabeledDataset<ILabeledInstance> readDataset(final boolean sparseMode, final File datasetFile) throws DatasetDeserializationFailedException, InterruptedException {
		try (BufferedReader br = Files.newBufferedReader(datasetFile.toPath())) {
			ILabeledDataset<ILabeledInstance> dataset = null;
			KVStore relationMetaData = new KVStore();
			List<IAttribute> attributes = new LinkedList<>();

			boolean instanceReadMode = false;
			String line;
			long lineCounter = 1;

			while ((line = br.readLine()) != null) {
				if (!instanceReadMode) {
					if (line.startsWith(EArffItem.RELATION.getValue())) {
						// parse relation meta data
						relationMetaData = parseRelation(line);
					} else if (line.startsWith(EArffItem.ATTRIBUTE.getValue())) {
						// parse attribute meta data
						attributes.add(parseAttribute(line));
					} else if (line.startsWith(EArffItem.DATA.getValue())) {
						// switch to instance read mode
						if (!line.trim().equals(EArffItem.DATA.getValue())) {
							throw new IllegalArgumentException(
									"Error while parsing arff-file on line " + lineCounter + ": There is more in this line than just the data declaration " + EArffItem.DATA.getValue() + ", which is not supported");
						}
						instanceReadMode = true;
						try {
							dataset = createDataset(relationMetaData, attributes);
						} catch (IllegalArgumentException e) {
							LOGGER.warn("Invalid class index in the dataset's meta data (" + relationMetaData.get(K_CLASS_INDEX) + "): Assuming last column to be the target attribute!");
							relationMetaData.put(K_CLASS_INDEX, attributes.size() - 1);
							dataset = createDataset(relationMetaData, attributes);
						}
					}
				} else {
					Object parsedInstance = parseInstance(sparseMode, attributes, line);
					ILabeledInstance newI;
					if (parsedInstance instanceof Object[]) {
						Object[] parsedDenseInstance = (Object[]) parsedInstance;
						newI = new DenseInstance(ArrayUtil.copyArrayExlcuding(parsedDenseInstance, relationMetaData.getAsIntList(K_CLASS_INDEX)), parsedDenseInstance[relationMetaData.getAsInt(K_CLASS_INDEX)]);
					} else if (parsedInstance instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<Integer, Object> parsedSparseInstance = (Map<Integer, Object>) parsedInstance;
						Object label = parsedSparseInstance.remove(relationMetaData.getAsInt(K_CLASS_INDEX));
						newI = new SparseInstance(attributes.size(), parsedSparseInstance, label);
					} else {
						throw new IllegalStateException("Severe Error: The format of the parsed instance is not as expected.");
					}
					dataset.add(newI);
				}
			}
			lineCounter++;
			return dataset;
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException("Could not deserialize dataset from ARFF file.", e);
		}
	}

}
