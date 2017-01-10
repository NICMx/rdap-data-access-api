package mx.nic.rdap.db.services;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.Entity;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.spi.EntitySpi;
import mx.nic.rdap.db.struct.SearchResultStruct;

public class EntityService {

	private final static EntityService service = new EntityService();
	private ServiceLoader<EntitySpi> loader;
	private static EntitySpi implementation;

	private EntityService() {
		loader = ServiceLoader.load(EntitySpi.class);
		loadEntityImpl();
	}

	private void loadEntityImpl() {
		if (loader == null) {
			throw new NullPointerException("No Entity Implementation was loaded");
		}
		Iterator<EntitySpi> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("No Entity implementations was loaded");
		}

		implementation = loaderIterator.next();
		Logger.getLogger(EntityService.class.getName()).log(Level.INFO,
				"Class loaded : " + implementation.getClass().getName());
		if (loaderIterator.hasNext()) {
			throw new RuntimeException("Two or more implementations were loaded.");
		}
	}

	public static EntityService getInstance() {
		return service;
	}

	public static void existsByHandle(String entityHandle) throws RdapDatabaseException {
		implementation.existByHandle(entityHandle);
	}

	public static long storeToDatabase(Entity entity) throws RdapDatabaseException {
		return implementation.storeToDatabase(entity);
	}

	public static Entity getByHandle(String entityHandle) throws RdapDatabaseException {
		return implementation.getByHandle(entityHandle);
	}

	public static SearchResultStruct searchByHandle(String handle, Integer resultLimit) throws RdapDatabaseException {
		return implementation.searchByHandle(handle, resultLimit);
	}

	public static SearchResultStruct searchByVCardName(String handle, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByVCardName(handle, resultLimit);
	}

	public static SearchResultStruct searchByRegexHandle(String regexHandle, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByRegexHandle(regexHandle, resultLimit);
	}

	public static SearchResultStruct searchByRegexVCardName(String regexName, Integer resultLimit)
			throws RdapDatabaseException {
		return implementation.searchByRegexVCardName(regexName, resultLimit);

	}

}
