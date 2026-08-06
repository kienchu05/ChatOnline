package com.example.ChatOnline.Configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
public class CustomDecoderJwt implements JwtDecoder {

    @Value("${app.auth.tokenSecret}")
    private String secretKey;

    //object thuc hien viec decode
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @PostConstruct // sau khi Spring tao Bean va inject dependency xong thi chi chay method nay 1 lan duy nhat
    public void init(){
        if(Objects.isNull(nimbusJwtDecoder)){
            // NimbusJwtDecoder khong nhan String ma nhan SecretKey nen can chuyen doi sang SecretKeySpec
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(secretKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
    }

    @Override
    public Jwt decode(String token) throws JwtException {
            return nimbusJwtDecoder.decode(token);
    }
}
