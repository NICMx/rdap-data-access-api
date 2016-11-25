package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.PublicId;
import mx.nic.rdap.db.PublicIdDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.Util;

/**
 * Model for the {@link PublicId} Object
 * 
 */
public class PublicIdModel {

	private final static Logger logger = Logger.getLogger(PublicIdModel.class.getName());

	private final static String QUERY_GROUP = "PublicId";

	private static QueryGroup queryGroup = null;

	private static final String STORE_QUERY = "storeToDatabase";
	private static final String ENTITY_GET_QUERY = "getByEntity";
	private static final String DOMAIN_GET_QUERY = "getByDomain";
	private static final String ENTITY_STORE_QUERY = "storeEntityPublicIdsToDatabase";
	private static final String DOMAIN_STORE_QUERY = "storeDomainPublicIdsToDatabase";
	private static final String DELETE_QUERY = "deleteById";
	private static final String DELETE_ENTITY_RELATION_QUERY = "deleteEntityPublicId";
	private static final String DELETE_DOMAIN_RELATION_QUERY = "deleteDomainPublicId";

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	public static void storeAllToDatabase(List<PublicId> publicIds, Connection connection)
			throws SQLException, IOException {
		for (PublicId publicId : publicIds) {
			PublicIdModel.storeToDatabase(publicId, connection);
		}
	}

	public static Long storeToDatabase(PublicId publicId, Connection connection) throws SQLException, IOException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(STORE_QUERY),
				Statement.RETURN_GENERATED_KEYS);) {
			((PublicIdDAO) publicId).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			result.next();
			// The id of the link inserted
			Long resultId = result.getLong(1);
			publicId.setId(resultId);

			return publicId.getId();
		}
	}

	private static void storeBy(List<PublicId> publicIds, Long id, Connection connection, String query)
			throws SQLException, IOException {
		if (publicIds.isEmpty())
			return;

		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(query))) {
			for (PublicId publicId : publicIds) {
				Long resultId = PublicIdModel.storeToDatabase(publicId, connection);
				statement.setLong(1, id);
				statement.setLong(2, resultId);
				logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	public static void storePublicIdByDomain(List<PublicId> publicIds, Long domainId, Connection connection)
			throws SQLException, IOException {
		storeBy(publicIds, domainId, connection, DOMAIN_STORE_QUERY);
	}

	public static void storePublicIdByEntity(List<PublicId> publicIds, Long entityId, Connection connection)
			throws SQLException, IOException {
		storeBy(publicIds, entityId, connection, ENTITY_STORE_QUERY);
	}

	private static List<PublicId> getBy(Long entityId, Connection connection, String query)
			throws SQLException, IOException {
		PublicIdModel.queryGroup = new QueryGroup(QUERY_GROUP);
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(query))) {
			statement.setLong(1, entityId);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				return processResultSet(resultSet);
			}
		}
	}

	public static List<PublicId> getAll(Connection connection) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("getAll")) {
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			return processResultSet(resultSet);
		}
	}

	public static List<PublicId> getByDomain(Long domainId, Connection connection) throws SQLException, IOException {
		return getBy(domainId, connection, DOMAIN_GET_QUERY);
	}

	public static List<PublicId> getByEntity(Long entityId, Connection connection) throws SQLException, IOException {
		return getBy(entityId, connection, ENTITY_GET_QUERY);
	}

	private static List<PublicId> processResultSet(ResultSet resultSet) throws SQLException {
		if (!resultSet.next()) {
			// Did not retrieve any public Ids
			return Collections.emptyList();
		}
		List<PublicId> publicIds = new ArrayList<PublicId>();
		do {
			PublicIdDAO publicId = new PublicIdDAO(resultSet);
			publicIds.add(publicId);
		} while (resultSet.next());
		return publicIds;
	}

	public static void updateEntityPublicIdsInDatabase(List<PublicId> previousPublicIds, List<PublicId> entityPublicIds,
			Long entityId, Connection connection) throws SQLException, IOException {
		if (!previousPublicIds.isEmpty()) {
			deletePublicIdsRelation(queryGroup.getQuery(DELETE_ENTITY_RELATION_QUERY), entityId, connection);
			deletePreviousPublicIds(previousPublicIds, connection);
		}
		if (!entityPublicIds.isEmpty())
			storePublicIdByEntity(entityPublicIds, entityId, connection);
	}

	public static void updateDomainPublicIdsInDatabase(List<PublicId> previousPublicIds, List<PublicId> domainPublicIds,
			Long domainId, Connection connection) throws SQLException, IOException {
		if (!previousPublicIds.isEmpty()) {
			deletePublicIdsRelation(queryGroup.getQuery(DELETE_DOMAIN_RELATION_QUERY), domainId, connection);
			deletePreviousPublicIds(previousPublicIds, connection);
		}
		if (!domainPublicIds.isEmpty())
			storePublicIdByDomain(domainPublicIds, domainId, connection);
	}

	private static void deletePreviousPublicIds(List<PublicId> previousPublicIds, Connection connection)
			throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (PublicId publicId : previousPublicIds) {
			ids.add(publicId.getId());
		}
		String dynamicQuery = Util.createDynamicQueryWithInClause(ids.size(), queryGroup.getQuery(DELETE_QUERY));
		try (PreparedStatement statement = connection.prepareStatement(dynamicQuery)) {
			int index = 1;
			for (Long id : ids) {
				statement.setLong(index++, id);
			}
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

	private static void deletePublicIdsRelation(String query, Long parentId, Connection connection)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, parentId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

}
