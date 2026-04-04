package com.recruting.demo.signature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Component
public class JsonCanonicalizer {

    private final ObjectMapper objectMapper;

    public JsonCanonicalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] canonicalize(Object payload) {
        JsonNode root = objectMapper.valueToTree(payload);
        String canonicalJson = toCanonicalJson(root);
        return canonicalJson.getBytes(StandardCharsets.UTF_8);
    }

    private String toCanonicalJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return "null";
        }
        if (node.isObject()) {
            List<String> names = new ArrayList<>();
            Iterator<String> fields = node.fieldNames();
            fields.forEachRemaining(names::add);
            names.sort(Comparator.naturalOrder());

            StringBuilder builder = new StringBuilder();
            builder.append('{');
            boolean first = true;
            for (String name : names) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(escape(name));
                builder.append(':');
                builder.append(toCanonicalJson(node.get(name)));
            }
            builder.append('}');
            return builder.toString();
        }
        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(toCanonicalJson(node.get(i)));
            }
            builder.append(']');
            return builder.toString();
        }
        if (node.isTextual()) {
            return escape(node.textValue());
        }
        if (node.isBoolean()) {
            return Boolean.toString(node.booleanValue());
        }
        if (node.isNumber()) {
            return canonicalizeNumber(node);
        }
        throw new SignatureException("Unsupported JSON node for canonicalization: " + node.getNodeType());
    }

    private String canonicalizeNumber(JsonNode node) {
        double asDouble = node.doubleValue();
        if (!Double.isFinite(asDouble)) {
            throw new SignatureException("NaN/Infinity are not allowed in canonical payload");
        }
        if (node.isIntegralNumber()) {
            return node.bigIntegerValue().toString();
        }
        BigDecimal decimal = node.decimalValue().stripTrailingZeros();
        if (decimal.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        return decimal.toPlainString();
    }

    private String escape(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new SignatureException("Cannot escape JSON string", ex);
        }
    }
}
