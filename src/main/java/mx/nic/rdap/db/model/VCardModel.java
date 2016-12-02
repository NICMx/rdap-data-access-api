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

import mx.nic.rdap.core.db.VCard;
import mx.nic.rdap.core.db.VCardPostalInfo;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.Util;
import mx.nic.rdap.db.VCardDAO;
import mx.nic.rdap.db.exception.ObjectNotFoundException;

/**
 * Model for the {@link VCard}
 * 
 */
public class VCardModel {

	private static final Logger logger = Logger.getLogger(VCardModel.class.getName());

	private static final String QUERY_GROUP = "VCard";

	protected static QueryGroup queryGroup = null;

	private final static String STORE_QUERY = "storeToDatabase";
	private final static String STORE_ENTITY_CONTACT_QUERY = "storeEntityContact";
	private final static String GET_QUERY = "getById";
	private final static String GET_BY_ENTITY_QUERY = "getByEntityId";
	private final static String DELETE_QUERY = "deleteById";
	private final static String DELETE_REGISTRAR_CONTACT_QUERY = "deleteRegistrarContact";

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error while loading query group on " + VCardModel.class.getName(), e);
		}
	}

	/**
	 * Store a VCard
	 * 
	 */
	public static long storeToDatabase(VCard vCard, Connection connection) throws SQLException {
		long vCardId;

		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(STORE_QUERY),
				Statement.RETURN_GENERATED_KEYS);) {
			((VCardDAO) vCard).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();

			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			vCardId = resultSet.getLong(1);
			vCard.setId(vCardId);
		}

		for (VCardPostalInfo postalInfo : vCard.getPostalInfo()) {
			postalInfo.setVCardId(vCardId);
			VCardPostalInfoModel.storeToDatabase(postalInfo, connection);
		}

		return vCardId;
	}

	public static void storeRegistrarContactToDatabase(List<VCard> vCardList, Long registrarId, Connection connection)
			throws SQLException {
		if (vCardList.isEmpty())
			return;

		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(STORE_ENTITY_CONTACT_QUERY),
				Statement.RETURN_GENERATED_KEYS);) {
			for (VCard vCard : vCardList) {
				statement.setLong(1, registrarId);
				statement.setLong(2, vCard.getId());
				logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
				statement.executeUpdate();
			}
		}
	}

	/**
	 * Get a {@link VCard} by its Id.
	 * 
	 */
	public static VCard getById(Long vCardId, Connection connection) throws SQLException {
		VCard vCardResult = null;
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_QUERY));) {
			statement.setLong(1, vCardId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			vCardResult = processResultSet(resultSet, connection);
		}

		setSonObjects(vCardResult, connection);

		return vCardResult;
	}

	/**
	 * Get a {@link List} of {@link VCard} belonging to a {@link Registrar} by
	 * the registrar Id.
	 * 
	 */
	public static List<VCard> getByEntityId(Long registrarId, Connection connection) throws SQLException {
		List<VCard> vCardResults = null;
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_BY_ENTITY_QUERY));) {
			statement.setLong(1, registrarId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			ResultSet resultSet = statement.executeQuery();

			vCardResults = processListResultSet(resultSet, connection);
		}

		for (VCard vCard : vCardResults) {
			setSonObjects(vCard, connection);
		}

		return vCardResults;
	}

	/**
	 * Get and Set the nested objects of the {@link VCard}.
	 * 
	 */
	private static void setSonObjects(VCard vCard, Connection connection) throws SQLException {
		try {
			List<VCardPostalInfo> postalInfoList = VCardPostalInfoModel.getByVCardId(vCard.getId(), connection);
			vCard.setPostalInfo(postalInfoList);
		} catch (ObjectNotFoundException e) {
			// TODO: a VCard couldn't have postal info ?
		}
	}

	/**
	 * Process a resultSet and return one {@link VCard}.
	 * 
	 */
	private static VCard processResultSet(ResultSet resultSet, Connection connection) throws SQLException {
		if (!resultSet.next()) {
			throw new ObjectNotFoundException("Object not found");
		}
		VCardDAO vCard = new VCardDAO();
		vCard.loadFromDatabase(resultSet);

		return vCard;
	}

	/**
	 * Process a {@link ResultSet} and return a {@link List} of {@link VCard}s.
	 * 
	 */
	private static List<VCard> processListResultSet(ResultSet resultSet, Connection connection) throws SQLException {
		List<VCard> result = new ArrayList<>();
		if (!resultSet.next()) {
			throw new ObjectNotFoundException("Object not found");
		}
		do {
			VCardDAO vCard = new VCardDAO();
			vCard.loadFromDatabase(resultSet);
			result.add(vCard);
		} while (resultSet.next());

		return result;
	}

	public static void deletePreviousVCards(List<VCard> previousVCards, Connection connection) throws SQLException {
		List<Long> ids = new ArrayList<Long>();
		for (VCard vCard : previousVCards) {
			VCardPostalInfoModel.deleteByVCardId(vCard.getId(), connection);
			ids.add(vCard.getId());
		}
		String dynamicQuery = Util.createDynamicQueryWithInClause(ids.size(), queryGroup.getQuery(DELETE_QUERY));
		try (PreparedStatement statement = connection.prepareStatement(dynamicQuery)) {
			int index = 1;
			for (Long id : ids) {
				statement.setLong(index++, id);
			}
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

	public static void deleteRegistrarContact(Long registrarId, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection
				.prepareStatement(queryGroup.getQuery(DELETE_REGISTRAR_CONTACT_QUERY))) {
			statement.setLong(1, registrarId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}

	}

}
