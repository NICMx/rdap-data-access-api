package mx.nic.rdap.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DBConnection {
	private static DataSource dataSource;

	private DBConnection() {
		// no code;
	}

	public static void loadDatasource(DataSource dataSource) {
		DBConnection.dataSource = dataSource;
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

}
