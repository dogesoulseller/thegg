package pl.dogesoulseller.thegg.query;

import java.util.List;

import org.springframework.data.domain.Sort;

/**
 * Standard query parser interface
 */
public interface QueryParser {
	/**
	 * Resets the parser to its initial state
	 */
	void reset();

	/**
	 * Resets the parser to its initial state, setting the query
	 *
	 * @param query query
	 */
	void reset(String query);

	/**
	 * Process the query
	 *
	 * @return this
	 */
	QueryParser parse();

	List<String> getInclusions();

	List<String> getExclusions();

	Sort getSorting();
}
