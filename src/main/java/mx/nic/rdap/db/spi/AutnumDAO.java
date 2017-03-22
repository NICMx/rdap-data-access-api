package mx.nic.rdap.db.spi;

import mx.nic.rdap.core.db.Autnum;
import mx.nic.rdap.db.exception.RdapDataAccessException;

/**
 * Interface for {@link Autnum} DAO functions.
 */
public interface AutnumDAO extends DAO {

	/**
	 * Returns the {@link Autnum} block that contains Autonomous System Number
	 * (ASN) <code>autnum</code>.
	 * 
	 * @param autnum
	 *            The ASN, or one of the ASNs, contained in the {@link Autnum}
	 *            block desired.
	 * @return The {@link Autnum} block that contains ASN <code>autnum</code>.
	 *         <p>
	 *         This function can return <code>null</code> to express that none
	 *         of the registered blocks contain <code>autnum</code>.
	 */
	public Autnum getByRange(long autnum) throws RdapDataAccessException;

}