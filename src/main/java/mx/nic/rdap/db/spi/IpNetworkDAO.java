package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.db.struct.AddressBlock;

/**
 * Interface for {@link IpNetwork} DAO functions.
 */
public interface IpNetworkDAO extends DAO {

	/**
	 * Retrieves the smallest IP network that completely encompasses the
	 * <code>block</code> address block.
	 * 
	 * @param block
	 *            The IP address block the client is looking up.
	 * @return the smallest IP network that completely encompasses the
	 *         <code>ipAddress</code>/<code>cidr</code> address block.
	 */
	public IpNetwork getByAddressBlock(AddressBlock block) throws RdapDataAccessException;

}
