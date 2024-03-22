package ondicho.co.ke.RentalManangementSystemBackend.services.Auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ondicho.co.ke.RentalManangementSystemBackend.models.Auth.User;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.BlacklistRepository;
import ondicho.co.ke.RentalManangementSystemBackend.repositories.Auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    BlacklistRepository blacklistRepository;

    @Autowired
    UserRepository userRepository;

    private static  String SECRET_KEY="d01iVnBlQktqQVpWR3o1Vmhob1AzSG9qRlRETnRNcU0=";
//    private static  String SECRET_KEY=null;

//    public JwtService(@Value("{jwt.secret.key}")String SECRET_KEY){
//        JwtService.SECRET_KEY =SECRET_KEY;
//    }

//    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    public String generateToken(UserDetails userDetails){

        User user=userRepository.findByEmail(userDetails.getUsername()).get();

        return Jwts
                .builder()
                .claim("authorities",user.getUserRoles())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+900000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractUsername(String token){
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e){
            LOGGER.error("extract claims error : " +e);
            return null;
        }
    }
    public Boolean isTokenValid(String token, UserDetails userDetails){
        final String username=extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && !isTokenInBlackList(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenInBlackList(String token){
        return blacklistRepository.findByToken(token).isPresent();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(){
        byte[] keyBytes= Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
