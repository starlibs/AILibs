package de.upb.crc901.mlplan.metamining;

/**
 * Indicates Runtime failures for the {@link WEKAMetaminer}.
 * 
 * @author Helena Graf
 *
 */
public class WEKAMetaminerRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1246022912468302026L;

    public WEKAMetaminerRuntimeException(String message) {
        super(message);
    }

    public WEKAMetaminerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public WEKAMetaminerRuntimeException(Throwable cause) {
        super(cause);
    }
}
