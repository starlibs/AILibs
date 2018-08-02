package autofe.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Utility class for file operations (storing and reading data sets, file system
 * checks, ...).
 * 
 * @author Julian Lienen
 *
 */
public final class FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	private FileUtils() {
		// Utility class
	}

	public static boolean checkIfDirIsEmpty(final String dirPath) {
		File file = new File(dirPath);
		if (file.isDirectory())
			return file.list().length == 0;
		else
			throw new IllegalArgumentException("Parameter 'dirPath' must specify an existing directory.");
	}

	public static void createDirIfNotExists(final String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			file.createNewFile();
		}
	}

	public static boolean checkIfFilesWithPrefixExist(final String dirPath, final String prefix) {
		File file = new File(dirPath);
		if (file.isDirectory()) {
			for (String filePath : file.list()) {
				File tmpFile = new File(filePath);
				if (tmpFile.getName().startsWith(prefix))
					return true;

			}
			return false;
		} else
			throw new IllegalArgumentException("Parameter 'dirPath' must specify an existing directory.");
	}

	public static void saveInstances(final List<Instances> dataSets, final String dirPath, final String dataSetPrefix)
			throws Exception {
		for (int i = 0; i < dataSets.size(); i++) {
			logger.debug("Saving instances with " + dataSets.get(i).numInstances() + " instances and "
					+ dataSets.get(i).numAttributes() + " attributes.");

			ArffSaver saver = new ArffSaver();
			saver.setInstances(dataSets.get(i));
			File destFile = new File(dirPath + "\\" + dataSetPrefix + i + ".arff");
			saver.setFile(destFile);

			saver.writeBatch();
		}
	}

	public static List<Instances> readInstances(final String dirPath, final String dataSetPrefix,
			final String excludePostfix) {
		List<Instances> results = new ArrayList<>();

		// TODO: Implement me
		File dir = new File(dirPath);
		File[] dataSetFiles = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (excludePostfix != null)
					return name.startsWith(dataSetPrefix) && !name.endsWith(excludePostfix);
				else
					return name.startsWith(dataSetPrefix);
			}
		});
		for (File file : dataSetFiles) {
			try {
				DataSource source = new DataSource(new FileInputStream(file));
				Instances insts;

				insts = source.getDataSet();
				insts.setClassIndex(insts.numAttributes() - 1);

				results.add(insts);

			} catch (Exception e) {
				logger.error("Could not import data set. Reason: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
		}

		return results;
	}

	public static void writeDoubleArrayToFile(final double[] array, final String filePath, final String delimiter) {
		try (FileWriter fw = new FileWriter(new File(filePath))) {
			for (int i = 0; i < array.length; i++) {
				fw.append(array[i] + delimiter);
			}
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double[] readDoubleArrayFromFile(final String filePath, final String delimiter) {
		try (BufferedReader fr = new BufferedReader(new FileReader(new File(filePath)))) {
			String[] splitted = fr.readLine().split(delimiter);
			double[] result = new double[splitted.length];
			for (int i = 0; i < splitted.length; i++)
				result[i] = Double.parseDouble(splitted[i]);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, List<double[]>> readClassImages(final String path) {
		Map<String, List<double[]>> result = new HashMap<>();

		File dataSetDir = new File(path);
		if (!dataSetDir.exists())
			throw new IllegalArgumentException("The data set directory '" + path + "' does not exist.");

		for (String subDir : dataSetDir.list()) {
			File tmpFile = new File(subDir);
			if (tmpFile.exists() && tmpFile.isDirectory()) {
				String className = tmpFile.getName();
				List<double[]> dataSamples = new ArrayList<>();

				for (File dataSample : tmpFile.listFiles()) {
					if (dataSample.isDirectory())
						continue;

					// Read image to double array
					try {
						BufferedImage bufferedImage = ImageIO.read(dataSample);
						double[] imageData = bufferedImage.getData().getPixels(0, 0, bufferedImage.getWidth(),
								bufferedImage.getHeight(), new double[] {});
						dataSamples.add(imageData);

					} catch (IOException e) {
						e.printStackTrace();
						logger.warn("Could not read image. Reason: " + e.getMessage() + "\nSkipping example");
						continue;
					}
				}
				result.put(className, dataSamples);
			}
		}

		return result;
	}
}
