package jaicore.processes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.stream.Collectors;

import com.sun.jna.Pointer;

public class ProcessUtil {

	public static OS getOS() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("windows") > -1)
			return OS.WIN;
		if (osName.indexOf("linux") > -1)
			return OS.LINUX;
		throw new UnsupportedOperationException("Cannot detect operating system " + osName);
	}

	public static Process getProcessListProcess() throws IOException {
		OS os = getOS();
		switch (os) {
		case WIN:
			return Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
		case LINUX:
			return Runtime.getRuntime().exec("ps -e -o user,pid,ppid,c,size,cmd");
		}
		throw new UnsupportedOperationException("No action defined for OS " + os);
	}
	
	public static Collection<ProcessInfo> getRunningJavaProcesses() throws IOException {
		return new ProcessList().stream().filter(pd -> pd.getDescr().startsWith("java")).collect(Collectors.toList());
//		return new ProcessList();
	}
	
	public static int getPID(Process process) {
		Integer pid;
		try {
			if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
				/* get the PID on unix/linux systems */
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getInt(process);
				return pid;

			} else if (process.getClass().getName().equals("java.lang.Win32Process") || process.getClass().getName().equals("java.lang.ProcessImpl")) {

				/* determine the pid on windows plattforms */
				Field f = process.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				long handl = f.getLong(process);

				Kernel32 kernel = Kernel32.INSTANCE;
				W32API.HANDLE handle = new W32API.HANDLE();
				handle.setPointer(Pointer.createConstant(handl));
				pid = kernel.GetProcessId(handle);
				return pid;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		throw new UnsupportedOperationException();
	}
	
	public static void killProcess(int pid) throws IOException {
		Runtime rt = Runtime.getRuntime();
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
			rt.exec("taskkill /F /PID " + pid);
		else
			rt.exec("kill -9 " + pid);
	}
}
