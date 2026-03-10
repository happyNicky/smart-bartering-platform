package com.finalyear.liwatch.userManagement.service;

import com.finalyear.liwatch.userManagement.DTO.LoginUserDto;
import com.finalyear.liwatch.userManagement.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private String secretKey=null;

    public  boolean isTokenValid(String token  , UserDetails userDetails) {
        final  String username= extractUserName(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token) ;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(LoginUserDto user) {

        Map<String,Object> claims= new HashMap<>();
        return Jwts
                .builder()
                .addClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer("codingBuddy")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+60*10*1000))
                .signWith(generateKey())
                .compact();

    }
    public String generateToken(String email) {
        Map<String,Object> claims= new HashMap<>();
        return Jwts.builder()
                .addClaims(claims)
                .setSubject(email)
                .setIssuer("codingBuddy")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 10 * 1000))
                .signWith(generateKey())
                .compact();
    }
    public SecretKey generateKey()
    {
        byte[] decode= Base64.getDecoder().decode(getSecretKey());
        return Keys.hmacShaKeyFor(decode);
    }

    public String getSecretKey() {
        return secretKey="3oJ1mlIQ6khoslsTEasRQQ3nENHj5rrMsDtS2Cntlyn";
    }

    public String extractUserName(String token) {
        return extractClaim(token , Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver) {
        Claims claims = extractClaim(token);
        return claimResolver.apply(claims);
    }
    private Claims extractClaim(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(generateKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }
}
