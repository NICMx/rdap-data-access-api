package mx.nic.rdap.db.model;

import java.io.IOException;
import java.math.BigInteger;
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

import mx.nic.rdap.IpUtils;
import mx.nic.rdap.core.catalog.IpVersion;
import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.IpNetworkDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.exception.InvalidValueException;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * Model for the {@link IpNetworkModel} Object
 * 
 */
public class IpNetworkModel {

	private final static Logger logger = Logger.getLogger(EntityModel.class.getName());

	private final static String QUERY_GROUP = "IpNetwork";

	protected static QueryGroup queryGroup = null;

	private static final String GET_BY_IPV4 = "getByIPv4";
	private static final String GET_BY_IPV6 = "getByIPv6";
	private static final String STORE_TO_DATABASE = "storeToDatabase";
	private static final String GET_BY_ENTITY_ID = "getByEntityId";
	private static final String GET_BY_DOMAIN_ID = "getByDomainId";
	private static final String GET_BY_HANDLE = "getByHandle";

	private static final String EXIST_BY_IPV4 = "existByIPv4";
	private static final String EXIST_BY_IPV6 = "existByIPv6";
	private static final String UPDATE_QUERY = "updateInDatabase";

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error while loading query group on " + IpNetworkModel.class.getName(), e);
		}
	}

	private static void isValidForStore(IpNetwork ipNetwork) throws RdapDatabaseException {
		if (ipNetwork.getHandle() == null)
			throw new RequiredValueNotFoundException("handle", "IpNetwork");

		if (ipNetwork.getStartAddress() == null) {
			throw new RequiredValueNotFoundException("startAddress", "IpNetwork");
		}

		if (ipNetwork.getEndAddress() == null) {
			throw new RequiredValueNotFoundException("endAddress", "IpNetwork");
		}

		if (ipNetwork.getIpVersion() == null) {
			throw new RequiredValueNotFoundException("ipVersion", "IpNetwork");
		}

		if (ipNetwork.getIpVersion() == IpVersion.V4) {
			if (ipNetwork.getStartAddress() instanceof Inet6Address
					|| ipNetwork.getEndAddress() instanceof Inet6Address) {
				throw new RuntimeException("IP version doesn't match with the IpVersion set to the object");
			}
		} else {
			if (ipNetwork.getStartAddress() instanceof Inet4Address
					|| ipNetwork.getEndAddress() instanceof Inet4Address) {
				throw new RuntimeException("IP version doesn't match with the IpVersion set to the object");
			}

		}

		IpUtils.validateIpCidr(ipNetwork.getStartAddress(), ipNetwork.getCidr());
		IpUtils.validateLastIpCidr(ipNetwork.getEndAddress(), ipNetwork.getCidr());
	}

	public static Long storeToDatabase(IpNetwork ipNetwork, Connection connection)
			throws RdapDatabaseException, SQLException, IOException {
		isValidForStore(ipNetwork);
		String query = queryGroup.getQuery(STORE_TO_DATABASE);

		Long resultId = null;
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			((IpNetworkDAO) ipNetwork).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();

			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			resultId = resultSet.getLong(1);
			ipNetwork.setId(resultId);
		}

		storeNestedObjects(ipNetwork, connection);

		return resultId;
	}

	private static void storeNestedObjects(IpNetwork ipNetwork, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		StatusModel.storeIpNetworkStatusToDatabase(ipNetwork.getStatus(), ipNetwork.getId(), connection);
		RemarkModel.storeIpNetworkRemarksToDatabase(ipNetwork.getRemarks(), ipNetwork.getId(), connection);
		LinkModel.storeIpNetworkLinksToDatabase(ipNetwork.getLinks(), ipNetwork.getId(), connection);
		EventModel.storeIpNetworkEventsToDatabase(ipNetwork.getEvents(), ipNetwork.getId(), connection);
		for (Entity ent : ipNetwork.getEntities()) {
			Long entId = EntityModel.getIdByHandle(ent.getHandle(), connection);
			if (entId == null) {
				throw new NullPointerException(
						"Entity: " + ent.getHandle() + "was not inserted previously to the database");
			}
			ent.setId(entId);
		}
		RolModel.storeIpNetworkEntityRoles(ipNetwork.getEntities(), ipNetwork.getId(), connection);

	}

	private static void loadNestedObjects(IpNetwork ipNetwork, Connection connection) throws SQLException, IOException {
		Long ipNetworkId = ipNetwork.getId();

		loadSimpleNestedObjects(ipNetwork, connection);

		// Retrieve the entities
		try {
			ipNetwork.getEntities().addAll(EntityModel.getEntitiesByIpNetworkId(ipNetworkId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, entities is not required
		}

	}

	private static void loadSimpleNestedObjects(IpNetwork ipNetwork, Connection connection)
			throws SQLException, IOException {
		Long ipNetworkId = ipNetwork.getId();

		// Retrieve the events
		try {
			ipNetwork.getEvents().addAll(EventModel.getByIpNetworkId(ipNetworkId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, events is not required
		}
		// Retrieve the links
		try {
			ipNetwork.getLinks().addAll(LinkModel.getByIpNetworkId(ipNetworkId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, links is not required
		}
		// Retrieve the status
		try {
			ipNetwork.getStatus().addAll(StatusModel.getByIpNetworkId(ipNetworkId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, status is not required
		}
		// Retrieve the remarks
		try {
			ipNetwork.getRemarks().addAll(RemarkModel.getByIpNetworkId(ipNetworkId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, remarks is not required
		}
	}

	private static IpNetwork getByInet4Address(Inet4Address inetAddress, Integer cidr, Connection connection)
			throws UnknownHostException, SQLException {
		InetAddress lastAddressFromNetwork = IpUtils.getLastAddressFromNetwork(inetAddress, cidr);

		BigInteger start = IpUtils.addressToNumber(inetAddress);
		BigInteger end = IpUtils.addressToNumber((Inet4Address) lastAddressFromNetwork);

		String query = queryGroup.getQuery(GET_BY_IPV4);
		IpNetworkDAO ipDao = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setInt(1, cidr);
			statement.setString(2, start.toString());
			statement.setString(3, end.toString());
			ResultSet rs = statement.executeQuery();
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			if (!rs.next()) {
				throw new ObjectNotFoundException("Object not found");
			}

			ipDao = new IpNetworkDAO();
			ipDao.loadFromDatabase(rs);
		}

		return ipDao;
	}

	private static IpNetwork getByInet6Address(Inet6Address inetAddress, Integer cidr, Connection connection)
			throws UnknownHostException, SQLException {
		InetAddress lastAddressFromNetwork = IpUtils.getLastAddressFromNetwork(inetAddress, cidr);

		BigInteger startUpperPart = IpUtils.inet6AddressToUpperPart(inetAddress);
		BigInteger startLowerPart = IpUtils.inet6AddressToLowerPart(inetAddress);

		BigInteger endUpperPart = IpUtils.inet6AddressToUpperPart((Inet6Address) lastAddressFromNetwork);
		BigInteger endLowerPart = IpUtils.inet6AddressToLowerPart((Inet6Address) lastAddressFromNetwork);

		String query = queryGroup.getQuery(GET_BY_IPV6);
		IpNetworkDAO ipDao = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setInt(1, cidr);
			statement.setString(2, startUpperPart.toString());
			statement.setString(3, startLowerPart.toString());

			statement.setString(4, endUpperPart.toString());
			statement.setString(5, endLowerPart.toString());

			ResultSet rs = statement.executeQuery();
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());

			if (!rs.next()) {
				throw new ObjectNotFoundException("Object not found");
			}

			ipDao = new IpNetworkDAO();
			ipDao.loadFromDatabase(rs);
		}

		return ipDao;
	}

	public static IpNetwork getByInetAddress(InetAddress inetAddress, Integer cidr, Connection connection)
			throws SQLException, IOException {
		IpNetwork result = null;
		if (inetAddress instanceof Inet4Address) {
			result = getByInet4Address((Inet4Address) inetAddress, cidr, connection);
		} else if (inetAddress instanceof Inet6Address) {
			result = getByInet6Address((Inet6Address) inetAddress, cidr, connection);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}

		loadNestedObjects(result, connection);

		return result;
	}

	public static IpNetwork getByInetAddress(InetAddress inetAddress, Connection connection)
			throws SQLException, IOException {
		return getByInetAddress(inetAddress, IpUtils.getMaxValidCidr(inetAddress), connection);
	}

	public static IpNetwork getByInetAddress(String ipAddress, Connection connection) throws SQLException, IOException {
		InetAddress inetAddress;
		try {
			inetAddress = IpUtils.validateIpAddress(ipAddress);
		} catch (InvalidValueException e) {
			throw new UnknownHostException(ipAddress);
		}
		return getByInetAddress(inetAddress, connection);
	}

	public static IpNetwork getByInetAddress(String ipAddress, Integer cidr, Connection connection)
			throws SQLException, IOException {
		InetAddress inetAddress;
		try {
			inetAddress = IpUtils.validateIpAddress(ipAddress);
		} catch (InvalidValueException e) {
			throw new UnknownHostException(ipAddress);
		}
		return getByInetAddress(inetAddress, cidr, connection);
	}

	public static IpNetwork getByDomainId(long domainId, Connection connection) throws SQLException, IOException {
		String query = queryGroup.getQuery(GET_BY_DOMAIN_ID);
		IpNetworkDAO result = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setLong(1, domainId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			ResultSet rs = statement.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("Object not found");
			}

			result = new IpNetworkDAO();
			result.loadFromDatabase(rs);
			loadSimpleNestedObjects(result, connection);
		}

		return result;
	}

	public static List<IpNetwork> getByEntityId(long entityId, Connection connection) throws SQLException, IOException {
		return getByRdapObjectId(entityId, connection, GET_BY_ENTITY_ID);
	}

	private static List<IpNetwork> getByRdapObjectId(long id, Connection connection, String getQueryId)
			throws SQLException, IOException {
		String query = queryGroup.getQuery(getQueryId);
		List<IpNetwork> results = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setLong(1, id);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			ResultSet rs = statement.executeQuery();
			if (!rs.next()) {
				return Collections.emptyList();
			}

			results = new ArrayList<IpNetwork>();

			do {
				IpNetworkDAO ipNetwork = new IpNetworkDAO();
				ipNetwork.loadFromDatabase(rs);
				results.add(ipNetwork);
			} while (rs.next());
		}

		for (IpNetwork ip : results) {
			loadSimpleNestedObjects(ip, connection);
		}

		return results;
	}

	public static IpNetworkDAO getByHandle(String handle, Connection connection) throws SQLException, IOException {
		String query = queryGroup.getQuery(GET_BY_HANDLE);// TOODO);
		IpNetworkDAO result = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setString(1, handle);
			logger.log(Level.INFO, "Executing QUERY : " + statement.toString());
			ResultSet rs = statement.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("Object not found");
			}

			result = new IpNetworkDAO();
			result.loadFromDatabase(rs);
		}

		loadNestedObjects(result, connection);

		return result;
	}

	/**
	 * Can't use a regular upsert sql statement, because domain table has
	 * multiple unique constraints, instead will check if the nameserver
	 * exist,then update it on insert it,if not exist.
	 * 
	 */
	public static void upsertToDatabase(IpNetworkDAO ipNetwork, Connection rdapConnection)
			throws SQLException, IOException, RdapDatabaseException {
		try {
			IpNetworkDAO previousNetwork = getByHandle(ipNetwork.getHandle(), rdapConnection);
			ipNetwork.setId(previousNetwork.getId());
			update(previousNetwork, ipNetwork, rdapConnection);
		} catch (ObjectNotFoundException onfe) {
			storeToDatabase(ipNetwork, rdapConnection);
		}
	}

	public static void isValidForUpdate(IpNetworkDAO ipNetwork) throws RdapDatabaseException {
		isValidForStore(ipNetwork);

		if (ipNetwork.getId() == null)
			throw new RequiredValueNotFoundException("id", "IpNetwork");
	}

	private static void update(IpNetworkDAO previousNetwork, IpNetworkDAO ipNetwork, Connection connection)
			throws RdapDatabaseException, SQLException, IOException {
		isValidForStore(ipNetwork);
		String query = queryGroup.getQuery(UPDATE_QUERY);

		try (PreparedStatement statement = connection.prepareStatement(query)) {
			ipNetwork.updateInDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}

		updatedNestedObjects(previousNetwork, ipNetwork, connection);
	}

	private static void updatedNestedObjects(IpNetworkDAO previousNetwork, IpNetworkDAO ipNetwork,
			Connection connection) throws RequiredValueNotFoundException, SQLException, IOException {
		Long networkId = ipNetwork.getId();
		RemarkModel.updateIpNetworkRemarksInDatabase(previousNetwork.getRemarks(), ipNetwork.getRemarks(), networkId,
				connection);
		EventModel.updateIpNetworkEventsInDatabase(previousNetwork.getEvents(), ipNetwork.getEvents(), networkId,
				connection);
		StatusModel.updateIpNetworkStatusInDatabase(previousNetwork.getStatus(), ipNetwork.getStatus(), networkId,
				connection);
		LinkModel.updateIpNetworkLinksInDatabase(previousNetwork.getLinks(), ipNetwork.getLinks(), networkId,
				connection);
		RolModel.updateIpNetworkEntityRoles(previousNetwork.getEntities(), ipNetwork.getEntities(), networkId,
				connection);
	}

	public static void existByInetAddress(String ipAddress, Integer cidr, Connection connection)
			throws SQLException, IOException {
		InetAddress inetAddress;
		try {
			inetAddress = IpUtils.validateIpAddress(ipAddress);
		} catch (InvalidValueException e) {
			throw new UnknownHostException(ipAddress);
		}
		existByInetAddress(inetAddress, cidr, connection);

	}

	public static void existByInetAddress(String ipAddress, Connection connection) throws SQLException, IOException {
		InetAddress inetAddress;
		try {
			inetAddress = IpUtils.validateIpAddress(ipAddress);
		} catch (InvalidValueException e) {
			throw new UnknownHostException(ipAddress);
		}
		existByInetAddress(inetAddress, connection);

	}

	private static void existByInetAddress(InetAddress inetAddress, Connection connection)
			throws SQLException, IOException {
		existByInetAddress(inetAddress, IpUtils.getMaxValidCidr(inetAddress), connection);
	}

	private static void existByInetAddress(InetAddress inetAddress, Integer cidr, Connection connection)
			throws SQLException, IOException {
		if (inetAddress instanceof Inet4Address) {
			existByInet4Address((Inet4Address) inetAddress, cidr, connection);
		} else if (inetAddress instanceof Inet6Address) {
			existByInet6Address((Inet6Address) inetAddress, cidr, connection);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static void existByInet4Address(Inet4Address inetAddress, Integer cidr, Connection connection)
			throws UnknownHostException, SQLException {
		InetAddress lastAddressFromNetwork = IpUtils.getLastAddressFromNetwork(inetAddress, cidr);

		BigInteger start = IpUtils.addressToNumber(inetAddress);
		BigInteger end = IpUtils.addressToNumber((Inet4Address) lastAddressFromNetwork);

		String query = queryGroup.getQuery(EXIST_BY_IPV4);
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setInt(1, cidr);
			statement.setString(2, start.toString());
			statement.setString(3, end.toString());
			ResultSet rs = statement.executeQuery();
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			rs.next();
			if (rs.getInt(1) == 0) {
				throw new ObjectNotFoundException("Object not found.");
			}

		}

	}

	private static void existByInet6Address(Inet6Address inetAddress, Integer cidr, Connection connection)
			throws UnknownHostException, SQLException {
		InetAddress lastAddressFromNetwork = IpUtils.getLastAddressFromNetwork(inetAddress, cidr);

		BigInteger startUpperPart = IpUtils.inet6AddressToUpperPart(inetAddress);
		BigInteger startLowerPart = IpUtils.inet6AddressToLowerPart(inetAddress);

		BigInteger endUpperPart = IpUtils.inet6AddressToUpperPart((Inet6Address) lastAddressFromNetwork);
		BigInteger endLowerPart = IpUtils.inet6AddressToLowerPart((Inet6Address) lastAddressFromNetwork);

		String query = queryGroup.getQuery(EXIST_BY_IPV6);
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setInt(1, cidr);
			statement.setString(2, startUpperPart.toString());
			statement.setString(3, startLowerPart.toString());

			statement.setString(4, endUpperPart.toString());
			statement.setString(5, endLowerPart.toString());

			ResultSet rs = statement.executeQuery();
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());

			rs.next();
			if (rs.getInt(1) == 0) {
				throw new ObjectNotFoundException("Object not found.");
			}

		}

	}
}
