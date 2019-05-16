package jaicore.ml.tsc.latexeval;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jaicore.basic.SQLAdapter;
import jaicore.basic.kvstore.IKVFilter;
import jaicore.basic.kvstore.KVStore;
import jaicore.basic.kvstore.KVStoreCollection;
import jaicore.basic.kvstore.KVStoreCollection.EGroupMethod;
import jaicore.basic.kvstore.KVStoreStatisticsUtil;
import jaicore.basic.kvstore.KVStoreUtil;

/**
 * Class calculating LaTeX tables out of TSC experiments.
 * 
 * @author Julian Lienen
 *
 */
public class ResultCollector {
	/**
	 * Enumeration of available classifier which can be evaluated.
	 */
	public enum AvailableClassifier {
		LEARN_SHAPELETS("ls"), SHAPELET_TRANSFORM("st"), TIME_SERIES_FOREST("tsf"), TIME_SERIES_BAG_OF_FEATURES(
				"tsbf"), LEARN_PATTERN_SIMILARITY("lps");

		/**
		 * Technical string used to select the correct database table
		 */
		private String technicalString;

		AvailableClassifier(String technicalString) {
			this.technicalString = technicalString;
		}
	}

	/**
	 * Attribute key used for storing the implementation.
	 */
	private static final String IMPL_ATT_KEY = "impl";
	/**
	 * Attribute value for own implementation.
	 */
	private static final String IMPL_ATT_OWN = "own";
	/**
	 * Attribute value for reference implementation.
	 */
	private static final String IMPL_ATT_REF = "ref";

	/**
	 * Output format used to print out double values.
	 */
	private static final DecimalFormat OUTPUT_FORMAT = new DecimalFormat("#.##",
			DecimalFormatSymbols.getInstance(Locale.US));

	/**
	 * Main method creating the LaTeX tables for experiment results in database.
	 */
	public static void generateLateXTableForClassifier(final AvailableClassifier classifier) throws Exception {

		// Inverse filter used if best calls require to select maximum rather than
		// minimum values
		IKVFilter inverseFilter = new IKVFilter() {
			@Override
			public String filter(Object objValue) {
				if (objValue == null)
					return null;

				String value = objValue.toString();
				String[] values = value.split(",");
				String result = "";
				for (int i = 0; i < values.length; i++) {

					result += new Double((-1) * Double.parseDouble(values[i])).toString();
					if (i != values.length - 1)
						result += ",";
				}
				return result;
			}
		};

		// TODO: Fill int host, user, password, database and table
		String tablePrefix = "";
		try (SQLAdapter adapter = new SQLAdapter("", "", "", "tsc")) {
			KVStoreCollection csvChunks = KVStoreUtil.readFromMySQLTable(adapter,
					tablePrefix + "_" + classifier.technicalString, new HashMap<>());

			KVStoreCollection newStore = new KVStoreCollection();

			// Create new key values for own and reference implementation comparison
			for (KVStore k : csvChunks) {
				if (k.getAsString("accuracy") == null || k.getAsString("ref_accuracy") == null)
					continue;

				KVStore ownK = new KVStore();
				KVStore refK = new KVStore();
				ownK.put(IMPL_ATT_KEY, IMPL_ATT_OWN);

				ownK.put("acc", Double.parseDouble(k.getAsString("accuracy")));
				ownK.put("train_time", Double.parseDouble(k.getAsString("train_time")));

				refK.put(IMPL_ATT_KEY, IMPL_ATT_REF);

				refK.put("acc", Double.parseDouble(k.getAsString("ref_accuracy")));
				refK.put("train_time", Double.parseDouble(k.getAsString("ref_train_time")));

				ownK.put("dataset", k.getAsString("dataset"));
				refK.put("dataset", k.getAsString("dataset"));

				newStore.add(ownK);
				newStore.add(refK);
			}

			// Aggregate rows for implementations and datasets
			HashMap<String, EGroupMethod> handler = new HashMap<String, EGroupMethod>();
			handler.put("acc", EGroupMethod.AVG);
			handler.put("train_time", EGroupMethod.AVG);
			newStore = newStore.group(new String[] { IMPL_ATT_KEY, "dataset" }, handler);

			// Format values
			for (KVStore k : newStore) {
				k.replace("acc", Double.valueOf(OUTPUT_FORMAT.format(k.getAsDouble("acc"))));
				k.replace("train_time", Double.valueOf(OUTPUT_FORMAT.format(k.getAsDouble("train_time"))));
			}

			// Invert accuracy entries due to best comparison based on min value
			Map<String, IKVFilter> filterConfig = new HashMap<>();
			filterConfig.put("acc", inverseFilter);
			newStore.applyFilter(filterConfig);

			// Determine best implementation for each dataset
			KVStoreStatisticsUtil.best(newStore, "dataset", IMPL_ATT_KEY, "acc", "best_acc");
			KVStoreStatisticsUtil.best(newStore, "dataset", IMPL_ATT_KEY, "train_time", "best_train_time");

			// Revert inversion for output
			filterConfig.put("acc", inverseFilter);
			newStore.applyFilter(filterConfig);

			// Format best entries
			for (KVStore k : newStore) {
				if (k.getAsBoolean("best_acc"))
					k.put("acc", "\\textbf{" + k.getAsString("acc") + "}");
				if (k.getAsBoolean("best_train_time"))
					k.put("train_time", "\\textbf{" + k.getAsString("train_time") + "}");
			}

			// Print out accuracy LaTeX table
			System.out.println("\\section{Accuracies}");

			for (KVStoreCollection c : new KVStoreCollection[] { newStore }) {
				String latexTable = KVStoreUtil.kvStoreCollectionToLaTeXTable(c, "dataset", IMPL_ATT_KEY, "acc",
						"-$\\phantom{\\bullet}$");
				System.out.println(latexTable);
			}

			// Print out training time LaTeX table
			System.out.println("\n\\section{Training times}");

			for (KVStoreCollection c : new KVStoreCollection[] { newStore }) {
				String latexTable = KVStoreUtil.kvStoreCollectionToLaTeXTable(c, "dataset", IMPL_ATT_KEY, "train_time",
						"-$\\phantom{\\bullet}$");
				System.out.println(latexTable);
			}
		}
	}

	/**
	 * Main method used for execution.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		generateLateXTableForClassifier(AvailableClassifier.LEARN_PATTERN_SIMILARITY);
	}

}
