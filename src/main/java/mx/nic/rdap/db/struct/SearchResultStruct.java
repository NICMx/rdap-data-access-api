package mx.nic.rdap.db.struct;

import java.util.ArrayList;
import java.util.List;

import mx.nic.rdap.core.db.RdapObject;

/**
 * Used to answer a search request. It contains a list of {@link RdapObject}
 * objects, depending on the search query it might truncate the result.
 */
public class SearchResultStruct {

	List<RdapObject> results;

	// The search limit param did its job?
	Boolean resultSetWasLimitedByUserConfiguration;

	Integer searchResultsLimitForUser;

	public SearchResultStruct() {
		results = new ArrayList<RdapObject>();
		resultSetWasLimitedByUserConfiguration = false;
	}

	public List<RdapObject> getResults() {
		return results;
	}

	public void setResults(List<RdapObject> results) {
		this.results = results;
	}

	public Boolean getResultSetWasLimitedByUserConfiguration() {
		return resultSetWasLimitedByUserConfiguration;
	}

	public void setResultSetWasLimitedByUserConfiguration(Boolean resultSetWasLimitedByUserConfiguration) {
		this.resultSetWasLimitedByUserConfiguration = resultSetWasLimitedByUserConfiguration;
	}

	public Integer getSearchResultsLimitForUser() {
		return searchResultsLimitForUser;
	}

	public void setSearchResultsLimitForUser(Integer searchResultsLimitForUser) {
		this.searchResultsLimitForUser = searchResultsLimitForUser;
	}

}
