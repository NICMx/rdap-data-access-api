package mx.nic.rdap.db.exception;

/**
 * Generic problem in data access implementation.
 */
public class RdapDatabaseException extends Exception {

	private static final long serialVersionUID = 3344319049605710358L;

	public RdapDatabaseException() {
	}

	public RdapDatabaseException(String message) {
		super(message);
	}

	public RdapDatabaseException(Throwable cause) {
		super(cause);
	}

	public RdapDatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
