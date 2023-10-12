package it.mds.sdk.sdkritorno.service;

import it.mds.sdk.connettoremds.ConnettoreMds;
import it.mds.sdk.gestoreesiti.GestoreLogElaborazione;
import it.mds.sdk.gestoreesiti.modelli.InfoElaborazione;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.gestorefile.factory.GestoreFileFactory;
import it.mds.sdk.sdkritorno.GestoreDownload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("flussoRitornoService")
@Slf4j
public class FlussoRitornoService {

    private ConnettoreMds connettoreMds;

    @Autowired
    public FlussoRitornoService(@Qualifier("connettoreMds") final ConnettoreMds connettoreMds) {
        this.connettoreMds = connettoreMds;
    }

    public List<InfoElaborazione> getStatoElaborazioni(List<String> idsUpload,String nomeFlusso, String idClient) {
        log.debug("getStatoElaboorazioni :start per idUpload {}", idsUpload);
        GestoreFile gestoreFile= GestoreFileFactory.getGestoreFile("DEFAULT");
        GestoreLogElaborazione gestoreLogElaborazione = getGestoreElaborazioneFromGestoreFile(gestoreFile);
        GestoreDownload gestoreDownload = getGestoreDownload(gestoreFile,gestoreLogElaborazione,connettoreMds);
        return gestoreDownload.getStatoElaborazioneMdsNew(idsUpload,nomeFlusso,idClient);
    }

    public GestoreDownload getGestoreDownload(GestoreFile gestoreFile, GestoreLogElaborazione gestoreLogElaborazione, ConnettoreMds connettoreMds) {
        return new GestoreDownload(gestoreFile, gestoreLogElaborazione,connettoreMds);
    }

    public GestoreLogElaborazione getGestoreElaborazioneFromGestoreFile(GestoreFile gestoreFile) {
        return new GestoreLogElaborazione(gestoreFile);
    }

}
