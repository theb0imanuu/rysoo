package com.rysoo.api.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String sub = jwt.getSubject();
        UUID userId = UUID.fromString(sub);

        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = "unknown@user.rysoo";
        }

        AuthenticatedUser principal = new AuthenticatedUser(userId, email);

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
