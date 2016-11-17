package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.Variant;

/**
 * Data access class for the {@link Variant} object.
 * 
 */
public class VariantDAO extends Variant implements DatabaseObject {

	/**
	 * Default constructor
	 */
	public VariantDAO() {
		super();
	}

	/**
	 * Constructor with result set
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	public VariantDAO(ResultSet resultSet) throws SQLException {
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
		this.setId(resultSet.getLong("var_id"));
		this.setIdnTable(resultSet.getString("var_idn_table"));
		this.setDomainId(resultSet.getLong("dom_id"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.server.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, this.getIdnTable());
		preparedStatement.setLong(2, this.getDomainId());
	}

	/* (non-Javadoc)
	 * @see mx.nic.rdap.db.DatabaseObject#updateInDatabase(java.sql.PreparedStatement)
	 */
	@Override
	public void updateInDatabase(PreparedStatement preparedStatement) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
