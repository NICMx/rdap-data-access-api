package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.DsData;

/**
 * Data access class for the DsData Object. The DsData is one of the
 * representations of the SecureDNS information that is not stored in the
 * registration database.
 * 
 * @author evaldes
 *
 */
public class DsDataDAO extends DsData implements DatabaseObject {

	/**
	 * Default constructor
	 */
	public DsDataDAO() {
		super();
	}

	/**
	 * Construct DsData from a ResultSet
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	public DsDataDAO(ResultSet resultSet) throws SQLException {
		super();
		loadFromDatabase(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mx.nic.rdap.server.db.DatabaseObject#loadFromDatabase(java.sql.ResultSet)
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		this.setId(resultSet.getLong("dsd_id"));
		this.setKeytag(resultSet.getInt("dsd_keytag"));
		this.setAlgorithm(resultSet.getInt("dsd_algorithm"));
		this.setDigest(resultSet.getString("dsd_digest"));
		this.setDigestType(resultSet.getInt("dsd_digest_type"));
		this.setSecureDNSId(resultSet.getLong("sdns_id"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.server.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setLong(1, this.getSecureDNSId());
		preparedStatement.setInt(2, this.getKeytag());
		preparedStatement.setInt(3, this.getAlgorithm());
		preparedStatement.setString(4, this.getDigest());
		preparedStatement.setInt(5, this.getDigestType());

	}


}
