package pl.dogesoulseller.thegg.query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * MongoDB query builder for data obtained from {@link PostQueryParser}
 */
public class PostQueryBuilder {
	/**
	 * Output query
	 */
	Query query;

	/**
	 * Tags that must be present in the results
	 */
	List<String> includedTags;

	/**
	 * Tags that must not be present in the results
	 */
	List<String> excludedTags;

	/**
	 * Collection of per-field criteria. Each key represents a single field which can have multiple constraints applied to it.
	 * This is required due to limitations of com.mongodb.BasicDocument
	 */
	MultiValueMap<String, PostQuerySpecialFilter> criteria;

	public PostQueryBuilder() {
		query = new Query();
		criteria = new LinkedMultiValueMap<>();
	}

	/**
	 * Append a filter to the query
	 *
	 * @param filter filter to append
	 * @return this
	 */
	public PostQueryBuilder append(PostQuerySpecialFilter filter) {
		// Criteria are added to a multimap as multiple queries on a single field have to be
		// grouped together because of com.mongodb.BasicDocument limitations
		criteria.add(filter.getField(), filter);
		return this;
	}

	/**
	 * Set tags to be forced to be included (present)
	 *
	 * @param tags included tags
	 * @return this
	 */
	public PostQueryBuilder includedTags(List<String> tags) {
		this.includedTags = tags;
		return this;
	}

	/**
	 * Set tags to be forced to be excluded (not present)
	 *
	 * @param tags excluded tags
	 * @return this
	 */
	public PostQueryBuilder excludedTags(List<String> tags) {
		this.excludedTags = tags;
		return this;
	}

	/**
	 * Set sorting method to use in query
	 *
	 * @param sort sorting method
	 * @return this
	 */
	public PostQueryBuilder sort(Sort sort) {
		query.with(sort);
		return this;
	}

	/**
	 * Set the paging strategy to use
	 *
	 * @param page paging strategy
	 * @return this
	 */
	public PostQueryBuilder page(Pageable page) {
		query.with(page);
		return this;
	}

	/**
	 * Finish processing and assemble the final query
	 *
	 * @return ready-to-execute query
	 */
	public Query finish() {
		boolean notHasIncludedTags = includedTags == null || includedTags.isEmpty();
		boolean notHasExcludedTags = excludedTags == null || excludedTags.isEmpty();

		// Handle tags
		if (notHasIncludedTags && notHasExcludedTags) {

		} else if (notHasIncludedTags) {
			query.addCriteria(Criteria.where("tags").not().elemMatch(new Criteria().in(excludedTags)));
		} else if (notHasExcludedTags) {
			query.addCriteria(Criteria.where("tags").all(includedTags));
		} else {
			query.addCriteria(new Criteria().andOperator(
				Criteria.where("tags").not().elemMatch(new Criteria().in(excludedTags)),
				Criteria.where("tags").all(includedTags)
			));
		}

		// Handle each field's constraints as a group
		for (var field : criteria.entrySet()) {
			var key = field.getKey();
			var vals = field.getValue();

			// Only process the first value in the case of an equality-only field
			if (vals.get(0).getComparison() == null) {
				query.addCriteria(Criteria.where(key).is(vals.get(0).getValue()));
				continue;
			}

			ArrayList<Criteria> fieldMultiCriteria = new ArrayList<>(vals.size());

			// Dates require special handling
			if (key.equals("creation_date")) {
				for (var val : vals) {
					switch (val.getComparison()) {
						case '>':
							fieldMultiCriteria.add(Criteria.where(key).gt(Instant.parse(val.getValue())));
							break;
						case '<':
							fieldMultiCriteria.add(Criteria.where(key).lt(Instant.parse(val.getValue())));
							break;
						case '=':
							fieldMultiCriteria.add(Criteria.where(key).is(Instant.parse(val.getValue())));
							break;
						default:
							break;
					}
				}
			} else {
				for (var val : vals) {
					switch (val.getComparison()) {
						case '>':
							fieldMultiCriteria.add(Criteria.where(key).gt(Integer.parseInt(val.getValue())));
							break;
						case '<':
							fieldMultiCriteria.add(Criteria.where(key).lt(Integer.parseInt(val.getValue())));
							break;
						case '=':
							fieldMultiCriteria.add(Criteria.where(key).is(Integer.parseInt(val.getValue())));
							break;
						default:
							break;
					}
				}
			}

			query.addCriteria(new Criteria().andOperator(fieldMultiCriteria.toArray(new Criteria[fieldMultiCriteria.size()])));
		}


		return query;
	}
}
