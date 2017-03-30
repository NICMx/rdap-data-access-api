package mx.nic.rdap.db.struct;

import java.util.ArrayList;
import java.util.List;

import mx.nic.rdap.core.db.RdapObject;

/**
 * Used to answer a search request. It contains a list of {@link RdapObject}
 * objects, depending on the search query it might truncate the result.
 */
public class SearchResultStruct<T extends RdapObject> {

	private List<T> results;

	// The search limit param did its job?
	private Boolean resultSetWasLimitedByUserConfiguration;

	private Integer searchResultsLimitForUser;

	public SearchResultStruct() {
		results = new ArrayList<T>();
		resultSetWasLimitedByUserConfiguration = false;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
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

	/**
	 * Truncates the inner list to the specified amount of elements.
	 * 
	 * This needs to be called by the RDAP server after every search function in
	 * case the implementation was too lazy to truncate it itself.
	 * 
	 * @param resultLimit
	 *            maximum number of elements we should return to the user.
	 */
	public void truncate(int resultLimit) {
		if (resultLimit >= results.size()) {
			return;
		}
		results = results.subList(0, resultLimit);
	}

}
