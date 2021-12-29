package com.example.flux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User
                .withUsername("user")
                .password(passwordEncoder().encode("123"))
                .roles("USER")
                .build();
        UserDetails admin = User
                .withUsername("admin")
                .password(passwordEncoder().encode("123"))
                .roles("ADMIN")
                .build();
        return new MapReactiveUserDetailsService(user, admin);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange() // following path with specific ROLE
                .pathMatchers("/students/admin").hasAuthority("ROLE_ADMIN")
                .pathMatchers("/students/*").hasAuthority("ROLE_USER")
                .pathMatchers("/say/*").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .pathMatchers("/hello").permitAll()   // allow all, either login ot not
                .pathMatchers("/say-www").permitAll()  // allow all, either login ot not
                .pathMatchers("/say-www2").permitAll()

                .anyExchange().authenticated()  // all other path with authenticated

                .and().httpBasic()  // use http-basic auth method
                .and().csrf().disable()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
