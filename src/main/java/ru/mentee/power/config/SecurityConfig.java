/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/h2-console/**")
                                        .permitAll()
                                        .requestMatchers("/error")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET, "/api/books", "/api/books/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/books")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/api/books/**")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/api/books/**")
                                        .hasAnyRole("LIBRARIAN", "ADMIN")
                                        .requestMatchers("/api/users/**")
                                        .hasRole("ADMIN")
                                        .anyRequest()
                                        .authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
