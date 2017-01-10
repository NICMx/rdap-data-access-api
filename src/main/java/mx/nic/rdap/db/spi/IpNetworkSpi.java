package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.exception.RdapDatabaseException;

/**
 * Model for the {@link IpNetworkSpi} Object
 * 
 */
public interface IpNetworkSpi {

	public Long storeToDatabase(IpNetwork ipNetwork) throws RdapDatabaseException;

	public IpNetwork getByInetAddress(String ipAddress) throws RdapDatabaseException;

	public IpNetwork getByInetAddress(String ipAddress, Integer cidr) throws RdapDatabaseException;

	public boolean existByInetAddress(String ipAddress, Integer cidr) throws RdapDatabaseException;

	public boolean existByInetAddress(String ipAddress) throws RdapDatabaseException;

}
