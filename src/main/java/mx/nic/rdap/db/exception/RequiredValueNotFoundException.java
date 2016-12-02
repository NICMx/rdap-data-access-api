package mx.nic.rdap.db.exception;

/**
 * Problems when a necessary value wasn't set
 */
public class RequiredValueNotFoundException extends RdapDatabaseException {

	private static final long serialVersionUID = 1L;

	public RequiredValueNotFoundException(String attributeName, String className) {
		super("Missing required value: " + attributeName + " in Class: " + className);
	}

}
