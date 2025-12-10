package com.tripjoy.api.configuration.security;

import com.nimbusds.jose.JOSEException;
import com.tripjoy.api.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signer-key}")
    private String signerKey;

    @Autowired
    private JwtUtils jwtUtils;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {

        try {
            jwtUtils.verifyToken(token, false);
        } catch (JOSEException | ParseException | AppException e) {
            throw new JwtException(e.getMessage());
        }

        if (nimbusJwtDecoder == null) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
