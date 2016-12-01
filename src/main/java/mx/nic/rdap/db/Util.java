package mx.nic.rdap.db;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import mx.nic.rdap.db.exception.InvalidValueException;;

/**
 * @author L00000185
 *
 */
public class Util {
	//
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
	private static final int IP_ADDRESS_ARRAY_SIZE = 4;

	/**
	 * Validates if IpAddress is valid
	 * 
	 * @param ipAddress
	 * @throws MalformedRequestException
	 */
	public static void validateIpAddress(String ipAddress) throws InvalidValueException {
		// if the ipAddress contains ':' then InetAddress will try to parse it
		// like IPv6 address without doing a lookup to DNS.
		if (ipAddress.contains(":")) {
			try {
				InetAddress.getByName(ipAddress);
			} catch (UnknownHostException e) {
				throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
			}
			return;
		}

		if (ipAddress.startsWith(".") || !IP4_GENERIC_PATTERN.matcher(ipAddress).matches()) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

		String[] split = ipAddress.split("\\.");

		int arraySize = split.length;
		if (arraySize > IP_ADDRESS_ARRAY_SIZE) {
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
			InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			throw new InvalidValueException("Requested ip is invalid.", "Address", "IP");
		}

	}

	/**
	 * Prepared statement don't allow set a unique value for an "in" clause, so,
	 * you have to manually add the list of parameters as ?
	 */
	public static String createDynamicQueryWithInClause(int listSize, String query) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < listSize; i++) {
			builder.append("?,");
		}
		String dynamicQuery = query.replaceFirst("\\?", builder.deleteCharAt(builder.length() - 1).toString());
		return dynamicQuery;
	}

}
