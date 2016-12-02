package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.RdapUserRoleDAO;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;

/**
 * Model for RdapUserRole Data
 */
public class RdapUserRoleModel {

	private final static Logger logger = Logger.getLogger(RdapUserRoleModel.class.getName());

	private final static String QUERY_GROUP = "RdapUserRole";
	private final static String STORE_QUERY="storeToDatabase";
	private final static String GET_QUERY="getByUserName";
	private final static String DELETE_QUERY="deleteFromDatabase";
	private static QueryGroup queryGroup = null;

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	/**
	 * Validate the required attributes for the rdapUserRole
	 */
	private static void isValidForStore(RdapUserRoleDAO userRole) throws RequiredValueNotFoundException {
		if (userRole.getUserName() == null || userRole.getUserName().isEmpty())
			throw new RequiredValueNotFoundException("userName", "RdapUserRole");
		if (userRole.getRoleName() == null || userRole.getRoleName().isEmpty())
			throw new RequiredValueNotFoundException("roleName", "RdapUserRole");
	}

	public static void storeRdapUserRoleToDatabase(RdapUserRoleDAO userRole, Connection connection)
			throws RequiredValueNotFoundException, SQLException {
		isValidForStore(userRole);
		String query = queryGroup.getQuery(STORE_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			userRole.storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

	public static RdapUserRoleDAO getByUserName(String userName, Connection connection) throws SQLException {
		String query = queryGroup.getQuery(GET_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, userName);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				RdapUserRoleDAO userRole = new RdapUserRoleDAO(resultSet);
				return userRole;
			}
		}
	}

	/**
	 * To avoid a complete search on a likely small data,erase the previous data
	 * and insert the new
	 */
	public static void updateRdapUserRolesInDatabase(String name, RdapUserRoleDAO userRole, Connection rdapConnection)
			throws RequiredValueNotFoundException, SQLException {
		deleteRelationByParentId(name, rdapConnection);
		storeRdapUserRoleToDatabase(userRole, rdapConnection);

	}

	private static void deleteRelationByParentId(String name, Connection rdapConnection) throws SQLException {
		String query = queryGroup.getQuery(DELETE_QUERY);
		try (PreparedStatement statement = rdapConnection.prepareStatement(query)) {
			statement.setString(1, name);
			logger.log(Level.INFO, "Executing query: " + statement.toString());
			statement.executeUpdate();
		}
	}

}
