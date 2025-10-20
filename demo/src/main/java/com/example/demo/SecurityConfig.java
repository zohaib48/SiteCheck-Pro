package com.example.demo;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.example.demo.Services.UserService;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final DataSource dataSource; // Inject DataSource for persistent token storage
    private final PasswordEncoder passwordEncoder;

  
    public SecurityConfig(UserService userService, DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

  @Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests(requests -> requests
            .antMatchers("/", "/register", "/login", "/check-status", "/css/**", "/images/**", "/fonts/**","/css1/**", "/img1/**","/js1/**", "/vendor1/**","/home","/index1","/crawl","/downloadCsvs").permitAll() // Allow access to static resources
            .antMatchers("/logout").permitAll()
            .antMatchers("/verify").permitAll()  // Permit access to the logout URL
            .anyRequest().authenticated())
        .formLogin(login -> login
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("email")
            .defaultSuccessUrl("/home", true)
            .failureUrl("/login?error")
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/logout") // Specify the logout URL
            .logoutSuccessUrl("/home") // Redirect to the home page after logout
            .permitAll())
      .rememberMe()
    .key("your-remember-me-key")
    .rememberMeParameter("remember-me")
    .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(30))
    .tokenRepository(customPersistentTokenRepository())
    .userDetailsService(userService) // Add userDetailsService explicitly

        .and()
        .csrf().disable(); // Temporarily disable CSRF for testing
}

  

    @Override
    protected void configure(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    // Define a custom persistent token repository using JDBC
    @Bean
    public PersistentTokenRepository customPersistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
}
