package ai.libs.python;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.basic.StringUtil;

public class PythonUtilTest extends ATest {

	private static final File SIMPLE_SCRIPT_FILE = new File("testrsc/simple_python_script.py");
	private static final File SIMPLE_PARAM_SCRIPT_FILE = new File("testrsc/simple_parameterized_python_script.py");

	private static PythonUtil util;
	private static String randomStringForNegativeCheck;

	@BeforeAll
	public static void setup() {
		util = new PythonUtil();
		util.setLoggerName(ATest.LOGGER.getName() + "." + PythonUtil.class);
		randomStringForNegativeCheck = StringUtil.getRandomString(10, new char[] { 'a', 'b', 'c', 'd', 'e' }, 0);
	}

	@Test
	public void testGetInstalledVersion() throws IOException, InterruptedException {
		String installedVersion = util.getInstalledVersion();
		assertTrue(installedVersion.matches("[0-9]+\\.[0-9]+\\.[0-9]+"), "Installed Python version could not be retrieved or has incorrect format: " + installedVersion);
	}

	@Test
	public void testIsSingleModuleInstalled() throws IOException, InterruptedException {
		assertTrue(util.isModuleInstalled("os"), "Cannot check whether the os module is installed which should always be available.");
		assertFalse(util.isModuleInstalled(randomStringForNegativeCheck), "Apparently there seems to be a module installed named like the randomly generated string: " + randomStringForNegativeCheck);
	}

	@Test
	public void testGetMissingModules() throws IOException, InterruptedException {
		List<String> moduleList = Arrays.asList("os", randomStringForNegativeCheck);
		List<String> missingModules = util.getMissingModules(moduleList);
		assertEquals(Arrays.asList(randomStringForNegativeCheck), missingModules, "List of missing modules is not as expected.");
	}

	@Test
	public void testExecuteScript() throws IOException, InterruptedException {
		String scriptToExec = "print('Hello World')";
		assertEquals(util.executeScript(scriptToExec).trim(), "Hello World", "Script has not been executed correctly or generated non-expected outputs.");
	}

	@Test
	public void testExecuteScriptFile() throws IOException, InterruptedException {
		assertEquals(0, util.executeScriptFile(Arrays.asList(SIMPLE_SCRIPT_FILE.getCanonicalPath())));
	}

	@Test
	public void testExecuteScriptFileAndGetOutput() throws IOException, InterruptedException {
		assertEquals("Hello World", util.executeScriptFileAndGetOutput(Arrays.asList(SIMPLE_SCRIPT_FILE.getCanonicalPath())).trim());
	}

	@Test
	public void testExecuteScriptFileWithParamsAndGetOutput() throws IOException, InterruptedException {
		assertEquals("Hello " + randomStringForNegativeCheck, util.executeScriptFileAndGetOutput(Arrays.asList(SIMPLE_PARAM_SCRIPT_FILE.getCanonicalPath(), randomStringForNegativeCheck)).trim());
	}

}
