package mx.nic.rdap.db.services;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.Autnum;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.spi.AutnumSpi;

public class AutnumService {

	private final static AutnumService service = new AutnumService();
	private ServiceLoader<AutnumSpi> loader;
	private static AutnumSpi implementation;

	private AutnumService() {
		loader = ServiceLoader.load(AutnumSpi.class);
		loadAutnumImpl();
	}

	private void loadAutnumImpl() {
		if (loader == null) {
			throw new NullPointerException("No Autnum Implementation was loaded");
		}
		Iterator<AutnumSpi> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("No Autnum implementations was loaded");
		}

		implementation = loaderIterator.next();
		Logger.getLogger(AutnumService.class.getName()).log(Level.INFO,
				"Class loaded : " + implementation.getClass().getName());

		if (loaderIterator.hasNext()) {
			throw new RuntimeException("Two or more implementations were loaded.");
		}
	}

	public static AutnumService getInstance() {
		return service;
	}

	public static Autnum getByRange(Long autnumValue) throws RdapDatabaseException {
		return implementation.getByRange(autnumValue);
	}

	public static boolean existByRange(Long autnumValue) throws RdapDatabaseException {
		return implementation.existByRange(autnumValue);
	}

}
