package autofe.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 */
public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        // Utility class
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
        }
        throw new IllegalArgumentException("Parameter 'dirPath' must specify an existing directory.");
    }

    public static void saveInstances(final List<Instances> dataSets, final String dirPath, final String dataSetPrefix)
            throws Exception {
        for (int i = 0; i < dataSets.size(); i++) {
            logger.debug("Saving instances with {} instances and {} attributes.", dataSets.get(i).numInstances(),
                    dataSets.get(i).numAttributes());

            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataSets.get(i));
            File destFile = new File(dirPath + File.separator + dataSetPrefix + i + ".arff");
            saver.setFile(destFile);

            saver.writeBatch();
        }
    }

    public static void saveSingleInstances(final Instances dataSet, final String filePath)
            throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataSet);
        File destFile = new File(filePath);
        saver.setFile(destFile);

        saver.writeBatch();
    }

    public static List<Instances> readInstances(final String dirPath, final String dataSetPrefix,
                                                final String excludePostfix) {
        List<Instances> results = new ArrayList<>();

        File dir = new File(dirPath);
        File[] dataSetFiles = dir.listFiles((dir1, name) -> {
            if (excludePostfix != null)
                return name.startsWith(dataSetPrefix) && !name.endsWith(excludePostfix);
            else
                return name.startsWith(dataSetPrefix);
        });
        for (File file : dataSetFiles) {
            try {
                DataSource source = new DataSource(new FileInputStream(file));
                Instances insts = source.getDataSet();
                insts.setClassIndex(insts.numAttributes() - 1);

                results.add(insts);

            } catch (Exception e) {
                logger.error("Could not import data set. Reason: {}", e.getMessage());
                return new ArrayList<>();
            }
        }

        return results;
    }

    public static Instances readSingleInstances(final String file) {
        File inputFile = new File(file);
        if (!inputFile.exists()) {
            logger.warn("File {} does not exist!", file);
            return null;
        }

        try {
            DataSource source = new DataSource(new FileInputStream(inputFile));

            Instances insts = source.getDataSet();
            insts.setClassIndex(insts.numAttributes() - 1);
            return insts;
        } catch (FileNotFoundException e) {
            logger.warn("Could not find file {}. Got exception {}.", file, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("Got the following exception when trying to read instances from file: {}", e.getMessage());
            return null;
        }

    }

    public static void writeDoubleArrayToFile(final double[] array, final String filePath, final String delimiter) {
        try (FileWriter fw = new FileWriter(new File(filePath))) {
            for (double v : array) {
                fw.append(Double.toString(v));
                fw.append(delimiter);
            }
            fw.flush();
        } catch (IOException e) {
            logger.warn("Got IOException when writing double array to file.", e);
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
            logger.warn("Got IOException when writing double array to file.", e);
        }
        return new double[]{};
    }
}
