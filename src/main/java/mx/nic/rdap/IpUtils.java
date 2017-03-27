package mx.nic.rdap;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import mx.nic.rdap.db.exception.IpAddressFormatException;
import mx.nic.rdap.db.exception.http.BadRequestException;

public class IpUtils {

	/**
	 * This regex string match with ###.###.###.n or ###.###.n or #.n or n,
	 * where ###. is 000 or 0 to 255, and n is any integer number
	 */
	private static final String IP4_GENERIC_REGEX = "(((0|1)?[0-9]{0,2}|2[0-4][0-9]|25[0-5])\\.){0,3}\\d*[^\\.]";

	/**
	 * Compiled pattern of <code>IP4_GENERIC_REGEX<code>
	 */
	private static final Pattern IP4_GENERIC_PATTERN = Pattern.compile(IP4_GENERIC_REGEX);

	private static final BigInteger FIRST_OCTECT_LIMIT = new BigInteger("4294967295"); // 0xFFFF_FFFF
	private static final BigInteger SECOND_OCTECT_LIMIT = new BigInteger(0xFF_FFFF + "");// 16777215
	private static final BigInteger THIRD_OCTECT_LIMIT = new BigInteger(0xFFFF + "");// 65535
	private static final BigInteger FOURTH_OCTECT_LIMIT = new BigInteger(0xFF + ""); // 255
	private static final int IPV4_ADDRESS_ARRAY_SIZE = 4;
	private static final int IPV6_ADDRESS_ARRAY_SIZE = 16;

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

	private static final int IPV6_PART_SIZE = 8;
	private static final BigInteger IPV4_MAX_VALUE = FIRST_OCTECT_LIMIT; // 0xFFFF_FFFF

	private static Map<BigInteger, Short> ipv4CidrMap;
	private static Map<BigInteger, Short> ipv6CidrMap;

	public static void loadIpv4Cidr() {
		ipv4CidrMap = new HashMap<>(34);
		loadMap(ipv4CidrMap, (short) MAX_IPV4_CIDR);
	}

	public static void loadIpv6Cidr() {
		ipv6CidrMap = new HashMap<>(130);
		loadMap(ipv6CidrMap, (short) MAX_IPV6_CIDR);
	}

	private static void loadMap(Map<BigInteger, Short> ipCidrMap, Short maxCidr) {
		String bitValue = "1";
		for (short i = 0; i <= maxCidr; i++) {
			BigInteger bi = new BigInteger(bitValue, 2);
			bitValue = bitValue + "0";
			ipCidrMap.put(bi, (short) (maxCidr - i));
		}

	}

	public static BigInteger addressToNumber(Inet4Address address) {
		byte[] byteAddress = address.getAddress();
		byte[] number = new byte[IPV4_ADDRESS_ARRAY_SIZE + 1];
		System.arraycopy(byteAddress, 0, number, 1, IPV4_ADDRESS_ARRAY_SIZE);
		return new BigInteger(number);
	}

