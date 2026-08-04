package financa.backend.Config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import financa.backend.Filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import financa.backend.Repository.UserRepository;
import financa.backend.Service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Value("${jwt.public.key}")
    private Resource publicKey;
    @Value("${jwt.private.key}")
    private Resource privateKey;

    private final String[] publicPostRoutes = { "/login", "/cadastro", "/confirmacao","/" };

    private static RSAPrivateKey readPrivateKey(Resource resource) {
        try (InputStream is = resource.getInputStream()){
            String key = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
            String sanitized = key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(sanitized);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Erro: " + e.getLocalizedMessage());
        }
    }


    private static RSAPublicKey readPublicKey(Resource resource) {
        try (InputStream is = resource.getInputStream()){
            String key = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
            String sanitized = key.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(sanitized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Erro: "+e.getMessage());
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            AuthenticationSuccessHandler webAuthenticationSuccessHandler,
            AuthenticationFailureHandler webAuthenticationFailureHandler,
            RateLimitFilter rateLimitFilter
    ) throws Exception {

        return httpSecurity

                // Rate Limiting
                .addFilterBefore(rateLimitFilter, BearerTokenAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/login",
                                "/cadastro",
                                "/confirmar-email",
                                "/esqueci-senha",
                                "/verificar-codigo",
                                "/redefinir-senha",
                                "/erro/**",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/webjars/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/web-cadastro",
                                "/web-confirmar-email",
                                "/web-esqueci-senha",
                                "/web-verificar-codigo",
                                "/web-redefinir-senha"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, publicPostRoutes)
                        .permitAll()

                        .anyRequest()
                        .authenticated())

                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/login",
                        "/cadastro",
                        "/confirmacao",
                        "/operacao",
                        "/user",
                        "/user/**",
                        "/file"
                ))

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/web-login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .successHandler(webAuthenticationSuccessHandler)
                        .failureHandler(webAuthenticationFailureHandler)
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login")
                        ))

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults())
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return email -> users.findByEmail(email)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getSenha())
                        .roles("USER")
                        .disabled(!user.isVerify())
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Credenciais inválidas"));
    }

    @Bean
    public AuthenticationSuccessHandler webAuthenticationSuccessHandler(UserService userService) {
        return (request, response, authentication) -> {
            userService.afterSuccessfulLogin(authentication.getName());
            response.sendRedirect(request.getContextPath() + "/app");
        };
    }

    @Bean
    public AuthenticationFailureHandler webAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            String reason = exception instanceof org.springframework.security.authentication.DisabledException
                    ? "unverified"
                    : "error";
            response.sendRedirect(request.getContextPath() + "/login?" + reason);
        };
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall httpFirewall) {
        return web -> web.httpFirewall(httpFirewall);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        RSAPrivateKey privatekey = readPrivateKey(this.privateKey);
        RSAPublicKey publickey = readPublicKey(this.publicKey);
        JWK jwk = new RSAKey.Builder(publickey).privateKey(privatekey).build();
        var aux = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(aux);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        RSAPublicKey publickey = readPublicKey(this.publicKey);
        return NimbusJwtDecoder.withPublicKey(publickey).build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
