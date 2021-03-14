package pl.dogesoulseller.thegg.api.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * List of objects with associated data about paging
 */
@Getter
@AllArgsConstructor
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
	 * Constructs a new {@link PagedResults} using info from a filled out {@link Page} instance
	 * @param results {@link Page} containing info about results
	 * @param currentPage 0-based current page number
	 */
	public PagedResults(Page<T> results, int currentPage) {
		this.results = results.getContent();
		this.pageCount = results.getTotalPages();
		this.currentPage = currentPage;
	}
}
