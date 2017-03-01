package mx.nic.rdap.db.exception;

/**
 * An error signaling that the method the user requested is not implemented
 * database. Going to become an HTTP 501.
 */
public class NotImplementedException extends RdapDataAccessException {

	/**
	 * Random Serial
	 */
	private static final long serialVersionUID = 6504903692096575790L;

	public NotImplementedException() {
	}

	public NotImplementedException(String message) {
		super(message);
	}

	public NotImplementedException(Throwable cause) {
		super(cause);
	}

	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}

}
