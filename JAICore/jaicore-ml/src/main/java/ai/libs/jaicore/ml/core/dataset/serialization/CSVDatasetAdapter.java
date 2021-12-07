package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.DenseInstance;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.IntBasedCategoricalAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.StringAttribute;

public class CSVDatasetAdapter implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>> {

	private enum ColumnType {
		Numeric, String, Categorical
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public CSVDatasetAdapter() {
		// nothing to do here
	}
	
	@Override
	public ILabeledDataset<ILabeledInstance> deserializeDataset(final IDatasetDescriptor datasetDescriptor) throws DatasetDeserializationFailedException, InterruptedException {
		if (!(datasetDescriptor instanceof CSVFileDatasetDescriptor)) {
			throw new IllegalArgumentException(String.format("Only {}s supported", CSVFileDatasetDescriptor.class.getSimpleName()));
		}

		CSVFileDatasetDescriptor descriptor = (CSVFileDatasetDescriptor) datasetDescriptor;
		File csvFile = new File(descriptor.getCsvFile());

		List<String> columnNames;
		List<List<String>> data;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(csvFile));
			try {
				String header = reader.readLine();
				String[] allColumnNames = header.split(",", -1);
		
				List<Integer> ignoredColumnIndices = computeIgnoredColumnIndices(allColumnNames, descriptor.getIgnoredColumns());
				columnNames = filterValues(allColumnNames, ignoredColumnIndices);
				data = readAndFilterData(reader, ignoredColumnIndices, allColumnNames.length);
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new DatasetDeserializationFailedException(e);
		}

		List<ColumnType> columnTypes = computeColumnTypes(columnNames, descriptor.getCategoricalColumns(), data);
		int labelColumnIndex = computeLabelColumnIndex(columnNames, descriptor.getLabelColumn());

		List<IAttribute> allAttributes = computeAttributes(columnNames, columnTypes, data);

		List<IAttribute> valueAttributes = new ArrayList<>(allAttributes);
		IAttribute labelAttribute = valueAttributes.remove(labelColumnIndex);
		ILabeledInstanceSchema schema = new LabeledInstanceSchema(csvFile.getName(), valueAttributes, labelAttribute);

		Dataset dataset = new Dataset(schema);
		while (!data.isEmpty()) {
			dataset.add(createLabeledInstance(allAttributes, labelColumnIndex, data.remove(0)));
		}
		dataset.addInstruction(createReconstructionInstruction(descriptor));

		logger.info("Successfully created dataset from CSV, {} columns, {} rows, file: {}", columnNames.size(), dataset.size(), csvFile.getAbsolutePath());
		logger.debug("Column types:");
		for (IAttribute attribute : allAttributes) {
			logger.debug("  {}: {}", attribute.getName(), attribute.getClass().getSimpleName());
		}

		return dataset;
	}
	
	public static ILabeledDataset<ILabeledInstance> readDataset(final String csvInputFile, final String labelColumn) throws DatasetDeserializationFailedException, InterruptedException {
		return readDataset(csvInputFile, labelColumn, new ArrayList<>());
	}

	public static ILabeledDataset<ILabeledInstance> readDataset(final String csvInputFile, final String labelColumn, List<String> categoricalColumns) throws DatasetDeserializationFailedException, InterruptedException {
		return readDataset(csvInputFile, labelColumn, categoricalColumns, new ArrayList<>());
	}

	public static ILabeledDataset<ILabeledInstance> readDataset(final String csvInputFile, final String labelColumn, List<String> categoricalColumns, List<String> ignoredColumns) throws DatasetDeserializationFailedException, InterruptedException {
		CSVFileDatasetDescriptor desc = new CSVFileDatasetDescriptor(csvInputFile, labelColumn, categoricalColumns, ignoredColumns);
		return new CSVDatasetAdapter().deserializeDataset(desc);
	}
	
