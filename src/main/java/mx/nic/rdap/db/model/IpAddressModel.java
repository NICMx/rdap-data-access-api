package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.IpAddress;
import mx.nic.rdap.core.db.struct.NameserverIpAddressesStruct;
import mx.nic.rdap.db.IpAddressDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.exception.ObjectNotFoundException;

/**
 * Model for the {@link IpAddress} Object
 * 
 */
public class IpAddressModel {

	private final static Logger logger = Logger.getLogger(IpAddressModel.class.getName());

	private final static String QUERY_GROUP = "IpAddress";
	private static final String STORE_QUERY = "storeToDatabase";
	private static final String GET_QUERY = "getByNameserverId";
	private static final String GET_ALL_QUERY = "getAll";
	private final static String DELETE_QUERY = "deleteByNameserverId";
	
	
	protected static QueryGroup queryGroup = null;

	static {
		try {
			IpAddressModel.queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	public static void storeToDatabase(NameserverIpAddressesStruct struct, long nameserverId, Connection connection)
			throws IOException, SQLException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(STORE_QUERY),
				Statement.RETURN_GENERATED_KEYS)) {
			for (IpAddress addressV4 : struct.getIpv4Adresses()) {
				addressV4.setNameserverId(nameserverId);
				((IpAddressDAO) addressV4).storeToDatabase(statement);
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
				ResultSet generatedKeys = statement.getGeneratedKeys();
				generatedKeys.next();
				long id = generatedKeys.getLong(1);
				addressV4.setId(id);
			}
			for (IpAddress addressV6 : struct.getIpv6Adresses()) {
				addressV6.setNameserverId(nameserverId);
				((IpAddressDAO) addressV6).storeToDatabase(statement);
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
				ResultSet generatedKeys = statement.getGeneratedKeys();
				generatedKeys.next();
				long id = generatedKeys.getLong(1);
				addressV6.setId(id);
			}
		}
	}

	public static NameserverIpAddressesStruct getIpAddressStructByNameserverId(Long nameserverId, Connection connection)
			throws IOException, SQLException {
		String query = queryGroup.getQuery(GET_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, nameserverId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new ObjectNotFoundException("Object not found.");
			}

			// Process the resulset to construct the struct
			NameserverIpAddressesStruct struct = new NameserverIpAddressesStruct();
			do {
				IpAddressDAO ipAddressDAO = new IpAddressDAO(resultSet);
				if (ipAddressDAO.getType() == 4) {
					struct.getIpv4Adresses().add(ipAddressDAO);
				} else if (ipAddressDAO.getType() == 6) {
					struct.getIpv6Adresses().add(ipAddressDAO);
				}
			} while (resultSet.next());
			return struct;
		}
	}

	/**
	 * Unused. Getall the ipAddress from DB
	 */
	public static List<IpAddressDAO> getAll(Connection connection) throws IOException, SQLException {
		String query = queryGroup.getQuery(GET_ALL_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new ObjectNotFoundException("Object not found.");
			}

			// Process the resulset to construct the struct
			List<IpAddressDAO> addresses = new ArrayList<IpAddressDAO>();
			do {
				IpAddressDAO ipAddressDAO = new IpAddressDAO(resultSet);
				addresses.add(ipAddressDAO);
			} while (resultSet.next());
			return addresses;
		}
	}

	/**
	 * To avoid a complete search on a likely small data,erase the previous data
	 * and insert the new
	 */
	public static void updateInDatabase(NameserverIpAddressesStruct previousIpAddresses,
			NameserverIpAddressesStruct ipAddresses, Long nameserverId, Connection connection)
			throws SQLException, IOException {
		if (!previousIpAddresses.getIpv4Adresses().isEmpty() || !previousIpAddresses.getIpv6Adresses().isEmpty())
			deleteByNameserverId(nameserverId, connection);
		if (!ipAddresses.getIpv4Adresses().isEmpty() || !ipAddresses.getIpv6Adresses().isEmpty())
			storeToDatabase(ipAddresses, nameserverId, connection);

	}
	
	private static void deleteByNameserverId(Long nameserverId, Connection connection) throws SQLException {
		String query = queryGroup.getQuery(DELETE_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, nameserverId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}
}
