/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.sdkritorno.controller;

import it.mds.sdk.rest.api.controller.sdkritorno.MonitoraggioService;
import it.mds.sdk.rest.persistence.entity.sdkritorno.RisultatoElaborazione;
import it.mds.sdk.sdkritorno.service.FlussoRitornoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Configuration
@Slf4j
public class MonitoraggioServiceRest implements MonitoraggioService {

    private final FlussoRitornoService flussoRitornoService;

    @Autowired
    public MonitoraggioServiceRest(@Qualifier("flussoRitornoService") final FlussoRitornoService flussoRitornoService) {
        this.flussoRitornoService = flussoRitornoService;
    }

    @Override
    @GetMapping("/v1/monitoraggio/gestoreesitigaf")
    public ResponseEntity<RisultatoElaborazione> getStatoElaborazioni(List<String> idsUpload, String nomeFlusso, String idClient) {
        RisultatoElaborazione response = new RisultatoElaborazione();
        response.setInfoElaborazioneList(flussoRitornoService.getStatoElaborazioni(idsUpload, nomeFlusso,idClient));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
