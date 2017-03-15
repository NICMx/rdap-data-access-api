package mx.nic.rdap.db.exception;

/**
 * Any method from a data access implementation can throw this exception to
 * signal that the object lookup or search did not match any objects.
 * <p>
 * The same result can be usually attained by returning <code>null</code> or an
 * empty result set, which is in fact often the preferred method, but this
 * exception allows for a custom HTTP message response through the exception
 * message.
 */
public class ObjectNotFoundException extends RdapDataAccessException {

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException() {
	}

	/**
	 * @param message
	 *            This text is going to be included in the 404's error message.
	 */
	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 *            This text is going to be included in the 404's error message.
	 */
	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
