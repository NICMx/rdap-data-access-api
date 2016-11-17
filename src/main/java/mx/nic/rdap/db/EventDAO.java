package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import mx.nic.rdap.core.catalog.EventAction;
import mx.nic.rdap.core.db.Event;

/**
 * DAO for the {@link Event} object. This data structure represents events that
 * have occurred on an instance of an object class
 * 
 */
public class EventDAO extends Event implements DatabaseObject {

	/**
	 * Default Constructor
	 */
	public EventDAO() {
		super();
	}

	/**
	 * @param resultSet
	 * @throws SQLException
	 */
	public EventDAO(ResultSet resultSet) throws SQLException {
		super();
		this.loadFromDatabase(resultSet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mx.nic.rdap.core.db.DatabaseObject#loadFromDatabase(java.sql.ResultSet)
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		this.setId(resultSet.getLong("eve_id"));
		this.setEventAction(EventAction.getById(resultSet.getInt("eac_id")));
		this.setEventActor(resultSet.getString("eve_actor"));
		this.setEventDate(new Date(resultSet.getTimestamp("eve_date").getTime()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.core.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setLong(1, this.getEventAction().getId());
		preparedStatement.setString(2, this.getEventActor());
		preparedStatement.setTimestamp(3, new Timestamp(this.getEventDate().getTime()));
	}

	/* (non-Javadoc)
	 * @see mx.nic.rdap.db.DatabaseObject#updateInDatabase(java.sql.PreparedStatement)
	 */
	@Override
	public void updateInDatabase(PreparedStatement preparedStatement) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
