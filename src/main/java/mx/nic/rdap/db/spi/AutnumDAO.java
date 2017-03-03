package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Autnum;
import mx.nic.rdap.db.exception.RdapDataAccessException;

/**
 * Interface for {@link Autnum} DAO functions.
 */
public interface AutnumDAO extends DataAccessDAO {

	/**
	 * Gets an {@link Autnum} object by its registered block.
	 * 
	 * @param autnumValue
	 *            An Autonomous System Number in the range of it´s registered
	 *            block.
	 * @return The {@link Autnum} object related to <code>autnumValue</code>.
	 */
	public Autnum getByRange(Long autnumValue) throws RdapDataAccessException;

}