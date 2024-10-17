package pe.edu.vallegrande.OpenIA.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.OpenIA.model.OpenAiQuery;
import pe.edu.vallegrande.OpenIA.service.ChatGptService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Consultas OpenAI", description = "Puntos finales para interactuar con OpenAI")
public class ChatGptController {

    private final ChatGptService azureOpenAiService;

    @Operation(summary = "Obtener respuesta de OpenAI", description = "Obtener una respuesta de OpenAI para un prompt dado")
    @ApiResponse(responseCode = "200", description = "Respuesta obtenida con éxito")
    @ApiResponse(responseCode = "400", description = "Prompt inválido o error en la solicitud")
    @GetMapping
    public Mono<ResponseEntity<OpenAiQuery>> getOpenAiResponse(
            @Parameter(description = "Prompt para OpenAI") @RequestParam String prompt) {
        return azureOpenAiService.getOpenAiResponse(prompt)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(null))); // Manejo de errores
    }

    @Operation(summary = "Actualizar una consulta de OpenAI", description = "Actualizar el prompt de una consulta existente de OpenAI por ID")
    @ApiResponse(responseCode = "200", description = "Consulta actualizada con éxito")
    @ApiResponse(responseCode = "404", description = "Consulta no encontrada")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<OpenAiQuery>> updateQuery(
            @Parameter(description = "ID de la consulta a actualizar") @PathVariable Long id,
            @RequestBody @Parameter(description = "Nuevo prompt para la consulta") String newPrompt) {
        return azureOpenAiService.updateQuery(id, newPrompt)
                .map(updatedQuery -> ResponseEntity.ok(updatedQuery))
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Respuesta en caso de no encontrar la consulta
    }

    @Operation(summary = "Eliminar una consulta", description = "Eliminar una consulta existente de OpenAI por ID")
    @ApiResponse(responseCode = "200", description = "Consulta eliminada con éxito")
    @ApiResponse(responseCode = "404", description = "Consulta no encontrada")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteQuery(
            @Parameter(description = "ID de la consulta a eliminar") @PathVariable Long id) {
        return azureOpenAiService.deleteQuery(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build())) // Respuesta exitosa
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build())); // Respuesta en caso de no encontrar la consulta
    }

    @Operation(summary = "Crear una nueva consulta de OpenAI", description = "Crear una nueva consulta de OpenAI con el prompt dado")
    @ApiResponse(responseCode = "201", description = "Consulta creada con éxito")
    @ApiResponse(responseCode = "400", description = "Prompt inválido o error en la solicitud")
    @PostMapping
    public Mono<ResponseEntity<OpenAiQuery>> createQuery(
            @RequestBody @Parameter(description = "Prompt para la nueva consulta de OpenAI") String prompt) {
        return azureOpenAiService.createQuery(prompt)
                .map(createdQuery -> ResponseEntity.status(HttpStatus.CREATED).body(createdQuery))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(null))); // Manejo de errores
    }

    @Operation(summary = "Listar todas las consultas", description = "Recuperar todas las consultas de OpenAI almacenadas en la base de datos")
    @ApiResponse(responseCode = "200", description = "Consultas recuperadas con éxito")
    @GetMapping("/listar")
    public Flux<ResponseEntity<OpenAiQuery>> listAllQueries() {
        return azureOpenAiService.getAllQueries()
                .map(query -> ResponseEntity.ok(query))
                .switchIfEmpty(Flux.just(ResponseEntity.noContent().build())); // Respuesta si no hay consultas
    }
}