	public static BigInteger inet6AddressToNumber(Inet6Address address) {
		byte[] byteAddress = address.getAddress();
		byte[] number = new byte[IPV6_ADDRESS_ARRAY_SIZE + 1];
		System.arraycopy(byteAddress, 0, number, 1, IPV6_ADDRESS_ARRAY_SIZE);
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
			throws IpAddressFormatException {
		long upper;
		long lower;
		try {
			upper = Long.parseUnsignedLong(upperPartNumber);
			lower = Long.parseUnsignedLong(lowerPartNumber);
		} catch (NumberFormatException e) {
			throw new IpAddressFormatException("Invalid IPv6 address.", e);
		}

		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
		buffer.putLong(upper);
		buffer.putLong(lower);

		try {
			return InetAddress.getByAddress(buffer.array());
		} catch (UnknownHostException e) {
			throw new RuntimeException("Programming error: Automatically-generated array has an invalid length.", e);
		}
	}

	public static InetAddress numberToInet4(String ipv4Number) throws IpAddressFormatException {
		BigInteger ipNumber = new BigInteger(ipv4Number);
		if (ipNumber.compareTo(IPV4_MAX_VALUE) > 0) {
			throw new IpAddressFormatException("Invalid IPv4 address: " + ipv4Number);
		}

		try {
			return InetAddress.getByName(ipv4Number);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Programming error: Integer-formatted address triggered a lookup.", e);
		}
	}

	public static void validateIpCidr(String ip, int cidr) throws BadRequestException {
		InetAddress inetAddress = validateIpAddress(ip);
		validateIpCidr(inetAddress, cidr);
	}

	public static void validateIpCidr(InetAddress inetAddress, int cidr) throws BadRequestException {
		if (inetAddress instanceof Inet4Address) {
			validateIpCidr(inetAddress, cidr, MAX_IPV4_CIDR);
		} else if (inetAddress instanceof Inet6Address) {
			validateIpCidr(inetAddress, cidr, MAX_IPV6_CIDR);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static void validateIpCidr(InetAddress inetAddress, int cidr, int maxCidr) throws BadRequestException {
		validateCidr(cidr, maxCidr);
		if (cidr == maxCidr) {
			return;
		}

		int activeOctectBits = cidr % OCTECT_SIZE;
		int startOctectToCheck = cidr / OCTECT_SIZE;

		byte[] address = inetAddress.getAddress();

		checkBits(address, activeOctectBits, startOctectToCheck);
	}

	private static void checkBits(byte[] address, int activeOctectBits, int startOctectToCheck)
			throws BadRequestException {
		if ((address[startOctectToCheck] & getBitMask(activeOctectBits)) != address[startOctectToCheck]) {
			throw new BadRequestException("The 'IP' address is not the first IP of the network for the 'CIDR'");
		}

		for (int i = startOctectToCheck + 1; i < address.length; i++) {
			if (address[i] != ZERO_BIT_MASK) {
				throw new BadRequestException("The 'IP' address is not the first IP of the network for the 'CIDR'");
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
			throw new RuntimeException("Programming error. Invalid active octect bits: " + activeOctectBits);
		}
	}

	public static InetAddress getLastAddressFromNetwork(InetAddress inetAddress, int cidr)
			throws BadRequestException {
		if (inetAddress instanceof Inet4Address) {
			return getLastAddressFromNetwork(inetAddress, cidr, MAX_IPV4_CIDR);
		} else if (inetAddress instanceof Inet6Address) {
			return getLastAddressFromNetwork(inetAddress, cidr, MAX_IPV6_CIDR);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static InetAddress getLastAddressFromNetwork(InetAddress inetAddress, int cidr, int maxCidr)
			throws BadRequestException {
		validateCidr(cidr, maxCidr);
		if (cidr == maxCidr) {
			return inetAddress;
		}

		int activeOctectBits = OCTECT_SIZE - (cidr % OCTECT_SIZE);
		int startOctectToCheck = cidr / OCTECT_SIZE;

		byte[] lastAddressBytes = transformBytes(inetAddress, activeOctectBits, startOctectToCheck);
		try {
			return InetAddress.getByAddress(lastAddressBytes);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Programming error: Automatically-generated array has an invalid length.", e);
		}
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
			throw new RuntimeException("Programming error. Invalid active octect bits: " + activeOctectBits);
		}
	}

	public static void validateLastIpCidr(String ip, int cidr) throws BadRequestException {
		InetAddress inetAddress = validateIpAddress(ip);
		validateLastIpCidr(inetAddress, cidr);
	}

	public static void validateLastIpCidr(InetAddress inetAddress, int cidr) throws BadRequestException {
		if (inetAddress instanceof Inet4Address) {
			validateLastIpCidr(inetAddress, cidr, MAX_IPV4_CIDR);
		} else if (inetAddress instanceof Inet6Address) {
			validateLastIpCidr(inetAddress, cidr, MAX_IPV6_CIDR);
		} else {
			throw new UnsupportedOperationException("Unsupported class:" + inetAddress.getClass().getName());
		}
	}

	private static void validateLastIpCidr(InetAddress inetAddress, int cidr, int maxCidr)
			throws BadRequestException {
		validateCidr(cidr, maxCidr);
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
	 * <p>
	 * (From ydahhrk: I don't think the point of this function is to "validate"
	 * ipAddress. The point is to convert it to an InetAddress preventing DNS
	 * lookups.)
	 */
	public static InetAddress validateIpAddress(String ipAddress) throws BadRequestException {
		// if the ipAddress contains ':' then InetAddress will try to parse it
		// like IPv6 address without doing a lookup to DNS.
		if (ipAddress.contains(":")) {
			try {
				return InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e) {
				throw new BadRequestException("Invalid IPv6 address : " + ipAddress);
			}
		}

		if (ipAddress.startsWith(".") || !IP4_GENERIC_PATTERN.matcher(ipAddress).matches()) {
			throw new BadRequestException("Invalid IPv4 address : " + ipAddress);
		}

		String[] split = ipAddress.split("\\.");

		int arraySize = split.length;
		if (arraySize > IPV4_ADDRESS_ARRAY_SIZE) {
			throw new BadRequestException("Invalid IPv4 address : " + ipAddress);
		}

		BigInteger finalOctectValue;
		try {
			finalOctectValue = new BigInteger(split[arraySize - 1]);
		} catch (NumberFormatException e) {
			throw new BadRequestException("Invalid IPv4 address : " + ipAddress);
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
			throw new BadRequestException("Invalid IPv4 address : " + ipAddress);
		}

		try {
			return InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			throw new BadRequestException("Invalid IPv4 address : " + ipAddress);
		}

	}

	public static InetAddress getInetAddress(String ipNumber) throws BadRequestException {
		BigInteger bigInteger = new BigInteger(ipNumber);

		if (IPV4_MAX_VALUE.compareTo(bigInteger) <= 0) {
			try {
				return InetAddress.getByName(ipNumber);
			} catch (UnknownHostException e) {
				throw new RuntimeException("Programming error: Integer-formatted address triggered a lookup.", e);
			}
		}

		return bigIntegerToInet6Address(bigInteger);
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

	public static InetAddress bigIntegerToInet6Address(BigInteger bigInteger)
			throws BadRequestException {
		byte[] src = bigInteger.toByteArray();

		if (src.length > IPV6_ADDRESS_ARRAY_SIZE + 1 || (src.length == IPV6_ADDRESS_ARRAY_SIZE + 1 && src[0] != 0)) {
			throw new BadRequestException("Invalid number for an IPv6 address : " + bigInteger.toString());
		}

		if (src.length == IPV6_ADDRESS_ARRAY_SIZE) {
			try {
				return InetAddress.getByAddress(src);
			} catch (UnknownHostException e) {
				throw new RuntimeException("Programming error: Validated array has an invalid length.", e);
			}
		}

		int srcPos;
		int destPos;
		int length;
		if (src.length == IPV6_ADDRESS_ARRAY_SIZE + 1) {
			srcPos = 1;
			destPos = 0;
			length = IPV6_ADDRESS_ARRAY_SIZE;
		} else {
			srcPos = 0;
			destPos = IPV6_ADDRESS_ARRAY_SIZE - src.length;
			length = src.length;
		}

		byte[] dest = new byte[16];
		System.arraycopy(src, srcPos, dest, destPos, length);

		try {
			return InetAddress.getByAddress(dest);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Programming error: Automatically-generated array has an invalid length.", e);
		}
	}

	public static Short getCidrLenghtFromInetAddress(InetAddress startAddress, InetAddress endAddress)
			throws BadRequestException {
		if (startAddress == null || endAddress == null) {
			throw new BadRequestException("Address cannot be null.");
		}
		BigInteger hostNumbers = null;
		Map<BigInteger, Short> ipCidrMap = null;

		if (startAddress instanceof Inet4Address && endAddress instanceof Inet4Address) {
			BigInteger start = IpUtils.addressToNumber((Inet4Address) startAddress);
			BigInteger end = IpUtils.addressToNumber((Inet4Address) endAddress);
			hostNumbers = end.subtract(start).add(BigInteger.ONE);
			ipCidrMap = ipv4CidrMap;
		} else if (startAddress instanceof Inet6Address && endAddress instanceof Inet6Address) {
			BigInteger start = IpUtils.inet6AddressToNumber((Inet6Address) startAddress);
			BigInteger end = IpUtils.inet6AddressToNumber((Inet6Address) endAddress);
			hostNumbers = end.subtract(start).add(BigInteger.ONE);
			ipCidrMap = ipv6CidrMap;
		} else {
			throw new BadRequestException("Both InetAddress must be the Inet4Address or Inet6Address. StartAddress :'"
					+ startAddress.getHostAddress() + "', EndAddress : '" + endAddress.getHostAddress() + "'");
		}

		Short result = ipCidrMap.get(hostNumbers);
		if (result == null) {
			throw new BadRequestException("InetAddresses are not a valid range. StartAddress :'"
					+ startAddress.getHostAddress() + "', EndAddress : '" + endAddress.getHostAddress() + "'");
		}

		return result;
	}

	public static void validateIpv4Cidr(int cidr) throws BadRequestException {
		validateCidr(cidr, MAX_IPV4_CIDR);
	}

	public static void validateIpv6Cidr(int cidr) throws BadRequestException {
		validateCidr(cidr, MAX_IPV6_CIDR);
	}

	private static void validateCidr(int cidr, int maxCidr) throws BadRequestException {
		if (cidr > maxCidr || cidr < MIN_CIRD) {
			throw new BadRequestException("Invalid cidr,  max valid : " + maxCidr + ", input : " + cidr);
		}
	}
}
