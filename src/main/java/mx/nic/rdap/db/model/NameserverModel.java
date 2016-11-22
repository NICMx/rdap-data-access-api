package mx.nic.rdap.db.model;

import java.io.IOException;
import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import mx.nic.rdap.core.catalog.Rol;
import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.NameserverDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.exception.InvalidValueException;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * Model for the {@link Nameserver} Object
 * 
 */
public class NameserverModel {

	private final static Logger logger = Logger.getLogger(NameserverModel.class.getName());

	private final static String QUERY_GROUP = "Nameserver";

	private static QueryGroup queryGroup = null;

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	private static void isValidForStore(Nameserver nameserver) throws RequiredValueNotFoundException {
		if (nameserver.getHandle() == null || nameserver.getHandle().isEmpty())
			throw new RequiredValueNotFoundException("handle", "Nameserver");
		if (nameserver.getPunycodeName() == null || nameserver.getPunycodeName().isEmpty())
			throw new RequiredValueNotFoundException("ldhName", "Nameserver");
	}

	private static void isValidForUpdate(Nameserver nameserver) throws RequiredValueNotFoundException {
		if (nameserver.getId() == null)
			throw new RequiredValueNotFoundException("id", "Nameserver");
		if (nameserver.getPunycodeName() == null || nameserver.getPunycodeName().isEmpty())
			throw new RequiredValueNotFoundException("ldhName", "Nameserver");
		if (nameserver.getHandle() == null || nameserver.getHandle().isEmpty())
			throw new RequiredValueNotFoundException("handle", "Nameserver");
		if (nameserver.getPunycodeName() == null || nameserver.getPunycodeName().isEmpty())
			throw new RequiredValueNotFoundException("ldhName", "Nameserver");
	}

