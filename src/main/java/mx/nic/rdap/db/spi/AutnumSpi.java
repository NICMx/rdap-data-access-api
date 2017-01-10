package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Autnum;
import mx.nic.rdap.db.exception.RdapDatabaseException;

/**
 * Interface for {@link Autnum} DAO functions.
 */
public interface AutnumSpi {

	/**
	 * Stores an {@link Autnum} object to database.
	 * 
	 * @param autnum
	 *            The object to be stored
	 * @return The ID assigned to the object.
	 */
	public Long storeToDatabase(Autnum autnum) throws RdapDatabaseException;

	/**
	 * Gets an {@link Autnum} object by its registered block.
	 * 
	 * @param autnumValue
	 *            TODO find a description
	 * @return The {@link Autnum} object related to <code>autnumValue</code>
	 */
	public Autnum getByRange(Long autnumValue) throws RdapDatabaseException;

	public boolean existByRange(Long autnumValue) throws RdapDatabaseException;

}