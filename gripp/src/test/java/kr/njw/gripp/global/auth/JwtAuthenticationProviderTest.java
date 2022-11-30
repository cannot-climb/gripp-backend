package kr.njw.gripp.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class JwtAuthenticationProviderTest {
    private static final String SECRET = "secret";
    private static final String SECRET_BYTES = Base64.getEncoder().encodeToString(SECRET.getBytes());
    private static final long DURATION_SECONDS = 60;

    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @BeforeEach
    void setUp() {
        this.jwtAuthenticationProvider = new JwtAuthenticationProvider(SECRET, DURATION_SECONDS);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createToken() {
        String token = this.jwtAuthenticationProvider.createToken("test", List.of(Role.USER, Role.ADMIN));
        Claims body = Jwts.parser().setSigningKey(SECRET_BYTES).parseClaimsJws(token).getBody();

        assertThat(body.getSubject()).isEqualTo("test");
        assertThat(body.getExpiration().getTime() - body.getIssuedAt().getTime()).isEqualTo(DURATION_SECONDS * 1000);
        assertThat(body.getExpiration().getTime() - new Date().getTime()).isCloseTo(DURATION_SECONDS * 1000,
                within(5000L));
        assertThat(body.get("authorities")).isInstanceOf(List.class);
        assertThat(((List<?>) body.get("authorities")).stream().map(Object::toString).toList()).contains(
                Role.USER.toAuthority(), Role.ADMIN.toAuthority());
    }

    @Test
    void getAuthentication() {
        Date now = new Date();
        Claims claims = Jwts.claims();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() - 5000))
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();

        Claims claims2 = Jwts.claims();
        String token2 = Jwts.builder()
                .setClaims(claims2)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 5000))
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();

        Claims claims3 = Jwts.claims().setSubject("world");
        claims3.put("authorities", List.of(Role.USER.toAuthority()));
        String token3 = Jwts.builder()
                .setClaims(claims3)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 5000))
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();

        Claims claims4 = Jwts.claims().setSubject("hello");
        String token4 = Jwts.builder()
                .setClaims(claims4)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 5000))
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();

        Optional<Authentication> authentication = this.jwtAuthenticationProvider.getAuthentication(token);
        Optional<Authentication> authentication2 = this.jwtAuthenticationProvider.getAuthentication(token2);
        Optional<Authentication> authentication3 = this.jwtAuthenticationProvider.getAuthentication(token3);
        Optional<Authentication> authentication4 = this.jwtAuthenticationProvider.getAuthentication(token4);
        Optional<Authentication> authentication5 = this.jwtAuthenticationProvider.getAuthentication(
                this.jwtAuthenticationProvider.createToken("test", List.of(Role.USER, Role.ADMIN)));

        assertThat(authentication).isEmpty();
        assertThat(authentication2).isEmpty();

        assertThat(authentication3.orElseThrow().isAuthenticated()).isTrue();
        assertThat(((UserDetails) authentication3.orElseThrow().getPrincipal()).getUsername()).isEqualTo("world");
        assertThat(authentication3.orElseThrow()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()).containsExactly(Role.USER.toAuthority());

        assertThat(authentication4.orElseThrow().isAuthenticated()).isTrue();
        assertThat(((UserDetails) authentication4.orElseThrow().getPrincipal()).getUsername()).isEqualTo("hello");
        assertThat(authentication4.orElseThrow()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()).hasSize(0);

        assertThat(authentication5.orElseThrow().isAuthenticated()).isTrue();
        assertThat(((UserDetails) authentication5.orElseThrow().getPrincipal()).getUsername()).isEqualTo("test");
        assertThat(authentication5.orElseThrow()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList()).containsExactly(Role.USER.toAuthority(), Role.ADMIN.toAuthority());
    }

    @Test
    void validateToken() {
        Date now = new Date();
        Claims claims = Jwts.claims();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() - 5000))
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();

        Claims claims2 = Jwts.claims();
        String token2 = Jwts.builder()
                .setClaims(claims2)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 5000))
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();

        boolean test = this.jwtAuthenticationProvider.validateToken(null);
        boolean test2 = this.jwtAuthenticationProvider.validateToken("");
        boolean test3 = this.jwtAuthenticationProvider.validateToken("hi");
        boolean test4 = this.jwtAuthenticationProvider.validateToken(
                this.jwtAuthenticationProvider.createToken(null, new ArrayList<>()));
        boolean test5 = this.jwtAuthenticationProvider.validateToken(token);
        boolean test6 = this.jwtAuthenticationProvider.validateToken(token2);

        assertThat(test).isFalse();
        assertThat(test2).isFalse();
        assertThat(test3).isFalse();
        assertThat(test4).isTrue();
        assertThat(test5).isFalse();
        assertThat(test6).isTrue();
    }
}
