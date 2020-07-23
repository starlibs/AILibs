package ai.libs.jaicore.processes;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ProcessUtilTest {

	@Test
	public void testGetPID() throws ProcessIDNotRetrievableException, IOException {
		Process p = new ProcessBuilder("ping", "127.0.0.1", "-n 3").start();
		int pid = ProcessUtil.getPID(p);
		assertTrue("Invalid process ID obtained", pid > 1);
		p.destroyForcibly();
	}

}
