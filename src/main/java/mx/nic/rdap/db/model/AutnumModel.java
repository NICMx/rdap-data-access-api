package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.Autnum;
import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.db.AutnumDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * Model for the {@link Autnum} Object
 * 
 */
public class AutnumModel {

	private final static Logger logger = Logger.getLogger(Autnum.class.getName());

	private final static String QUERY_GROUP = "Autnum";

	private static QueryGroup queryGroup = null;

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	/**
	 * Stores Object autnum to database
	 * 
	 * @param autnum
	 * @param connection
	 * @return
	 * @throws SQLException
	 * @throws RequiredValueNotFoundException
	 * @throws IOException
	 */
	public static Long storeToDatabase(Autnum autnum, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		// Check if range is valid
		if (autnum.getStartAutnum() > autnum.getEndAutnum()) {
			throw new RuntimeException("Starting ASN is greater than final ASN");
		}

		Long autnumId;
		String query = queryGroup.getQuery("storeToDatabase");
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			((AutnumDAO) autnum).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing query:" + statement.toString());
			statement.executeUpdate();
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			autnumId = resultSet.getLong(1);
			autnum.setId(autnumId);
		}
		StatusModel.storeAutnumStatusToDatabase(autnum.getStatus(), autnumId, connection);
		RemarkModel.storeAutnumRemarksToDatabase(autnum.getRemarks(), autnumId, connection);
		LinkModel.storeAutnumLinksToDatabase(autnum.getLinks(), autnumId, connection);
		EventModel.storeAutnumEventsToDatabase(autnum.getEvents(), autnumId, connection);
		if (autnum.getEntities().size() > 0) {
			for (Entity ent : autnum.getEntities()) {
				Long entId = EntityModel.existsByHandle(ent.getHandle(), connection);
				if (entId == null) {
					throw new NullPointerException(
							"Entity: " + ent.getHandle() + "was not inserted previously to the database");
				}
				ent.setId(entId);
			}
			RolModel.storeAutnumEntityRoles(autnum.getEntities(), autnumId, connection);

		}
		return autnumId;
	}

	public static AutnumDAO getAutnumById(Long autnumId, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery("getAutnumById"))) {
			statement.setLong(1, autnumId);
			logger.log(Level.INFO, "Executing query: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found");
				}
				AutnumDAO autnum = new AutnumDAO(resultSet);
				loadNestedObjects(autnum, connection);
				return autnum;
			}
		}
	}

	public static AutnumDAO getByRange(Long autnumValue, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery("getByRange"))) {
			statement.setLong(1, autnumValue);
			statement.setLong(2, autnumValue);
			logger.log(Level.INFO, "Executing query: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				AutnumDAO autnum = new AutnumDAO(resultSet);
				loadNestedObjects(autnum, connection);
				return autnum;
			}
		}
	}

	private static void loadNestedObjects(Autnum autnum, Connection connection) {
		Long autnumId = autnum.getId();

		try {
			autnum.getStatus().addAll(StatusModel.getByAutnumId(autnumId, connection));
		} catch (Exception e) {

		}
		try {
			autnum.getRemarks().addAll(RemarkModel.getByAutnumId(autnumId, connection));
		} catch (Exception e) {

		}
		try {
			autnum.getLinks().addAll(LinkModel.getByAutnumId(autnumId, connection));
		} catch (Exception e) {

		}
		try {
			autnum.getEvents().addAll(EventModel.getByAutnumId(autnumId, connection));
		} catch (Exception e) {

		}
		try {
			autnum.getEntities().addAll(EntityModel.getEntitiesByAutnumId(autnumId, connection));
		} catch (Exception e) {

		}
	}

}