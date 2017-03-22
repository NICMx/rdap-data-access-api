package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Model for the {@link Nameserver} Object
 * 
 */
public interface NameserverDAO extends DAO {

	/**
	 * Retrieves a {@link Nameserver} object by name.
	 * 
	 * @param name
	 *            Fully qualified host name of the nameserver being looked up.
	 * @return The {@link Nameserver} object whose fully qualified host name is
	 *         <code>name</code>.
	 */
	public Nameserver getByName(String name) throws RdapDataAccessException;

	/**
	 * Gets a list of {@link Nameserver} objects using a search pattern
	 * referring to their names or their full name.
	 * 
	 * @param namePattern
	 *            A search pattern representing a nameserver's name. It can
	 *            contain an asterisk '*' to match zero or more trailing
	 *            characters at the end.
	 * @param resultLimit
	 *            Maximum number of nameservers that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up nameservers when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Nameserver}s that match <code>namePattern</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Nameserver> searchByName(String namePattern, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Gets a list of {@link Nameserver} objects using their IP addresses.
	 * 
	 * @param ipaddressPattern
	 *            A search pattern representing a nameserver's IP address.
	 * @param resultLimit
	 *            Maximum number of nameservers that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up nameservers when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Nameserver}s that match <code>ipaddressPattern</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Nameserver> searchByIp(String ipaddressPattern, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Gets a list of {@link Nameserver} objects using a regular expression
	 * referring to their names.
	 * 
	 * @param namePattern
	 *            A regular expression representing a pattern for nameserver's
	 *            name.
	 * @param resultLimit
	 *            Maximum number of nameservers that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up nameservers when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Nameserver}s that match <code>namePattern</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Nameserver> searchByRegexName(String namePattern, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Gets a list of {@link Nameserver} objects using a regular expression
	 * referring to their IP addresses.
	 * 
	 * @param ipaddressPattern
	 *            A regular expression representing a pattern for nameserver's
	 *            ip address.
	 * @param resultLimit
	 *            Maximum number of nameservers that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up nameservers when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Nameserver}s that match <code>ipaddressPattern</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Nameserver> searchByRegexIp(String ipaddressPattern, int resultLimit)
			throws RdapDataAccessException;

}
