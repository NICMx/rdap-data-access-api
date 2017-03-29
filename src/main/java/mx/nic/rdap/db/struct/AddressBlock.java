package mx.nic.rdap.db.struct;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import mx.nic.rdap.db.exception.IpAddressFormatException;

public class AddressBlock {

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

	private final static int MIN_CIRD = 0;
	private final static int MAX_IPV4_CIDR = 32;
	private final static int MAX_IPV6_CIDR = 128;

	private final static int OCTECT_SIZE = 8;

	private final static byte SEVEN_BIT_MASK = (byte) 0b1111_1110;
	private final static byte SIX_BIT_MASK = (byte) 0b1111_1100;
	private final static byte FIVE_BIT_MASK = (byte) 0b1111_1000;
	private final static byte FOUR_BIT_MASK = (byte) 0b1111_0000;
	private final static byte THREE_BIT_MASK = (byte) 0b1110_0000;
	private final static byte TWO_BIT_MASK = (byte) 0b1100_0000;
	private final static byte ONE_BIT_MASK = (byte) 0b1000_0000;
	private final static byte ZERO_BIT_MASK = (byte) 0b0000_0000;

	private InetAddress address;
	/** Measured in bits. */
	private int addressLength;
	private int prefix;

	public AddressBlock(String address) throws IpAddressFormatException {
		this(address, null);
	}

	public AddressBlock(String address, Integer prefix) throws IpAddressFormatException {
		this(parseAddress(address), prefix);
	}

	public AddressBlock(InetAddress address) throws IpAddressFormatException {
		this(address, (Integer) null);
	}

	public AddressBlock(InetAddress address, Integer prefix) throws IpAddressFormatException {
		this.address = address;
		this.addressLength = 8 * address.getAddress().length;
		this.prefix = parsePrefix(prefix);
		validatePostfix();
	}

	public AddressBlock(InetAddress first, InetAddress last) throws IpAddressFormatException {
		byte[] firstBytes = first.getAddress();
		byte[] lastBytes = last.getAddress();

		if (firstBytes.length != lastBytes.length) {
			throw new IpAddressFormatException("Addresses " + first + " and " + last //
					+ "belong to different protocols.");
		}

		this.address = first;
		this.addressLength = 8 * firstBytes.length;
		this.prefix = 0;

		// First, sum full identical bytes.
		int i;
		for (i = 0; i < firstBytes.length && firstBytes[i] == lastBytes[i]; i++) {
			this.prefix += 8;
		}
		if (i == firstBytes.length) {
			return;
		}

		// Then, sum leftover identical bits.
		int difference = (lastBytes[i] & 0xFF) - (firstBytes[i] & 0xFF);
		int mask;
		for (mask = 0b10000000; mask > 0 && ((difference & mask) == 0); mask >>>= 1) {
			this.prefix++;
		}

		// Then make sure all leftover bits are zero and one, respectively.
		for (; mask > 0; mask >>>= 1) {
			if ((difference & mask) == 0) {
				throw new IpAddressFormatException(first + " and " + last //
						+ " do not seem to be the first and last addresses of a network.");
			}
		}

		// Then make sure all leftover bytes are zero and 0xFF, respectively.
		for (i = i + 1; i < firstBytes.length; i++) {
			difference = (lastBytes[i] & 0xFF) - (firstBytes[i] & 0xFF);
			if (difference != 0xFF) {
				throw new IpAddressFormatException(first + " and " + last //
						+ " do not seem to be the first and last addresses of a network.");
			}
		}
	}

	/**
	 * Converts <code>ipAddress</code> to an {@link InetAddress} preventing DNS
	 * lookups.
	 */
	public static InetAddress parseAddress(String ipAddress) throws IpAddressFormatException {
		// if the ipAddress contains ':' then InetAddress will try to parse it
		// like IPv6 address without doing a lookup to DNS.
		if (ipAddress.contains(":")) {
			try {
				return InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e) {
				throw new IpAddressFormatException("Invalid IPv6 address : " + ipAddress);
			}
		}

		if (ipAddress.startsWith(".") || !IP4_GENERIC_PATTERN.matcher(ipAddress).matches()) {
			throw new IpAddressFormatException("Invalid IPv4 address : " + ipAddress);
		}

		String[] split = ipAddress.split("\\.");

		int arraySize = split.length;
		if (arraySize > IPV4_ADDRESS_ARRAY_SIZE) {
			throw new IpAddressFormatException("Invalid IPv4 address : " + ipAddress);
		}

		BigInteger finalOctectValue;
		try {
			finalOctectValue = new BigInteger(split[arraySize - 1]);
		} catch (NumberFormatException e) {
			throw new IpAddressFormatException("Invalid IPv4 address : " + ipAddress);
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
			throw new IpAddressFormatException("Invalid IPv4 address : " + ipAddress);
		}

		try {
			return InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			throw new IpAddressFormatException("Invalid IPv4 address : " + ipAddress);
		}
	}

	private int parsePrefix(Integer prefix) throws IpAddressFormatException {
		if (address instanceof Inet4Address) {
			return parsePrefix(prefix, MAX_IPV6_CIDR);
		}
		if (address instanceof Inet6Address) {
			return parsePrefix(prefix, MAX_IPV4_CIDR);
		}

		throw new IpAddressFormatException("Only IPv4 and IPv6 addresses are supported.");
	}

	private static int parsePrefix(Integer prefix, int maxPrefix) throws IpAddressFormatException {
		if (prefix == null) {
			return maxPrefix;
		}

		if (prefix < MIN_CIRD || maxPrefix < prefix) {
			throw new IpAddressFormatException("Invalid cidr,  max valid : " + maxPrefix + ", input : " + prefix);
		}

		return prefix;
	}

	/**
	 * Checks there are no suffix bytes on the address.
	 */
	private void validatePostfix() throws IpAddressFormatException {
		if (prefix == addressLength) {
			return;
		}

		int activeOctectBits = prefix % OCTECT_SIZE;
		int startOctectToCheck = prefix / OCTECT_SIZE;

		byte[] address = this.address.getAddress();

		if ((address[startOctectToCheck] & getBitMask(activeOctectBits)) != address[startOctectToCheck]) {
			throw new IpAddressFormatException("The 'IP' address is not the first IP of the network for the 'CIDR'");
		}

		for (int i = startOctectToCheck + 1; i < address.length; i++) {
			if (address[i] != ZERO_BIT_MASK) {
				throw new IpAddressFormatException(
						"The 'IP' address is not the first IP of the network for the 'CIDR'");
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

	public InetAddress getAddress() {
		return address;
	}

	public int getPrefix() {
		return prefix;
	}

	public InetAddress getLastAddress() {
		if (prefix == addressLength) {
			return address;
		}

		byte[] bytes = address.getAddress();

		// Fill 1's in the prefix's byte.
		bytes[prefix >> 3] |= 0xFF >>> (prefix & 7);
		// Fill 1's in the remaining bytes.
		for (int i = (prefix >> 3) + 1; i < bytes.length; i++) {
			bytes[i] = (byte) 0xFF;
		}

		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Programming error: Automatically-generated array has an invalid length.");
		}
	}

	@Override
	public String toString() {
		return address.getHostAddress() + "/" + prefix;
	}
}
