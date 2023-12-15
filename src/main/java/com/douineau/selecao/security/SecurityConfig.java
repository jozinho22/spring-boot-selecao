package com.douineau.selecao.security;

import com.douineau.selecao.model.security.Role;
import com.douineau.selecao.repository.security.AuthorizedUserRepository;
import com.douineau.selecao.security.JWTAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTAuthFilter JWTAuthFilter;
    private final AuthenticationProvider authProvider;

    public SecurityConfig(
            JWTAuthFilter JWTAuthFilter,
            AuthenticationProvider authProvider
    ) {
        this.JWTAuthFilter = JWTAuthFilter;
        this.authProvider = authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authz) ->
                        authz
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/api/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
                                .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .sessionManagement(policy ->
                        policy.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(JWTAuthFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();

    }

}
