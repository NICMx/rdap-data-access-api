package mx.nic.rdap.db.services;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.spi.NameserverSpi;
import mx.nic.rdap.db.struct.SearchResultStruct;

public class NameserverService {

	private final static NameserverService service = new NameserverService();
	private ServiceLoader<NameserverSpi> loader;
	private static NameserverSpi implementation;

	private NameserverService() {
		loader = ServiceLoader.load(NameserverSpi.class);
		loadNameserverImpl();
	}

	private void loadNameserverImpl() {
		if (loader == null) {
			throw new NullPointerException("No Nameserver Implementation was loaded");
		}
		Iterator<NameserverSpi> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("No Nameserver implementations was loaded");
		}

		implementation = loaderIterator.next();
		Logger.getLogger(NameserverService.class.getName()).log(Level.INFO,
				"Class loaded : " + implementation.getClass().getName());

		if (loaderIterator.hasNext()) {
			throw new RuntimeException("Two or more implementations were loaded.");
		}
	}

	public static NameserverService getInstance() {
		return service;
	}

	public static void storeToDatabase(Nameserver nameserver) throws RdapDatabaseException {
		implementation.storeToDatabase(nameserver);
	}

	public static Nameserver getByName(String name) throws RdapDatabaseException {
		return implementation.getByName(name);
	}

	public static SearchResultStruct searchByName(String namePattern, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByName(namePattern, resultLimit);
	}

	public static SearchResultStruct searchByIp(String ipaddressPattern, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByIp(ipaddressPattern, resultLimit);
	}

	public static SearchResultStruct searchByRegexName(String namePattern, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByRegexName(namePattern, resultLimit);
	}

	public static SearchResultStruct searchByRegexIp(String ipaddressPattern, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByRegexIp(ipaddressPattern, resultLimit);
	}

	public static boolean existByName(String name) throws RdapDatabaseException {
		return implementation.existByName(name);
	}
}
