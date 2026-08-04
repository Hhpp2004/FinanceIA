package financa.backend.Filter;

import financa.backend.Service.EmailService;
import financa.backend.Service.RateLimitService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@AllArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final EmailService es;

    /**
     * Assets are requested after the HTML document. If they were subject to the
     * same limit, an error page could be returned in place of the stylesheet,
     * leaving the error screen without its visual identity.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/webjars/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key;

        String email = request.getParameter("email");
        Authentication isAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if(isAuthentication != null && isAuthentication.isAuthenticated() && !(isAuthentication instanceof AnonymousAuthenticationToken)){
            key = isAuthentication.getName();
        } else {
            key = request.getRemoteAddr();
        }
        Bandwidth bandwidth;
        String uri = request.getRequestURI();
        if (uri.equals("/erro/429")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (uri.equals("/web-login")) {

            bandwidth = Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofMinutes(1))
                    .build();

        } else if (uri.equals("/web-cadastro")) {

            bandwidth = Bandwidth.builder()
                    .capacity(3)
                    .refillGreedy(3, Duration.ofMinutes(1))
                    .build();

        } else if (uri.startsWith("/api/")) {

            bandwidth = Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofMinutes(1))
                    .build();

        } else {

            bandwidth = Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofMinutes(1))
                    .build();
        }

        Bucket bucket = rateLimitService.resolveBucket(
                key + ":" + uri,
                bandwidth
        );

        if(bucket.tryConsume(1)){
            filterChain.doFilter(request,response);
        } else {
            response.sendRedirect(request.getContextPath() + "/erro/429");
            if(email != null){
                es.sendEmailError(email);
            }
        }
    }
}
