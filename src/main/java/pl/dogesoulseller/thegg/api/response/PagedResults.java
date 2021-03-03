package pl.dogesoulseller.thegg.api.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * List of objects with associated data about paging
 */
public class PagedResults<T> {
	/**
	 * List of returned objects
	 */
	private List<T> results;

	/**
	 * Total page count
	 */
	private int pageCount;

	/**
	 * Current page number (0-based)
	 */
	private int currentPage;

	/**
	 * Constructs a new {@link PagedResults} using raw info
	 * @param results List of objects returned from query
	 * @param pageCount total number of pages available
	 * @param currentPage 0-based current page number
	 */
	public PagedResults(List<T> results, int pageCount, int currentPage) {
		this.results = results;
		this.pageCount = pageCount;
		this.currentPage = currentPage;
	}

	/**
	 * Constructs a new {@link PagedResults} using info from a filled out {@link Page} instance
	 * @param results {@link Page} containing info about results
	 * @param currentPage 0-based current page number
	 */
	public PagedResults(Page<T> results, int currentPage) {
		this.results = results.getContent();
		this.pageCount = results.getTotalPages();
		this.currentPage = currentPage;
	}

	/**
	 * Get results
	 * @return {@link List} of objects
	 */
	public List<T> getResults() {
		return results;
	}

	/**
	 * Get current 0-based page number
	 * @return page number
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Get total page count
	 * @return page count
	 */
	public int getPageCount() {
		return pageCount;
	}
}
