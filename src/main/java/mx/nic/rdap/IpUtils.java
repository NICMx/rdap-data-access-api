package mx.nic.rdap;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import mx.nic.rdap.db.exception.InvalidValueException;

public class IpUtils {

	/**
	 * This regex string match with ###.###.###.n or ###.###.n or #.n or n,
	 * where ###. is 000 or 0 to 255, and n is any integer number
	 */
	private static String IP4_GENERIC_REGEX = "(((0|1)?[0-9]{0,2}|2[0-4][0-9]|25[0-5])\\.){0,3}\\d*[^\\.]";

	/**
	 * Compiled pattern of <code>IP4_GENERIC_REGEX<code>
	 */
	private static Pattern IP4_GENERIC_PATTERN = Pattern.compile(IP4_GENERIC_REGEX);

	private static final BigInteger FIRST_OCTECT_LIMIT = new BigInteger("4294967295"); // 0xFFFF_FFFF
	private static final BigInteger SECOND_OCTECT_LIMIT = new BigInteger(0xFF_FFFF + "");// 16777215
	private static final BigInteger THIRD_OCTECT_LIMIT = new BigInteger(0xFFFF + "");// 65535
	private static final BigInteger FOURTH_OCTECT_LIMIT = new BigInteger(0xFF + ""); // 255
	private static final int IPV4_ADDRESS_ARRAY_SIZE = 4;

	private final static int MAX_IPV4_CIDR = 32;
	private final static int MAX_IPV6_CIDR = 128;

	private final static int MIN_CIRD = 0;
	private final static int OCTECT_SIZE = 8;

	private final static byte SEVEN_BIT_MASK = (byte) 0b1111_1110;
	private final static byte SIX_BIT_MASK = (byte) 0b1111_1100;
	private final static byte FIVE_BIT_MASK = (byte) 0b1111_1000;
	private final static byte FOUR_BIT_MASK = (byte) 0b1111_0000;
	private final static byte THREE_BIT_MASK = (byte) 0b1110_0000;
	private final static byte TWO_BIT_MASK = (byte) 0b1100_0000;
	private final static byte ONE_BIT_MASK = (byte) 0b1000_0000;
	private final static byte ZERO_BIT_MASK = (byte) 0b0000_0000;

	private final static byte ONE_BIT_WILDCARD = (byte) 0b0000_0001;
	private final static byte TWO_BIT_WILDCARD = (byte) 0b0000_0011;
	private final static byte THREE_BIT_WILDCARD = (byte) 0b0000_0111;
	private final static byte FOUR_BIT_WILDCARD = (byte) 0b0000_1111;
	private final static byte FIVE_BIT_WILDCARD = (byte) 0b0001_1111;
	private final static byte SIX_BIT_WILDCARD = (byte) 0b0011_1111;
	private final static byte SEVEN_BIT_WILDCARD = (byte) 0b0111_1111;
	private final static byte EIGHT_BIT_WILDCARD = (byte) 0b1111_1111;

	public static final int IPV6_PART_SIZE = 8;
	private static final BigInteger IPV4_MAX_VALUE = FIRST_OCTECT_LIMIT; // 0xFFFF_FFFF

	public static BigInteger addressToNumber(Inet4Address address) {
		byte[] byteAddress = address.getAddress();
		byte[] number = new byte[IPV4_ADDRESS_ARRAY_SIZE + 1];
		System.arraycopy(byteAddress, 0, number, 1, IPV4_ADDRESS_ARRAY_SIZE);
		return new BigInteger(number);
	}

	public static BigInteger inet6AddressToUpperPart(Inet6Address address) {
		return new BigInteger(toUpperPart(address.getAddress()));
	}

	public static BigInteger inet6AddressToLowerPart(Inet6Address address) {
		return new BigInteger(toLowerPart(address.getAddress()));
	}

	private static byte[] toUpperPart(byte[] address) {
		byte[] upper = new byte[IPV6_PART_SIZE + 1];
		System.arraycopy(address, 0, upper, 1, IPV6_PART_SIZE);
		return upper;
	}

	private static byte[] toLowerPart(byte[] address) {
		byte[] upper = new byte[IPV6_PART_SIZE + 1];
		System.arraycopy(address, IPV6_PART_SIZE, upper, 1, IPV6_PART_SIZE);
		return upper;
	}

