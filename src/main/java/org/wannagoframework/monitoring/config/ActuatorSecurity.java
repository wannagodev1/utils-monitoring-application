/*
 * This file is part of the WannaGo distribution (https://github.com/wannago).
 * Copyright (c) [2019] - [2020].
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


package org.wannagoframework.monitoring.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@Order(1)
public class ActuatorSecurity extends WebSecurityConfigurerAdapter {

  private Environment env;

  public ActuatorSecurity(Environment env) {
    this.env = env;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser(env.getProperty("spring.security.user.name"))
        .password(passwordEncoder().encode(env.getProperty("spring.security.user.password")))
        .roles(env.getProperty("spring.security.user.roles"));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.antMatcher("/actuator/**")
        .authorizeRequests()
        .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("MONITORING").anyRequest()
        .permitAll()
        .anyRequest().authenticated()
        .and()
        .httpBasic();
  }
}

//