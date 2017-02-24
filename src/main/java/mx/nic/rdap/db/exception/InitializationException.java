package mx.nic.rdap.db.exception;

public class InitializationException extends RdapDataAccessException {

	private static final long serialVersionUID = 1L;

	public InitializationException(String message) {
		super(message);
	}

	public InitializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
