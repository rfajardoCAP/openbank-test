package com.capgemini.test.code.config;

import com.capgemini.test.code.CodeApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Activa los clientes Feign. Está aquí y no en {@link CodeApplication} para que los tests de slice
 * (por ejemplo {@code @WebMvcTest}) no intenten crear los clientes HTTP.
 */
@Configuration(proxyBeanMethods = false)
@EnableFeignClients(basePackageClasses = CodeApplication.class)
public class FeignClientsConfig {
}