	public static void storeToDatabase(Nameserver nameserver, Connection connection)
			throws IOException, SQLException, RequiredValueNotFoundException {
		isValidForStore(nameserver);
		String query = queryGroup.getQuery("storeToDatabase");
		Long nameserverId = null;
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			((NameserverDAO) nameserver).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			result.next();
			nameserverId = result.getLong(1);// The id of the nameserver
												// inserted
			nameserver.setId(nameserverId);
		}
		storeNestedObjects(nameserver, connection);
	}

	public static void storeNestedObjects(Nameserver nameserver, Connection connection)
			throws IOException, SQLException, RequiredValueNotFoundException {
		Long nameserverId = nameserver.getId();
		IpAddressModel.storeToDatabase(nameserver.getIpAddresses(), nameserverId, connection);
		StatusModel.storeNameserverStatusToDatabase(nameserver.getStatus(), nameserverId, connection);
		RemarkModel.storeNameserverRemarksToDatabase(nameserver.getRemarks(), nameserverId, connection);
		LinkModel.storeNameserverLinksToDatabase(nameserver.getLinks(), nameserverId, connection);
		EventModel.storeNameserverEventsToDatabase(nameserver.getEvents(), nameserverId, connection);
		storeNameserverEntities(nameserver, connection);
	}

	public static void storeNameserverEntities(Nameserver nameserver, Connection connection) throws SQLException {
		if (nameserver.getEntities().size() > 0) {
			validateNameserverEntities(nameserver, connection);
			RolModel.storeNameserverEntityRoles(nameserver.getEntities(), nameserver.getId(), connection);
		}

	}

	public static void validateNameserverEntities(Nameserver nameserver, Connection connection) throws SQLException {
		for (Entity entity : nameserver.getEntities()) {
			Long entityId = EntityModel.existsByHandle(entity.getHandle(), connection);
			if (entityId == null) {
				throw new NullPointerException(
						"Entity: " + entity.getHandle() + " was not insert previously to the database");
			}
			entity.setId(entityId);
		}
	}

	/**
	 * Store a list of nameservers that belong from a domain
	 * 
	 */
	public static void storeDomainNameserversToDatabase(List<Nameserver> nameservers, Long domainId,
			Connection connection) throws SQLException, IOException, RequiredValueNotFoundException {
		if (nameservers.isEmpty()) {
			return;
		}

		String query = queryGroup.getQuery("storeDomainNameserversToDatabase");
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			Long nameserverId;
			for (Nameserver nameserver : nameservers) {
				statement.setLong(1, domainId);
				nameserverId = nameserver.getId();
				statement.setLong(2, nameserverId);
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	public static NameserverDAO findByName(String name, Connection connection) throws IOException, SQLException {
		String query = queryGroup.getQuery("findByName");
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, IDN.toASCII(name));
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				NameserverDAO nameserver = new NameserverDAO(resultSet);
				NameserverModel.loadNestedObjects(nameserver, connection);
				return nameserver;
			}
		}
	}

	public static List<NameserverDAO> searchByName(String namePattern, Integer resultLimit, Connection connection)
			throws SQLException, IOException {
		String query = "";
		String criteria = "";
		List<NameserverDAO> nameservers = new ArrayList<NameserverDAO>();
		if (namePattern.contains("*")) {// check if is a partial search

			query = queryGroup.getQuery("searchByPartialName");
			criteria = namePattern.replace('*', '%');
		} else {
			query = queryGroup.getQuery("searchByName");
			criteria = namePattern;
		}
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, criteria);
			statement.setInt(2, resultLimit);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {

				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found");
				}
				do {
					NameserverDAO nameserver = new NameserverDAO(resultSet);
					NameserverModel.loadNestedObjects(nameserver, connection);
					nameservers.add(nameserver);
				} while (resultSet.next());

				return nameservers;
			}
		}
	}

	public static List<NameserverDAO> searchByIp(String ipaddressPattern, Integer resultLimit, Connection connection)
			throws SQLException, IOException, InvalidValueException {
		String query = "";
		List<NameserverDAO> nameservers = new ArrayList<NameserverDAO>();
		try {
			InetAddress address = InetAddress.getByName(ipaddressPattern);
			if (address instanceof Inet6Address) {
				query = queryGroup.getQuery("searchByIp6");
			} else if (address instanceof Inet4Address) {
				query = queryGroup.getQuery("searchByIp4");
			}
		} catch (UnknownHostException e) {
			throw new InvalidValueException("Requested ip is invalid.", "Ip", "Nameserver");
		}
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, ipaddressPattern);
			statement.setInt(2, resultLimit);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {

				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found");
				}
				do {
					NameserverDAO nameserver = new NameserverDAO(resultSet);
					NameserverModel.loadNestedObjects(nameserver, connection);
					nameservers.add(nameserver);
				} while (resultSet.next());

				return nameservers;
			}
		}
	}

	/**
	 * Find nameservers that belongs from a domain by the domain's id
	 * 
	 */
	public static List<Nameserver> getByDomainId(Long domainId, Connection connection)
			throws SQLException, IOException {
		String query = queryGroup.getQuery("getByDomainId");
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, domainId);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList();
				}
				List<Nameserver> nameservers = new ArrayList<Nameserver>();
				do {
					Nameserver nameserver = new NameserverDAO(resultSet);
					NameserverModel.loadNestedObjects(nameserver, connection);
					nameservers.add(nameserver);
				} while (resultSet.next());
				return nameservers;
			}
		}
	}

	public static boolean existNameserverByName(String name, Connection connection) throws IOException, SQLException {
		String query = queryGroup.getQuery("existByName");
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, name);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return false;
				}
				return true;
			}
		}
	}

	private static void loadNestedObjects(Nameserver nameserver, Connection connection)
			throws IOException, SQLException {

		// Retrieve the ipAddress
		try {
			nameserver.setIpAddresses(IpAddressModel.getIpAddressStructByNameserverId(nameserver.getId(), connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, ipaddresses is not required
		}
		// Retrieve the status
		try {
			nameserver.getStatus().addAll(StatusModel.getByNameServerId(nameserver.getId(), connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, status is not required
		}
		// Retrieve the remarks
		try {
			nameserver.getRemarks().addAll(RemarkModel.getByNameserverId(nameserver.getId(), connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, remarks is not required
		}
		// Retrieve the links
		try {
			nameserver.getLinks().addAll(LinkModel.getByNameServerId(nameserver.getId(), connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, links is not required
		}
		// Retrieve the events
		nameserver.getEvents().addAll(EventModel.getByNameServerId(nameserver.getId(), connection));
		// Retrieve the entities
		try {
			List<Entity> entities = EntityModel.getEntitiesByNameserverId(nameserver.getId(), connection);
			nameserver.getEntities().addAll(entities);
			for (Entity entity : entities) {
				List<Rol> roles = RolModel.getNameserverEntityRol(nameserver.getId(), entity.getId(), connection);
				entity.setRoles(roles);
			}
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, entitys is not required
		}
	}

	public static List<Nameserver> getAll(Connection connection) throws IOException, SQLException {
		String query = queryGroup.getQuery("getAll");
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return Collections.emptyList();
				}
				List<Nameserver> nameservers = new ArrayList<Nameserver>();
				do {
					Nameserver nameserver = new NameserverDAO(resultSet);
					NameserverModel.loadNestedObjects(nameserver, connection);
					nameservers.add(nameserver);
				} while (resultSet.next());
				return nameservers;
			}
		}
	}

	private static NameserverDAO getByHandle(String handle, Connection rdapConnection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		if (handle == null || handle.isEmpty()) {
			throw new RequiredValueNotFoundException("handle", "Nameserver");
		}
		String query = queryGroup.getQuery("getByHandle");
		try (PreparedStatement statement = rdapConnection.prepareStatement(query)) {
			statement.setString(1, handle);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				NameserverDAO nameserver = new NameserverDAO(resultSet);
				loadNestedObjects(nameserver, rdapConnection);
				return nameserver;
			}
		}
	}

	private static void update(Nameserver previusNameserver, Nameserver nameserver, Connection rdapConnection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		isValidForUpdate(nameserver);
		String query = queryGroup.getQuery("updateInDatabase");
		try (PreparedStatement statement = rdapConnection.prepareStatement(query)) {
			((NameserverDAO) nameserver).updateInDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
		updatedNestedObjects(previusNameserver, nameserver, rdapConnection);
	}

	public static void updatedNestedObjects(Nameserver previusNameserver, Nameserver nameserver, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		Long nameserverId = nameserver.getId();
		IpAddressModel.updateInDatabase(nameserver.getIpAddresses(), nameserverId, connection);
		StatusModel.updateNameserverStatusInDatabase(nameserver.getStatus(), nameserverId, connection);
		RemarkModel.updateNameserverRemarksInDatabase(previusNameserver.getRemarks(), nameserver.getRemarks(),
				nameserverId, connection);
		LinkModel.updateNameserverLinksInDatabase(previusNameserver.getLinks(), nameserver.getLinks(), nameserverId,
				connection);
		EventModel.updateNameserverEventsInDatabase(previusNameserver.getEvents(), nameserver.getEvents(), nameserverId,
				connection);
		updateNameserverEntities(nameserver, connection);
	}

	public static void updateNameserverEntities(Nameserver nameserver, Connection connection) throws SQLException {
		validateNameserverEntities(nameserver, connection);
		RolModel.updateNameserverEntityRoles(nameserver.getEntities(), nameserver.getId(), connection);
	}

	/**
	 * Can't use a regular upsert sql statement, because nameserver table has
	 * multiple unique constraints, instead will check if the nameserver
	 * exist,then update it on insert it,if not exist.
	 */
	public static void upsertToDatabase(Nameserver nameserver, Connection rdapConnection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		try {
			NameserverDAO previusNameserver = getByHandle(nameserver.getHandle(), rdapConnection);
			nameserver.setId(previusNameserver.getId());
			update(previusNameserver, nameserver, rdapConnection);
		} catch (ObjectNotFoundException onfe) {
			storeToDatabase(nameserver, rdapConnection);
		}
	}
}
