package com.busapi.core.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Jackson 3.x (tools.jackson) için XSS sanitizer deserializer.
 * Tüm String değerlerden HTML/script taglarını temizler.
 */
public class HtmlSanitizer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getString();

        if (value != null) {
            return Jsoup.clean(value, Safelist.none()).trim();
        }
        return null;
    }
}