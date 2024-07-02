/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.sdkritorno;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
@ComponentScan({"it.mds.sdk.rest.persistence.entity",
                "it.mds.sdk.sdkritorno.service","it.mds.sdk.gestorefile",
                "it.mds.sdk.gestoreesiti", "it.mds.sdk.connettoremds",
                "it.mds.sdk.sdkritorno.controller","it.mds.sdk.sdkritorno.service",
                "it.mds.sdk.sdkritorno"})
@Slf4j
@OpenAPIDefinition(info=@Info(title = "SDK Ministero Della Salute - SDK Ritorno", version = "0.0.2-SNAPSHOT", description = "SDK Di Ritorno"))
public class SdkRitorno {
    public static void main(String[] args) {
        SpringApplication.run(SdkRitorno.class, args);
    }
}
