package mx.nic.rdap.db.spi;

import mx.nic.rdap.db.RdapUser;
import mx.nic.rdap.db.exception.RdapDataAccessException;

public interface RdapUserDAO extends DataAccessDAO {

	public RdapUser getByUsername(String username) throws RdapDataAccessException;

}
