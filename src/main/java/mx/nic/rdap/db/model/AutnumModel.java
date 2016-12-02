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

	private static final String STORE_QUERY = "storeToDatabase";
	private static final String GET_BY_RANGE = "getByRange";
	private static final String GET_BY_ID = "getAutnumById";
	private static final String GET_BY_HANDLE = "getAutnumByHandle";
	private static final String UPDATE_QUERY = "updateInDatabase";

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
	 */
	public static Long storeToDatabase(Autnum autnum, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {

		isValidForStore(autnum);

		Long autnumId;
		String query = queryGroup.getQuery(STORE_QUERY);
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
		for (Entity ent : autnum.getEntities()) {
			Long entId = EntityModel.existsByHandle(ent.getHandle(), connection);
			if (entId == null) {
				throw new NullPointerException(
						"Entity: " + ent.getHandle() + "was not inserted previously to the database");
			}
			ent.setId(entId);
		}
		RolModel.storeAutnumEntityRoles(autnum.getEntities(), autnumId, connection);

		return autnumId;
	}

	private static void isValidForStore(Autnum autnum) throws RequiredValueNotFoundException {
		if (autnum.getHandle() == null || autnum.getHandle().isEmpty())
			throw new RequiredValueNotFoundException("handle", "Autnum");
		if (autnum.getStartAutnum() == null)
			throw new RequiredValueNotFoundException("startAutnum", "Autnum");
		if (autnum.getEndAutnum() == null)
			throw new RequiredValueNotFoundException("endAutnum", "Autnum");
		if (autnum.getStartAutnum() > autnum.getEndAutnum()) {
			throw new RuntimeException("Starting ASN is greater than final ASN");
		}
	}

	public static AutnumDAO getAutnumById(Long autnumId, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_BY_ID))) {
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
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_BY_RANGE))) {
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

	public static AutnumDAO getByHandle(String handle, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_BY_HANDLE))) {
			statement.setString(1, handle);
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

	/**
	 * Can't use a regular upsert sql statement, because Autnum table has
	 * multiple unique constraints, instead will check if the nameserver
	 * exist,then update it on insert it,if not exist.
	 */
	public static void upsertToDatabase(AutnumDAO autnum, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		try {
			AutnumDAO previousAutnum = getByHandle(autnum.getHandle(), connection);
			autnum.setId(previousAutnum.getId());
			update(previousAutnum, autnum, connection);
		} catch (ObjectNotFoundException e) {
			storeToDatabase(autnum, connection);
		}

	}

	private static void update(AutnumDAO previousAutnum, AutnumDAO autnum, Connection connection)
			throws RequiredValueNotFoundException, SQLException, IOException {
		isValidForUpdate(autnum);
		String query = queryGroup.getQuery(UPDATE_QUERY);

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			autnum.updateInDatabase(statement);
			logger.log(Level.INFO, "Executing query: " + statement.toString());
			statement.executeUpdate();
		}
		updateNestedObjects(previousAutnum, autnum, connection);
	}

	private static void updateNestedObjects(AutnumDAO previousAutnum, AutnumDAO autnum, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		Long autnumId = autnum.getId();
		StatusModel.updateAutnumStatusInDatabase(previousAutnum.getStatus(), autnum.getStatus(), autnumId, connection);
		RemarkModel.updateAutnumRemarksInDatabase(previousAutnum.getRemarks(), autnum.getRemarks(), autnumId,
				connection);
		LinkModel.updateAutnumLinksInDatabase(previousAutnum.getLinks(), autnum.getLinks(), autnumId, connection);
		EventModel.updateAutnumEventsInDatabase(previousAutnum.getEvents(), autnum.getEvents(), autnumId, connection);
		updateAutnumEntities(previousAutnum, autnum, connection);
	}

	private static void updateAutnumEntities(AutnumDAO previousAutnum, AutnumDAO autnum, Connection connection)
			throws SQLException {
		EntityModel.validateParentEntities(autnum.getEntities(), connection);
		RolModel.updateAutnumEntityRoles(previousAutnum.getEntities(), autnum.getEntities(), autnum.getId(),
				connection);

	}

	public static void storeAutnumEntities(Autnum autnum, Connection connection) throws SQLException {
		EntityModel.validateParentEntities(autnum.getEntities(), connection);
		RolModel.storeAutnumEntityRoles(autnum.getEntities(), autnum.getId(), connection);
	}

	private static void isValidForUpdate(AutnumDAO autnum) throws RequiredValueNotFoundException {
		if (autnum.getId() == null)
			throw new RequiredValueNotFoundException("id", "Autnum");
		if (autnum.getHandle() == null || autnum.getHandle().isEmpty())
			throw new RequiredValueNotFoundException("handle", "Autnum");
		if (autnum.getStartAutnum() == null)
			throw new RequiredValueNotFoundException("startAutnum", "Autnum");
		if (autnum.getEndAutnum() == null)
			throw new RequiredValueNotFoundException("endAutnum", "Autnum");
		if (autnum.getStartAutnum() > autnum.getEndAutnum()) {
			throw new RuntimeException("Starting ASN is greater than final ASN");
		}
	}

}