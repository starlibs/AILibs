package ai.libs.jaicore.processes;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public abstract class W32API implements StdCallLibrary, W32Errors {

	protected W32API() {
		/* avoid instantiation */
	}

	public static class HANDLE extends PointerType {

		public HANDLE() {
			super.setPointer(Pointer.createConstant(-1));
		}

		@Override
		public Object fromNative(final Object nativeValue, final FromNativeContext context) {
			Object o = super.fromNative(nativeValue, context);
			if (W32API.invalidHandleValue.equals(o)) {
				return W32API.invalidHandleValue;
			}
			return o;
		}
	}

	/** Standard options to use the unicode version of a w32 API. */
	private static Map<Object,Object> unicodeOptions = new HashMap<>();

	/** Standard options to use the ASCII/MBCS version of a w32 API. */
	private static Map<Object,Object> asciiOptions = new HashMap<>();

	/** Constant value representing an invalid HANDLE. */
	private static HANDLE invalidHandleValue;

	static {
		unicodeOptions.put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
		unicodeOptions.put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
		asciiOptions.put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
		asciiOptions.put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);

		invalidHandleValue = new HANDLE() {

			@Override
			public void setPointer(final Pointer p) {
				throw new UnsupportedOperationException("Immutable reference");
			}
		};
	}
}