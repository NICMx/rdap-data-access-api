package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Model for the {@link Nameserver} Object
 * 
 */
public interface NameserverSpi {

	/**
	 * Stores a {@link Nameserver} object to database.
	 * 
	 * @param nameserver
	 *            The object to be stored.
	 * @return The ID assigned to the object.
	 * @throws RdapDatabaseException
	 */
	public void storeToDatabase(Nameserver nameserver) throws RdapDatabaseException;

	/**
	 * Gets a {@link Nameserver} object by its name.
	 * 
	 * @param name
	 *            A string containing the name of the nameserver.
	 * @return The {@link Nameserver} object related to the <code>name</code>.
	 * @throws RdapDatabaseException
	 */
	public Nameserver getByName(String name) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Nameserver} objects using a search pattern
	 * referring to their names or their full name.
	 * 
	 * @param namePattern
	 *            A search pattern representing a nameserver's name, it can
	 *            contain an asterisk '*' to match zero or more trailing
	 *            characters at the end.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Nameserver}
	 *         objects related to the <code>namePattern</code>.
	 * @throws RdapDatabaseException
	 */
	public SearchResultStruct searchByName(String namePattern, Integer resultLimit) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Nameserver} objects using their ip addresses
	 * 
	 * @param ipaddressPattern
	 *            A search pattern representing a nameserver's ip address.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Nameserver}
	 *         objects related to the <code>ipaddressPattern</code>.
	 */
	public SearchResultStruct searchByIp(String ipaddressPattern, Integer resultLimit) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Nameserver} objects using a regular expression
	 * referring to their names.
	 * 
	 * @param namePattern
	 *            A regular expression representing a pattern for nameserver's
	 *            name.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Nameserver}
	 *         objects related to the <code>namePattern</code>.
	 */
	public SearchResultStruct searchByRegexName(String namePattern, Integer resultLimit) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Nameserver} objects using a regular expression
	 * referring to their ip addresses.
	 * 
	 * @param ipaddressPattern
	 *            A regular expression representing a pattern for nameserver's
	 *            ip address.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Nameserver}
	 *         objects related to the <code>ipaddressPattern</code>.
	 */
	public SearchResultStruct searchByRegexIp(String ipaddressPattern, Integer resultLimit)
			throws RdapDatabaseException;

	/**
	 * Verifies if a {@link Nameserver} object exists by it's name.
	 * 
	 * @param name
	 *            A string representing a nameserver´s name.
	 * @return A boolean value that is true if the {@link Nameserver} exists.
	 */
	public boolean existByName(String name) throws RdapDatabaseException;

}
