package mx.nic.rdap.db.exception;

/**
 * Problems when a necessary value wasn'tset
 * <p>
 * TODO this exception has nothing to do with data access. It should probably
 * extend from InitializationException, moved to some other project or be
 * removed altogether.
 */
public class RequiredValueNotFoundException extends RdapDataAccessException {

	private static final long serialVersionUID = 1L;

	public RequiredValueNotFoundException(String attributeName, String className) {
		super("Missing required value: " + attributeName + " in Class: " + className);
	}

	public RequiredValueNotFoundException() {
	}

	public RequiredValueNotFoundException(String message) {
		super(message);
	}

	public RequiredValueNotFoundException(Throwable cause) {
		super(cause);
	}

	public RequiredValueNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
