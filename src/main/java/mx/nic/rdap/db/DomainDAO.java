package mx.nic.rdap.db;

import java.net.IDN;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import mx.nic.rdap.core.db.Domain;
import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.model.ZoneModel;

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
	 * Construct Domain using a {@link ResultSet}
	 */
	public DomainDAO(ResultSet resultSet) throws SQLException {
		super();
		loadFromDatabase(resultSet);
	}

	/**
	 * Loads the information coming from the database in an instance of Domain
	 * 
	 */
	@Override
	public void loadFromDatabase(ResultSet resultSet) throws SQLException {
		this.setId(resultSet.getLong("dom_id"));
		this.setHandle(resultSet.getString("dom_handle"));
		this.setPunycodeName(resultSet.getString("dom_ldh_name"));
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

		String domName = this.getLdhName();
		String ldhName = IDN.toASCII(domName);
		String unicodeName = IDN.toUnicode(domName);

		if (ldhName.equals(unicodeName)) {
			preparedStatement.setString(2, ldhName);
			preparedStatement.setNull(3, Types.VARCHAR);
		} else {
			preparedStatement.setString(2, ldhName);
			preparedStatement.setString(3, unicodeName);
		}

		preparedStatement.setString(4, this.getPort43());
		preparedStatement.setInt(5, this.getZoneId());

	}

	/**
	 * Same as storeToDatabase,but using different order and should use the
	 * object id as criteria
	 */
	public void updateInDatabase(PreparedStatement preparedStatement) throws SQLException {

		String domName = this.getLdhName();
		String ldhName = IDN.toASCII(domName);
		String unicodeName = IDN.toUnicode(domName);
		if (ldhName.equals(unicodeName)) {
			preparedStatement.setString(1, ldhName);
			preparedStatement.setNull(2, Types.VARCHAR);
		} else {
			preparedStatement.setString(1, ldhName);
			preparedStatement.setString(2, unicodeName);
		}
		preparedStatement.setString(1, this.getLdhName());

		preparedStatement.setString(3, this.getPort43());
		preparedStatement.setInt(4, this.getZoneId());
		preparedStatement.setLong(5, this.getId());
	}

	/**
	 * Generates a link with the self information and add it to the domain
	 */
	public void addSelfLinks(String header, String contextPath) {
		LinkDAO self = new LinkDAO(header, contextPath, "domain",
				this.getLdhName() + "." + ZoneModel.getZoneNameById(this.getZoneId()));
		this.getLinks().add(self);

		for (Nameserver ns : this.getNameServers()) {
			self = new LinkDAO(header, contextPath, "nameserver", ns.getLdhName());
			ns.getLinks().add(self);
		}

		for (Entity ent : this.getEntities()) {
			self = new LinkDAO(header, contextPath, "entity", ent.getHandle());
			ent.getLinks().add(self);
		}

		if (this.getIpNetwork() != null) {
			self = new LinkDAO(header, contextPath, "ip", this.getIpNetwork().getStartAddress().getHostAddress());
			this.getIpNetwork().getLinks().add(self);
		}
	}

}
