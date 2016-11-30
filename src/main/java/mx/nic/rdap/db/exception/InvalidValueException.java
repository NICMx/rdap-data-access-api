package mx.nic.rdap.db.exception;

/**
 * Generic problems with the validation of the objects
 * 
 */
public class InvalidValueException extends RdapDatabaseException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param attributeName
	 *            the attribute missing
	 * @param className
	 *            the class
	 */
	public InvalidValueException(String attributeName, String className, Object value) {
		super("Invalid value of " + className + "." + attributeName + ": " + value.toString());
	}

	public InvalidValueException(String message) {
		super(message);
	}

	public InvalidValueException(Throwable cause) {
		super(cause);
	}

	public InvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
