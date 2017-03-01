package mx.nic.rdap.db.spi;

import java.util.Properties;

import mx.nic.rdap.db.exception.InitializationException;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.db.service.DataAccessService;

/**
 * The interface of the main/central class of the implementation.
 * <p>
 * It's mainly just a hub that references the several implementation classes
 * that actually take care of the data access.
 */
public interface DataAccessImplementation {

	/**
	 * Can be used by the implementation to initialize itself.
	 * <p>
	 * Users of this library are not supposed to call this method. Call
	 * {@link DataAccessService#initialize(Properties)} instead.
	 * 
	 * @param properties
	 *            user-supplied configuration.
	 */
	public void init(Properties properties) throws InitializationException;

	/**
	 * Returns an instance of the implementation class that retrieves Autonomous
	 * System Number data from whatever source the implementation is wrapping.
	 * <p>
	 * Not all implementations are expected to provide ASN data. In such cases,
	 * this function is expected to return <code>null</code>.
	 */
	public AutnumDAO getAutnumDAO() throws RdapDataAccessException;

	/**
	 * Returns an instance of the implementation class that retrieves domain
	 * data from whatever source the implementation is wrapping.
	 * <p>
	 * Not all implementations are expected to provide domain data. In such
	 * cases, this function is expected to return <code>null</code>.
	 */
	public DomainDAO getDomainDAO() throws RdapDataAccessException;

	/**
	 * Returns an instance of the implementation class that retrieves entity
	 * data from whatever source the implementation is wrapping.
	 * <p>
	 * Not all implementations are expected to provide entity data. In such
	 * cases, this function is expected to return <code>null</code>.
	 */
	public EntityDAO getEntityDAO() throws RdapDataAccessException;

	/**
	 * Returns an instance of the implementation class that retrieves IP network
	 * data from whatever source the implementation is wrapping.
	 * <p>
	 * Not all implementations are expected to provide IP network data. In such
	 * cases, this function is expected to return <code>null</code>.
	 */
	public IpNetworkDAO getIpNetworkDAO() throws RdapDataAccessException;

	/**
	 * Returns an instance of the implementation class that retrieves nameserver
	 * data from whatever source the implementation is wrapping.
	 * <p>
	 * Not all implementations are expected to provide nameserver data. In such
	 * cases, this function is expected to return <code>null</code>.
	 */
	public NameserverDAO getNameserverDAO() throws RdapDataAccessException;

	/**
	 * Returns an instance of the implementation class that retrieves user data
	 * from whatever source the implementation is wrapping.
	 * <p>
	 * Not all implementations are expected to provide user data. In such cases,
	 * this function is expected to return <code>null</code>.
	 */
	public RdapUserDAO getRdapUserDAO() throws RdapDataAccessException;

}
