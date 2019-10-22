package ai.libs.jaicore.ml.core.newdataset.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.OptionsParser;
import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.attribute.CategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.newdataset.reader.arff.EArffItem;
import weka.core.UnsupportedAttributeTypeException;

public class ArffDatasetReader implements IDatasetLoader {

	private static final String M_RELATION = "@relation";
	private static final String M_ATTRIBUTE = "@attribute";
	private static final String M_DATA = "@data";

	private static final String M_NUMERIC_ATT = "numeric";
	private static final String M_NOMINAL_ATT = "nominal";

	private static final String F_CLASS_INDEX = "C";
	private static final String F_MULTI_TARGET = "MT";
	private static final String F_DATASET_SIZE = "I";

	private static final String K_RELATION_NAME = "relationName";
	private static final String K_CLASS_INDEX = "classIndex";

	private static final String SEPARATOR_RELATIONNAME = ":";
	private static final String SEPARATOR_ATTRIBUTE_DESCRIPTION = " ";
	private static final String SEPARATOR_DENSE_INSTANCE_VALUES = ",";

	private final File datasetFile;

	public ArffDatasetReader(final File datasetFile) {
		this.datasetFile = datasetFile;
	}

	/**
	 * Extracts meta data about a relation from a string.
	 *
	 * @param line The line which is to be parsed to extract the necessary information from the relation name.
	 * @return A KVStore containing the parsed meta data.
	 */
	private KVStore parseRelation(final String line) {
		KVStore metaData = new KVStore();
		String[] relationNameAndOptions = line.substring(line.indexOf('\'') + 1, line.lastIndexOf('\'')).split(SEPARATOR_RELATIONNAME);

		metaData.put(K_RELATION_NAME, relationNameAndOptions[0].trim());
		if (relationNameAndOptions.length > 1) {
			OptionsParser optParser = new OptionsParser(relationNameAndOptions[1]);
			metaData.put(K_CLASS_INDEX, optParser.get(F_CLASS_INDEX));
		}

		return metaData;
	}

	private IAttribute parseAttributeMetaData(final String line) throws UnsupportedAttributeTypeException {
		String[] attributeDefinitionSplit = line.substring(M_ATTRIBUTE.length() + 1).split(SEPARATOR_ATTRIBUTE_DESCRIPTION);
		String name = attributeDefinitionSplit[0].trim();
		if (name.startsWith("'") && name.endsWith("'")) {
			name = name.substring(1, name.length() - 1);
		}
		String type = attributeDefinitionSplit[1].trim();

		String[] values = null;
		if (type.startsWith("{") && type.endsWith("}")) {
			values = type.substring(1, type.length() - 1).split(SEPARATOR_DENSE_INSTANCE_VALUES);
			type = M_NOMINAL_ATT;
		}

		switch (type) {
		case M_NUMERIC_ATT:
			return new NumericAttribute(name);
		case M_NOMINAL_ATT:
			if (values != null) {
				return new CategoricalAttribute(name, Arrays.stream(values).map(String::trim).collect(Collectors.toList()));
			} else {
				throw new IllegalStateException("Identified a nominal attribute but it seems to have no values.");
			}
		default:
			throw new UnsupportedAttributeTypeException("Can not deal with attribute type " + type);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance> loadDataset() throws InterruptedException {
		try (BufferedReader br = Files.newBufferedReader(this.datasetFile.toPath())) {
			KVStore relationMetaData = new KVStore();
			Dataset dataset = null;
			List<IAttribute> attributeList = new LinkedList<>();

			boolean instanceReadMode = false;
			String line;
			long lineCounter = 1;

			ILabeledInstanceSchema schema = null;

			while ((line = br.readLine()) != null) {
				if (!instanceReadMode) {
					if (line.startsWith(EArffItem.RELATION.getValue())) {
						// parse relation meta data
						relationMetaData = this.parseRelation(line);

					} else if (line.startsWith(EArffItem.ATTRIBUTE.getValue())) {
						// parse attribute meta data
						attributeList.add(this.parseAttributeMetaData(line));

					} else if (line.startsWith(EArffItem.DATA.getValue())) {
						// switch to instance read mode
						if (!line.trim().equals(EArffItem.DATA.getValue())) {
							throw new IllegalArgumentException(
									"Error while parsing arff-file on line " + lineCounter + ": There is more in this line than just the data declaration " + EArffItem.DATA.getValue() + ", which is not supported");
						}
						instanceReadMode = true;

						Integer classIndex = null;
						if (relationMetaData != null) {
							classIndex = relationMetaData.getAsInt(K_CLASS_INDEX);
						}
						if (classIndex == null) {
							classIndex = attributeList.size() - 1;
						}

						for (int i = 0; i < attributeList.size(); i++) {
							List<IAttribute> listToAddAttributeTo = this.chooseTheList(instanceAttribute, targetAttribute, attributeList.size(), i, classIndex, relationMetaData.getAsBoolean(F_MULTI_TARGET));
							listToAddAttributeTo.add(attributeList.get(i));
						}

						schema = new LabeledInstanceSchema((relationMetaData != null) ? relationMetaData.getAsString(K_RELATION_NAME) : "NaN", instanceAttribute, targetAttribute);
						dataset = new Dataset(schema);

					}
				} else {
					Objet[] instance = this.parseInstance(line, );

					if (!line.trim().isEmpty() && !instanceAttribute.isEmpty() && !targetAttribute.isEmpty()) {

						for (int i = 0; i < attributeList.size(); i++) {
							int instanceAttributeIndex = instanceAttribute.indexOf(attributeList.get(i));

							if (instanceAttributeIndex >= 0) {
								x[instanceAttributeIndex] = instance[i];
							} else {
								y[targetAttribute.indexOf(attributeList.get(i))] = instance[i];
							}
						}
						dataset.add(x, y[0]);
					}
				}
				lineCounter++;
			}

			return dataset;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAttributeTypeException e) {
			e.printStackTrace();
		}

		ILabeledInstanceSchema schema = new LabeledInstanceSchema(relationName, attributeList, labelAttribute);
		return null;
	}

}
