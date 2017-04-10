package mx.nic.rdap.db.spi;

import mx.nic.rdap.db.RdapUser;
import mx.nic.rdap.db.exception.RdapDataAccessException;

/**
 * Interface for {@link RdapUser} DAO functions.
 */
public interface RdapUserDAO extends DAO {

	/**
	 * Returns the user whose username is <code>username</code>.
	 */
	public RdapUser getByUsername(String username) throws RdapDataAccessException;

}
