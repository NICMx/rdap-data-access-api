package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Model for the {@link Nameserver} Object
 * 
 */
public interface NameserverSpi {

	public void storeToDatabase(Nameserver nameserver) throws RdapDatabaseException;

	public Nameserver getByName(String name) throws RdapDatabaseException;

	public SearchResultStruct searchByName(String namePattern, Integer resultLimit) throws RdapDatabaseException;

	public SearchResultStruct searchByIp(String ipaddressPattern, Integer resultLimit) throws RdapDatabaseException;

	public SearchResultStruct searchByRegexName(String namePattern, Integer resultLimit) throws RdapDatabaseException;

	public SearchResultStruct searchByRegexIp(String ipaddressPattern, Integer resultLimit)
			throws RdapDatabaseException;

	public boolean existByName(String name) throws RdapDatabaseException;

}
