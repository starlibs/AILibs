package ai.libs.hasco.pcsbasedoptimization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;

/**
 * Utility class for handling scenario file for optimizer scripts
 * @author kadirayk
 *
 */
public class ScenarioFileUtil {
	private static Logger logger = LoggerFactory.getLogger(SMACOptimizer.class);

	public static void updateMultipleParams(String filePath, Map<String, String> newParams) {
		Map<String, String> params = readAsKeyValuePairs(filePath);
		params.putAll(newParams);
		writeParamsToFile(filePath, params);
	}

	public static void updateParam(String filePath, String key, String value) {
		Map<String, String> params = readAsKeyValuePairs(filePath);
		params.put(key, value);
		writeParamsToFile(filePath, params);
	}

	private static void writeParamsToFile(String filePath, Map<String, String> params) {
		List<String> newLines = new ArrayList<>();
		for (Map.Entry<String, String> e : params.entrySet()) {
			newLines.add(e.getKey() + " = " + e.getValue());
		}
		try {
			FileUtil.writeFileAsList(newLines, filePath + "/scenario.txt");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static Map<String, String> readAsKeyValuePairs(String filePath) {
		List<String> lines = null;
		Map<String, String> params = new HashMap<>();
		try {
			lines = FileUtil.readFileAsList(filePath + "/scenario.txt");
			for (String line : lines) {
				String[] keyVal = line.split(" = ");
				params.put(keyVal[0], keyVal[1]);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return params;
	}

}
