package com.shop.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinimalJsonParser {
    private final String json;
    private int index = 0;

    public MinimalJsonParser(String json) {
        this.json = json;
    }

    public Object parse() {
        skipWhitespace();
        if (index >= json.length()) return null;

        char c = json.charAt(index);

        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't') return parseToken("true", true);
        if (c == 'f') return parseToken("false", false);
        if (c == 'n') return parseToken("null", null);
        if (Character.isDigit(c) || c == '-') return parseNumber();

        throw new IllegalArgumentException("Invalid JSON at position " + index);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();
        index++;
        skipWhitespace();

        if (json.charAt(index) == '}') {
            index++;
            return map;
        }

        while (index < json.length()) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();

            if (json.charAt(index) != ':') throw new IllegalArgumentException("Expected ':'");
            index++;

            Object value = parse();
            map.put(key, value);

            skipWhitespace();
            char c = json.charAt(index);
            if (c == '}') {
                index++;
                break;
            }
            if (c != ',') throw new IllegalArgumentException("Expected ',' or '}'");
            index++;
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        index++;
        skipWhitespace();

        if (json.charAt(index) == ']') {
            index++;
            return list;
        }

        while (index < json.length()) {
            list.add(parse());
            skipWhitespace();

            char c = json.charAt(index);
            if (c == ']') {
                index++;
                break;
            }
            if (c != ',') throw new IllegalArgumentException("Expected ',' or ']'");
            index++;
        }
        return list;
    }

    private String parseString() {
        index++;
        StringBuilder sb = new StringBuilder();

        while (index < json.length()) {
            char c = json.charAt(index++);
            if (c == '"') return sb.toString();

            sb.append(c);
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    private Number parseNumber() {
        int start = index;
        while (index < json.length() &&
                (Character.isDigit(json.charAt(index)) || json.charAt(index) == '.' || json.charAt(index) == '-')) {
            index++;
        }
        String numStr = json.substring(start, index);
        if (numStr.contains(".")) {
            return Double.parseDouble(numStr);
        }
        return Long.parseLong(numStr);
    }

    private Object parseToken(String token, Object returnValue) {
        if (json.substring(index).startsWith(token)) {
            index += token.length();
            return returnValue;
        }
        throw new IllegalArgumentException("Invalid token at " + index);
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }
}