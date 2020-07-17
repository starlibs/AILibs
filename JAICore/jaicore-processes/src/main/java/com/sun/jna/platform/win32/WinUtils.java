package com.sun.jna.platform.win32;

import java.lang.reflect.Field;

import com.sun.jna.Pointer;

public class WinUtils {

	private WinUtils() {
		// prevent instantiation of this class
	}

	public static Long getWindowsProcessId(final Process process) throws NoSuchFieldException, IllegalAccessException {

		/* determine the pid on windows plattforms */
		Field f = process.getClass().getDeclaredField("handle");
		f.setAccessible(true);
		long handl = f.getLong(process);
		Kernel32 kernel = Kernel32.INSTANCE;
		WinNT.HANDLE handle = new WinNT.HANDLE();
		handle.setPointer(Pointer.createConstant(handl));
		int ret = kernel.GetProcessId(handle);
		return Long.valueOf(ret);
	}

}
