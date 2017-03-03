package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Domain;
import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Interface for {@link Domain} DAO functions.
 */
public interface DomainDAO extends DataAccessDAO {


	/**
	 * Search for a {@link Domain} by its fqdn.
	 * 
	 * @param domainName
	 *            the <code>fqdn</code> of the object to be searched
	 * @param useNsAsAttribute
	 *            Boolean value which defines if nameservers are being used as
	 *            attributes or as objects.
	 * @return a {@link Domain} related to <code>ldhName</code>
	 * @throws RdapDataAccessException
	 */
	public Domain getByName(String domainName, Boolean useNsAsAttribute) throws RdapDataAccessException;

	/**
	 * Search for domains by a ldhName in full or partial form, as well as by a
	 * complete or partial zone.
	 * 
	 * @param namePattern
	 *            A search pattern representing a domain's name, it can contain
	 *            an asterisk '*' to match zero or more trailing characters at
	 *            the end.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @param useNsAsAttribute
	 *            Boolean value which defines if nameservers are being used as
	 *            attributes or as objects.
	 * @return A {@link SearchResultStruct} with a List of {@link Nameserver}
	 *         objects related to the <code>namePattern</code>.
	 * @throws RdapDataAccessException
	 */
	public SearchResultStruct<Domain> searchByName(String domainName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDataAccessException;

	/**
	 * Search for domains by their nameserver's name in full or partial form.
	 * 
	 * @param nsName
	 *            A search pattern representing a domain's nameserver name, it
	 *            can contain an asterisk '*' to match zero or more trailing
	 *            characters at the end.
	 * @param resultLimit
	 *            Maximum number of results
	 * @param useNsAsAttribute
	 *            Boolean value which defines if nameservers are being used as
	 *            attributes or as objects.
	 * @return A {@link SearchResultStruct} containing a list of {@link Domain}
	 *         objects related to the <code>nsName</code>
	 * @throws RdapDataAccessException
	 */
	public SearchResultStruct<Domain> searchByNsName(String nsName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDataAccessException;

	/**
	 * Search for domains by their nameserver' ip in full or partial form.
	 * 
	 * @param ip
	 *            A search pattern representing a domain's nameserver ip
	 *            address.
	 * @param resultLimit
	 *            Maximum number of results
	 * @param useNsAsAttribute
	 *            Boolean value which defines if nameservers are being used as
	 *            attributes or as objects.
	 * @return A {@link SearchResultStruct} containing a list of {@link Domain}
	 *         objects related to the <code>ip</code>
	 * @throws RdapDataAccessException
	 */
	public SearchResultStruct<Domain> searchByNsIp(String ip, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDataAccessException;

	/**
	 * Search for domains by their names, either unicode or ldh, using a regular
	 * expression.
	 * 
	 * @param regexName
	 *            A regular expression representing a pattern for a domain's
	 *            name.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @param useNsAsAttribute
	 *            Boolean value which defines if nameservers are being used as
	 *            attributes or as objects.
	 * @return A {@link SearchResultStruct} containing a list of {@link Domain}
	 *         objects related to the <code>regexName</code>
	 * @throws RdapDataAccessException
	 */
	public SearchResultStruct<Domain> searchByRegexName(String regexName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDataAccessException;

	/**
	 * Search for domains by their nameserver's ip address using a regular
	 * expression.
	 * 
	 * @param ip
	 *            A regular expression representing a pattern for a domain's
	 *            nameserver ip address.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @param useNameserverAsDomainAttribute
	 *            Boolean value which defines if nameservers are being used as
	 *            attributes or as objects.
	 * @return A {@link SearchResultStruct} containing a list of {@link Domain}
	 *         objects related to the <code>ip</code>
	 * @throws RdapDataAccessException
	 */
	public SearchResultStruct<Domain> searchByRegexNsIp(String ip, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDataAccessException;

}
