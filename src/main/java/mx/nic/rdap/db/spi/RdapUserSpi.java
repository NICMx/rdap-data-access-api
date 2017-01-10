package mx.nic.rdap.db.spi;

import mx.nic.rdap.db.RdapUser;
import mx.nic.rdap.db.exception.RdapDatabaseException;

public interface RdapUserSpi {
	public Integer getMaxSearchResults(String username) throws RdapDatabaseException;

	public RdapUser getByUsername(String username) throws RdapDatabaseException;
}
