package mx.nic.rdap.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

public class DatabaseSession {

	public static final String RDAP_DB = "database";

	private static BasicDataSource getEnvironmentDataSource(Properties config) throws SQLException {
		BasicDataSource ds;
		ds = new BasicDataSource();
		ds.setDriverClassName(config.getProperty("driverClassName"));
		ds.setUrl(config.getProperty("url"));
		ds.setUsername(config.getProperty("userName"));
		ds.setPassword(config.getProperty("password"));
		ds.setDefaultAutoCommit(false);
		testDatabase(ds);
		return ds;
	}

	private static void testDatabase(BasicDataSource ds) throws SQLException {
		// http://stackoverflow.com/questions/3668506
		final String TEST_QUERY = "select 1";
		try (Connection connection = ds.getConnection(); Statement statement = connection.createStatement();) {
			ResultSet resultSet = statement.executeQuery(TEST_QUERY);

			if (!resultSet.next()) {
				throw new SQLException("'" + TEST_QUERY + "' returned no rows.");
			}
			int result = resultSet.getInt(1);
			if (result != 1) {
				throw new SQLException("'" + TEST_QUERY + "' returned " + result);
			}
		}
	}

	public static Connection getRdapConnection() throws SQLException, IOException {
		return getEnvironmentDataSource(Util.loadProperties(RDAP_DB)).getConnection();
	}

	/**
	 * Return a connection to the origin database
	 * 
	 * @param migrationDBProperties
	 *            Information about the database origin of the migration data
	 */
	public static Connection getMigrationConnection(Properties migrationDBProperties) throws SQLException, IOException {
		return getEnvironmentDataSource(migrationDBProperties).getConnection();
	}

}
