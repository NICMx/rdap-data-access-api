package mx.nic.rdap.db.services;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.db.RdapUser;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.spi.RdapUserSpi;

public class RdapUserService {

	private static RdapUserService service = new RdapUserService();
	private ServiceLoader<RdapUserSpi> loader;
	private static RdapUserSpi implementation;

	private RdapUserService() {
		loader = ServiceLoader.load(RdapUserSpi.class);
		loadRdapUserImpl();
	}

	private void loadRdapUserImpl() {
		if (loader == null) {
			throw new NullPointerException("No RdapUser Implementation was loaded");
		}
		Iterator<RdapUserSpi> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("No RdapUser implementations was loaded");
		}

		implementation = loaderIterator.next();
		Logger.getLogger(RdapUserService.class.getName()).log(Level.INFO,
				"Class loaded : " + implementation.getClass().getName());

		if (loaderIterator.hasNext()) {
			throw new RuntimeException("Two or more implementations were loaded.");
		}
	}

	public static synchronized RdapUserService getInstance() {
		if (service == null) {
			service = new RdapUserService();
		}

		return service;
	}

	public static Integer getMaxSearchResults(String username) throws RdapDatabaseException {
		return implementation.getMaxSearchResults(username);

	}

	public static RdapUser getByUsername(String username) throws RdapDatabaseException {
		return implementation.getByUsername(username);
	}

}
