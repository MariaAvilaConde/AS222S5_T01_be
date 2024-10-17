package pe.edu.vallegrande.OpenIA.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                // Puedes personalizar el WebClient aquí, por ejemplo, estableciendo un timeout
                .baseUrl("https://api.example.com") // Cambia esto a tu URL base si es necesario
                .defaultHeader("Authorization", "Bearer your-token"); // Agrega encabezados predeterminados si es necesario
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
