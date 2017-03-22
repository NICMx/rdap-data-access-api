package mx.nic.rdap.db.spi;

import java.net.InetAddress;

import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.exception.RdapDataAccessException;

/**
 * Interface for {@link IpNetwork} DAO functions.
 */
public interface IpNetworkDAO extends DAO {

	/**
	 * Retrieves the smallest IP network that completely encompasses
	 * <code>ipAddress</code>.
	 * 
	 * @param ipAddress
	 *            The IPv4 or IPv6 address the user wants to retrieve the
	 *            network from.
	 * @return the smallest IP network that completely encompasses
	 *         <code>ipAddress</code>.
	 */
	public IpNetwork getByInetAddress(InetAddress ipAddress) throws RdapDataAccessException;

	/**
	 * Retrieves the smallest IP network that completely encompasses the
	 * <code>ipAddress</code>/<code>cidr</code> address block.
	 * 
	 * @param ipAddress
	 *            The address part of the prefix the client is looking up.
	 * @param cidr
	 *            The prefix length of the network the client is looking up.
	 * @return the smallest IP network that completely encompasses the
	 *         <code>ipAddress</code>/<code>cidr</code> address block.
	 */
	public IpNetwork getByInetAddress(InetAddress ipAddress, int cidr) throws RdapDataAccessException;

}
