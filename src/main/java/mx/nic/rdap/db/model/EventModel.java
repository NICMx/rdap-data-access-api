
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

import mx.nic.rdap.core.db.Event;
import mx.nic.rdap.db.EventDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.Util;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * The model for the Event object
 * 
 */
public class EventModel {

	private final static Logger logger = Logger.getLogger(RemarkModel.class.getName());

	private final static String QUERY_GROUP = "Event";

	protected static QueryGroup queryGroup = null;

	private static final String NAMESERVER_GET_QUERY = "getByNameServerId";
	private static final String DS_DATA_GET_QUERY = "getByDsDataId";
	private static final String DOMAIN_GET_QUERY = "getByDomainId";
	private static final String ENTITY_GET_QUERY = "getByEntityId";
	private static final String AUTNUM_GET_QUERY = "getByAutnumId";

	private static final String NAMESERVER_STORE_QUERY = "storeNameserverEventsToDatabase";
	private static final String DS_DATA_STORE_QUERY = "storeDsDataEventsToDatabase";
	private static final String DOMAIN_STORE_QUERY = "storeDomainEventsToDatabase";
	private static final String ENTITY_STORE_QUERY = "storeEntityEventsToDatabase";
	private static final String AUTNUM_STORE_QUERY = "storeAutnumEventsToDatabase";

	private static final String DELETE_QUERY = "deleteEventById";
	private static final String NAMESERVER_DELETE_QUERY = "deleteNameserverEventsRelation";
	private static final String ENTITY_DELETE_QUERY = "deleteEntityEventsRelation";
	private static final String DS_DELETE_QUERY = "deleteDsEventsRelation";
	private static final String DOMAIN_DELETE_QUERY = "deleteDomainEventsRelation";
	private static final String AUTNUM_DELETE_QUERY = "deleteAutnumEventsRelation";
	private static final String IP_NETWORK_DELETE_QUERY = "deleteIpNetworkEventsRelation";

	static {
		try {
			EventModel.queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	private static void isValidForStore(Event event) throws RequiredValueNotFoundException {
		if (event.getEventAction() == null)
			throw new RequiredValueNotFoundException("eventAction", "Event");
		if (event.getEventDate() == null)
			throw new RequiredValueNotFoundException("eventDate", "Event");
	}

	public static long storeToDatabase(Event event, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		isValidForStore(event);
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery("storeToDatabase"),
				Statement.RETURN_GENERATED_KEYS)) {
			((EventDAO) event).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			result.next();
			Long eventId = result.getLong(1);// The id of the link inserted
			event.setId(eventId);
			LinkModel.storeEventLinksToDatabase(event.getLinks(), eventId, connection);
			return eventId;
		}
	}

	public static void storeNameserverEventsToDatabase(List<Event> events, Long nameserverId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {

		storeRelationEventsToDatabase(events, nameserverId, connection, NAMESERVER_STORE_QUERY);
	}

	public static void storeEntityEventsToDatabase(List<Event> events, Long entityId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationEventsToDatabase(events, entityId, connection, ENTITY_STORE_QUERY);
	}

	public static void storeDomainEventsToDatabase(List<Event> events, Long domainId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationEventsToDatabase(events, domainId, connection, DOMAIN_STORE_QUERY);
	}

	public static void storeAutnumEventsToDatabase(List<Event> events, Long autnumId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationEventsToDatabase(events, autnumId, connection, AUTNUM_STORE_QUERY);
	}

	public static void storeDsDataEventsToDatabase(List<Event> events, Long dsDataId, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		storeRelationEventsToDatabase(events, dsDataId, connection, DS_DATA_STORE_QUERY);
	}

	private static void storeRelationEventsToDatabase(List<Event> events, Long id, Connection connection,
			String storeQueryId) throws SQLException, IOException, RequiredValueNotFoundException {
		if (events.isEmpty())
			return;

		String query = queryGroup.getQuery(storeQueryId);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			for (Event event : events) {
				Long eventId = EventModel.storeToDatabase(event, connection);
				statement.setLong(1, id);
				statement.setLong(2, eventId);
				logger.log(Level.INFO, "Excuting QUERY:" + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	public static List<Event> getByNameServerId(Long nameserverId, Connection connection)
			throws IOException, SQLException {
		return getByRelationId(nameserverId, connection, NAMESERVER_GET_QUERY);
	}

	public static List<Event> getByDsDataId(Long dsDataId, Connection connection) throws SQLException, IOException {
		return getByRelationId(dsDataId, connection, DS_DATA_GET_QUERY);
	}

	public static List<Event> getByDomainId(Long domainId, Connection connection) throws SQLException, IOException {
		return getByRelationId(domainId, connection, DOMAIN_GET_QUERY);
	}

	public static List<Event> getByAutnumId(Long autnumId, Connection connection) throws SQLException, IOException {
		return getByRelationId(autnumId, connection, AUTNUM_GET_QUERY);
	}

	public static List<Event> getByEntityId(Long entityId, Connection connection) throws SQLException, IOException {
		return getByRelationId(entityId, connection, ENTITY_GET_QUERY);
	}

	private static List<Event> getByRelationId(Long id, Connection connection, String getQueryId)
			throws SQLException, IOException {
		String query = queryGroup.getQuery(getQueryId);
		List<Event> result = null;

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, id);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList();
				}
				List<Event> events = new ArrayList<Event>();
				do {
					EventDAO event = new EventDAO(resultSet);
					event.getLinks().addAll(LinkModel.getByEventId(event.getId(), connection));
					events.add(event);
				} while (resultSet.next());
				result = events;
			}
		}

		return result;
	}

