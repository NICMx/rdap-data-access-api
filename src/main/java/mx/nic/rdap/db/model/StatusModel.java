package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.catalog.Status;
import mx.nic.rdap.db.QueryGroup;

/**
 * Model for the {@link Status}
 * 
 */
public class StatusModel {

	private final static Logger logger = Logger.getLogger(StatusModel.class.getName());

	private final static String QUERY_GROUP = "Status";

	protected static QueryGroup queryGroup = null;

	private static final String NS_STORE_QUERY = "storeNameserverStatusToDatabase";
	private static final String DOMAIN_STORE_QUERY = "storeDomainStatusToDatabase";
	private static final String ENTITY_STORE_QUERY = "storeEntityStatusToDatabase";
	private static final String AUTNUM_STORE_QUERY = "storeAutnumStatusToDatabase";
	private static final String IP_NETWORK_STORE_QUERY = "storeIpNetworkStatusToDatabase";

	private static final String NS_GET_QUERY = "getByNameServerId";
	private static final String DOMAIN_GET_QUERY = "getByDomainId";
	private static final String ENTITY_GET_QUERY = "getByEntityId";
	private static final String AUTNUM_GET_QUERY = "getByAutnumid";
	private static final String IP_NETWORK_GET_QUERY = "getByIpNetworkId";

	static {
		try {
			StatusModel.queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	/**
	 * Store a array of statement in the relational table nameserver_status
	 * 
	 * @param status
	 * @param nameserverId
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void storeNameserverStatusToDatabase(List<Status> statusList, Long nameserverId,
			Connection connection) throws IOException, SQLException {
		storeRelationStatusToDatabase(statusList, nameserverId, connection, NS_STORE_QUERY);
	}

	/**
	 * Stores an array of statements in the relational table domain_status
	 * 
	 * @param statusList
	 * @param domainId
	 * @param connection
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void storeDomainStatusToDatabase(List<Status> statusList, Long domainId, Connection connection)
			throws IOException, SQLException {
		storeRelationStatusToDatabase(statusList, domainId, connection, DOMAIN_STORE_QUERY);
	}

	/**
	 * Stores an array of status in the relational table entity_status.
	 * 
	 * @param statusList
	 * @param entityId
	 * @param connection
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void storeEntityStatusToDatabase(List<Status> statusList, Long entityId, Connection connection)
			throws IOException, SQLException {
		storeRelationStatusToDatabase(statusList, entityId, connection, ENTITY_STORE_QUERY);
	}

	/**
	 * Stores an array of status in the relational table asn_status.
	 * 
	 * @param statusList
	 * @param autnumId
	 * @param connection
	 * @throws SQLException
	 */
	public static void storeAutnumStatusToDatabase(List<Status> statusList, Long autnumId, Connection connection)
			throws SQLException {
		storeRelationStatusToDatabase(statusList, autnumId, connection, AUTNUM_STORE_QUERY);
	}

	/**
	 * Stores an array of status in the relational table ip_network_status
	 */
	public static void storeIpNetworkStatusToDatabase(List<Status> statusList, Long ipNetworkId, Connection connection)
			throws SQLException {
		storeRelationStatusToDatabase(statusList, ipNetworkId, connection, IP_NETWORK_STORE_QUERY);
	}

	private static void storeRelationStatusToDatabase(List<Status> statusList, Long id, Connection connection,
			String storeQueryId) throws SQLException {
		if (statusList.isEmpty())
			return;

		String query = queryGroup.getQuery(storeQueryId);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			for (Status status : statusList) {
				statement.setLong(1, id);
				statement.setLong(2, status.getId());
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	/**
	 * Get all Status for a Nameserver
	 * 
	 * @param nameserverId
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<Status> getByNameServerId(Long nameserverId, Connection connection)
			throws IOException, SQLException {
		return getByRelationsId(nameserverId, connection, NS_GET_QUERY);
	}

	/**
	 * Get all status from a domain
	 * 
	 * @param domainId
	 * @param connection
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<Status> getByDomainId(Long domainId, Connection connection) throws IOException, SQLException {
		return getByRelationsId(domainId, connection, DOMAIN_GET_QUERY);
	}

	/**
	 * Get all status from an entityId.
	 * 
	 * @param entityId
	 * @param connection
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<Status> getByEntityId(Long entityId, Connection connection) throws IOException, SQLException {
		return getByRelationsId(entityId, connection, ENTITY_GET_QUERY);
	}

	/**
	 * Get all status with an autnums Id
	 * 
	 * @param autnumId
	 * @param connection
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<Status> getByAutnumId(Long autnumId, Connection connection) throws IOException, SQLException {
		return getByRelationsId(autnumId, connection, AUTNUM_GET_QUERY);
	}

	/**
	 * Get all status with an IpNetwork Id
	 */
	public static List<Status> getByIpNetworkId(Long ipNetworkId, Connection connection)
			throws IOException, SQLException {
		return getByRelationsId(ipNetworkId, connection, IP_NETWORK_GET_QUERY);
	}

	public static List<Status> getByRelationsId(Long id, Connection connection, String getQueryId)
			throws IOException, SQLException {
		List<Status> result = null;
		String query = queryGroup.getQuery(getQueryId);

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, id);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList();
				}
				List<Status> status = new ArrayList<Status>();
				do {
					status.add(Status.getById(resultSet.getInt("sta_id")));
				} while (resultSet.next());
				result = status;
			}
		}

		return result;
	}

	private static void deleteByNameserverId(Long nameserverId, Connection connection) throws SQLException {
		String query = queryGroup.getQuery("deleteByNameserverId");
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, nameserverId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

	/**
	 * To avoid a complete search on a likely small data,erase the previous data
	 * and insert the new
	 */
	public static void updateNameserverStatusInDatabase(List<Status> status, Long nameserverId, Connection connection)
			throws SQLException, IOException {
		deleteByNameserverId(nameserverId, connection);
		storeNameserverStatusToDatabase(status, nameserverId, connection);
	}

}
