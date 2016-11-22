package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.RemarkDescription;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.RemarkDescriptionDAO;
import mx.nic.rdap.db.exception.ObjectNotFoundException;

/**
 * Model for the {@link RemarkDescription} object
 * 
 */
public class RemarkDescriptionModel {

	private final static Logger logger = Logger.getLogger(RemarkDescriptionModel.class.getName());

	private final static String QUERY_GROUP = "RemarkDescription";

	private static final String DELETE_QUERY = "deleteRemarkDescriptionByRemarkId";
	private static final String GET_QUERY = "getByRemarkId";
	private static final String INSERT_QUERY = "storeToDatabase";

	protected static QueryGroup queryGroup = null;

	static {
		try {
			RemarkDescriptionModel.queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	public static void storeAllToDatabase(List<RemarkDescription> descriptions, Long remarkInsertedId,
			Connection connection) throws IOException, SQLException {
		for (RemarkDescription remarkDescription : descriptions) {
			remarkDescription.setRemarkId(remarkInsertedId);
			RemarkDescriptionModel.storeToDatabase(remarkDescription, connection);
		}
	}

	public static void storeToDatabase(RemarkDescription remarkDescription, Connection connection)
			throws IOException, SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(INSERT_QUERY))) {
			((RemarkDescriptionDAO) remarkDescription).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

	public static List<RemarkDescription> findByRemarkId(Long id, Connection connection)
			throws IOException, SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_QUERY))) {
			statement.setLong(1, id);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				List<RemarkDescription> remarks = new ArrayList<RemarkDescription>();
				do {
					RemarkDescriptionDAO remarkDescription = new RemarkDescriptionDAO(resultSet);
					remarks.add(remarkDescription);
				} while (resultSet.next());
				return remarks;
			}
		}
	}

	public static void deletePreviousDescriptions(Long remarkId, Connection connection) throws SQLException {
		String query = queryGroup.getQuery(DELETE_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, remarkId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}

	}

}
