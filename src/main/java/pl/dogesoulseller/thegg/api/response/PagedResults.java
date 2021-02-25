package pl.dogesoulseller.thegg.api.response;

import java.util.List;

import org.springframework.data.domain.Page;

public class PagedResults<T> {
	private List<T> results;
	private int pageCount;
	private int currentPage;

	public PagedResults(List<T> results, int pageCount, int currentPage) {
		this.results = results;
		this.pageCount = pageCount;
		this.currentPage = currentPage;
	}

	public PagedResults(Page<T> results, int currentPage) {
		this.results = results.getContent();
		this.pageCount = results.getTotalPages();
		this.currentPage = currentPage;
	}

	public List<T> getResults() {
		return results;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getPageCount() {
		return pageCount;
	}
}
