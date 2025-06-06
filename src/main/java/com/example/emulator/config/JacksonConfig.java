package com.example.emulator.config;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // for Java 8 date/time types
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        // Используем 4 пробела для отступа
        DefaultIndenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        prettyPrinter.indentObjectsWith(indenter);
        prettyPrinter.indentArraysWith(indenter);
        
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);

        // Настройка для корректной обработки слешей без экранирования
        objectMapper.getFactory().setCharacterEscapes(new com.fasterxml.jackson.core.io.CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                // Не экранировать никаких дополнительных ASCII символов
                return com.fasterxml.jackson.core.io.CharacterEscapes.standardAsciiEscapesForJSON();
            }

            @Override
            public com.fasterxml.jackson.core.SerializableString getEscapeSequence(int ch) {
                // Не экранировать слеш
                if (ch == '/') {
                    return null;
                }
                return null;
            }
        });

        return objectMapper;
    }

    @Bean
    public Jackson2JsonEncoder jackson2JsonEncoder(ObjectMapper objectMapper) {
        return new Jackson2JsonEncoder(objectMapper);
    }
}
