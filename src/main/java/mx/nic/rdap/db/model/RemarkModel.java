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

import mx.nic.rdap.core.db.Remark;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.RemarkDAO;
import mx.nic.rdap.db.Util;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * Model for the Remark Object
 * 
 */
public class RemarkModel {

	private static final Logger logger = Logger.getLogger(RemarkModel.class.getName());

	private static final String QUERY_GROUP = "Remark";

	private static final String NAMESERVER_STORE_QUERY = "storeNameserverRemarksToDatabase";
	private static final String DOMAIN_STORE_QUERY = "storeDomainRemarksToDatabase";
	private static final String ENTITY_STORE_QUERY = "storeEntityRemarksToDatabase";
	private static final String AUTNUM_STORE_QUERY = "storeAutnumRemarksToDatabase";
	private static final String IP_NETWORK_STORE_QUERY = "storeIpNetworkRemarksToDatabase";

	private static final String NAMESERVER_GET_QUERY = "getByNameserverId";
	private static final String DOMAIN_GET_QUERY = "getByDomainId";
	private static final String ENTITY_GET_QUERY = "getByEntityId";
	private static final String AUTNUM_GET_QUERY = "getByAutnumId";
	private static final String IP_NETWORK_GET_QUERY = "getByIpNetworkId";

	private static final String DELETE_QUERY = "deleteRemarksById";
	private static final String NAMESERVER_DELETE_QUERY = "deleteNameserverRemarksRelation";
	private static final String ENTITY_DELETE_QUERY = "deleteEntityRemarksRelation";
	private static final String DOMAIN_DELETE_QUERY = "deleteDomainRemarksRelation";
	private static final String AUTNUM_DELETE_QUERY = "deleteAutnumRemarksRelation";
	private static final String IP_NETWORK_DELETE_QUERY = "deleteIpNetworkRemarksRelation";

	protected static QueryGroup queryGroup = null;

	static {
		try {
			RemarkModel.queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	public static long storeToDatabase(Remark remark, Connection connection)
			throws IOException, SQLException, RequiredValueNotFoundException {

		// The Remark's id is autoincremental, Statement.RETURN_GENERATED_KEYS
		// give us the id generated for the object stored
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery("storeToDatabase"),
				Statement.RETURN_GENERATED_KEYS)) {
			((RemarkDAO) remark).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			result.next();
			// The id of the remark inserted
			Long remarkInsertedId = result.getLong(1);
			remark.setId(remarkInsertedId);
			RemarkDescriptionModel.storeAllToDatabase(remark.getDescriptions(), remarkInsertedId, connection);
			LinkModel.storeRemarkLinksToDatabase(remark.getLinks(), remarkInsertedId, connection);
			return remarkInsertedId;
		}
	}

