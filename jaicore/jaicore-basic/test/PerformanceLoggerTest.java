package jaicore.basic;

import org.junit.Test;

public class PerformanceLoggerTest {

	@Test
	public void test() {
		PerformanceLogger.logStart();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
		PerformanceLogger.logEnd();

		System.out.println(PerformanceLogger.getPerformanceLog());
	}

}