	public static void writeDataset(final File arffOutputFile, final ILabeledDataset<? extends ILabeledInstance> data) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(arffOutputFile))) {
			// write header line for csv
			bw.write(data.getInstanceSchema().getAttributeList().stream().map(x -> "\"" + x.getName() + "\"").collect(Collectors.joining(",")));
			bw.write(",");
			bw.write("\"" + data.getLabelAttribute().getName() + "\"");
			bw.write("\n");

			for (ILabeledInstance instance : data) {
				bw.write(IntStream.range(0, instance.getNumAttributes()).mapToObj(x -> serializeAttributeValue(data.getAttribute(x), instance.getAttributeValue(x))).collect(Collectors.joining(",")));
				bw.write(",");
				bw.write(serializeAttributeValue(data.getInstanceSchema().getLabelAttribute(), instance.getLabel()));
				bw.write("\n");
			}
		}
	}

	private static String serializeAttributeValue(final IAttribute att, final Object attValue) {
		if (attValue == null) {
			return "";
		}

		String value = att.serializeAttributeValue(attValue);
		if (att instanceof ICategoricalAttribute) {
			if (value.startsWith("'") && value.endsWith("'")) {
				value = value.substring(1, value.length() - 2);
			}
			if (!value.startsWith("\"")) {
				value = "\"" + value + "\"";
			}
		}
		return value;
	}

	private List<Integer> computeIgnoredColumnIndices(String[] allColumnNames, List<String> ignoredColumns) {
		ArrayList<Integer> ignoredColumnIndices = new ArrayList<>();
		for (int col = 0; col < allColumnNames.length; col++) {
			String columnName = normalize(allColumnNames[col]);
			if (ignoredColumns.contains(columnName)) {
				ignoredColumnIndices.add(col);
			}
		}
		return ignoredColumnIndices;
	}

	private List<String> filterValues(String[] allValues, List<Integer> ignoredColumnIndices) {
		ArrayList<String> values = new ArrayList<>();
		for (int col = 0; col < allValues.length; col++) {
			if (!ignoredColumnIndices.contains(col)) {
				values.add(normalize(allValues[col]));
			}
		}
		return values;
	}

	private List<List<String>> readAndFilterData(BufferedReader reader, List<Integer> ignoredColumnIndices, int targetValueNumber)
			throws IOException {
		ArrayList<List<String>> data = new ArrayList<>();
		String line = reader.readLine();
		int lineNumber = 2;
		while (line != null) {
			String[] allValues = line.split(",", -1);
			if (allValues.length == targetValueNumber) {
				List<String> values = filterValues(allValues, ignoredColumnIndices);
				data.add(values);
			} else {
				logger.warn("Ignored line {}: should have {} elements, but has {}", lineNumber, targetValueNumber, allValues.length);
			}

			line = reader.readLine();
			lineNumber++;
		}
		return data;
	}

	private List<ColumnType> computeColumnTypes(List<String> columnNames, List<String> categoricalColumns, List<List<String>> data) {
		ArrayList<ColumnType> columnTypes = new ArrayList<>();
		for (int col = 0; col < columnNames.size(); col++) {
			ColumnType columnType;
			if (categoricalColumns.contains(columnNames.get(col))) {
				columnType = ColumnType.Categorical;
			} else {
				columnType = ColumnType.Numeric;
				for (List<String> values : data) {
					String value = values.get(col);
					if (!value.trim().isEmpty()) {
						try {
							Double.parseDouble(value);
						} catch (NumberFormatException e) {
							columnType = ColumnType.String;
							break;
						}
					}
				}
			}
			columnTypes.add(columnType);
		}

		return columnTypes;
	}

	private int computeLabelColumnIndex(List<String> columnNames, String labelColumn) throws DatasetDeserializationFailedException {
		for (int col = 0; col < columnNames.size(); col++) {
			if (columnNames.get(col).equals(labelColumn)) {
				return col;
			}
		}
		
		String msg = String.format("Label column {} not found, columns: {}", labelColumn, String.join(", ", columnNames));
		throw new DatasetDeserializationFailedException(msg);
	}

	private List<IAttribute> computeAttributes(List<String> columnNames, List<ColumnType> columnTypes,
			List<List<String>> data) {
		List<IAttribute> attributes = new ArrayList<>();
		for (int col = 0; col < columnTypes.size(); col++) {
			switch (columnTypes.get(col)) {
			case Numeric:
				attributes.add(new NumericAttribute(columnNames.get(col)));
				break;
			case String:
				attributes.add(new StringAttribute(columnNames.get(col)));
				break;
			case Categorical:
				List<String> domain = computeDistinctValues(data, col);
				attributes.add(new IntBasedCategoricalAttribute(columnNames.get(col), domain));
				break;
			default:
				throw new RuntimeException("Unsupported literal: " + columnTypes.get(col));
			}
		}
		return attributes;
	}

	private List<String> computeDistinctValues(List<List<String>> data, int column) {
		HashSet<String> distinctValues = new HashSet<>();
		for (List<String> values : data) {
			distinctValues.add(values.get(column));
		}
		return new ArrayList<String>(distinctValues);
	}

	private ILabeledInstance createLabeledInstance(List<IAttribute> allAttributes, int labelColumnIndex,
			List<String> stringValues) {
		ArrayList<Object> values = new ArrayList<>();
		for (int col = 0; col < stringValues.size(); col++) {
			if (col != labelColumnIndex) {
				IAttribute attribute = allAttributes.get(col);
				String stringValue = stringValues.get(col);
				values.add(computeValue(attribute, stringValue));
			}
		}

		IAttribute attribute = allAttributes.get(labelColumnIndex);
		String stringValue = stringValues.get(labelColumnIndex);
		Object labelValue = computeValue(attribute, stringValue);

		return new DenseInstance(values, labelValue);
	}
	
	private Object computeValue(IAttribute attribute, String stringValue) {
		Object value;
		if (stringValue.trim().isEmpty()) {
			if (attribute instanceof StringAttribute) {
				value = "";
			} else {
				value = null;
			}
		} else {
			value = attribute.deserializeAttributeValue(stringValue);
		}
		return value;
	}

	private ReconstructionInstruction createReconstructionInstruction(CSVFileDatasetDescriptor descriptor) throws DatasetDeserializationFailedException {
		try {
			Method method = getClass().getMethod("readDataset", String.class, String.class, List.class, List.class);
			Object[] args = new Object[] {
					descriptor.getCsvFile(), descriptor.getLabelColumn(), 
					descriptor.getCategoricalColumns(), descriptor.getIgnoredColumns()};
			return new ReconstructionInstruction(method, args);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	private String normalize(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}
}