package com.i2i.evrencell.aom.configuration;

import com.i2i.evrencell.voltdb.VoltdbOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VoltConfiguration {
    @Bean
    public VoltdbOperator voltdbOperator() {
        return new VoltdbOperator();
    }
}
