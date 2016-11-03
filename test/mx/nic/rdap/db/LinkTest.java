package mx.nic.rdap.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import mx.nic.rdap.core.db.Link;
import mx.nic.rdap.db.DatabaseSession;
import mx.nic.rdap.db.LinkDAO;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;
import mx.nic.rdap.db.model.LinkModel;

/**
 * Test for the class link
 * 
 * @author dalpuche
 *
 */
public class LinkTest  {

	@Test
	/**
	 * Store a link in the database
	 */
	public void insert() {
		try {

			Link link = new LinkDAO();
			link.setValue("spotify.com");
			link.setHref("test");
			try (Connection connection = DatabaseSession.getRdapConnection()) {
				LinkModel.storeToDatabase(link, connection);
			}
			assert true;
		} catch (RequiredValueNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void getAll() throws IOException {
		try {
			try (Connection connection = DatabaseSession.getRdapConnection()) {
				List<Link> links = LinkModel.getAll(connection);
				for (Link link : links) {
					System.out.println(link.toString());
				}
				assert true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			assert false;
		}

	}
}
