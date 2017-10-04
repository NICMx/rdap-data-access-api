package mx.nic.rdap.db;

import java.util.HashSet;
import java.util.Set;

/**
 * Data access class for the RDAPUser. The object is a data structure of an rdap
 * user information
 * 
 */
public class RdapUser {

	private Long id;
	private String name;
	private String pass;
	private Integer maxSearchResults;
	private Set<String> accessRoles;

	/**
	 * Default constructor
	 */
	public RdapUser() {
		accessRoles = new HashSet<String>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pass == null) ? 0 : pass.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((maxSearchResults == null) ? 0 : maxSearchResults.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RdapUser))
			return false;
		RdapUser other = (RdapUser) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pass == null) {
			if (other.pass != null)
				return false;
		} else if (!pass.equals(other.pass))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (maxSearchResults == null) {
			if (other.maxSearchResults != null)
				return false;
		} else if (!maxSearchResults.equals(other.maxSearchResults))
			return false;
		return true;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the pass
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @param pass
	 *            the pass to set
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}

	/**
	 * @return the maxSearchResults
	 */
	public Integer getMaxSearchResults() {
		return maxSearchResults;
	}

	/**
	 * @param maxSearchResults
	 *            the maxSearchResults to set
	 */
	public void setMaxSearchResults(Integer maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}

	/**
	 * @return the accessRoles
	 */
	public Set<String> getAccessRoles() {
		return accessRoles;
	}

	/**
	 * @param accessRoles
	 *            the accessRoles to set
	 */
	public void setAccessRoles(Set<String> accessRoles) {
		this.accessRoles = accessRoles;
	}

}
