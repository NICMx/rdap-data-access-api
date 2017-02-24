package mx.nic.rdap.db.exception;

/**
 * Generic problem in data access implementation.
 */
public class RdapDataAccessException extends Exception {

	private static final long serialVersionUID = 3344319049605710358L;

	public RdapDataAccessException() {
	}

	public RdapDataAccessException(String message) {
		super(message);
	}

	public RdapDataAccessException(Throwable cause) {
		super(cause);
	}

	public RdapDataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

}
