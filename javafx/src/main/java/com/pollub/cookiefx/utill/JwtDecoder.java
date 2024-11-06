package com.pollub.cookiefx.utill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;
import java.util.ArrayList;

public class JwtDecoder {

    public static JsonNode decodePayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Nieprawidłowy format tokenu JWT.");
        }

        String payload = parts[1];
        int paddingLength = 4 - (payload.length() % 4);
        if (paddingLength > 0 && paddingLength < 4) {
            payload += "=".repeat(paddingLength);
        }

        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String decodedPayload = new String(decodedBytes, "UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(decodedPayload);
    }

    public static List<String> getUserRoles(String token) throws Exception {
        JsonNode payload = decodePayload(token);
        JsonNode rolesNode = payload.get("roles");
        if (rolesNode == null || !rolesNode.isArray()) {
            return new ArrayList<>();
        }

        List<String> roles = new ArrayList<>();
        for (JsonNode roleNode : rolesNode) {
            JsonNode authorityNode = roleNode.get("authority");
            if (authorityNode != null && authorityNode.isTextual()) {
                roles.add(authorityNode.asText());
            }
        }
        return roles;
    }

    public static void printDecodedPayload(String token) throws Exception {
        JsonNode payload = decodePayload(token);
        System.out.println("Decoded Payload: " + payload.toPrettyString());
    }
}
