package mx.nic.rdap.db.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.exception.RdapDatabaseException;
import mx.nic.rdap.db.spi.IpNetworkSpi;

public class IpNetworkService {

	private final static IpNetworkService service = new IpNetworkService();
	private ServiceLoader<IpNetworkSpi> loader;
	private static IpNetworkSpi implementation;

	private IpNetworkService() {
		loader = ServiceLoader.load(IpNetworkSpi.class);
		loadIpNetworkImpl();
	}

	private void loadIpNetworkImpl() {
		if (loader == null) {
			throw new NullPointerException("No IpNetwork Implementation was loaded");
		}
		Iterator<IpNetworkSpi> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("No IpNetwork implementations was loaded");
		}

		implementation = loaderIterator.next();
		Logger.getLogger(IpNetworkService.class.getName()).log(Level.INFO,
				"Class loaded : " + implementation.getClass().getName());
		if (loaderIterator.hasNext()) {
			throw new RuntimeException("Two or more implementations were loaded.");
		}
	}

	public static IpNetworkService getInstance() {
		return service;
	}

	public static IpNetwork getByInetAddress(String ipAddress, Integer cidr)
			throws SQLException, IOException, RdapDatabaseException {
		return implementation.getByInetAddress(ipAddress, cidr);

	}

	public static IpNetwork getByInetAddress(String ipAddress) throws SQLException, IOException, RdapDatabaseException {
		return implementation.getByInetAddress(ipAddress);

	}

	public static boolean existByInetAddress(String ipAddress, Integer cidr)
			throws SQLException, IOException, RdapDatabaseException {
		return implementation.existByInetAddress(ipAddress, cidr);
	}

	public static boolean existByInetAddress(String ipAddress) throws SQLException, IOException, RdapDatabaseException {
		return implementation.existByInetAddress(ipAddress);
	}

}
