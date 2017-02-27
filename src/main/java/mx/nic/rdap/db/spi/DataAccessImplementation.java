package mx.nic.rdap.db.spi;

import java.util.Properties;

import mx.nic.rdap.db.exception.InitializationException;
import mx.nic.rdap.db.service.DataAccessService;

public interface DataAccessImplementation {

	/**
	 * Users of this library are not supposed to call this method. Call
	 * {@link DataAccessService#initialize(Properties)} instead.
	 */
	public void init(Properties properties) throws InitializationException;

	public AutnumDAO getAutnumDAO();

	public DomainDAO getDomainDAO();

	public EntityDAO getEntityDAO();

	public IpNetworkDAO getIpNetworkDAO();

	public NameserverDAO getNameserverDAO();

	public RdapUserDAO getRdapUserDAO();

}
