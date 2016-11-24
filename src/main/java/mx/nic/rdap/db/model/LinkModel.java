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

import mx.nic.rdap.core.db.Link;
import mx.nic.rdap.db.LinkDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.Util;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * The model for the Link object
 * 
 * @author dalpuche
 * @author dhfelix
 *
 */
public class LinkModel {

	private final static Logger logger = Logger.getLogger(LinkModel.class.getName());

	private final static String QUERY_GROUP = "Link";

	protected static QueryGroup queryGroup = null;

	private static final String NAMESERVER_GET_QUERY = "getByNameServerId";
	private static final String EVENT_GET_QUERY = "getByEventId";
	private static final String DS_DATA_GET_QUERY = "getByDsDataId";
	private static final String DOMAIN_GET_QUERY = "getByDomainId";
	private static final String REMARK_GET_QUERY = "getByRemarkId";
	private static final String ENTITY_GET_QUERY = "getByEntityId";
	private static final String AUTNUM_GET_QUERY = "getByAutnumId";
	private static final String IP_NETWORK_GET_QUERY = "getByIpNetworkId";

	private static final String NAMESERVER_STORE_QUERY = "storeNameserverLinksToDatabase";
	private static final String EVENT_STORE_QUERY = "storeEventLinksToDatabase";
	private static final String REMARK_STORE_QUERY = "storeRemarkLinksToDatabase";
	private static final String DS_DATA_STORE_QUERY = "storeDsDataLinksToDatabase";
	private static final String DOMAIN_STORE_QUERY = "storeDomainLinksToDatabase";
	private static final String ENTITY_STORE_QUERY = "storeEntityLinksToDatabase";
	private static final String AUTNUM_STORE_QUERY = "storeAutnumLinksToDatabase";
	private static final String IP_NETWORK_STORE_QUERY = "storeIpNetworkLinksToDatabase";

	private static final String DELETE_QUERY = "deleteLinksById";
	private static final String NAMESERVER_DELETE_QUERY = "deleteNameserverLinksRelation";
	private static final String ENTITY_DELETE_QUERY = "deleteEntityLinksRelation";
	private static final String DS_DELETE_QUERY = "deleteDSLinksRelation";
	private static final String DOMAIN_DELETE_QUERY = "deleteDomainLinksRelation";
	private static final String AUTNUM_DELETE_QUERY = "deleteAutnumLinksRelation";
	private static final String IP_NETWORK_DELETE_QUERY = "deleteIpNetworkLinksRelation";
	private static final String REMARK_DELETE_QUERY = "deleteRemarkLinksRelation";
	private static final String EVENTS_DELETE_QUERY = "deleteLinksById";

