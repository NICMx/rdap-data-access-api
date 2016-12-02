package mx.nic.rdap.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.nic.rdap.core.db.Link;

/**
 * Data access class for the {@link Link} object.
 * 
 */
public class LinkDAO extends Link implements DatabaseObject {

	/**
	 * Default Constructor
	 */
	public LinkDAO() {
		super();
	}

	/**
	 * Construct a Link using a {@link ResultSet}
	 */
	public LinkDAO(ResultSet resultSet) throws SQLException {
		super();
		loadFromDatabase(resultSet);
	}

	/**
	 * Construct a "self" Link
	 * 
	 */
	public LinkDAO(String contextPath, String objectClassName, String query) {
		this.setValue("http://example.com" + contextPath + "/" + objectClassName + "/" + query);
		this.setRel("self");
		this.setHref("http://example.com" + contextPath + "/" + objectClassName + "/" + query);
		this.setType("application/rdap+json");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mx.nic.rdap.core.db.DatabaseObject#loadFromDatabase(java.sql.ResultSet)
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		this.setId(resultSet.getLong("lin_id"));
		this.setValue(resultSet.getString("lin_value"));
		this.setRel(resultSet.getString("lin_rel"));
		this.setHref(resultSet.getString("lin_href"));
		this.setHreflag(resultSet.getString("lin_hreflang"));
		this.setTitle(resultSet.getString("lin_title"));
		this.setMedia(resultSet.getString("lin_media"));
		this.setType(resultSet.getString("lin_type"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mx.nic.rdap.core.db.DatabaseObject#storeToDatabase(java.sql.
	 * PreparedStatement)
	 */
	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, this.getValue());
		preparedStatement.setString(2, this.getRel());
		preparedStatement.setString(3, this.getHref());
		preparedStatement.setString(4, this.getHreflag());
		preparedStatement.setString(5, this.getTitle());
		preparedStatement.setString(6, this.getMedia());
		preparedStatement.setString(7, this.getType());
	}

}
