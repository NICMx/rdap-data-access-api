package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.Domain;

/**
 * Data access class for the {@link Domain} object.
 * 
 */
public class DomainDAO extends Domain implements DatabaseObject {

	/**
	 * Default Constructor
	 */
	public DomainDAO() {
		super();
	}

	/**
	 * Construct Domain from a ResultSet
	 * 
	 * @param resultSet
	 *            Information from a select statement on database
	 */
	public DomainDAO(ResultSet resultSet) throws SQLException {
		super();
		loadFromDatabase(resultSet);
	}

	/**
	 * Loads the information coming from the database in an instance of Domain
	 * 
	 * @param resultSet
	 *            ResultSet from where all information is obtained
	 * @throws SQLException
	 *             If there is an error during ResultSet access
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		this.setId(resultSet.getLong("dom_id"));
		this.setHandle(resultSet.getString("dom_handle"));
		this.setLdhName(resultSet.getString("dom_ldh_name"));
		this.setPort43(resultSet.getString("dom_port43"));
		this.setZoneId(resultSet.getInt("zone_id"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.server.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, this.getHandle());
		preparedStatement.setString(2, this.getLdhName());
		preparedStatement.setString(3, this.getPort43());
		preparedStatement.setInt(4, this.getZoneId());

	}

}
