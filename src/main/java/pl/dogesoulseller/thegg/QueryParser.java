package pl.dogesoulseller.thegg;

import java.util.List;

import org.springframework.data.domain.Sort;

/**
 * Standard query parser interface
 */
public interface QueryParser {
	/**
	 * Resets the parser to its initial state
	 */
	public void reset();

	/**
	 * Resets the parser to its initial state, setting the query
	 * @param query
	 */
	public void reset(String query);

	/**
	 * Process the query
	 * @return this
	 */
	public QueryParser parse();

	public List<String> getInclusions();
	public List<String> getExclusions();
	public Sort getSorting();
}
