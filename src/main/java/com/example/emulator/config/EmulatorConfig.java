package com.example.emulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "emulator")
public class EmulatorConfig {
    // Настройки задержки
    private Delay delay = new Delay();
    // Путь к файлу с JSON-ответом
    private String responseConfig;

    public Delay getDelay() {
        return delay;
    }

    public void setDelay(Delay delay) {
        this.delay = delay;
    }

    public String getResponseConfig() {
        return responseConfig;
    }

    public void setResponseConfig(String responseConfig) {
        this.responseConfig = responseConfig;
    }

    public static class Delay {
        private int min;
        private int max;

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }
}
