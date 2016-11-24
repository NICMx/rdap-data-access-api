package mx.nic.rdap.db.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.catalog.Rol;
import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.db.EntityDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.exception.ObjectNotFoundException;

/**
 * Model for the {@link Rol} objects of nested entities of main objects.
 * 
 */
public class RolModel {

	private final static Logger logger = Logger.getLogger(RolModel.class.getName());

	private final static String QUERY_GROUP = "Rol";

	private static final String DOMAIN_STORE_QUERY = "storeDomainsEntityRol";
	private static final String ENTITY_STORE_QUERY = "storeEntitiesEntityRol";
	private static final String NAMESERVER_STORE_QUERY = "storeNSEntityRol";
	private static final String AUTNUM_STORE_QUERY = "storeAutnumEntityRol";

	private static final String DOMAIN_GET_QUERY = "getDomainRol";
	private static final String ENTITY_GET_QUERY = "getEntityRol";
	private static final String NAMESERVER_GET_QUERY = "getNSRol";
	private static final String AUTNUM_GET_QUERY = "getAutnumRol";

	private static final String ENTITY_DELETE_QUERY = "deleteEntityEntityRoles";
	private static final String NAMESERVER_DELETE_QUERY = "deleteNameserverEntityRoles";
	private static final String DOMAIN_DELETE_QUERY = "deleteDomainEntityRoles";
	private static final String AUTNUM_DELETE_QUERY = "deleteAutnumEntityRoles";
	private static final String IP_NETWORK_DELETE_QUERY = "deleteIpNetworkEntityRoles";
	private static final String MAIN_ENTITY_DELETE_QUERY = "deleteMainEntityRelation";

