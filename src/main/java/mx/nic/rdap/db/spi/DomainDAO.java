package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Domain;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Interface for {@link Domain} DAO functions.
 */
public interface DomainDAO extends DAO {

	/**
	 * Retrieves a {@link Domain} by Fully Qualified Domain Name (FQDN).
	 * 
	 * @param domainName
	 *            the FQDN of the desired domain.
	 * @return The {@link Domain} whose FQDN is <code>domainName</code>.
	 *         <p>
	 *         This function can return <code>null</code> to express that the
	 *         domain was not found.
	 */
	public Domain getByName(String domainName) throws RdapDataAccessException;

	/**
	 * Searches for domains by a ldhName in full or partial form.
	 * 
	 * @param namePattern
	 *            A search pattern representing a domain's name. It can contain
	 *            an asterisk '*' to match zero or more trailing characters at
	 *            the end.
	 * @param resultLimit
	 *            Maximum number of domains that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up domains when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Domain}s that match <code>namePattern</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Domain> searchByName(String namePattern, int resultLimit) throws RdapDataAccessException;

	/**
	 * Searches for domains by their nameserver's name in full or partial form.
	 * 
	 * @param nsName
	 *            A search pattern representing a domain's nameserver name. It
	 *            can contain an asterisk '*' to match zero or more trailing
	 *            characters at the end.
	 * @param resultLimit
	 *            Maximum number of domains that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up domains when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Domain}s whose nameservers' names match
	 *         <code>nsName</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Domain> searchByNsName(String nsName, int resultLimit) throws RdapDataAccessException;

	/**
	 * Searches for domains by nameserver IP in full or partial form.
	 * 
	 * @param ip
	 *            A search pattern representing a domain's nameserver IP
	 *            address.
	 * @param resultLimit
	 *            Maximum number of domains that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up domains when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Domain}s that contain at least one nameserver whose IP
	 *         address matches <code>ip</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Domain> searchByNsIp(String ip, int resultLimit) throws RdapDataAccessException;

	/**
	 * Searches for domains by their names, either unicode or ldh, using a
	 * regular expression.
	 * 
	 * @param regexName
	 *            A regular expression representing a pattern for a domain's
	 *            name.
	 * @param resultLimit
	 *            Maximum number of domains that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up domains when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Domain}s that match <code>regexName</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Domain> searchByRegexName(String regexName, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Searches for domains by their nameserver's name using a regular
	 * expression.
	 * 
	 * @param regexNsName
	 *            A regular expression representing a pattern for a domain's
	 *            nameserver name.
	 * @param resultLimit
	 *            Maximum number of domains that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up domains when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Domain}s whose nameservers' names match
	 *         <code>regexNsName</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Domain> searchByRegexNsName(String regexNsName, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Searches for domains by their nameserver's IP address using a regular
	 * expression.
	 * 
	 * @param ip
	 *            A regular expression representing a pattern for a domain's
	 *            nameserver IP address.
	 * @param resultLimit
	 *            Maximum number of domains that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up domains when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return {@link Domain}s that contain at least one nameserver whose IP
	 *         address matches <code>ip</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Domain> searchByRegexNsIp(String ip, int resultLimit) throws RdapDataAccessException;

}
