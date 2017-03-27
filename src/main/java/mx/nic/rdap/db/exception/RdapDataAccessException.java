package mx.nic.rdap.db.exception;

/**
 * A checked exception implementations are free to extend from and throw when
 * there's a problem accessing data.
 */
public class RdapDataAccessException extends Exception {

	private static final long serialVersionUID = 3344319049605710358L;

	public RdapDataAccessException() {
		super();
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
