package com.ems.config;

import com.ems.common.ApiError;
import com.ems.security.AuthCookieService;
import com.ems.security.JwtAuthenticationFilter;
import com.ems.security.JwtService;
import com.ems.security.SpaCsrfTokenRequestHandler;
import com.ems.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // active @PreAuthorize sur les contrôleurs
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final Set<String> CSRF_EXEMPT_PATHS = Set.of("/api/auth/login", "/api/auth/logout");

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthCookieService cookieService;
    private final AppProperties props;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(c -> c.sameSite("Strict").secure(props.cookie().secure()));

        http
                // CSRF : nécessaire dès que l'authentification passe par un cookie
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        .requireCsrfProtectionMatcher(csrfRequired()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)      // déconnexion gérée par AuthController
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // 401 : pas connecté, token invalide ou expiré
                        .authenticationEntryPoint((req, res, e) ->
                                writeError(res, HttpStatus.UNAUTHORIZED, "Authentication required"))
                        // 403 : connecté mais pas la permission (ou jeton CSRF manquant)
                        .accessDeniedHandler((req, res, e) -> writeError(res, HttpStatus.FORBIDDEN,
                                e instanceof CsrfException
                                        ? "Your request could not be verified. Please refresh the page."
                                        : "You do not have permission to perform this action")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService, cookieService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Le jeton CSRF est exigé pour les requêtes qui modifient des données (POST, PUT, DELETE)
     * et qui portent le cookie de session. Le login et le logout en sont exemptés :
     * il n'y a pas encore (ou plus) de session à protéger.
     */
    private RequestMatcher csrfRequired() {
        return (HttpServletRequest request) ->
                !SAFE_METHODS.contains(request.getMethod())
                        && !CSRF_EXEMPT_PATHS.contains(request.getRequestURI())
                        && cookieService.isPresent(request);
    }

    /** BCrypt : les mots de passe ne sont jamais stockés en clair. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void writeError(HttpServletResponse res, HttpStatus status, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getOutputStream(), ApiError.of(status, message));
    }
}