	private static QueryGroup queryGroup = null;

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error while loading query group on " + EntityModel.class.getName(), e);
		}
	}

	public static List<Rol> getDomainEntityRol(Long domainId, Long entityId, Connection connection)
			throws SQLException {
		return getNestedEntityRol(domainId, entityId, connection, DOMAIN_GET_QUERY);
	}

	public static List<Rol> getNameserverEntityRol(Long nameserverId, Long entityId, Connection connection)
			throws SQLException {
		return getNestedEntityRol(nameserverId, entityId, connection, NAMESERVER_GET_QUERY);
	}

	public static List<Rol> getEntityEntityRol(Long mainEntityId, Long nestedEntityId, Connection connection)
			throws SQLException {
		return getNestedEntityRol(mainEntityId, nestedEntityId, connection, ENTITY_GET_QUERY);
	}

	public static List<Rol> getAutnumEntityRol(Long autnumId, Long id, Connection connection) throws SQLException {
		return getNestedEntityRol(autnumId, id, connection, AUTNUM_GET_QUERY);
	}

	private static List<Rol> getNestedEntityRol(Long ownerId, Long nestedEntityId, Connection connection,
			String getQuery) throws SQLException {
		String query = queryGroup.getQuery(getQuery);
		List<Rol> roles = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setLong(1, ownerId);
			statement.setLong(2, nestedEntityId);
			ResultSet rs = statement.executeQuery();

			if (!rs.next()) {
				return Collections.emptyList();
			}

			roles = new ArrayList<>();
			do {
				int rolId = rs.getInt(1);
				if (rs.wasNull()) {
					throw new NullPointerException("Rol id was null");
				}
				roles.add(Rol.getById(rolId));
			} while (rs.next());
		}

		return roles;
	}

	public static void storeEntityEntityRoles(List<Entity> entities, Long entityId, Connection connection)
			throws SQLException {
		storeEntitiesRoles(entities, entityId, connection, ENTITY_STORE_QUERY);
	}

	public static void storeNameserverEntityRoles(List<Entity> entities, Long nsId, Connection connection)
			throws SQLException {
		storeEntitiesRoles(entities, nsId, connection, NAMESERVER_STORE_QUERY);
	}

	/**
	 * Stores all domain entity role relations into database on table
	 * domain_entity_role
	 */
	public static void storeDomainEntityRoles(List<Entity> entities, Long domainId, Connection connection)
			throws SQLException {
		storeEntitiesRoles(entities, domainId, connection, DOMAIN_STORE_QUERY);
	}

	public static void storeAutnumEntityRoles(List<Entity> entities, Long autnumId, Connection connection)
			throws SQLException {
		storeEntitiesRoles(entities, autnumId, connection, AUTNUM_STORE_QUERY);
	}

	private static void storeEntitiesRoles(List<Entity> entities, Long ownerId, Connection connection,
			String storeQuery) throws SQLException {
		if (entities.isEmpty())
			return;

		String query = queryGroup.getQuery(storeQuery);

		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setLong(1, ownerId);
			for (Entity entity : entities) {
				statement.setLong(2, entity.getId());
				for (Rol rol : entity.getRoles()) {
					statement.setLong(3, rol.getId());
					logger.log(Level.INFO, "Executing QUERY" + statement.toString());
					statement.execute();
				}
			}
		}
	}

	public static void storeMainEntityRol(List<Entity> nestedEntities, Entity mainEntity, Connection connection)
			throws SQLException {
		if (nestedEntities.isEmpty() || mainEntity.getRoles().isEmpty()) {
			return;
		}

		String query = queryGroup.getQuery(ENTITY_STORE_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			for (Rol rol : mainEntity.getRoles()) {
				for (Entity nestedEntity : nestedEntities) {
					statement.setLong(1, nestedEntity.getId());
					statement.setLong(2, mainEntity.getId());
					statement.setInt(3, rol.getId());
					logger.log(Level.INFO, "Executing QUERY" + statement.toString());
					statement.execute();
				}
			}
		}

	}

	public static List<Rol> getMainEntityRol(List<Entity> nestedEntitiesId, Entity mainEntity, Connection connection)
			throws SQLException {
		if (nestedEntitiesId.isEmpty()) {
			return Collections.emptyList();
		}
		String query = queryGroup.getQuery("getMainEntityRol");

		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < nestedEntitiesId.size() - 1; i++) {
			sb.append(nestedEntitiesId.get(i).getId() + ", ");
		}
		sb.append(nestedEntitiesId.get(i).getId());

		List<Rol> resultRoles = null;
		try (PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setLong(1, mainEntity.getId());
			statement.setString(2, sb.toString());
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return Collections.emptyList();

			resultRoles = new ArrayList<>();
			do {
				int rolId = rs.getInt(1);
				if (rs.wasNull()) {
					throw new ObjectNotFoundException("Return rows, but not valid rol id");
				}
				resultRoles.add(Rol.getById(rolId));
			} while (rs.next());
		}

		return resultRoles;
	}

	public static void updateEntityEntityRoles(List<Entity> entities, EntityDAO entity, Connection connection)
			throws SQLException {
		deleteRelationByParentId(queryGroup.getQuery(ENTITY_DELETE_QUERY), entity.getId(), connection);
		storeEntityEntityRoles(entities, entity.getId(), connection);
		updateMainEntityRol(entities, entity, connection);
	}

	/**
	 * Update the main entity rol regarding the nested entities
	 */
	private static void updateMainEntityRol(List<Entity> nestedEntities, Entity mainEntity, Connection connection)
			throws SQLException {
		deleteRelationByParentId(queryGroup.getQuery(MAIN_ENTITY_DELETE_QUERY), mainEntity.getId(), connection);
		if (!mainEntity.getRoles().isEmpty() && !nestedEntities.isEmpty())
			storeMainEntityRol(nestedEntities, mainEntity, connection);
	}

	public static void updateNameserverEntityRoles(List<Entity> entities, Long nameserverId, Connection connection)
			throws SQLException {
		deleteRelationByParentId(queryGroup.getQuery(NAMESERVER_DELETE_QUERY), nameserverId, connection);
		storeNameserverEntityRoles(entities, nameserverId, connection);
	}

	public static void updateDomainEntityRoles(List<Entity> entities, Long domainId, Connection connection)
			throws SQLException {
		deleteRelationByParentId(queryGroup.getQuery(DOMAIN_DELETE_QUERY), domainId, connection);
		storeDomainEntityRoles(entities, domainId, connection);
	}

	public static void updateAutnumrEntityRoles(List<Entity> entities, Long autnumId, Connection connection)
			throws SQLException {
		deleteRelationByParentId(queryGroup.getQuery(AUTNUM_DELETE_QUERY), autnumId, connection);
		storeAutnumEntityRoles(entities, autnumId, connection);
	}

	public static void updateIpNetworkEntityRoles(List<Entity> entities, Long ipNetworkId, Connection connection)
			throws SQLException {
		deleteRelationByParentId(queryGroup.getQuery(IP_NETWORK_DELETE_QUERY), ipNetworkId, connection);
		// storeIpNetworkEntityRoles(entities, ipNetworkId, connection);
	}

	private static void deleteRelationByParentId(String query, Long objectId, Connection connection)
			throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, objectId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

}
