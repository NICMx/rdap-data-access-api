package mx.nic.rdap.db.services;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.Domain;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.spi.DomainSpi;
import mx.nic.rdap.db.struct.SearchResultStruct;

public class DomainService {

	private final static DomainService service = new DomainService();
	private ServiceLoader<DomainSpi> loader;
	private static DomainSpi implementation;

	private DomainService() {
		loader = ServiceLoader.load(DomainSpi.class);
		loadDomainImpl();
	}

	private void loadDomainImpl() {
		if (loader == null) {
			throw new NullPointerException("No Domain Implementation was loaded");
		}
		Iterator<DomainSpi> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("No Domain implementations was loaded");
		}

		implementation = loaderIterator.next();
		Logger.getLogger(DomainService.class.getName()).log(Level.INFO,
				"Class loaded : " + implementation.getClass().getName());
		if (loaderIterator.hasNext()) {
			throw new RuntimeException("Two or more implementations were loaded.");
		}
	}

	public static DomainService getInstance() {
		return service;
	}

	public static Long storeToDatabase(Domain domain, Boolean useNsAsAttribute) throws RdapDatabaseException {
		return implementation.storeToDatabase(domain, useNsAsAttribute);
	}

	public static Domain getByName(String domainName, Boolean useNsAsAttribute) throws RdapDatabaseException {
		return implementation.getByName(domainName, useNsAsAttribute);
	}

	public static SearchResultStruct searchByName(String domainName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException {
		return implementation.searchByName(domainName, resultLimit, useNsAsAttribute);
	}

	public static SearchResultStruct searchByNsName(String nsName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException {
		return implementation.searchByNsName(nsName, resultLimit, useNsAsAttribute);
	}

	public static SearchResultStruct searchByNsIp(String ip, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException {
		return implementation.searchByNsIp(ip, resultLimit, useNsAsAttribute);
	}

	public static SearchResultStruct searchByRegexName(String regexName, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException {
		return implementation.searchByRegexName(regexName, resultLimit, useNsAsAttribute);
	}

	public static SearchResultStruct searchByRegexNsName(String name, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException {
		return implementation.searchByRegexNsName(name, resultLimit, useNsAsAttribute);
	}

	public static SearchResultStruct searchByRegexNsIp(String ip, Integer resultLimit, boolean useNsAsAttribute)
			throws RdapDatabaseException {
		return implementation.searchByRegexNsIp(ip, resultLimit, useNsAsAttribute);
	}

	public static boolean existByName(String domainName) throws RdapDatabaseException {
		return implementation.existByName(domainName);
	}

}
