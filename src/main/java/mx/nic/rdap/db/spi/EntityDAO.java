package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Interface for {@link Entity} DAO functions.
 */
public interface EntityDAO extends DAO {

	/**
	 * Retrieves an {@link Entity} object by handle.
	 * 
	 * @param entityHandle
	 *            A string representing a registry-specific unique identifier of
	 *            the entity.
	 * @return The {@link Entity} whose registry-specific unique identifier is
	 *         <code>entityHandle</code>.
	 *         <p>
	 *         This function can return <code>null</code> to express that the
	 *         entity was not found.
	 */
	public Entity getByHandle(String entityHandle) throws RdapDataAccessException;

	/**
	 * Searches for entities using a search pattern referring to their handles.
	 * 
	 * @param handle
	 *            A search pattern representing an entity identifier. It can
	 *            contain an asterisk '*' to match zero or more trailing
	 *            characters at the end.
	 * @param resultLimit
	 *            Maximum number of entities that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up entities when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return Entities that match <code>handle</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Entity> searchByHandle(String handle, int resultLimit) throws RdapDataAccessException;

	/**
	 * Searches for entities using a search pattern referring to their names.
	 * 
	 * @param vCardName
	 *            A search pattern representing an entity's full name in a
	 *            vCard. It can contain an asterisk '*' to match zero or more
	 *            trailing characters at the end.
	 * @param resultLimit
	 *            Maximum number of entities that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up entities when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return Entities that match <code>vCardName</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Entity> searchByVCardName(String vCardName, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Searches for entities using a regular expression referring to their
	 * handles.
	 * 
	 * @param regexHandle
	 *            A regular expression representing a search pattern for an
	 *            entity's handle. Must be base64url encoded.
	 * @param resultLimit
	 *            Maximum number of entities that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up entities when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return Entities that match <code>regexHandle</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Entity> searchByRegexHandle(String regexHandle, int resultLimit)
			throws RdapDataAccessException;

	/**
	 * Searches for entities using a regular expression referring to their
	 * names.
	 * 
	 * @param vCardName
	 *            A regular expression representing a search pattern for an
	 *            entity's full name in the vCard.
	 * @param resultLimit
	 *            Maximum number of entities that should be listed in the
	 *            resulting {@link SearchResultStruct}. The implementation of
	 *            this method can choose to stop adding up entities when this
	 *            limit is reached for the sake of performance, but if not, the
	 *            caller is expected truncate the response accordingly.
	 * @return Entities that match <code>vCardName</code>.
	 *         <p>
	 *         Empty result sets can be expressed by <code>null</code> or an
	 *         empty {@link SearchResultStruct}.
	 */
	public SearchResultStruct<Entity> searchByRegexVCardName(String vCardName, int resultLimit)
			throws RdapDataAccessException;

}
