package pl.dogesoulseller.thegg.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dogesoulseller.thegg.api.model.oprequest.OpRequest;

import java.util.List;

public interface MongoRequestRepository extends MongoRepository<OpRequest, String>  {
	List<OpRequest> findByResolvedFalse();
}
