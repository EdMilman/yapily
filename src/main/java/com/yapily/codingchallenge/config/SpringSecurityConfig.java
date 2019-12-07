package com.yapily.codingchallenge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.inMemoryAuthentication()
                .withUser("user").password("{noop}password").roles(ROLE_USER)
                .and()
                .withUser("admin").password("{noop}password").roles(ROLE_USER, ROLE_ADMIN);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/fact/**").hasRole(ROLE_USER)
                .antMatchers(HttpMethod.GET, "/facts").hasRole(ROLE_USER)
                .antMatchers(HttpMethod.GET, "/status").hasRole(ROLE_ADMIN)
                .antMatchers(HttpMethod.GET, "/all-facts").hasRole(ROLE_USER)
                .and()
                .csrf().disable()
                .formLogin().disable();
    }
}
