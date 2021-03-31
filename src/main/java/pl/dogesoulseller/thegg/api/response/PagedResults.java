package pl.dogesoulseller.thegg.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * List of objects with associated data about paging
 */
@XmlRootElement
public class PagedResults<T> {
	/**
	 * List of returned objects
	 */
	private List<T> results;

	/**
	 * Total page count
	 */
	private long pageCount;

	/**
	 * Current page number (0-based)
	 */
	private long currentPage;

	/**
	 * Constructs a new {@link PagedResults} using info from a filled out {@link Page} instance
	 * @param results {@link Page} containing info about results
	 * @param currentPage 0-based current page number
	 */
	public PagedResults(Page<T> results, long currentPage) {
		this.results = results.getContent();
		this.pageCount = results.getTotalPages();
		this.currentPage = currentPage;
	}

	@JsonCreator
	public PagedResults(@JsonProperty("results") List<T> results, @JsonProperty("pageCount") long pageCount, @JsonProperty("currentPage") long currentPage) {
		this.results = results;
		this.pageCount = pageCount;
		this.currentPage = currentPage;
	}

	public List<T> getResults() {
		return this.results;
	}

	public long getPageCount() {
		return this.pageCount;
	}

	public long getCurrentPage() {
		return this.currentPage;
	}
}