	public static InetAddress numberToInet6(String upperPartNumber, String lowerPartNumber)
			throws UnknownHostException {
		long upper = Long.parseUnsignedLong(upperPartNumber);
		long lower = Long.parseUnsignedLong(lowerPartNumber);

		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
		buffer.putLong(upper);
		buffer.putLong(lower);

		return InetAddress.getByAddress(buffer.array());
	}

	public static InetAddress numberToInet4(String ipv4Number) throws UnknownHostException {

		BigInteger ipNumber = new BigInteger(ipv4Number);
		if (ipNumber.compareTo(IPV4_MAX_VALUE) > 0) {
			throw new RuntimeException("ip es mayor"); // TODO cambiar esto
		}

		return InetAddress.getByName(ipv4Number);
	}

	public static void validateIpCidr(String ip, int cidr) throws InvalidValueException {
		InetAddress inetAddress = validateIpAddress(ip);
		validateIpCidr(inetAddress, cidr);
	}

	public static void validateIpCidr(InetAddress inetAddress, int cidr) {
		if (inetAddress instanceof Inet4Address) {
			validateIpCidr(inetAddress, cidr, MAX_IPV4_CIDR);
		} else if (inetAddress instanceof Inet6Address) {
			validateIpCidr(inetAddress, cidr, MAX_IPV6_CIDR);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static void validateIpCidr(InetAddress inetAddress, int cidr, int maxCidr) {
		if (cidr > maxCidr || cidr < MIN_CIRD) {
			throw new RuntimeException("Invalid CIDR");
		}
		if (cidr == maxCidr) {
			return;
		}

		int activeOctectBits = cidr % OCTECT_SIZE;
		int startOctectToCheck = cidr / OCTECT_SIZE;

		byte[] address = inetAddress.getAddress();

		checkBits(address, activeOctectBits, startOctectToCheck);
	}

	private static void checkBits(byte[] address, int activeOctectBits, int startOctectToCheck) {
		if ((address[startOctectToCheck] & getBitMask(activeOctectBits)) != address[startOctectToCheck]) {
			throw new RuntimeException("The IP address is not the first one for the CIDR.");
		}

		for (int i = startOctectToCheck + 1; i < address.length; i++) {
			if (address[i] != ZERO_BIT_MASK) {
				throw new RuntimeException("The IP address is not the first one for the CIDR.");
			}
		}
	}

	private static byte getBitMask(int activeOctectBits) {
		switch (activeOctectBits) {
		case 0:
			return ZERO_BIT_MASK;
		case 1:
			return ONE_BIT_MASK;
		case 2:
			return TWO_BIT_MASK;
		case 3:
			return THREE_BIT_MASK;
		case 4:
			return FOUR_BIT_MASK;
		case 5:
			return FIVE_BIT_MASK;
		case 6:
			return SIX_BIT_MASK;
		case 7:
			return SEVEN_BIT_MASK;
		default:
			throw new InvalidParameterException("Invalid active octect bits: " + activeOctectBits);
		}
	}

	public static InetAddress getLastAddressFromNetwork(InetAddress inetAddress, int cidr) throws UnknownHostException {
		if (inetAddress instanceof Inet4Address) {
			return getLastAddressFromNetwork(inetAddress, cidr, MAX_IPV4_CIDR);
		} else if (inetAddress instanceof Inet6Address) {
			return getLastAddressFromNetwork(inetAddress, cidr, MAX_IPV6_CIDR);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static InetAddress getLastAddressFromNetwork(InetAddress inetAddress, int cidr, int maxCidr)
			throws UnknownHostException {
		if (cidr > maxCidr || cidr < MIN_CIRD) {
			throw new RuntimeException("Invalid CIDR");
		}
		if (cidr == maxCidr) {
			return inetAddress;
		}

		int activeOctectBits = OCTECT_SIZE - (cidr % OCTECT_SIZE);
		int startOctectToCheck = cidr / OCTECT_SIZE;

		byte[] lastAddressBytes = transformBytes(inetAddress, activeOctectBits, startOctectToCheck);
		return InetAddress.getByAddress(lastAddressBytes);
	}

	private static byte[] transformBytes(InetAddress inetAddress, int activeOctectBits, int startOctectToCheck) {
		byte[] address = inetAddress.getAddress();
		byte[] lastAddress = new byte[address.length];

		for (int i = 0; i < startOctectToCheck; i++) {
			lastAddress[i] = address[i];
		}

		byte bitWildcard = getBitWildcard(activeOctectBits);

		lastAddress[startOctectToCheck] = (byte) (address[startOctectToCheck] | bitWildcard);

		for (int i = startOctectToCheck + 1; i < address.length; i++) {
			lastAddress[i] = EIGHT_BIT_WILDCARD;
		}

		return lastAddress;
	}

	private static byte getBitWildcard(int activeOctectBits) {
		switch (activeOctectBits) {
		case 1:
			return ONE_BIT_WILDCARD;
		case 2:
			return TWO_BIT_WILDCARD;
		case 3:
			return THREE_BIT_WILDCARD;
		case 4:
			return FOUR_BIT_WILDCARD;
		case 5:
			return FIVE_BIT_WILDCARD;
		case 6:
			return SIX_BIT_WILDCARD;
		case 7:
			return SEVEN_BIT_WILDCARD;
		case 8:
			return EIGHT_BIT_WILDCARD;
		default:
			throw new InvalidParameterException("Invalid active octect bits: " + activeOctectBits);
		}
	}

	public static void validateLastIpCidr(String ip, int cidr) throws InvalidValueException {
		InetAddress inetAddress = validateIpAddress(ip);
		validateLastIpCidr(inetAddress, cidr);
	}

	public static void validateLastIpCidr(InetAddress inetAddress, int cidr) {
		if (inetAddress instanceof Inet4Address) {
			validateLastIpCidr(inetAddress, cidr, MAX_IPV4_CIDR);
		} else if (inetAddress instanceof Inet6Address) {
			validateLastIpCidr(inetAddress, cidr, MAX_IPV6_CIDR);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static void validateLastIpCidr(InetAddress inetAddress, int cidr, int maxCidr) {
		if (cidr > maxCidr || cidr < MIN_CIRD) {
			throw new RuntimeException("Invalid CIDR");
		}
		if (cidr == maxCidr) {
			return;
		}

		int activeOctectBits = OCTECT_SIZE - (cidr % OCTECT_SIZE);
		int startOctectToCheck = cidr / OCTECT_SIZE;

		byte[] address = inetAddress.getAddress();

		if ((address[startOctectToCheck] | getBitWildcard(activeOctectBits)) != address[startOctectToCheck]) {
			throw new RuntimeException("The IP address is not the last one for the CIDR.");
		}

		for (int i = startOctectToCheck + 1; i < address.length; i++) {
			if (address[i] != EIGHT_BIT_WILDCARD) {
				throw new RuntimeException("The IP address is not the last one for the CIDR.");
			}
		}

	}

	/**
	 * Validates if an String representing an InetAddress is valid, and if it is
	 * valid, return it as {@link InetAddress}
	 */
	public static InetAddress validateIpAddress(String ipAddress) throws InvalidValueException {
		// if the ipAddress contains ':' then InetAddress will try to parse it
		// like IPv6 address without doing a lookup to DNS.
		if (ipAddress.contains(":")) {
			try {
				return InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e) {
				throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
			}
		}

		if (ipAddress.startsWith(".") || !IP4_GENERIC_PATTERN.matcher(ipAddress).matches()) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

		String[] split = ipAddress.split("\\.");

		int arraySize = split.length;
		if (arraySize > IPV4_ADDRESS_ARRAY_SIZE) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

		BigInteger finalOctectValue;
		try {
			finalOctectValue = new BigInteger(split[arraySize - 1]);
		} catch (NumberFormatException e) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

		BigInteger limitValue = null;
		switch (arraySize) {
		case 1:
			limitValue = FIRST_OCTECT_LIMIT;
			break;
		case 2:
			limitValue = SECOND_OCTECT_LIMIT;
			break;
		case 3:
			limitValue = THIRD_OCTECT_LIMIT;
			break;
		case 4:
			limitValue = FOURTH_OCTECT_LIMIT;
			break;
		}

		if (limitValue.compareTo(finalOctectValue) < 0) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

		try {
			return InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

	}

	public static Integer getMaxValidCidr(InetAddress inetAddress) {
		if (inetAddress instanceof Inet4Address) {
			return MAX_IPV4_CIDR;
		} else if (inetAddress instanceof Inet6Address) {
			return MAX_IPV6_CIDR;
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

}