package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Autnum;
import mx.nic.rdap.db.exception.RdapDatabaseException;

/**
 * Interface for {@link Autnum} DAO functions.
 */
public interface AutnumSpi {

	/**
	 * Gets an {@link Autnum} object by its registered block.
	 * 
	 * @param autnumValue
	 *            An Autonomous System Number in the range of it´s registered
	 *            block.
	 * @return The {@link Autnum} object related to <code>autnumValue</code>.
	 */
	public Autnum getByRange(Long autnumValue) throws RdapDatabaseException;

	/**
	 * Verifies if an {@link Autnum} object exists by it's registered block.
	 * 
	 * @param autnumValue
	 *            The Autonomous System Number in the range of it´s registered
	 *            block.
	 * @return A boolean value that is true if the {@link Autnum} exists.
	 */
	public boolean existByRange(Long autnumValue) throws RdapDatabaseException;

}