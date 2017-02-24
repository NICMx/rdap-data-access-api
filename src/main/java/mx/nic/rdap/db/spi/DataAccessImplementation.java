package mx.nic.rdap.db.spi;

import java.util.Properties;

import mx.nic.rdap.db.exception.InitializationException;

public interface DataAccessImplementation {

	public void init(Properties properties) throws InitializationException;

	public AutnumDAO getAutnumDAO();
	public DomainDAO getDomainDAO();
	public EntityDAO getEntityDAO();
	public IpNetworkDAO getIpNetworkDAO();
	public NameserverDAO getNameserverDAO();
	public RdapUserDAO getRdapUserDAO();

}