	private static void storeRelationRemarksToDatabase(List<Remark> remarks, Long id, Connection connection,
			String queryId) throws SQLException, IOException, RequiredValueNotFoundException {
		if (remarks.isEmpty())
			return;

		String query = queryGroup.getQuery(queryId);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			for (Remark remark : remarks) {
				Long remarkId = RemarkModel.storeToDatabase(remark, connection);
				statement.setLong(1, id);
				statement.setLong(2, remarkId);
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	public static void storeNameserverRemarksToDatabase(List<Remark> remarks, Long nameserverId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationRemarksToDatabase(remarks, nameserverId, connection, NAMESERVER_STORE_QUERY);
	}

	public static void storeDomainRemarksToDatabase(List<Remark> remarks, Long domainId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationRemarksToDatabase(remarks, domainId, connection, DOMAIN_STORE_QUERY);
	}

	public static void storeAutnumRemarksToDatabase(List<Remark> remarks, Long autnumId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationRemarksToDatabase(remarks, autnumId, connection, AUTNUM_STORE_QUERY);
	}

	public static void storeEntityRemarksToDatabase(List<Remark> remarks, Long entityId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationRemarksToDatabase(remarks, entityId, connection, ENTITY_STORE_QUERY);
	}

	public static void storeIpNetworkRemarksToDatabase(List<Remark> remarks, Long ipNetworkId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationRemarksToDatabase(remarks, ipNetworkId, connection, IP_NETWORK_STORE_QUERY);
	}

	private static List<Remark> getByRelationId(Long id, Connection connection, String queryId)
			throws IOException, SQLException {
		String query = queryGroup.getQuery(queryId);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, id);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery();) {
				return processResultSet(resultSet, connection);
			}
		}
	}

	public static List<Remark> getByNameserverId(Long nameserverId, Connection connection)
			throws IOException, SQLException {
		return getByRelationId(nameserverId, connection, NAMESERVER_GET_QUERY);
	}

	public static List<Remark> getByDomainId(Long domainId, Connection connection) throws SQLException, IOException {
		return getByRelationId(domainId, connection, DOMAIN_GET_QUERY);
	}

	public static List<Remark> getByEntityId(Long entityId, Connection connection) throws SQLException, IOException {
		return getByRelationId(entityId, connection, ENTITY_GET_QUERY);
	}

	public static List<Remark> getByAutnumId(Long autnumId, Connection connection) throws IOException, SQLException {
		return getByRelationId(autnumId, connection, AUTNUM_GET_QUERY);
	}

	public static List<Remark> getByIpNetworkId(Long ipNetworkId, Connection connection)
			throws IOException, SQLException {
		return getByRelationId(ipNetworkId, connection, IP_NETWORK_GET_QUERY);
	}

	public static List<Remark> getAll(Connection connection) throws IOException, SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery("getAll"));
				ResultSet resultSet = statement.executeQuery();) {
			return processResultSet(resultSet, connection);
		}
	}

	private static List<Remark> processResultSet(ResultSet resultSet, Connection connection)
			throws SQLException, ObjectNotFoundException, IOException {
		if (!resultSet.next()) {
			return Collections.emptyList();
		}
		List<Remark> remarks = new ArrayList<Remark>();
		do {
			RemarkDAO remark = new RemarkDAO(resultSet);
			// load the remark descriptions of the remark
			remark.setDescriptions(RemarkDescriptionModel.findByRemarkId(remark.getId(), connection));
			// Load the remark's links
			remark.getLinks().addAll(LinkModel.getByRemarkId(remark.getId(), connection));
			remarks.add(remark);
		} while (resultSet.next());
		return remarks;
	}

	public static void updateEntityRemarksInDatabase(List<Remark> previousRemarks, List<Remark> remarks, Long entityId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousRemarks.isEmpty()) {
			deleteRemarkRelationByRemarksId(queryGroup.getQuery(ENTITY_DELETE_QUERY), previousRemarks, connection);
			deletePreviusRemarks(previousRemarks, connection);
		}
		storeEntityRemarksToDatabase(remarks, entityId, connection);
	}

	public static void updateNameserverRemarksInDatabase(List<Remark> previousRemarks, List<Remark> remarks,
			Long nameserverId, Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousRemarks.isEmpty()) {
			deleteRemarkRelationByRemarksId(queryGroup.getQuery(NAMESERVER_DELETE_QUERY), previousRemarks, connection);
			deletePreviusRemarks(previousRemarks, connection);
		}
		storeNameserverRemarksToDatabase(remarks, nameserverId, connection);
	}

	public static void updateDomainRemarksInDatabase(List<Remark> previousRemarks, List<Remark> remarks, Long domainId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousRemarks.isEmpty()) {
			deleteRemarkRelationByRemarksId(queryGroup.getQuery(DOMAIN_DELETE_QUERY), previousRemarks, connection);
			deletePreviusRemarks(previousRemarks, connection);
		}
		storeDomainRemarksToDatabase(remarks, domainId, connection);
	}

	public static void updateAutnumRemarksInDatabase(List<Remark> previousRemarks, List<Remark> remarks, Long asnId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousRemarks.isEmpty()) {
			deleteRemarkRelationByRemarksId(queryGroup.getQuery(AUTNUM_DELETE_QUERY), previousRemarks, connection);
			deletePreviusRemarks(previousRemarks, connection);
		}
		storeAutnumRemarksToDatabase(remarks, asnId, connection);
	}

	public static void updateIpNetworkRemarksInDatabase(List<Remark> previousRemarks, List<Remark> remarks, Long ipId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousRemarks.isEmpty()) {
			deleteRemarkRelationByRemarksId(queryGroup.getQuery(IP_NETWORK_DELETE_QUERY), previousRemarks, connection);
			deletePreviusRemarks(previousRemarks, connection);
		}
		storeIpNetworkRemarksToDatabase(remarks, ipId, connection);
	}

	private static void deleteRemarkRelationByRemarksId(String query, List<Remark> previousRemarks,
			Connection connection) throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (Remark remark : previousRemarks) {
			ids.add(remark.getId());
		}
		String dynamicQuery = Util.createDynamicQueryWithInClause(ids.size(), query);
		try (PreparedStatement statement = connection.prepareStatement(dynamicQuery)) {
			int index = 1;
			for (Long id : ids) {
				statement.setLong(index++, id);
			}
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}

	}

	private static void deletePreviusRemarks(List<Remark> previousRemarks, Connection connection) throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (Remark remark : previousRemarks) {
			ids.add(remark.getId());
			LinkModel.deleteRemarksLinksData(remark.getLinks(), connection);
			RemarkDescriptionModel.deletePreviousDescriptions(remark.getId(), connection);
		}

		String query = queryGroup.getQuery(DELETE_QUERY);
		String dynamicQuery = Util.createDynamicQueryWithInClause(ids.size(), query);

		try (PreparedStatement statement = connection.prepareStatement(dynamicQuery)) {
			int index = 1;
			for (Long id : ids) {
				statement.setLong(index++, id);
			}
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

}
