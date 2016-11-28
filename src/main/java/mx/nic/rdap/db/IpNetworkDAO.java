package mx.nic.rdap.db;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import mx.nic.rdap.IpUtils;
import mx.nic.rdap.core.catalog.IpVersion;
import mx.nic.rdap.core.db.IpNetwork;
import mx.nic.rdap.db.model.CountryCodeModel;

public class IpNetworkDAO extends IpNetwork implements DatabaseObject {

	@Override
	public void loadFromDatabase(ResultSet rs) throws SQLException {
		setId(rs.getLong("ine_id"));
		setHandle(rs.getString("ine_handle"));
		setParentHandle(rs.getString("ine_parent_handle"));
		int ipVersion = rs.getInt("ip_version_id");
		setIpVersion(IpVersion.getByVersionNumber(ipVersion));
		try {
			if (getIpVersion().equals(IpVersion.V4)) {
				setStartAddress(IpUtils.numberToInet4(rs.getString("ine_start_address_down")));
				setEndAddress(IpUtils.numberToInet4(rs.getString("ine_end_address_down")));
			} else if (getIpVersion().equals(IpVersion.V6)) {
				setStartAddress(IpUtils.numberToInet6(rs.getString("ine_start_address_up"),
						rs.getString("ine_start_address_down")));
				setEndAddress(IpUtils.numberToInet6(rs.getString("ine_end_address_up"),
						rs.getString("ine_end_address_down")));
			} else {

			}
		} catch (UnknownHostException e) {
			throw new SQLException(e);
		}
		setName(rs.getString("ine_name"));
		setType(rs.getString("ine_type"));
		setCountry(CountryCodeModel.getCountryNameById(rs.getInt("ccd_id")));
		setPort43(rs.getString("ine_port43"));
		setCidr(rs.getInt("ine_cidr"));
	}

	@Override
	public void storeToDatabase(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, getHandle());
		if (getStartAddress() instanceof Inet4Address) {
			preparedStatement.setNull(2, Types.BIGINT);
			preparedStatement.setLong(3, IpUtils.addressToNumber((Inet4Address) getStartAddress()).longValueExact());
			preparedStatement.setNull(4, Types.BIGINT);
			preparedStatement.setLong(5, IpUtils.addressToNumber((Inet4Address) getEndAddress()).longValueExact());
		} else if (getStartAddress() instanceof Inet6Address) {
			preparedStatement.setString(2,
					IpUtils.inet6AddressToUpperPart((Inet6Address) getStartAddress()).toString());
			preparedStatement.setString(3,
					IpUtils.inet6AddressToLowerPart((Inet6Address) getStartAddress()).toString());
			preparedStatement.setString(4, IpUtils.inet6AddressToUpperPart((Inet6Address) getEndAddress()).toString());
			preparedStatement.setString(5, IpUtils.inet6AddressToLowerPart((Inet6Address) getEndAddress()).toString());
		}

		preparedStatement.setString(6, getName());
		preparedStatement.setString(7, getType());
		preparedStatement.setString(8, getPort43());
		preparedStatement.setInt(9, CountryCodeModel.getIdByCountryName(getCountry()));
		preparedStatement.setInt(10, getIpVersion().getVersion());
		preparedStatement.setString(11, getParentHandle());
		preparedStatement.setInt(12, getCidr());
	}

	@Override
	public void updateInDatabase(PreparedStatement preparedStatement) throws SQLException {
		// TODO Auto-generated method stub

	}

}