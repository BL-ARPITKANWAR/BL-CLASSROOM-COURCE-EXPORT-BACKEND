package com.bridgelabz.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final BridgeLabzOAuth2UserService oauth2UserService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String frontendUrl;
    private final String allowedOrigins;

    public SecurityConfig(
            BridgeLabzOAuth2UserService oauth2UserService,
            OAuth2AuthorizedClientService authorizedClientService,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${app.cors.allowed-origins:http://localhost:3000}") String allowedOrigins) {
        this.oauth2UserService = oauth2UserService;
        this.authorizedClientService = authorizedClientService;
        this.frontendUrl = frontendUrl;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String successUrl = frontendUrl + "/dashboard";
        String failureUrl = frontendUrl + "/register?error=access-denied";
        String logoutUrl = frontendUrl + "/";
        List<String> corsOrigins = List.of(allowedOrigins.split(","));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/status").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                        .defaultSuccessUrl(successUrl, true)
                        .failureUrl(failureUrl)
                        .authorizedClientRepository(authorizedClientRepository())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(logoutUrl)
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .addLogoutHandler(oauth2AuthorizedClientLogoutHandler())
                        .permitAll());
        return http.build();
    }

    @Bean
    public LogoutHandler oauth2AuthorizedClientLogoutHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) -> {
            if (authentication != null && authentication.getName() != null) {
                authorizedClientService.removeAuthorizedClient("google", authentication.getName());
            }
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        };
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}