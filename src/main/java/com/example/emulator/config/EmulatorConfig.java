package com.example.emulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация эмулятора, загружаемая из application.yml
 * 
 * @ConfigurationProperties автоматически привязывает свойства из файла конфигурации
 * к полям класса, используя префикс "emulator"
 * 
 * @Validated включает валидацию полей при загрузке конфигурации
 */
@ConfigurationProperties(prefix = "emulator")
public class EmulatorConfig {
    
    /**
     * Настройки задержки ответа.
     * Используется record для создания иммутабельного объекта с автогенерацией
     * конструктора, геттеров, equals, hashCode и toString.
     * 
     * @param min минимальная задержка в миллисекундах
     * @param max максимальная задержка в миллисекундах
     */
    private Delay delay = new Delay();

    public Delay getDelay() {
        return delay;
    }

    public void setDelay(Delay delay) {
        if (delay != null && delay.getMin() <= delay.getMax()) {
            this.delay = delay;
        } else {
            throw new IllegalArgumentException("Некорректные настройки задержки");
        }
    }

    /**
     * Внутренний класс для настроек задержки.
     * Хранит минимальное и максимальное значения задержки.
     */
    public static class Delay {
        private int min;
        private int max;

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            if (min >= 0) {
                this.min = min;
            } else {
                throw new IllegalArgumentException("Минимальная задержка не может быть отрицательной");
            }
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            if (max <= 10000) {
                this.max = max;
            } else {
                throw new IllegalArgumentException("Максимальная задержка не может превышать 10 секунд");
            }
        }
    }
}
