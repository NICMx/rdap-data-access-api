package mx.nic.rdap.db.exception;

/**
 * Any method from a data access implementation can throw this exception to
 * signal that the Registry does not intend to provide the relevant
 * functionality, and as such the RDAP server should respond HTTP 501 whenever
 * it is queried for it.
 */
public class NotImplementedException extends RdapDataAccessException {

	/**
	 * Random Serial
	 */
	private static final long serialVersionUID = 6504903692096575790L;

	public NotImplementedException() {
	}

	/**
	 * @param message
	 *            This text is going to be included in the 501's error message.
	 */
	public NotImplementedException(String message) {
		super(message);
	}

	public NotImplementedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 *            This text is going to be included in the 501's error message.
	 */
	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}

}
