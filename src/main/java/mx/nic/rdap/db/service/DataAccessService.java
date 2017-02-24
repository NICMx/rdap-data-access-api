package mx.nic.rdap.db.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.db.spi.AutnumDAO;
import mx.nic.rdap.db.spi.DataAccessImplementation;
import mx.nic.rdap.db.spi.DomainDAO;
import mx.nic.rdap.db.spi.EntityDAO;
import mx.nic.rdap.db.spi.IpNetworkDAO;
import mx.nic.rdap.db.spi.NameserverDAO;
import mx.nic.rdap.db.spi.RdapUserDAO;

/**
 * This is the class that loads the global {@link DataAccessImplementation} the
 * user wants.
 */
public class DataAccessService {

	private static final Logger logger = Logger.getLogger(DataAccessService.class.getName());

	/**
	 * Name of the file where the user will configure this class.
	 * <p>
	 * This might seem like redundant configuration, since META-INF/services is
	 * roughly the same thing. Thing is, META-INF is supposed to be jar meta
	 * stuff, so the *intent* is different. META-INF/services defines the
	 * providers present in a particular jar, whereas {@value #CONFIG_FILE} is
	 * the actual provider the user wants.
	 */
	private static final String CONFIG_FILE = "data-access.properties";
	/** The implementation that was loaded. */
	private static final DataAccessImplementation implementation = loadImplementation();

	private static DataAccessImplementation loadImplementation() {
		DataAccessImplementation result = loadImplementationFromProperties();
		if (result != null) {
			return result;
		}

		return loadImplementationFromClasspath();
	}

	/**
	 * Returns the implementation of {@link DataAccessImplementation} the user
	 * configured in {@value #CONFIG_FILE}.
	 * <p>
	 * Returns null if the user didn't set up the file or the property.
	 */
	private static DataAccessImplementation loadImplementationFromProperties() {
		Properties config = new Properties();
		try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
			config.load(in);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException("Trouble found reading the " + CONFIG_FILE + " file.", e);
		}

		String className = config.getProperty("implementation");
		if (className == null) {
			return null;
		}

		DataAccessImplementation result;
		try {
			result = (DataAccessImplementation) Class.forName(className).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException("Could not instantiate class " + className + ".", e);
		}

		logger.log(Level.INFO, "Data access implementation loaded: " + result.getClass().getName());
		return result;
	}

	/**
	 * Scans the classpath, returns the one implementation of
	 * {@link DataAccessImplementation} found.
	 * <p>
	 * I consider this a fallback implementation finding strategy. This is why:
	 * According to Oracle,
	 * <p>
	 * "It's not an error to install more than one provider for the same
	 * service. (...) In such a case, the system arbitrarily chooses one of the
	 * providers. Users who care which provider is chosen should install only
	 * the desired one."<br>
	 * (https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)
	 * <p>
	 * Now, pardon my bluntness, but I find that very, very, VERY stupid. "We
	 * found an error! Let's do stuff that no one intended instead of throwing a
	 * proper error message!" Also, I don't think it's reasonable to expect the
	 * user to keep track of jars. This code's main user is a servlet
	 * application; the classpath is a royal mess by definition.
	 * <p>
	 * If a user installs some other provider aside from the default one
	 * (rdap-sql-provider), I don't want the server to crash because this method
	 * arbitrarily chose the default provider and the user did not supply its
	 * configuration. (Or worse: Grab default or stale configuration and appear
	 * as if everything is working.) Ideally, the new provider would take
	 * precedence over whatever is default, but since this project cannot
	 * referece rdap-sql-provider (because that would make it a circular
	 * dependency), I cannot tell which is the default one. (I proposed joining
	 * the two projects, but the idea was rejected for the sake of modularity.
	 * I'd still very much rather be pragmatic.)
	 * <p>
	 * So, for the sake of throwing the user a proper error message, only one
	 * provider is allowed in the classpath. That kind of defeats the point of
	 * using SPIs, now doesn't it? It would make more sense to do a
	 * {@link Class#forName(String)}. But, to be fair, that's what I do in
	 * {@link #loadImplementationFromProperties()} above. So that's why I
	 * consider this a fallback strategy.
	 * <p>
	 * If there is any number of implementations other than one, this method
	 * crashes.
	 * <p>
	 * Either returns something valid or throws; null must never be returned.
	 */
	private static DataAccessImplementation loadImplementationFromClasspath() {
		ServiceLoader<DataAccessImplementation> loader = ServiceLoader.load(DataAccessImplementation.class);
		if (loader == null) {
			throw new NullPointerException("ServiceLoader#load(Class) returned null. I don't know what to do; sorry.");
		}

		Iterator<DataAccessImplementation> loaderIterator = loader.iterator();
		if (!loaderIterator.hasNext()) {
			throw new NullPointerException("I could not find any implementations of "
					+ DataAccessImplementation.class.toString() + " in the classpath.");
		}

		DataAccessImplementation result = loaderIterator.next();
		if (loaderIterator.hasNext()) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("There is more than one data access implementation in the classpath.\n");
			errorMsg.append("Please remove redundant ones or specify the one you want in " + CONFIG_FILE + ".\n");
			errorMsg.append("FYI, I found:\n");
			errorMsg.append("- ").append(result.getClass().getName()).append("\n");
			do {
				errorMsg.append("- ").append(loaderIterator.next().getClass().getName()).append("\n");
			} while (loaderIterator.hasNext());
			throw new RuntimeException(errorMsg.toString());
		}

		logger.log(Level.INFO, "Data access implementation loaded: " + result.getClass().getName());
		return result;
	}

	public static DataAccessImplementation getImplementation() {
		return implementation;
	}

	public static AutnumDAO getAutnumDAO() {
		return implementation.getAutnumDAO();
	}

	public static DomainDAO getDomainDAO() {
		return implementation.getDomainDAO();
	}

	public static EntityDAO getEntityDAO() {
		return implementation.getEntityDAO();
	}

	public static IpNetworkDAO getIpNetworkDAO() {
		return implementation.getIpNetworkDAO();
	}

	public static NameserverDAO getNameserverDAO() {
		return implementation.getNameserverDAO();
	}

	public static RdapUserDAO getRdapUserDAO() {
		return implementation.getRdapUserDAO();
	}

}
