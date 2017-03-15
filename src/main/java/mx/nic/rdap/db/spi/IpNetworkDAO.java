package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.IpAddress;
import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.exception.RdapDataAccessException;

/**
 * Interface for {@link IpNetwork} DAO functions.
 * 
 */
public interface IpNetworkDAO extends DAO {

	/**
	 * Retrieves the smallest IP network that completely encompasses
	 * <code>ipAddress</code>.
	 * 
	 * @param ipAddress
	 *            A string representing an IP address. This ip address can also
	 *            be in it's decimal equivalent.
	 *            <p>
	 *            TODO NO. This should receive an InetAddress, not a String. The
	 *            server should be the one worrying about validating and parsing
	 *            the thing, not every single implementation.
	 *            <p>
	 *            Also, where does it say that we're supposed to support the
	 *            "decimal equivalent" representation?
	 * @return The {@link IpAddress} object related to the
	 *         <code>ipAddress</code>.
	 */
	public IpNetwork getByInetAddress(String ipAddress) throws RdapDataAccessException;

	/**
	 * Retrieves the smallest IP network that completely encompasses the
	 * <code>ipAddress</code>/<code>cidr</code> address block.
	 * 
	 * @param ipAddress
	 *            A String representing an ipNetwork's ipAddress. This ip
	 *            address can also be in it's decimal equivalent.
	 *            <p>
	 *            TODO NO. This should receive an InetAddress, not a String. The
	 *            server should be the one worrying about validating and parsing
	 *            the thing, not every single implementation.
	 *            <p>
	 *            Also, where does it say that we're supposed to support the
	 *            "decimal equivalent" representation?
	 * @param cidr
	 *            An Integer containing the cidr value of the {@link IpNetwork}
	 *            object.
	 * @return The {@link IpAddress} object related to the
	 *         <code>ipAddress</code>.
	 */
	public IpNetwork getByInetAddress(String ipAddress, Integer cidr) throws RdapDataAccessException;

}
