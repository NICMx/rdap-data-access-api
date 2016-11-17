package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.Autnum;

/**
 * Data access class for the {@link Autnum} object.
 *
 */
public class AutnumDAO extends Autnum implements DatabaseObject {

	/**
	 * Default constructor
	 */
	public AutnumDAO() {
		super();
	}

	/**
	 * Constructs an Autonomous System Number using a {@link ResultSet}
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	public AutnumDAO(ResultSet resultSet) throws SQLException {
		super();
		loadFromDatabase(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.db.DatabaseObject#loadFromDatabase(java.sql.ResultSet)
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		this.setId(resultSet.getLong("asn_id"));
		this.setHandle(resultSet.getString("asn_handle"));
		this.setStartAutnum(resultSet.getLong("asn_start_autnum"));
		this.setEndAutnum(resultSet.getLong("asn_end_autnum"));
		this.setName(resultSet.getString("asn_name"));
		this.setType(resultSet.getString("asn_type"));
		this.setPort43(resultSet.getString("asn_port43"));
		this.setCountry(resultSet.getInt("ccd_id"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mx.nic.rdap.db.DatabaseObject#storeToDatabase(java.sql.PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, this.getHandle());
		preparedStatement.setLong(2, this.getStartAutnum());
		preparedStatement.setLong(3, this.getEndAutnum());
		preparedStatement.setString(4, this.getName());
		preparedStatement.setString(5, this.getType());
		preparedStatement.setString(6, this.getPort43());
		preparedStatement.setInt(7, this.getCountry());
	}

}