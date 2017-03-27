package mx.nic.rdap.db.exception.http;

/**
 * Any method from a data access implementation can throw this exception to
 * signal that the Registry does not intend to provide the relevant
 * functionality. If the server is HTTP, this will become a 501.
 */
public class NotImplementedException extends HttpException {

	private static final long serialVersionUID = 1L;
	private static final int CODE = 501;
	private static final String DEFAULT_MSG = "Not Implemented";

	public NotImplementedException() {
		super(CODE, DEFAULT_MSG);
	}

	public NotImplementedException(String message) {
		super(CODE, message);
	}

	public NotImplementedException(Throwable cause) {
		super(CODE, DEFAULT_MSG, cause);
	}

	public NotImplementedException(String message, Throwable cause) {
		super(CODE, message, cause);
	}

}
