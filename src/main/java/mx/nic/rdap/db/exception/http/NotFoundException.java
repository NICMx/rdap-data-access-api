package mx.nic.rdap.db.exception.http;

/**
 * Whatever the user was trying to search was not found on this server. If the
 * server is HTTP, this errors translates into a 404.
 * <p>
 * "The requested resource could not be found but may be available in the
 * future. Subsequent requests by the client are permissible."
 * <p>
 * (Quoted from Wikipedia.)
 * <p>
 * Any method from a data access implementation can throw this exception to
 * signal that the object lookup or search did not match any objects.
 * <p>
 * The same result can be usually attained by returning <code>null</code> or an
 * empty result set, which is in fact often the preferred method (as there's
 * nothing exceptional about a lookup not yielding results), but this exception
 * allows for a custom HTTP message response through the exception message.
 */
public class NotFoundException extends HttpException {

	private static final long serialVersionUID = 1L;
	private static final int CODE = 404;
	private static final String DEFAULT_MSG = "Not Found";

	public NotFoundException() {
		super(CODE, DEFAULT_MSG);
	}

	public NotFoundException(String message) {
		super(CODE, message);
	}

	public NotFoundException(Throwable cause) {
		super(CODE, DEFAULT_MSG, cause);
	}

	public NotFoundException(String message, Throwable cause) {
		super(CODE, message, cause);
	}

}