	static {
		try {
			LinkModel.queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	/**
	 * Validate the required attributes for the link
	 * 
	 * @param link
	 * @throws RequiredValueNotFoundException
	 */
	private static void isValidForStore(Link link) throws RequiredValueNotFoundException {
		if (link.getValue() == null || link.getValue().isEmpty())
			throw new RequiredValueNotFoundException("value", "Link");
		if (link.getHref() == null || link.getHref().isEmpty())
			throw new RequiredValueNotFoundException("href", "Link");
	}

	/**
	 * Store a Link in the Database
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static Long storeToDatabase(Link link, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		isValidForStore(link);
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery("storeToDatabase"),
				Statement.RETURN_GENERATED_KEYS)) {
			((LinkDAO) link).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			result.next();
			Long linkId = result.getLong(1);// The id of the link inserted
			link.setId(linkId);
			return linkId;
		}
	}

	/**
	 * Store the nameserver links
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static void storeNameserverLinksToDatabase(List<Link> links, Long nameserverId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, nameserverId, connection, NAMESERVER_STORE_QUERY);
	}

	/**
	 * Stores the Domain links
	 * 
	 * @param links
	 * @param domainId
	 * @param connection
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static void storeDomainLinksToDatabase(List<Link> links, Long domainId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, domainId, connection, DOMAIN_STORE_QUERY);
	}

	/**
	 * Stores the DsData links
	 * 
	 * @param links
	 * @param dsDataId
	 * @param connection
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static void storeDsDataLinksToDatabase(List<Link> links, Long dsDataId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, dsDataId, connection, DS_DATA_STORE_QUERY);
	}

	/**
	 * Store the event links
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static void storeEventLinksToDatabase(List<Link> links, Long eventId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, eventId, connection, EVENT_STORE_QUERY);
	}

	/**
	 * Store the remark links
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static void storeRemarkLinksToDatabase(List<Link> links, Long remarkId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, remarkId, connection, REMARK_STORE_QUERY);
	}

	public static void storeAutnumLinksToDatabase(List<Link> links, Long autnumId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, autnumId, connection, AUTNUM_STORE_QUERY);

	}

	/**
	 * Store the entity links
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	public static void storeEntityLinksToDatabase(List<Link> links, Long entityId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, entityId, connection, ENTITY_STORE_QUERY);
	}

	public static void storeIpNetworkLinksToDatabase(List<Link> links, Long ipNetworkId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeLinkRelationToDatabase(links, ipNetworkId, connection, IP_NETWORK_STORE_QUERY);
	}

	/**
	 * @param links
	 *            The links to be stored in the relation.
	 * @param id
	 *            Id of the owner of the links.
	 * @param connection
	 *            Connection to a database.
	 * @param storeQueryId
	 *            SQL query to use to store the relation of the links.
	 * @throws SQLException
	 * @throws IOException
	 * @throws RequiredValueNotFoundException
	 */
	private static void storeLinkRelationToDatabase(List<Link> links, Long id, Connection connection,
			String storeQueryId) throws SQLException, IOException, RequiredValueNotFoundException {
		if (links.isEmpty())
			return;

		String query = queryGroup.getQuery(storeQueryId);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			for (Link link : links) {
				Long linkId = LinkModel.storeToDatabase(link, connection);
				statement.setLong(1, id);
				statement.setLong(2, linkId);
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	/**
	 * Get all links for a Nameserver
	 * 
	 * @param nameserverId
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<LinkDAO> getByNameServerId(Long nameserverId, Connection connection)
			throws IOException, SQLException {
		return getByRelationId(nameserverId, connection, NAMESERVER_GET_QUERY);
	}

	/**
	 * Gets all links from a domain
	 * 
	 * @param domainId
	 * @param connection
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<LinkDAO> getByDomainId(Long domainId, Connection connection) throws IOException, SQLException {
		return getByRelationId(domainId, connection, DOMAIN_GET_QUERY);
	}

	/**
	 * Get all links for a event
	 * 
	 * @param nameserverId
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<LinkDAO> getByEventId(Long eventId, Connection connection) throws IOException, SQLException {
		return getByRelationId(eventId, connection, EVENT_GET_QUERY);
	}

	/**
	 * Get all links for a Remark
	 * 
	 * @param nameserverId
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<LinkDAO> getByRemarkId(Long remarkId, Connection connection) throws IOException, SQLException {
		return getByRelationId(remarkId, connection, REMARK_GET_QUERY);
	}

	/**
	 * Get all links for a DsData
	 * 
	 * @param dsDataId
	 * @param connection
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<LinkDAO> getByDsDataId(Long dsDataId, Connection connection) throws IOException, SQLException {
		return getByRelationId(dsDataId, connection, DS_DATA_GET_QUERY);
	}

	/**
	 * Get all links for an entity
	 * 
	 */
	public static List<LinkDAO> getByEntityId(Long entityId, Connection connection) throws IOException, SQLException {
		return getByRelationId(entityId, connection, ENTITY_GET_QUERY);
	}

	public static List<LinkDAO> getByAutnumId(Long autnumId, Connection connection) throws SQLException {
		return getByRelationId(autnumId, connection, AUTNUM_GET_QUERY);
	}

	public static List<LinkDAO> getByIpNetworkId(Long ipNetworkId, Connection connection) throws SQLException {
		return getByRelationId(ipNetworkId, connection, IP_NETWORK_GET_QUERY);
	}

	/**
	 * @param id
	 *            Id of the owner of the links
	 * @param connection
	 *            connection to a database.
	 * @param queryGetId
	 *            SQL query to get the links of the id.
	 * @return
	 * @throws SQLException
	 */
	private static List<LinkDAO> getByRelationId(Long id, Connection connection, String queryGetId)
			throws SQLException {
		String query = queryGroup.getQuery(queryGetId);
		List<LinkDAO> result = null;

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, id);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList(); // A Data can have no links
				}
				List<LinkDAO> links = new ArrayList<LinkDAO>();
				do {
					LinkDAO link = new LinkDAO(resultSet);
					links.add(link);
				} while (resultSet.next());
				result = links;
			}
		}

		return result;
	}

	/**
	 * Unused. Get all the ipAddress from DB
	 * 
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<Link> getAll(Connection connection) throws SQLException {
		String query = queryGroup.getQuery("getAll");
		List<Link> result = null;

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList(); // A Data can have no links
				}
				List<Link> links = new ArrayList<Link>();
				do {
					LinkDAO link = new LinkDAO(resultSet);
					links.add(link);
				} while (resultSet.next());
				result = links;
			}
		}

		return result;
	}

	public static void updateEntityLinksInDatabase(List<Link> previousLinks, List<Link> links, Long entityId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(ENTITY_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
		storeEntityLinksToDatabase(links, entityId, connection);
	}

	public static void updateNameserverLinksInDatabase(List<Link> previousLinks, List<Link> links, Long nameserverId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(NAMESERVER_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
		storeNameserverLinksToDatabase(links, nameserverId, connection);
	}

	public static void updateDSLinksInDatabase(List<Link> previousLinks, List<Link> links, Long dsId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(DS_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
		storeDsDataLinksToDatabase(links, dsId, connection);
	}

	public static void updateDomainLinksInDatabase(List<Link> previousLinks, List<Link> links, Long domainId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(DOMAIN_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
		storeDomainLinksToDatabase(links, domainId, connection);
	}

	public static void updateIpNetworkLinksInDatabase(List<Link> previousLinks, List<Link> links, Long ipNetworkId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(IP_NETWORK_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
		// storeIpNetworkLinksToDatabase(links, domainId, connection);
	}

	public static void updateAutnumLinksInDatabase(List<Link> previousLinks, List<Link> links, Long autnumId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(AUTNUM_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
		storeAutnumLinksToDatabase(links, autnumId, connection);
	}

	public static void deleteEventLinksData(List<Link> previousLinks, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(EVENTS_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
	}

	public static void deleteRemarksLinksData(List<Link> previousLinks, Connection connection) throws SQLException {
		if (!previousLinks.isEmpty()) {
			deleteLinksRelationByLinkId(REMARK_DELETE_QUERY, previousLinks, connection);
			deletePreviousLinks(previousLinks, connection);
		}
	}

	private static void deleteLinksRelationByLinkId(String query, List<Link> previousLinks, Connection connection)
			throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (Link linkk : previousLinks) {
			ids.add(linkk.getId());
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

	private static void deletePreviousLinks(List<Link> previousLinks, Connection connection) throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (Link link : previousLinks) {
			ids.add(link.getId());
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
