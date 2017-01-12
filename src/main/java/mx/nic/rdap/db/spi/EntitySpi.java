package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Interface for {@link Entity} DAO functions.
 * 
 */
public interface EntitySpi {

	/**
	 * Stores an {@link Entity} object to database.
	 * 
	 * @param entity
	 *            The object to be stored.
	 * @return The ID assigned to the object.
	 */
	public long storeToDatabase(Entity entity) throws RdapDatabaseException;

	/**
	 * Gets an {@link Entity} object by its handle.
	 * 
	 * @param entityHandle
	 *            A string representing a registry unique identifier of the
	 *            entity.
	 * @return The {@link Entity} object related to the
	 *         <code>entityHanlde</code>.
	 */
	public Entity getByHandle(String entityHandle) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Entity} objects using a search pattern referring to
	 * their handles.
	 * 
	 * @param handle
	 *            A search pattern representing an entity identifier, it can
	 *            contain an asterisk '*' to match zero or more trailing
	 *            characters at the end.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A List of {@link Entity} objects related to the
	 *         <code>handle</code>.
	 */
	public SearchResultStruct searchByHandle(String handle, Integer resultLimit) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Entity} objects using a search pattern referring to
	 * their names.
	 * 
	 * @param vCardName
	 *            A search pattern representing an entity's full name in a
	 *            vCard, it can contain an asterisk '*' to match zero or more
	 *            trailing characters at the end.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Entity}
	 *         objects related to the <code>vCardName</code>.
	 */
	public SearchResultStruct searchByVCardName(String vCardName, Integer resultLimit) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Entity} objects using a regular expression
	 * referring to their handles.
	 * 
	 * @param regexHandle
	 *            A regular expression representing a search pattern for an
	 *            entity's handle. Must be base64url encoded.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Entity}
	 *         objects related to the <code>regexHandle</code>.
	 */
	public SearchResultStruct searchByRegexHandle(String regexHandle, Integer resultLimit) throws RdapDatabaseException;

	/**
	 * Gets a List of {@link Entity} objects using a regular expression
	 * referring to their names.
	 * 
	 * @param vCardName
	 *            A regular expression representing a search pattern for an
	 *            entity's full name in the vCard.
	 * @param resultLimit
	 *            Maximum number of results.
	 * @return A {@link SearchResultStruct} with a List of {@link Entity}
	 *         objects related to the <code>vCardName</code>.
	 */
	public SearchResultStruct searchByRegexVCardName(String vCardName, Integer resultLimit)
			throws RdapDatabaseException;

	/**
	 * Verifies if an {@link Entity} object exists by it's handle.
	 * 
	 * @param entityHandle
	 *            A string representing a registry unique identifier of the
	 *            entity.
	 * @return A boolean value that is true if the {@link Entity} exists.
	 */
	public boolean existByHandle(String entityHandle) throws RdapDatabaseException;

}
