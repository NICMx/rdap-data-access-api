package mx.nic.rdap.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import mx.nic.rdap.db.DatabaseSession;
import mx.nic.rdap.db.RdapUserDAO;
import mx.nic.rdap.db.RdapUserRoleDAO;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;
import mx.nic.rdap.db.model.RdapUserModel;

/**
 * Test for the rdapuserDAO class
 * 
 * @author dalpuche
 *
 */
public class RdapUserTest  {

	private String userName = "Test2";
	private String pass = "12345678A";
	private Integer maxSearchResult = 1;
	private String roleName = "AUTHENTICATED";

	@Test
	public void storeToDatabase() {
		try {
			try (Connection connection = DatabaseSession.getRdapConnection()) {
				RdapUserDAO user = new RdapUserDAO();
				user.setName(userName);
				user.setPass(pass);
				user.setMaxSearchResults(maxSearchResult);
				RdapUserRoleDAO role = new RdapUserRoleDAO();
				role.setRoleName(roleName);
				user.setUserRole(role);
				RdapUserModel.storeToDatabase(user, connection);
				connection.commit();
			}
			assert true;
		} catch (RequiredValueNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void getByName() {
		try {
			try (Connection connection = DatabaseSession.getRdapConnection()) {
				RdapUserModel.getByName(userName, connection);
			}
			assert true;
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void cleanUsersTableTest() throws IOException {
		try {
			try (Connection connection = DatabaseSession.getRdapConnection()) {
				RdapUserModel.cleanRdapUserDatabase(connection);
				connection.commit();
			}
			assert true;
		} catch (SQLException e) {
			e.printStackTrace();
			assert false;
		}
	}

}
