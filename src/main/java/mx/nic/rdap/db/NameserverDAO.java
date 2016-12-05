package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.core.db.Nameserver;

/**
 * Data access class for the {@link Nameserver} object.
 * 
 */
public class NameserverDAO extends Nameserver implements DatabaseObject {

	/**
	 * Constructor default
	 */
	public NameserverDAO() {
		super();
	}

	/**
	 * Contruct a NameserverDAO using a {@link ResultSet}
	 * 
	 */
	public NameserverDAO(ResultSet resultSet) throws SQLException {
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
		this.setId(resultSet.getLong("nse_id"));
		this.setHandle(resultSet.getString("nse_handle"));
		this.setPunycodeName(resultSet.getString("nse_ldh_name"));
		this.setPort43(resultSet.getString("nse_port43"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.core.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, this.getHandle());
		preparedStatement.setString(2, this.getLdhName());
		preparedStatement.setString(3, this.getPort43());
	}

	/**
	 * Same as storeToDatabase,but using different order and should use the
	 * object id as criteria
	 */
	public void updateInDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, this.getLdhName());
		preparedStatement.setString(2, this.getPort43());
		preparedStatement.setLong(3, this.getId());
	}

	/**
	 * Generates a link with the self information and add it to the domain
	 */
	public void addSelfLinks(String header, String contextPath) {
		LinkDAO self = new LinkDAO(header, contextPath, "nameserver", this.getLdhName());
		this.getLinks().add(self);

		for (Entity ent : this.getEntities()) {
			self = new LinkDAO(header, contextPath, "entity", ent.getHandle());
			ent.getLinks().add(self);
		}
	}

}
