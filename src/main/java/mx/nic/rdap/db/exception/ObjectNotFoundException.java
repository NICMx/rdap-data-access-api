package mx.nic.rdap.db.exception;

/**
 * An error signaling that the object the user requested was not found in the
 * database. Going to become an HTTP 404.
 */
public class ObjectNotFoundException extends RdapDataAccessException {

	private static final long serialVersionUID = 1L;

	public ObjectNotFoundException() {
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}

	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
