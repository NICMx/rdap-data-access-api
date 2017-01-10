package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Interface for {@link Entity} DAO functions.
 * 
 */
public interface EntitySpi {

	public long storeToDatabase(Entity entity) throws RdapDatabaseException;

	public Entity getByHandle(String entityHandle) throws RdapDatabaseException;

	public SearchResultStruct searchByHandle(String handle, Integer resultLimit) throws RdapDatabaseException;

	public SearchResultStruct searchByVCardName(String vCardName, Integer resultLimit) throws RdapDatabaseException;

	public SearchResultStruct searchByRegexHandle(String regexHandle, Integer resultLimit) throws RdapDatabaseException;

	public SearchResultStruct searchByRegexVCardName(String vCardName, Integer resultLimit) throws RdapDatabaseException;

	public boolean existByHandle(String entityHandle) throws RdapDatabaseException;

}
