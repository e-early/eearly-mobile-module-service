package si.result.eearly;

import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile(value = {"public"})
public class WebSecurityConfigurationLocal {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(authorize -> authorize
                    .anyRequest()
                    .permitAll());

    http.csrf(AbstractHttpConfigurer::disable);
    http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
        SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
    final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();

    return source.setDefault(AccessPredicate.permitAll());
  }
}