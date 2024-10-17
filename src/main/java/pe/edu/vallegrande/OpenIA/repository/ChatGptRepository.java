package pe.edu.vallegrande.OpenIA.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.OpenIA.model.OpenAiQuery;
import reactor.core.publisher.Flux;

@Repository
public interface ChatGptRepository extends ReactiveCrudRepository<OpenAiQuery, Long> {

}
