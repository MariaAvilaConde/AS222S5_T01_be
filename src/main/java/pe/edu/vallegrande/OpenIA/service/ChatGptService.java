package pe.edu.vallegrande.OpenIA.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.OpenIA.model.OpenAiQuery;
import pe.edu.vallegrande.OpenIA.repository.ChatGptRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatGptService {

    private final WebClient.Builder webClientBuilder;
    private final ChatGptRepository repository;

//no te olvides de cambiar la URL de la API y la contraseña del AZURE 
    private static final String AZURE_OPENAI_API_URL = "https://openaiarias.openai.azure.com/openai/deployments/chatLiz/chat/completions?api-version=2023-03-15-preview";
    private static final String AZURE_API_KEY = "62fe7279ceea4fd1a4c5dcc08b74eb1a";

    public Mono<OpenAiQuery> getOpenAiResponse(String prompt) {
        WebClient webClient = webClientBuilder.build();

        // Crear el cuerpo de la solicitud (request body) para el API
        Map<String, Object> body = Map.of(
                "messages", new Object[] {
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", prompt)
                },
                "max_tokens", 1000,
                "temperature", 0.7
        );

        return webClient.post()
                .uri(AZURE_OPENAI_API_URL)
                .header("api-key", AZURE_API_KEY)  // Autenticación
                .bodyValue(body)                   // Enviar el cuerpo de la solicitud
                .retrieve()
                .bodyToMono(String.class)          // Convertir la respuesta en String
                .flatMap(response -> {
                    // Crear y guardar la entidad en la base de datos de manera reactiva
                    OpenAiQuery query = OpenAiQuery.builder()
                            .prompt(prompt)
                            .response(response)
                            .timestamp(LocalDateTime.now())
                            .build();
                    return repository.save(query);
                })
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.error(new RuntimeException("Error en la solicitud a Azure OpenAI: " + ex.getResponseBodyAsString(), ex))
                );
    }

    public Mono<OpenAiQuery> updateQuery(Long id, String newPrompt) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Query not found with id " + id)))
                .flatMap(existingQuery -> {
                    // Actualizar el prompt y la fecha de modificación
                    existingQuery.setPrompt(newPrompt);
                    existingQuery.setTimestamp(LocalDateTime.now());

                    // Crear el cuerpo de la solicitud (request body) para el API
                    Map<String, Object> body = Map.of(
                            "messages", new Object[] {
                                    Map.of("role", "system", "content", "You are a helpful assistant."),
                                    Map.of("role", "user", "content", newPrompt)
                            },
                            "max_tokens", 1000,
                            "temperature", 0.7
                    );

                    WebClient webClient = webClientBuilder.build();

                    return webClient.post()
                            .uri(AZURE_OPENAI_API_URL)
                            .header("api-key", AZURE_API_KEY)  // Autenticación
                            .bodyValue(body)                   // Enviar el cuerpo de la solicitud
                            .retrieve()
                            .bodyToMono(String.class)          // Convertir la respuesta en String
                            .flatMap(response -> {
                                existingQuery.setResponse(response);
                                return repository.save(existingQuery);
                            });
                });
    }

    public Mono<Void> deleteQuery(Long id) {
        return repository.deleteById(id);
    }

    public Mono<OpenAiQuery> createQuery(String prompt) {
        return getOpenAiResponse(prompt);
    }

    public Flux<OpenAiQuery> getAllQueries() {
        return repository.findAll();
    }
}
