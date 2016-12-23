package mx.nic.rdap.db;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import mx.nic.rdap.core.catalog.EventAction;
import mx.nic.rdap.core.catalog.Status;
import mx.nic.rdap.core.db.Event;
import mx.nic.rdap.core.db.IpAddress;
import mx.nic.rdap.core.db.Link;
import mx.nic.rdap.core.db.Nameserver;
import mx.nic.rdap.core.db.Remark;
import mx.nic.rdap.core.db.RemarkDescription;
import mx.nic.rdap.core.db.struct.NameserverIpAddressesStruct;
import mx.nic.rdap.db.exception.InvalidValueException;
import mx.nic.rdap.db.exception.RequiredValueNotFoundException;
import mx.nic.rdap.db.model.NameserverModel;

/**
 * Test for the Nameserver object
 * 
 */
public class NameserverTest extends DatabaseTest {

	@Test
	public void insertMinimunNameServer() {

		try {
			// Nameserver base data
			Nameserver nameserver = new NameserverDAO();
			nameserver.setHandle("xx1");
			nameserver.setPunycodeName("ns.xn--test-minumun.example");
			NameserverModel.storeToDatabase(nameserver, connection);
			System.out.println(nameserver);

			assert true;
		} catch (RequiredValueNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void upsertMinimunNameServer() {

		try {
			NameserverDAO nameserver = new NameserverDAO();
			nameserver.setHandle("xx4");
			nameserver.setPunycodeName("ns.xn--test-minumun.example");
			NameserverModel.storeToDatabase(nameserver, connection);
			// first and update on the object
			nameserver.setPunycodeName("ns.test-minimun.example");
			NameserverModel.upsertToDatabase(nameserver, connection);
			// second a create
			nameserver.setHandle("xx3");
			nameserver.setPunycodeName("ns.test-minimun2.example");
			NameserverModel.upsertToDatabase(nameserver, connection);
			assert true;
		} catch (RequiredValueNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void insert() {

		// Nameserver base data
		Nameserver nameserver = new NameserverDAO();
		nameserver.setHandle("XXX13");
		nameserver.setPunycodeName("ns1.xn--fo-5ja.example");
		nameserver.setPort43("whois.example.net");

		// IpAddressStruct data
		NameserverIpAddressesStruct ipAddresses = new NameserverIpAddressesStruct();

		IpAddress ipv41 = new IpAddressDAO();
		try {
			ipv41.setAddress(InetAddress.getByName("192.0.2.1"));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		ipv41.setType(4);
		ipAddresses.getIpv4Adresses().add(ipv41);

		IpAddress ipv42 = new IpAddressDAO();
		try {
			ipv42.setAddress(InetAddress.getByName("192.0.2.2"));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		ipv42.setType(4);
		ipAddresses.getIpv4Adresses().add(ipv42);

		IpAddress ipv6 = new IpAddressDAO();
		try {
			ipv6.setAddress(InetAddress.getByName("2001:db8::123"));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		ipv6.setType(6);
		ipAddresses.getIpv6Adresses().add(ipv6);
		nameserver.setIpAddresses(ipAddresses);

		// Status data
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.ACTIVE);
		statusList.add(Status.ASSOCIATED);
		nameserver.setStatus(statusList);

		// Remarks data
		List<Remark> remarks = new ArrayList<Remark>();
		Remark remark = new RemarkDAO();
		remark.setLanguage("ES");
		remark.setTitle("Prueba");
		remark.setType("PruebaType");

		List<RemarkDescription> descriptions = new ArrayList<RemarkDescription>();
		RemarkDescription description1 = new RemarkDescriptionDAO();
		description1.setOrder(1);
		description1.setDescription("She sells sea shells down by the sea shore.");

		RemarkDescription description2 = new RemarkDescriptionDAO();
		description2.setOrder(2);
		description2.setDescription("Originally written by Terry Sullivan.");

		descriptions.add(description1);
		descriptions.add(description2);
		remark.setDescriptions(descriptions);
		remarks.add(remark);
		nameserver.setRemarks(remarks);

		// Links data
		List<Link> links = new ArrayList<Link>();
		Link link = new LinkDAO();
		link.setValue("http://example.net/nameserver/xxxx");
		link.setRel("self");
		link.setHref("http://example.net/nameserver/xxxx");
		link.setType("application/rdap+json");
		links.add(link);
		nameserver.setLinks(links);

		// Events Data
		List<Event> events = new ArrayList<Event>();
		Event event1 = new EventDAO();
		event1.setEventAction(EventAction.REGISTRATION);
		event1.setEventDate(new Date());

		Event event2 = new EventDAO();
		event2.setEventAction(EventAction.LAST_CHANGED);
		event2.setEventDate(new Date());
		event2.setEventActor("joe@example.com");

		// event links data
		List<Link> eventLinks = new ArrayList<Link>();
		Link eventLink = new LinkDAO();
		eventLink.setValue("eventLink1");
		eventLink.setRel("eventlink");
		eventLink.setHref("http://example.net/eventlink/xxxx");
		eventLink.setType("application/rdap+json");
		eventLinks.add(eventLink);
		event2.setLinks(eventLinks);

		events.add(event1);
		events.add(event2);
		nameserver.setEvents(events);
		try {
			NameserverModel.storeToDatabase(nameserver, connection);
		} catch (IOException | SQLException | RequiredValueNotFoundException e) {
			e.printStackTrace();
			fail();
		}

		assert true;
	}

	@Test
	public void upsert() {
		// insert a new nameserver
		insert();
		// Nameserver base data
		NameserverDAO nameserver = new NameserverDAO();
		nameserver.setHandle("XXX13");
		nameserver.setPunycodeName("updatedInfo.example");
		nameserver.setPort43("whois.example.net");

		// IpAddressStruct data
		NameserverIpAddressesStruct ipAddresses = new NameserverIpAddressesStruct();

		IpAddress ipv41 = new IpAddressDAO();
		try {
			ipv41.setAddress(InetAddress.getByName("127.0.0.1"));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		ipv41.setType(4);
		ipAddresses.getIpv4Adresses().add(ipv41);
		nameserver.setIpAddresses(ipAddresses);

		// Status data
		List<Status> statusList = new ArrayList<Status>();
		statusList.add(Status.DELETE_PROHIBITED);
		nameserver.setStatus(statusList);

		// Remarks data
		List<Remark> remarks = new ArrayList<Remark>();
		Remark remark = new RemarkDAO();
		remark.setLanguage("EN");
		remark.setTitle("TEST");

		List<RemarkDescription> descriptions = new ArrayList<RemarkDescription>();
		RemarkDescription description1 = new RemarkDescriptionDAO();
		description1.setOrder(1);
		description1.setDescription("kings are dying like flies");

		descriptions.add(description1);
		remark.setDescriptions(descriptions);
		remarks.add(remark);
		nameserver.setRemarks(remarks);

		// Events Data
		List<Event> events = new ArrayList<Event>();
		Event event1 = new EventDAO();
		event1.setEventAction(EventAction.DELETION);
		event1.setEventDate(new Date());

		// event links data
		List<Link> eventLinks = new ArrayList<Link>();
		Link eventLink = new LinkDAO();
		eventLink.setValue("eventLink1");
		eventLink.setRel("eventlink");
		eventLink.setHref("http://example.net/eventlink/xxxx");
		eventLink.setType("application/rdap+json");
		eventLinks.add(eventLink);

		events.add(event1);
		nameserver.setEvents(events);
		try {
			NameserverModel.upsertToDatabase(nameserver, connection);
		} catch (IOException | SQLException | RequiredValueNotFoundException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getAll() {
		try {
			List<Nameserver> nameservers = NameserverModel.getAll(connection);
			for (Nameserver nameserver : nameservers) {
				System.out.println(nameserver.toString());
			}
			assert true;
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}

	@Test
	public void existByName() {
		try {
			NameserverModel.existByName("ns1.fóo.example", connection);
		} catch (SQLException s) {
			fail();
		}
	}

	@Test
	public void existByIp() {
		try {
			NameserverModel.existByIp("2001:db8:0:0:0:0:0:123", connection);
		} catch (SQLException | InvalidValueException s) {
			fail();
		}
	}
}
