package tech.vtsign.userservice.filter;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.vtsign.userservice.exception.ExpiredException;
import tech.vtsign.userservice.exception.InvalidFormatException;
import tech.vtsign.userservice.model.UserResponseDto;
import tech.vtsign.userservice.security.UserDetailsImpl;
import tech.vtsign.userservice.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().contains("/v3/api-docs") && !request.getRequestURI().contains("/apt/")) {
            final String requestTokenHeader = request.getHeader("Authorization");
            UserResponseDto payload = null;

            // JWT Token is in the form "Bearer token". Remove Bearer word and get
            // only the Token
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                String jwtToken = requestTokenHeader.substring(7);
                if (jwtTokenUtil.isTokenExpired(jwtToken)) {
                    throw new ExpiredException("Token is expired");
                }
                payload = jwtTokenUtil.getObjectFromToken(jwtToken, "user");
            } else {
                logger.warn("JWT Token does not begin with Bearer String");
                throw new InvalidFormatException("Jwt Token does not begin with Bearer String");
            }

            // Once we get the token validate it.
            if (payload != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetailsImpl userDetails = new UserDetailsImpl(payload);

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            }
        }
        chain.doFilter(request, response);
    }
}