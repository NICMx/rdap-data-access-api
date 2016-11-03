/**
 * 
 */
package mx.nic.rdap.db;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mx.nic.rdap.db.exception.RequiredValueNotFoundException;
import mx.nic.rdap.db.model.ZoneModel;

/**
 * Tests for the {@link ZoneModel}
 * 
 * @author evaldes
 *
 */
public class ZoneTest  {

	/**
	 * Connection for these tests
	 */
	private static Connection connection = null;

	@Before
	public void before() throws SQLException, IOException {
		connection = DatabaseSession.getRdapConnection();
		Map<Integer, String> zoneByIdForServer = new HashMap<Integer, String>();
		Map<String, Integer> idByZoneForServer = new HashMap<String, Integer>();
		// Ovewrite the hashmaps to only use the configurated zones
		ZoneModel.setZoneById(zoneByIdForServer);
		ZoneModel.setIdByZone(idByZoneForServer);
	}

	@After
	public void after() throws SQLException {
		connection.rollback();
		connection.close();
	}

	@Test
	/**
	 * Creates a new Zone instance and stores it in the database, then it get an
	 * instance with the id generated
	 */
	public void insertAndGetBy() throws IOException, SQLException, RequiredValueNotFoundException {
		Random random = new Random();
		int randomInt = random.nextInt();

		String zoneName = "example" + randomInt + ".mx";
		Integer zoneId = null;

		try {
			zoneId = ZoneModel.storeToDatabase(zoneName, connection);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}

		String byId = ZoneModel.getZoneNameById(zoneId);

		Assert.assertTrue("Get by Id fails", zoneName.equals(byId));
	}

}