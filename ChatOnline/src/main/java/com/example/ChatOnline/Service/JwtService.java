package com.example.ChatOnline.Service;

import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class JwtService {

    @Value("${app.auth.tokenSecret}")
    private String secretKey;

    public String generateAccessToken(String userId, Set<String> authorities){
        //lua chon thuat toan ma hoa
        JWSAlgorithm algorithm = JWSAlgorithm.HS512;
        //chua thong tin mo ta token
        JWSHeader header = new JWSHeader(algorithm);

        Date issueTime = new Date();
        Date expireTime = new Date(Instant.now().plus(2, ChronoUnit.HOURS).toEpochMilli());

        String jwtId = UUID.randomUUID().toString();

        // Day la nhung gi co trong payload cua token
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issueTime(issueTime)
                .expirationTime(expireTime)
                .jwtID(jwtId)
                .claim("AUTHORITIES", authorities)
                .build();

        //payload
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey));
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }

        //Chuyen Object thanh chuoi
        return jwsObject.serialize();
    }

    public String generateRefreshToken(String userId) {
        JWSAlgorithm algorithm = JWSAlgorithm.HS512;
        JWSHeader header = new JWSHeader(algorithm);

        Date issueTime = new Date();
        Date expiredTime = new Date(Instant.now().plus(14, ChronoUnit.DAYS).toEpochMilli());

        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .jwtID(jwtId)
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(secretKey));
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }

        return jwsObject.serialize();
    }
}
