package com.yytech.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public Cat cat() {
        return new Cat();
    }

    @Bean
    public Dog dog() {
        return new Dog();
    }

    @Bean
    public StudentManager studentManager() {
        return new StudentManager();
    }

}
