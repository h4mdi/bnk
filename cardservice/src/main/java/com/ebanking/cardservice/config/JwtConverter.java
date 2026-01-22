package com.ebanking.cardservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(JwtConverter.class);

    @Value("${jwt.auth.converter.principal-attribute}")
    private String principalAttribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
        logger.debug("Converting JWT. Subject: {}, Issuer: {}", jwt.getSubject(), jwt.getIssuer());
        logger.debug("Resource ID configured: {}", resourceId);
        
        // Extract realm roles
        Stream<GrantedAuthority> realmRoles = Optional.ofNullable(jwt.getClaimAsMap("realm_access"))
                .map(map -> (Collection<String>) map.get("roles"))
                .orElse(Collections.emptyList())
                .stream()
                .map(role -> {
                    String authority = "ROLE_" + role.toUpperCase();
                    logger.debug("Extracted realm role: {} -> {}", role, authority);
                    return new SimpleGrantedAuthority(authority);
                });

        // Extract client (resource) roles
        Collection<GrantedAuthority> clientRolesCollection = extractResourceRoles(jwt);
        Stream<GrantedAuthority> clientRoles = clientRolesCollection.stream();

        // Combine realm + client roles
        Collection<GrantedAuthority> authorities = Stream.concat(realmRoles, clientRoles)
                .collect(Collectors.toSet());

        logger.info("JWT converted successfully. Principal: {}, Authorities: {}", 
                getPrincipalName(jwt), 
                authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalName(jwt));
    }

    /**
     * Determine the principal name from JWT claims.
     */
    private String getPrincipalName(Jwt jwt) {
        if (principalAttribute != null && jwt.getClaim(principalAttribute) != null) {
            return jwt.getClaim(principalAttribute);
        }
        return jwt.getSubject(); // fallback to subject
    }

    /**
     * Extract roles assigned to the client/resource.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        
        logger.debug("Resource access claim: {}", resourceAccess);
        logger.debug("Looking for resource: {}", resourceId);

        if (resourceAccess == null) {
            logger.warn("resource_access claim is null");
            return Set.of();
        }
        
        if (resourceAccess.get(resourceId) == null) {
            logger.warn("Resource '{}' not found in resource_access. Available resources: {}", 
                    resourceId, resourceAccess.keySet());
            return Set.of();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);
        Collection<String> roles = (Collection<String>) resource.get("roles");

        if (roles == null) {
            logger.warn("No roles found for resource '{}'", resourceId);
            return Set.of();
        }

        logger.debug("Found {} roles for resource '{}': {}", roles.size(), resourceId, roles);

        // Convert Keycloak roles like "student.read" -> Spring Security ROLE_STUDENT.READ
        // or "role_user" -> Spring Security ROLE_USER
        return roles.stream()
                .map(role -> {
                    // Keep dots in role names (e.g., student.read -> ROLE_STUDENT.READ)
                    String authority = "ROLE_" + role.replace("role_", "").toUpperCase();
                    logger.debug("Converted role: {} -> {}", role, authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toSet());
    }
}
