package ondicho.co.ke.RentalManangementSystemBackend.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.AuthenticationResponse;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.UserRepository;
import ondicho.co.ke.RentalManangementSystemBackend.services.Auth.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException, ServletException {
        final String authHeader=request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if(authHeader==null ||!authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
//      retrieve token from header
        jwt=authHeader.substring(7);
//        extract user email
        userEmail=jwtService.extractUsername(jwt);
        if(userEmail!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails=this.userDetailsService.loadUserByUsername(userEmail);
            if(jwtService.isTokenValid(jwt,userDetails)){
                User user1=userRepository.findByEmail(userDetails.getUsername()).get();

                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(
                        userDetails,null,userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
//                update context
                SecurityContextHolder.getContext().setAuthentication(authToken);

                AuthenticationResponse token= AuthenticationResponse.builder()
                        .access_token(jwt)
                        .email(userEmail)
                        .role(user1.getUserRoles())
                        .group(user1.getGroups())
                        .build();
                request.setAttribute("token", token);
            }
        }
//        pass context to next filter for processing
        filterChain.doFilter(request,response);
    }
}
