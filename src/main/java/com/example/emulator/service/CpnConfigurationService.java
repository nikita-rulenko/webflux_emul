package com.example.emulator.service;

import com.example.emulator.dto.Cpn;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для загрузки и хранения конфигурации CPN из JSON файла
 */
@Service
public class CpnConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(CpnConfigurationService.class);
    private static final String CONFIG_FILE = "cpn-list.json";
    
    private final ObjectMapper objectMapper;
    private final List<Cpn> cpnList;

    public CpnConfigurationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.cpnList = new ArrayList<>(); // Initialize final field in constructor
    }

    @PostConstruct
    public void loadConfiguration() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new IllegalStateException("Cannot find " + CONFIG_FILE + " in classpath");
            }
            List<Cpn> loadedCpns = objectMapper.readValue(is, new TypeReference<List<Cpn>>() {});
            this.cpnList.addAll(loadedCpns);
            log.info("Successfully loaded {} CPNs from configuration", this.cpnList.size());
        } catch (Exception e) {
            log.error("Failed to load configuration", e);
            throw new IllegalStateException("Could not load configuration", e);
        }
    }

    /**
     * Возвращает все CPN из конфигурации как Flux
     */
    public Flux<Cpn> getAllCpns() {
        return Flux.fromIterable(cpnList);
    }
}
