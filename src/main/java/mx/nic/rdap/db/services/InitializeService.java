package mx.nic.rdap.db.services;

import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

import mx.nic.rdap.db.spi.InitializeSpi;

public class InitializeService {

	public static synchronized void init(Properties properties) {
		ServiceLoader<InitializeSpi> loader = ServiceLoader.load(InitializeSpi.class);
		if (loader == null) {
			return;
		}

		Iterator<InitializeSpi> loaderIterator = loader.iterator();
		while (loaderIterator.hasNext()) {
			InitializeSpi next = loaderIterator.next();
			next.init(properties);
		}
	}

	private InitializeService() {
		// no code.
	}
}