	public static List<Event> getAll(Connection connection) throws SQLException, IOException {
		String query = queryGroup.getQuery("getAll");
		List<Event> result = null;
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList();
				}
				List<Event> events = new ArrayList<Event>();
				do {
					EventDAO event = new EventDAO(resultSet);
					event.getLinks().addAll(LinkModel.getByEventId(event.getId(), connection));
					events.add(event);
				} while (resultSet.next());
				result = events;
			}
		}

		return result;
	}

	public static void updateEntityEventsInDatabase(List<Event> previousEvents, List<Event> events, Long entityId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousEvents.isEmpty()) {
			deleteEventsRelationByEventId(queryGroup.getQuery(ENTITY_DELETE_QUERY), previousEvents, connection);
			deletePreviousEvents(previousEvents, connection);
		}
		storeEntityEventsToDatabase(events, entityId, connection);
	}

	public static void updateNameserverEventsInDatabase(List<Event> previousEvents, List<Event> events,
			Long nameserverId, Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousEvents.isEmpty()) {
			deleteEventsRelationByEventId(queryGroup.getQuery(NAMESERVER_DELETE_QUERY), previousEvents, connection);
			deletePreviousEvents(previousEvents, connection);
		}
		storeNameserverEventsToDatabase(events, nameserverId, connection);
	}

	public static void updateDsEventsInDatabase(List<Event> previousEvents, List<Event> events, Long dsId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousEvents.isEmpty()) {
			deleteEventsRelationByEventId(queryGroup.getQuery(DS_DELETE_QUERY), previousEvents, connection);
			deletePreviousEvents(previousEvents, connection);
		}
		storeDsDataEventsToDatabase(events, dsId, connection);
	}

	public static void updateDomainEventsInDatabase(List<Event> previousEvents, List<Event> events, Long domainId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousEvents.isEmpty()) {
			deleteEventsRelationByEventId(queryGroup.getQuery(DOMAIN_DELETE_QUERY), previousEvents, connection);
			deletePreviousEvents(previousEvents, connection);
		}
		storeDomainEventsToDatabase(events, domainId, connection);
	}

	public static void updateAutnumEventsInDatabase(List<Event> previousEvents, List<Event> events, Long autnumId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousEvents.isEmpty()) {
			deleteEventsRelationByEventId(queryGroup.getQuery(AUTNUM_DELETE_QUERY), previousEvents, connection);
			deletePreviousEvents(previousEvents, connection);
		}
		storeAutnumEventsToDatabase(events, autnumId, connection);
	}

	public static void updateIpNetworkEventsInDatabase(List<Event> previousEvents, List<Event> events, Long ipNetworkId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (!previousEvents.isEmpty()) {
			deleteEventsRelationByEventId(queryGroup.getQuery(IP_NETWORK_DELETE_QUERY), previousEvents, connection);
			deletePreviousEvents(previousEvents, connection);
		}
		// storeIpNetworkEventsToDatabase(events, ipNetworkId, connection);
	}

	private static void deleteEventsRelationByEventId(String query, List<Event> events, Connection connection)
			throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (Event event : events) {
			ids.add(event.getId());
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

	private static void deletePreviousEvents(List<Event> previousEvents, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		List<Long> ids = new ArrayList<Long>();
		for (Event event : previousEvents) {
			ids.add(event.getId());
			LinkModel.deleteEventLinksData(event.getLinks(), connection);
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
