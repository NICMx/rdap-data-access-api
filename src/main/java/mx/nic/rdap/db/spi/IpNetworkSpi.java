package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.IpAddress;
import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.exception.RdapDatabaseException;

/**
 * Interface for {@link IpNetwork} DAO functions.
 * 
 */
public interface IpNetworkSpi {

	/**
	 * Stores an {@link IpNetwork} object to database.
	 * 
	 * @param ipNetwork
	 *            The object to be stored.
	 * @return The ID assigned to the object.
	 */
	public Long storeToDatabase(IpNetwork ipNetwork) throws RdapDatabaseException;

	/**
	 * Gets an {@link IpNetwork} object by its ipAddress.
	 * 
	 * @param ipAddress
	 *            A string representing an ipNetwork ipAddress. This ip address
	 *            can also be in it's decimal equivalent.
	 * @return The {@link IpAddress} object related to the
	 *         <code>ipAddress</code>.
	 */
	public IpNetwork getByInetAddress(String ipAddress) throws RdapDatabaseException;

	/**
	 * Gets an {@link IpNetwork} object by its ipAddress and validates if it has
	 * a valid CIDR.
	 * 
	 * @param ipAddress
	 *            A String representing an ipNetwork's ipAddress. This ip
	 *            address can also be in it's decimal equivalent.
	 * @param cidr
	 *            An Integer containing the cidr value of the {@link IpNetwork}
	 *            object.
	 * @return The {@link IpAddress} object related to the
	 *         <code>ipAddress</code>.
	 */
	public IpNetwork getByInetAddress(String ipAddress, Integer cidr) throws RdapDatabaseException;

	/**
	 * Verifies if an {@link IpNetwork} object exist by it's ipAddress.
	 * 
	 * @param ipAddress
	 *            A string representing an ipNetwork ipAddress. This ip address
	 *            can also be in it's decimal equivalent.
	 * @return A boolean value which is true if the {@link IpNetwork} exists in
	 *         the database.
	 */
	public boolean existByInetAddress(String ipAddress, Integer cidr) throws RdapDatabaseException;

	/**
	 * Verifies if an {@link IpNetwork} object exist by it's ipAddress and CIDR.
	 * 
	 * @param ipAddress
	 *            A String representing an ipNetwork's ipAddress. This ip
	 *            address can also be in it's decimal equivalent.
	 * @param cidr
	 *            An Integer containing the CIDR value of the {@link IpNetwork}
	 *            object.
	 * @return A boolean value which is true if the {@link IpNetwork} exists in
	 *         the database and it has a valid CIDR.
	 */
	public boolean existByInetAddress(String ipAddress) throws RdapDatabaseException;

}