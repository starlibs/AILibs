package ai.libs.jaicore.processes;

import com.sun.jna.Native;

public abstract class Kernel32 extends W32API {

	protected Kernel32() {
		/* no instantiation */
	}

	public static final Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class);

	/* http://msdn.microsoft.com/en-us/library/ms683179(VS.85).aspx */
	public abstract HANDLE getCurrentProcess();

	/* http://msdn.microsoft.com/en-us/library/ms683215.aspx */
	public abstract int getProcessId(HANDLE process);
}
