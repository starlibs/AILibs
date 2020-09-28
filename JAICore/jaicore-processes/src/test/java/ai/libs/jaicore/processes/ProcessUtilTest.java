package ai.libs.jaicore.processes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ProcessUtilTest {

	@Test
	public void testGetPID() throws ProcessIDNotRetrievableException, IOException {
		Process p = new ProcessBuilder("ping", "127.0.0.1", "-n 3").start();
		int pid = ProcessUtil.getPID(p);
		assertTrue(pid > 1, "Invalid process ID obtained");
		p.destroyForcibly();
	}

}
