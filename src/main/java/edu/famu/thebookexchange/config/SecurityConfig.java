
package edu.famu.thebookexchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (for development only)
            .csrf(AbstractHttpConfigurer::disable)

            // Authorize requests
            .authorizeHttpRequests(auth -> auth
                // Permit open access to these endpoints
                .requestMatchers("/api/user/login").permitAll()
                .requestMatchers("/api/user/{userId}").permitAll()
                .anyRequest().permitAll()
            )

            // Basic Authentication (replace with JWT if needed)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

