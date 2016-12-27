package mx.nic.rdap.db.model;

import java.io.IOException;
import java.net.IDN;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.Domain;
import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.DomainDAO;
import mx.nic.rdap.db.IpAddressDAO;
import mx.nic.rdap.db.QueryGroup;
import mx.nic.rdap.db.Util;
import mx.nic.rdap.db.exception.InvalidValueException;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;
import mx.nic.rdap.db.struct.SearchResultStruct;

/**
 * Model for the {@link Domain} Object
 * 
 */
public class DomainModel {

	private final static Logger logger = Logger.getLogger(DomainModel.class.getName());

	private final static String QUERY_GROUP = "Domain";

	private static QueryGroup queryGroup = null;
	private static final String STORE_QUERY = "storeToDatabase";
	private static final String UPDATE_QUERY = "updateInDatabase";

	private static final String STORE_IP_NETWORK_RELATION_QUERY = "storeDomainIpNetworkRelation";
	private static final String GET_BY_LDH_QUERY = "getByLdhName";
	private static final String EXIST_BY_LDH_QUERY = "existByLdhName";
	private static final String GET_BY_ID_QUERY = "getDomainById";
	private static final String GET_BY_HANDLE_QUERY = "getByHandle";
	private static final String SEARCH_BY_PARTIAL_NAME_WITH_PARTIAL_ZONE_QUERY = "searchByPartialNameWPartialZone";
	private static final String SEARCH_BY_NAME_WITH_PARTIAL_ZONE_QUERY = "searchByNameWPartialZone";
	private static final String SEARCH_BY_PARTIAL_NAME_WITH_ZONE_QUERY = "searchByPartialNameWZone";
	private static final String SEARCH_BY_NAME_WITH_ZONE_QUERY = "searchByNameWZone";
	private static final String SEARCH_BY_PARTIAL_NAME_WITHOUT_ZONE_QUERY = "searchByPartialNameWOutZone";
	private static final String SEARCH_BY_NAME_WITHOUT_ZONE_QUERY = "searchByNameWOutZone";
	private static final String SEARCH_BY_NAMESERVER_LDH_QUERY = "searchByNsLdhName";
	private static final String SEARCH_BY_NAMESERVER_IP_QUERY = "searchByNsIp";

	private static final String EXIST_BY_PARTIAL_NAME_WITHOUT_ZONE_QUERY = "existByPartialNameWOutZone";
	private static final String EXIST_BY_NAME_WITHOUT_ZONE_QUERY = "existByNameWOutZone";
	private static final String EXIST_BY_NAME_WITH_ZONE_QUERY = "existByNameWZone";
	private static final String EXIST_BY_PARTIAL_NAME_WITH_ZONE_QUERY = "existByPartialNameWZone";
	private static final String EXIST_BY_NAME_WITH_PARTIAL_ZONE_QUERY = "existByNameWPartialZone";
	private static final String EXIST_BY_PARTIAL_NAME_WITH_PARTIAL_ZONE_QUERY = "existByPartialNameWPartialZone";
	private static final String EXIST_BY_NAMESERVER_IP_QUERY = "existByNsIp";
	private static final String EXIST_BY_NAMESERVER_LDH_QUERY = "existByNsLdhName";

	private static final String DELETE_IP_NETWORK_RELATION_QUERY = "deleteDomainIpNetworkRelation";

	static {
		try {
			queryGroup = new QueryGroup(QUERY_GROUP);
		} catch (IOException e) {
			throw new RuntimeException("Error loading query group");
		}
	}

	public static Long storeToDatabase(Domain domain, boolean useNameserverAsAttribute, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		String query = queryGroup.getQuery(STORE_QUERY);
		Long domainId;
		isValidForStore((DomainDAO) domain);
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			((DomainDAO) domain).storeToDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			statement.executeUpdate();
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			domainId = resultSet.getLong(1);
			domain.setId(domainId);
		}

