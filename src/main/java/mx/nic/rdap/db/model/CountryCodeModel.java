package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import mx.nic.rdap.db.QueryGroup;

/**
 * Model for the CountryCode table, read all CountryCodes in the
 * country_code_table and keeps it in memory for quickly access.
 *
 * TODO validate SQL tables and create SQL query in CountryCode.sql
 */

public class CountryCodeModel {

	private final static Logger logger = Logger.getLogger(CountryCodeModel.class.getName());

	private final static String QUERY_GROUP = "CountryCode";

	private static QueryGroup queryGroup = null;

	private static Map<Integer, String> countryNameById;
	private static Map<String, Integer> idByCountryName;

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group", e);
		}
	}

	/**
	 * Load all the country codes stored in the database.
	 * 
	 * @param con
	 *            Connection use to query a database.
	 * @throws SQLException
	 */
	public static void loadAllFromDatabase(Connection con) throws SQLException {
		countryNameById = new HashMap<Integer, String>();
		idByCountryName = new HashMap<String, Integer>();

		String query = queryGroup.getQuery("getAll");

		PreparedStatement statement = con.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		if (!rs.next()) {
			return;
		}

		do {
			Integer id = rs.getInt("ccd_id");
			String countryName = rs.getString("ccd_code");
			countryNameById.put(id, countryName);
			idByCountryName.put(countryName, id);
		} while (rs.next());

	}

	public static String getCountryNameById(Integer id) {
		return countryNameById.get(id);
	}

	public static Integer getIdByCountryName(String countryName) {
		return idByCountryName.get(countryName);
	}
}
