package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Domain;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Interface for {@link Domain} DAO functions.
 */
public interface DomainSpi {

	/**
	 * Stores a {@link Domain} object to database.
	 * 
	 * @param domain
	 *            the object to be stored
	 * @return The ID assigned to the object.
	 */
	public Long storeToDatabase(Domain domain) throws RdapDatabaseException;

	/**
	 * Search for a {@link Domain} by its fqdn.
	 * 
	 * @param domainName
	 *            the <code>fqdn</code> of the object to be searched
	 * @return a {@link Domain} related to <code>ldhName</code>
	 */
	public Domain getByName(String domainName, Boolean useNsAsAttribute) throws RdapDatabaseException;

	/**
	 * Search for domains by a ldhName in full or partial form, as well as by a
	 * complete or partial zone.
	 * 
	 * @param domainName
	 *            Complete or partial domain name.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return TODO
	 */
	public SearchResultStruct searchByName(String domainName, Integer resultLimit,
			boolean useNameserverAsDomainAttribute) throws RdapDatabaseException;

	public SearchResultStruct searchByNsName(String nsName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException;

	public SearchResultStruct searchByNsIp(String ip, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException;

	public SearchResultStruct searchByRegexName(String regexName, Integer resultLimit,
			boolean useNameserverAsDomainAttribute) throws RdapDatabaseException;

	public SearchResultStruct searchByRegexNsName(String regexNsName, Integer resultLimit,
			boolean useNameserverAsDomainAttribute) throws RdapDatabaseException;

	public SearchResultStruct searchByRegexNsIp(String ip, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException;

	public boolean existByName(String domainName) throws RdapDatabaseException;
}
