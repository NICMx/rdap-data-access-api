package mx.nic.rdap.db.exception;

/**
 * Invalidad structure found
 * 
 */
public class InvalidadDataStructure extends RdapDatabaseException {

	private static final long serialVersionUID = 1L;

	public InvalidadDataStructure(String dataStructureName, String expectedStructure) {
		super("Invalid data structure:" + dataStructureName + " Must have the form" + expectedStructure);
	}

	public InvalidadDataStructure() {
		super();
	}

}