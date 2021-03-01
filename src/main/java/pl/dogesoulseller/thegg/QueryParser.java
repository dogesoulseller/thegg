package pl.dogesoulseller.thegg;

import java.util.List;

import org.springframework.data.domain.Sort;

public interface QueryParser {
	public void reset();
	public void reset(String query);
	public QueryParser parse();

	public List<String> getInclusions();
	public List<String> getExclusions();
	public Sort getSorting();
}
