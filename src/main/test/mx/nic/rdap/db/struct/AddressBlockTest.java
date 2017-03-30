package mx.nic.rdap.db.struct;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import junit.framework.TestCase;
import mx.nic.rdap.core.ip.AddressBlock;
import mx.nic.rdap.core.ip.IpAddressFormatException;

public class AddressBlockTest extends TestCase {

	@Test
	public void testDoubleInetAddressConstructor() throws UnknownHostException, IpAddressFormatException {
		testDoubleConstructorSuccess("192.0.2.0", "192.0.2.255", 24);
		testDoubleConstructorSuccess("192.0.2.0", "192.0.2.0", 32);
		testDoubleConstructorSuccess("192.0.2.16", "192.0.2.31", 28);
		testDoubleConstructorSuccess("192.0.2.168", "192.0.2.175", 29);

		testDoubleConstructorSuccess("192.6.0.0", "192.6.255.255", 16);
		testDoubleConstructorSuccess("192.6.0.0", "192.7.255.255", 15);
		testDoubleConstructorSuccess("192.84.0.0", "192.87.255.255", 14);
		testDoubleConstructorSuccess("192.64.0.0", "192.95.255.255", 11);

		testDoubleConstructorSuccess("0.0.0.0", "255.255.255.255", 0);
		testDoubleConstructorSuccess("0.0.0.0", "0.255.255.255", 8);
		testDoubleConstructorSuccess("36.0.0.0", "37.255.255.255", 7);
		testDoubleConstructorSuccess("24.0.0.0", "27.255.255.255", 6);

		testDoubleConstructorFailure("192.0.2.1", "192.0.2.255");
		testDoubleConstructorFailure("4.0.0.0", "16.255.255.255");
		testDoubleConstructorFailure("192.0.2.0", "192.0.2.250");
		testDoubleConstructorFailure("192.0.0.0", "192.0.255.254");
		testDoubleConstructorFailure("0.0.0.0", "127.255.223.255");
	}

	private void testDoubleConstructorSuccess(String first, String last, int expected)
			throws UnknownHostException, IpAddressFormatException {
		AddressBlock block = new AddressBlock(InetAddress.getByName(first), InetAddress.getByName(last));
		TestCase.assertEquals(expected, block.getPrefix());
	}

	private void testDoubleConstructorFailure(String first, String last) throws UnknownHostException {
		try {
			new AddressBlock(InetAddress.getByName(first), InetAddress.getByName(last));
			TestCase.fail("Should have crashed. Test was [" + first + ", " + last + "].");
		} catch (IpAddressFormatException e) {
			// No code; success.
		}
	}

	@Test
	public void testGetLastAddress() throws UnknownHostException, IpAddressFormatException {
		testLastAddress("192.0.2.0", 32, "192.0.2.0");

		testLastAddress("192.0.2.0", 24, "192.0.2.255");
		testLastAddress("192.0.2.0", 25, "192.0.2.127");
		testLastAddress("192.0.2.0", 30, "192.0.2.3");
		testLastAddress("192.0.2.0", 31, "192.0.2.1");

		testLastAddress("192.0.0.0", 16, "192.0.255.255");
		testLastAddress("192.0.128.0", 17, "192.0.255.255");
		testLastAddress("192.0.0.0", 22, "192.0.3.255");
		testLastAddress("192.0.254.0", 23, "192.0.255.255");

		testLastAddress("192.0.0.0", 16, "192.0.255.255");
		testLastAddress("192.0.128.0", 17, "192.0.255.255");
		testLastAddress("192.0.0.0", 22, "192.0.3.255");
		testLastAddress("192.0.254.0", 23, "192.0.255.255");

		testLastAddress("0.0.0.0", 0, "255.255.255.255");
		testLastAddress("128.0.0.0", 1, "255.255.255.255");
		testLastAddress("0.0.0.0", 1, "127.255.255.255");
		testLastAddress("90.0.0.0", 7, "91.255.255.255");
		testLastAddress("123.0.0.0", 8, "123.255.255.255");
	}

	private void testLastAddress(String testAddress, int testPrefix, String expected)
			throws IpAddressFormatException, UnknownHostException {
		AddressBlock block = new AddressBlock(testAddress, testPrefix);
		TestCase.assertEquals(InetAddress.getByName(expected), block.getLastAddress());
	}

}
