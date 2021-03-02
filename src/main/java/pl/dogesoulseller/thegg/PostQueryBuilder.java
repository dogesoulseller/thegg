package pl.dogesoulseller.thegg;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class PostQueryBuilder {
	Query query;

	List<String> includedTags;
	List<String> excludedTags;

	// K: Field; V: Field constraint
	MultiValueMap<String, PostQuerySpecialFilter> criteria;

	public PostQueryBuilder() {
		query = new Query();
		criteria = new LinkedMultiValueMap<>();
	}

	// Triple<ComparisonOperator, Field, ValueCompared>
	public PostQueryBuilder append(PostQuerySpecialFilter tag) {
		// Criteria are added to a multimap as multiple queries on a single field have to be
		// grouped together because of com.mongodb.BasicDocument limitations
		criteria.add(tag.getField(), tag);
		return this;
	}

	public PostQueryBuilder includedTags(List<String> tags) {
		this.includedTags = tags;

		return this;
	}

	public PostQueryBuilder excludedTags(List<String> tags) {
		this.excludedTags = tags;

		return this;
	}

	public PostQueryBuilder sort(Sort sort) {
		query.with(sort);
		return this;
	}

	public PostQueryBuilder page(Pageable page) {
		query.with(page);
		return this;
	}

	public Query finish() {
		// Handle tags
		if ((includedTags == null && excludedTags == null) || (includedTags.isEmpty() && excludedTags.isEmpty())) {
		} else if (includedTags == null || includedTags.isEmpty()) {
			query.addCriteria(Criteria.where("tags").not().elemMatch(new Criteria().in(excludedTags)));
		} else if (excludedTags == null || excludedTags.isEmpty()) {
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
			if (key == "creation_date") {
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