		storeNestedObjects(domain, useNameserverAsAttribute, connection);
		return domainId;
	}

	public static void storeNestedObjects(Domain domain, boolean useNameserverAsAttribute, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		Long domainId = domain.getId();
		RemarkModel.storeDomainRemarksToDatabase(domain.getRemarks(), domainId, connection);
		EventModel.storeDomainEventsToDatabase(domain.getEvents(), domainId, connection);
		StatusModel.storeDomainStatusToDatabase(domain.getStatus(), domainId, connection);
		LinkModel.storeDomainLinksToDatabase(domain.getLinks(), domainId, connection);
		if (domain.getSecureDNS() != null) {
			domain.getSecureDNS().setDomainId(domainId);
			SecureDNSModel.storeToDatabase(domain.getSecureDNS(), connection);
		}
		PublicIdModel.storePublicIdByDomain(domain.getPublicIds(), domain.getId(), connection);
		VariantModel.storeAllToDatabase(domain.getVariants(), domain.getId(), connection);

		if (domain.getNameServers().size() > 0) {
			if (useNameserverAsAttribute) {
				NameserverModel.storeDomainNameserversAsAttributesToDatabase(domain.getNameServers(), domainId,
						connection);
			} else {
				storeDomainNameserversAsObjects(domain.getNameServers(), domainId, connection);
			}

		}
		storeDomainEntities(domain.getEntities(), domainId, connection);
		if (domain.getIpNetwork() != null) {
			storeDomainIpNetworkRelationToDatabase(domainId, domain.getIpNetwork().getId(), connection);
		}
	}

	private static void storeDomainNameserversAsObjects(List<Nameserver> nameservers, Long domainId,
			Connection connection) throws RequiredValueNotFoundException, SQLException, IOException {
		if (nameservers.size() > 0) {
			validateDomainNameservers(nameservers, connection);
			NameserverModel.storeDomainNameserversToDatabase(nameservers, domainId, connection);
		}
	}

	private static void validateDomainNameservers(List<Nameserver> nameservers, Connection connection)
			throws RequiredValueNotFoundException, SQLException, IOException {
		for (Nameserver ns : nameservers) {
			Long nsId = NameserverModel.getByHandle(ns.getHandle(), connection).getId();
			if (nsId == null) {
				throw new NullPointerException(
						"Nameserver: " + ns.getHandle() + "was not inserted previously to the database.");
			}
			ns.setId(nsId);
		}
	}

	private static void storeDomainEntities(List<Entity> entities, Long domainId, Connection connection)
			throws SQLException {
		if (entities.size() > 0) {
			EntityModel.validateParentEntities(entities, connection);
			RolModel.storeDomainEntityRoles(entities, domainId, connection);
		}

	}

	private static void isValidForStore(DomainDAO domain) throws RequiredValueNotFoundException {
		if (domain.getHandle() == null || domain.getHandle().isEmpty())
			throw new RequiredValueNotFoundException("handle", "Domain");
		if (domain.getLdhName() == null || domain.getLdhName().isEmpty())
			throw new RequiredValueNotFoundException("ldhName", "Domain");
	}

	private static void storeDomainIpNetworkRelationToDatabase(Long domainId, Long ipNetworkId, Connection connection)
			throws SQLException {
		String query = queryGroup.getQuery(STORE_IP_NETWORK_RELATION_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setLong(1, domainId);
			statement.setLong(2, ipNetworkId);
			logger.log(Level.INFO, "Excuting QUERY:" + statement.toString());
			statement.executeUpdate();
		}
	}

	public static DomainDAO findByLdhName(String name, Integer zoneId, boolean useNameserverAsDomainAttribute,
			Connection connection) throws SQLException, IOException, InvalidValueException {
		String query = queryGroup.getQuery(GET_BY_LDH_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, IDN.toASCII(name));
			statement.setString(2, IDN.toUnicode(name));
			statement.setInt(3, zoneId);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				DomainDAO domain = new DomainDAO(resultSet);
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
				return domain;
			}
		}
	}

	public static void existByLdhName(String name, Integer zoneId, Connection connection) throws SQLException {
		String query = queryGroup.getQuery(EXIST_BY_LDH_QUERY);
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, IDN.toASCII(name));
			statement.setString(2, name);
			statement.setInt(3, zoneId);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				if (resultSet.getInt(1) == 0) {
					throw new ObjectNotFoundException("Object not found.");
				}
			}
		}
	}

	public static Domain getDomainById(Long domainId, boolean useNameserverAsDomainAttribute, Connection connection)
			throws SQLException, IOException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_BY_ID_QUERY))) {
			statement.setLong(1, domainId);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				Domain domain = new DomainDAO(resultSet);
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
				return domain;
			}
		}
	}

	public static DomainDAO getByHandle(String handle, boolean useNameserverAsDomainAttribute, Connection connection)
			throws SQLException, IOException {
		try (PreparedStatement statement = connection.prepareStatement(queryGroup.getQuery(GET_BY_HANDLE_QUERY))) {
			statement.setString(1, handle);
			logger.log(Level.INFO, "Executing QUERY: " + statement.toString());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new ObjectNotFoundException("Object not found.");
				}
				DomainDAO domain = new DomainDAO(resultSet);
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
				return domain;
			}
		}
	}

	/**
	 * Searches a domain by it´s name and TLD
	 * 
	 */
	public static SearchResultStruct searchByName(String name, String zone, Integer resultLimit,
			boolean useNameserverAsDomainAttribute, Connection connection)
			throws SQLException, IOException, InvalidValueException {
		SearchResultStruct result = new SearchResultStruct();
		// Hack to know is there is more domains that the limit, used for
		// notices
		resultLimit = resultLimit + 1;
		boolean isPartialZone = zone.contains("*");
		boolean isPartialName = name.contains("*");
		String query = null;
		List<Integer> zoneIds = null;

		if (isPartialZone) {
			zoneIds = ZoneModel.getValidZoneIds();

			zone = zone.replaceAll("\\*", "%");
			if (isPartialName) {
				name = name.replaceAll("\\*", "%");
				query = queryGroup.getQuery(SEARCH_BY_PARTIAL_NAME_WITH_PARTIAL_ZONE_QUERY);
				query = Util.createDynamicQueryWithInClause(zoneIds.size(), query);
			} else {
				query = queryGroup.getQuery(SEARCH_BY_NAME_WITH_PARTIAL_ZONE_QUERY);
				query = Util.createDynamicQueryWithInClause(zoneIds.size(), query);
			}
		} else {

			if (!ZoneModel.existsZone(zone)) {
				throw new ObjectNotFoundException("Zone not found.");
			}

			if (isPartialName) {
				name = name.replaceAll("\\*", "%");
				query = queryGroup.getQuery(SEARCH_BY_PARTIAL_NAME_WITH_ZONE_QUERY);
			} else {
				query = queryGroup.getQuery(SEARCH_BY_NAME_WITH_ZONE_QUERY);
			}
		}

		try (PreparedStatement statement = connection.prepareStatement(query);) {

			if (isPartialZone) {
				for (int i = 1; i <= zoneIds.size(); i++) {
					statement.setInt(i, zoneIds.get(i - 1));
				}
				statement.setString(zoneIds.size() + 1, name);
				statement.setString(zoneIds.size() + 2, name);
				statement.setString(zoneIds.size() + 3, zone);
				statement.setInt(zoneIds.size() + 4, resultLimit);
			} else {
				statement.setString(1, name);
				statement.setString(2, name);
				Integer zoneId = ZoneModel.getIdByZoneName(zone);
				statement.setInt(3, zoneId);
				statement.setInt(4, resultLimit);
			}

			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				throw new ObjectNotFoundException("Object not found.");
			}
			List<DomainDAO> domains = new ArrayList<DomainDAO>();
			do {
				DomainDAO domain = new DomainDAO(resultSet);
				domains.add(domain);
			} while (resultSet.next());
			resultLimit = resultLimit - 1;// Back to the original limit
			if (domains.size() > resultLimit) {
				result.setResultSetWasLimitedByUserConfiguration(true);
				domains.remove(domains.size() - 1);
			}
			for (DomainDAO domain : domains) {
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
			}
			result.setSearchResultsLimitForUser(resultLimit);
			result.getResults().addAll(domains);
			return result;
		}
	}

	/**
	 * Searches a domain by it's name when user don´t care about the TLD
	 * 
	 */
	public static SearchResultStruct searchByName(String domainName, Integer resultLimit,
			boolean useNameserverAsDomainAttribute, Connection connection) throws SQLException, IOException {
		SearchResultStruct result = new SearchResultStruct();
		// Hack to know is there is more domains that the limit, used for
		// notices
		resultLimit = resultLimit + 1;
		String query = null;
		if (domainName.contains("*")) {
			domainName = domainName.replaceAll("\\*", "%");
			query = queryGroup.getQuery(SEARCH_BY_PARTIAL_NAME_WITHOUT_ZONE_QUERY);
		} else {
			query = queryGroup.getQuery(SEARCH_BY_NAME_WITHOUT_ZONE_QUERY);
		}

		List<Integer> zoneIds = ZoneModel.getValidZoneIds();
		query = Util.createDynamicQueryWithInClause(zoneIds.size(), query);

		try (PreparedStatement statement = connection.prepareStatement(query);) {

			for (int i = 1; i <= zoneIds.size(); i++) {
				statement.setInt(i, zoneIds.get(i - 1));
			}

			statement.setString(zoneIds.size() + 1, domainName);
			statement.setString(zoneIds.size() + 2, domainName);
			statement.setInt(zoneIds.size() + 3, resultLimit);
			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();

			if (!resultSet.next()) {
				throw new ObjectNotFoundException("Object not found.");
			}
			List<DomainDAO> domains = new ArrayList<DomainDAO>();
			do {
				DomainDAO domain = new DomainDAO(resultSet);
				domains.add(domain);
			} while (resultSet.next());
			resultLimit = resultLimit - 1;// Back to the original limit
			if (domains.size() > resultLimit) {
				result.setResultSetWasLimitedByUserConfiguration(true);
				domains.remove(domains.size() - 1);
			}
			for (DomainDAO domain : domains) {
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
			}
			result.setSearchResultsLimitForUser(resultLimit);
			result.getResults().addAll(domains);
			return result;
		}
	}

	/**
	 * Searches all domains with a nameserver by name
	 * 
	 */
	public static SearchResultStruct searchByNsLdhName(String name, Integer resultLimit,
			boolean useNameserverAsDomainAttribute, Connection connection) throws SQLException, IOException {
		SearchResultStruct result = new SearchResultStruct();
		// Hack to know is there is more domains that the limit, used for
		// notices
		resultLimit = resultLimit + 1;
		name = name.replace("*", "%");
		try (PreparedStatement statement = connection
				.prepareStatement(queryGroup.getQuery(SEARCH_BY_NAMESERVER_LDH_QUERY))) {
			statement.setString(1, name);
			statement.setString(2, name);
			statement.setInt(3, resultLimit);
			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();

			if (!resultSet.next()) {
				throw new ObjectNotFoundException("Object not found.");
			}
			List<DomainDAO> domains = new ArrayList<DomainDAO>();
			do {
				DomainDAO domain = new DomainDAO(resultSet);
				domains.add(domain);
			} while (resultSet.next());
			resultLimit = resultLimit - 1;// Back to the original limit
			if (domains.size() > resultLimit) {
				result.setResultSetWasLimitedByUserConfiguration(true);
				domains.remove(domains.size() - 1);
			}
			for (DomainDAO domain : domains) {
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
			}
			result.setSearchResultsLimitForUser(resultLimit);
			result.getResults().addAll(domains);
			return result;
		}
	}

	/**
	 * searches all domains with a nameserver by address
	 * 
	 */
	public static SearchResultStruct searchByNsIp(String ip, Integer resultLimit,
			boolean useNameserverAsDomainAttribute, Connection connection) throws SQLException, IOException {
		SearchResultStruct result = new SearchResultStruct();
		// Hack to know is there is more domains that the limit, used for
		// notices
		resultLimit = resultLimit + 1;
		IpAddressDAO ipAddress = new IpAddressDAO();
		InetAddress address = InetAddress.getByName(ip);
		ipAddress.setAddress(address);
		if (ipAddress.getAddress() instanceof Inet4Address) {
			ipAddress.setType(4);

		} else if (ipAddress.getAddress() instanceof Inet4Address) {
			ipAddress.setType(6);
		}
		try (PreparedStatement statement = connection
				.prepareStatement(queryGroup.getQuery(SEARCH_BY_NAMESERVER_IP_QUERY))) {
			statement.setInt(1, ipAddress.getType());
			statement.setString(2, ipAddress.getAddress().getHostAddress());
			statement.setString(3, ipAddress.getAddress().getHostAddress());
			statement.setInt(4, resultLimit);
			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();

			if (!resultSet.next()) {
				throw new ObjectNotFoundException("Object not found.");
			}
			List<DomainDAO> domains = new ArrayList<DomainDAO>();
			do {
				DomainDAO domain = new DomainDAO(resultSet);
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
				domains.add(domain);
			} while (resultSet.next());
			resultLimit = resultLimit - 1;// Back to the original limit
			if (domains.size() > resultLimit) {
				result.setResultSetWasLimitedByUserConfiguration(true);
				domains.remove(domains.size() - 1);
			}
			for (DomainDAO domain : domains) {
				loadNestedObjects(domain, useNameserverAsDomainAttribute, connection);
			}
			result.setSearchResultsLimitForUser(resultLimit);
			result.getResults().addAll(domains);
			return result;
		}

	}

	/**
	 * Load the nested object of the domain
	 * 
	 * @param useNameserverAsDomainAttribute
	 *            if false, load all the nameserver object
	 * 
	 */
	public static void loadNestedObjects(Domain domain, boolean useNameserverAsDomainAttribute, Connection connection)
			throws SQLException, IOException {
		Long domainId = domain.getId();

		// Retrieve the events
		try {
			domain.getEvents().addAll(EventModel.getByDomainId(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, events is not required
		}
		// Retrieve the links
		try {
			domain.getLinks().addAll(LinkModel.getByDomainId(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, links is not required
		}
		// Retrieve the status
		try {
			domain.getStatus().addAll(StatusModel.getByDomainId(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, status is not required
		}
		// Retrieve the remarks
		try {
			domain.getRemarks().addAll(RemarkModel.getByDomainId(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, remarks is not required
		}
		// Retrieve the public ids
		try {
			domain.setPublicIds(PublicIdModel.getByDomain(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, public ids is not required
		}
		// Retrieve the secure dns
		try {
			domain.setSecureDNS(SecureDNSModel.getByDomain(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, secure dns is not required
		}
		// Retrieve the variants
		try {
			domain.setVariants(VariantModel.getByDomainId(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, variants is not required
		}
		// Retrieve the domainsNs
		try {
			domain.getNameServers()
					.addAll(NameserverModel.getByDomainId(domainId, useNameserverAsDomainAttribute, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, nameservers is not required
		}
		// Retrieve the entities
		try {
			domain.getEntities().addAll(EntityModel.getEntitiesByDomainId(domainId, connection));
		} catch (ObjectNotFoundException onfe) {
			// Do nothing, entities is not required
		}

		// Retrieve the ipNetwork
		try {
			IpNetwork network = IpNetworkModel.getByDomainId(domainId, connection);
			domain.setIpNetwork(network);
		} catch (ObjectNotFoundException e) {
			// Do nothing, ipNetwork is not requiered
		}
	}

	/**
	 * Validate if the zone of the request domain is managed by the server
	 * 
	 */
	public static void validateDomainZone(String domainName) throws InvalidValueException, ObjectNotFoundException {
		String domainZone;

		if (ZoneModel.isReverseAddress(domainName)) {
			domainZone = ZoneModel.getArpaZoneNameFromAddress(domainName);
			if (domainZone == null) {
				throw new ObjectNotFoundException("Zone not found.");
			}
		} else {
			int indexOf = domainName.indexOf('.');
			if (indexOf <= 0) {
				throw new ObjectNotFoundException("Zone not found.");
			}

			domainZone = domainName.substring(indexOf + 1, domainName.length());

		}

		if (!ZoneModel.existsZone(domainZone)) {
			throw new ObjectNotFoundException("Zone not found.");
		}
	}

	/**
	 * Can't use a regular upsert sql statement, because domain table has
	 * multiple unique constraints, instead will check if the nameserver
	 * exist,then update it on insert it,if not exist.
	 */
	public static void upsertToDatabase(DomainDAO domain, boolean useNameserverAsAttribute, Connection rdapConnection)
			throws SQLException, RequiredValueNotFoundException, IOException {
		try {
			DomainDAO previousDomain = getByHandle(domain.getHandle(), useNameserverAsAttribute, rdapConnection);
			domain.setId(previousDomain.getId());
			update(previousDomain, domain, rdapConnection);
		} catch (ObjectNotFoundException onfe) {
			storeToDatabase(domain, useNameserverAsAttribute, rdapConnection);
		}
	}

	private static void update(DomainDAO previousDomain, DomainDAO domain, Connection rdapConnection)
			throws RequiredValueNotFoundException, SQLException, IOException {
		isValidForUpdate(domain);
		String query = queryGroup.getQuery(UPDATE_QUERY);
		try (PreparedStatement statement = rdapConnection.prepareStatement(query)) {
			domain.updateInDatabase(statement);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}
		updatedNestedObjects(previousDomain, domain, rdapConnection);
	}

	private static void isValidForUpdate(DomainDAO domain) throws RequiredValueNotFoundException {
		if (domain.getId() == null)
			throw new RequiredValueNotFoundException("id", "Domain");
		if (domain.getHandle() == null || domain.getHandle().isEmpty())
			throw new RequiredValueNotFoundException("handle", "Domain");
		if (domain.getLdhName() == null || domain.getLdhName().isEmpty())
			throw new RequiredValueNotFoundException("ldhName", "Domain");
		if (domain.getZoneId() == null)
			throw new RequiredValueNotFoundException("zone", "Domain");
	}

	private static void updatedNestedObjects(DomainDAO previousDomain, DomainDAO domain, Connection connection)
			throws SQLException, IOException, RequiredValueNotFoundException {
		Long domainId = domain.getId();
		RemarkModel.updateDomainRemarksInDatabase(previousDomain.getRemarks(), domain.getRemarks(), domainId,
				connection);
		EventModel.updateDomainEventsInDatabase(previousDomain.getEvents(), domain.getEvents(), domainId, connection);
		StatusModel.updateDomainStatusInDatabase(previousDomain.getStatus(), domain.getStatus(), domainId, connection);
		LinkModel.updateDomainLinksInDatabase(previousDomain.getLinks(), domain.getLinks(), domainId, connection);
		RolModel.updateDomainEntityRoles(previousDomain.getEntities(), domain.getEntities(), domainId, connection);
		PublicIdModel.updateDomainPublicIdsInDatabase(previousDomain.getPublicIds(), domain.getPublicIds(), domainId,
				connection);
		NameserverModel.updateDomainNameservers(previousDomain.getNameServers(), domain.getNameServers(), domainId,
				connection);
		SecureDNSModel.updateSecureDns(previousDomain.getSecureDNS(), domain.getSecureDNS(), domainId, connection);
		VariantModel.updateVariants(previousDomain.getVariants(), domain.getVariants(), domain.getId(), connection);
		updateDomainIpNetworkOnDatabase(previousDomain.getIpNetwork(), domain.getIpNetwork(), domainId, connection);
	}

	private static void updateDomainIpNetworkOnDatabase(IpNetwork previousIpNetwork, IpNetwork ipNetwork, Long domainId,
			Connection connection) throws SQLException {
		if (previousIpNetwork != null)
			deletePreviousIpNetworkRelation(domainId, connection);
		if (ipNetwork != null)
			storeDomainIpNetworkRelationToDatabase(domainId, ipNetwork.getId(), connection);
	}

	private static void deletePreviousIpNetworkRelation(Long domainId, Connection connection) throws SQLException {
		try (PreparedStatement statement = connection
				.prepareStatement(queryGroup.getQuery(DELETE_IP_NETWORK_RELATION_QUERY))) {
			statement.setLong(1, domainId);
			logger.log(Level.INFO, "Executing QUERY:" + statement.toString());
			statement.executeUpdate();
		}

	}

	public static void existByNsIp(String ip, Connection connection) throws SQLException, UnknownHostException {
		IpAddressDAO ipAddress = new IpAddressDAO();
		InetAddress address = InetAddress.getByName(ip);
		ipAddress.setAddress(address);
		if (ipAddress.getAddress() instanceof Inet4Address) {
			ipAddress.setType(4);

		} else if (ipAddress.getAddress() instanceof Inet4Address) {
			ipAddress.setType(6);
		}
		try (PreparedStatement statement = connection
				.prepareStatement(queryGroup.getQuery(EXIST_BY_NAMESERVER_IP_QUERY))) {
			statement.setInt(1, ipAddress.getType());
			statement.setString(2, ipAddress.getAddress().getHostAddress());
			statement.setString(3, ipAddress.getAddress().getHostAddress());
			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			if (resultSet.getInt(1) == 0) {
				throw new ObjectNotFoundException("Object not found.");
			}
		}

	}

	public static void existByNsLdhName(String name, Connection connection) throws SQLException {
		name = name.replace("*", "%");
		try (PreparedStatement statement = connection
				.prepareStatement(queryGroup.getQuery(EXIST_BY_NAMESERVER_LDH_QUERY))) {
			statement.setString(1, name);
			statement.setString(2, name);
			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			if (resultSet.getInt(1) == 0) {
				throw new ObjectNotFoundException("Object not found.");
			}
		}
	}

	public static void existByName(String name, String zone, Connection connection) throws SQLException {
		boolean isPartialZone = zone.contains("*");
		boolean isPartialName = name.contains("*");
		String query = null;
		List<Integer> zoneIds = null;

		if (isPartialZone) {
			zoneIds = ZoneModel.getValidZoneIds();

			zone = zone.replaceAll("\\*", "%");
			if (isPartialName) {
				name = name.replaceAll("\\*", "%");
				query = queryGroup.getQuery(EXIST_BY_PARTIAL_NAME_WITH_PARTIAL_ZONE_QUERY);
				query = Util.createDynamicQueryWithInClause(zoneIds.size(), query);
			} else {
				query = queryGroup.getQuery(EXIST_BY_NAME_WITH_PARTIAL_ZONE_QUERY);
				query = Util.createDynamicQueryWithInClause(zoneIds.size(), query);
			}
		} else {

			if (!ZoneModel.existsZone(zone)) {
				throw new ObjectNotFoundException("Zone not found.");
			}

			if (isPartialName) {
				name = name.replaceAll("\\*", "%");
				query = queryGroup.getQuery(EXIST_BY_PARTIAL_NAME_WITH_ZONE_QUERY);
			} else {
				query = queryGroup.getQuery(EXIST_BY_NAME_WITH_ZONE_QUERY);
			}
		}

		try (PreparedStatement statement = connection.prepareStatement(query);) {

			if (isPartialZone) {
				for (int i = 1; i <= zoneIds.size(); i++) {
					statement.setInt(i, zoneIds.get(i - 1));
				}
				statement.setString(zoneIds.size() + 1, name);
				statement.setString(zoneIds.size() + 2, name);
				statement.setString(zoneIds.size() + 3, zone);
			} else {
				statement.setString(1, name);
				statement.setString(2, name);
				Integer zoneId = ZoneModel.getIdByZoneName(zone);
				statement.setInt(3, zoneId);
			}

			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			if (resultSet.getInt(1) == 0) {
				throw new ObjectNotFoundException("Object not found.");
			}
		}

	}

	public static void existByName(String domainName, Connection connection) throws SQLException {
		String query = null;
		if (domainName.contains("*")) {
			domainName = domainName.replaceAll("\\*", "%");
			query = queryGroup.getQuery(EXIST_BY_PARTIAL_NAME_WITHOUT_ZONE_QUERY);
		} else {
			query = queryGroup.getQuery(EXIST_BY_NAME_WITHOUT_ZONE_QUERY);
		}

		List<Integer> zoneIds = ZoneModel.getValidZoneIds();
		query = Util.createDynamicQueryWithInClause(zoneIds.size(), query);

		try (PreparedStatement statement = connection.prepareStatement(query);) {

			for (int i = 1; i <= zoneIds.size(); i++) {
				statement.setInt(i, zoneIds.get(i - 1));
			}

			statement.setString(zoneIds.size() + 1, domainName);
			statement.setString(zoneIds.size() + 2, domainName);
			logger.log(Level.INFO, "Executing query" + statement.toString());
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			if (resultSet.getInt(1) == 0) {
				{
					throw new ObjectNotFoundException("Object not found.");
				}
			}

		}
	}

}
