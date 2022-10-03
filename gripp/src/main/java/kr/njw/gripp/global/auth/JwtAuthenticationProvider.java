package kr.njw.gripp.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationProvider {
    public static final long TOKEN_VALID_TIME_MS = 60 * 60 * 1000;
    private static final String AUTHORITIES_KEY = "authorities";

    private final String secret;

    public JwtAuthenticationProvider(@Value("${gripp.jwt.secret}") String secret) {
        this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(String username, List<Role> roles) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(username);

        claims.put(AUTHORITIES_KEY, roles.stream().map(Role::toAuthority).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + TOKEN_VALID_TIME_MS))
                .signWith(SignatureAlgorithm.HS256, this.secret)
                .compact();
    }

    public Optional<Authentication> getAuthentication(String token) {
        if (!this.validateToken(token)) {
            return Optional.empty();
        }

        Claims claims = this.getClaims(token);
        Collection<? extends GrantedAuthority> authorities = this.getAuthorities(claims);
        UserDetails userDetails = new User(claims.getSubject(), "", authorities);

        if (StringUtils.isBlank(claims.getSubject())) {
            return Optional.empty();
        }

        return Optional.of(new UsernamePasswordAuthenticationToken(userDetails, "", authorities));
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = this.getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
        Object authorities = claims.get(AUTHORITIES_KEY);

        if (authorities instanceof Collection<?> authorityCollection) {
            return authorityCollection.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
