package mx.nic.rdap.db;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import mx.nic.rdap.db.exception.RequiredValueNotFoundException;
import mx.nic.rdap.db.model.RdapUserModel;

/**
 * Test for the rdapuserDAO class
 * 
 */
public class RdapUserTest extends DatabaseTest {

	private String userName = "Test2";
	private String pass = "12345678A";
	private Integer maxSearchResult = 1;
	private String roleName = "AUTHENTICATED";

	@Test
	public void storeToDatabase() {
		try {
			RdapUserDAO user = new RdapUserDAO();
			user.setName(userName);
			user.setPass(pass);
			user.setMaxSearchResults(maxSearchResult);
			RdapUserRoleDAO role = new RdapUserRoleDAO();
			role.setRoleName(roleName);
			user.setUserRole(role);
			RdapUserModel.storeToDatabase(user, connection);
			assert true;
		} catch (RequiredValueNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void getByName() {
		try {
			RdapUserDAO user = new RdapUserDAO();
			user.setName(userName);
			user.setPass(pass);
			user.setMaxSearchResults(maxSearchResult);
			RdapUserRoleDAO role = new RdapUserRoleDAO();
			role.setRoleName(roleName);
			user.setUserRole(role);
			RdapUserModel.storeToDatabase(user, connection);
			RdapUserModel.getByName(userName, connection);
			connection.close();
			assert true;
		} catch (SQLException | IOException | RequiredValueNotFoundException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void cleanUsersTableTest() throws IOException {
		try {
			RdapUserModel.cleanRdapUserDatabase(connection);
			connection.commit();
			connection.close();
			assert true;
		} catch (SQLException e) {
			e.printStackTrace();
			assert false;
		}
	}

}