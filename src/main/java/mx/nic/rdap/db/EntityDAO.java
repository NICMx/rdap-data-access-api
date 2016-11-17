package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.Entity;

/**
 * DAO for the {@link Entity} Object.This object class represents the
 * information of organizations, corporations, governments, non-profits, clubs,
 * individual persons, and informal groups of people.
 * 
 */
public class EntityDAO extends Entity implements DatabaseObject {

	/**
	 * Default Constructor
	 */
	public EntityDAO() {
		super();
	}

	/**
	 * Construct Entity with a ResultSet
	 * 
	 * @param resultSet
	 * @throws SQLException
	 */
	public EntityDAO(ResultSet resultSet) throws SQLException {
		super();
		loadFromDatabase(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mx.nic.rdap.core.db.DatabaseObject#loadFromDatabase(java.sql.ResultSet)
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		setId(resultSet.getLong("ent_id"));
		setHandle(resultSet.getString("ent_handle"));
		setPort43(resultSet.getString("ent_port43"));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.core.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, getHandle());
		preparedStatement.setString(2, getPort43());
	}

	/* (non-Javadoc)
	 * @see mx.nic.rdap.db.DatabaseObject#updateInDatabase(java.sql.PreparedStatement)
	 */
	@Override
	public void updateInDatabase(PreparedStatement preparedStatement) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
