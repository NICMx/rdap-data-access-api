package mx.nic.rdap.db.exception;

/**
 * A checked exception implementations are free to extend from and throw when
 * there's a problem initializing themselves.
 */
public class InitializationException extends RdapDataAccessException {

	private static final long serialVersionUID = 1L;

	public InitializationException(String message) {
		super(message);
	}

	public InitializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
