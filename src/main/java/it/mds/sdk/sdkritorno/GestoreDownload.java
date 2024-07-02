/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.sdkritorno;

import it.mds.sdk.connettoremds.enums.EsitoDownloadEnum;
import it.mds.sdk.connettoremds.exception.ConnettoreMdsException;
import it.mds.sdk.connettoremds.modelli.EsitoElaborazioneMds;
import it.mds.sdk.connettoremds.modelli.ResponseDownload;
import it.mds.sdk.connettoremds.modelli.ResponseEsitoUpload;
import it.mds.sdk.sdkritorno.conf.Configurazione;
import it.mds.sdk.connettoremds.ConnettoreMds;
import it.mds.sdk.connettoremds.enums.EsitoElaborazioneEnum;
import it.mds.sdk.connettoremds.modelli.EsitoDownloadMds;
import it.mds.sdk.gestoreesiti.GestoreLogElaborazione;
import it.mds.sdk.gestoreesiti.modelli.InfoElaborazione;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.sdkritorno.model.EsitiDownloadSelezionati;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class GestoreDownload {
    private final GestoreFile gestoreFile;
    private final GestoreLogElaborazione gestoreLogElaborazione;
    private final ConnettoreMds proxyMds;
    private final Configurazione config;
    public static final String RICHIESTO ="RICHIESTO";
    public static final String DISPONIBILE = "DISPONIBILE";

    public GestoreDownload(final GestoreFile gestoreFile,
                           final GestoreLogElaborazione gestoreLogElaborazione,
                           final ConnettoreMds proxyMds) {
        this.gestoreFile = gestoreFile;
        this.gestoreLogElaborazione = gestoreLogElaborazione;
        this.proxyMds = proxyMds;
        this.config = new Configurazione();
    }

    public List<InfoElaborazione> getStatoElaborazioneMdsNew(final List<String> idsUpload,String nomeFlusso,
                                                           String idClient)
    {
        List<InfoElaborazione> fileElaborazioneList = recuperaCreaLog(idsUpload, nomeFlusso, idClient);
        List<String> idUploadPerDownload = callEsitiCreaLog(fileElaborazioneList);
        //Thread per la gestione asincrona del download FUS
        startThread(idUploadPerDownload, fileElaborazioneList);

        return fileElaborazioneList;
    }

    private List<InfoElaborazione> recuperaCreaLog(final List<String> idsUpload, String nomeFlusso,
                                                   String idClient) {
        List<InfoElaborazione> fileElaborazioneList = new ArrayList<>();
        List<String> idUploadSenzaLog = new ArrayList<>();
        //Check esistenza file LOG
        for (String idUpload : idsUpload) {
            if (esisteFileLog(idUpload)) {
                log.debug("Il file log per idUpload {} esiste, viene recuparata infoElaborazione", idUpload);
                var infoRecupata = gestoreLogElaborazione.recuperaInfoElaborazione(idUpload);
                log.trace("Info letta da file per idupload {} : {}", idUpload, infoRecupata);
                fileElaborazioneList.add(infoRecupata);
            } else {
                log.debug("Il file log per idUpload {} non esiste, viene aggiornata la lista sulla quale chiamare il " +
                                "ministero",
                        idUpload);
                idUploadSenzaLog.add(idUpload);
            }
        }
        //Creazione info vuoto
        List<InfoElaborazione> infoVuote = creaInfoVuota(idUploadSenzaLog, nomeFlusso, idClient);
        fileElaborazioneList.addAll(infoVuote);
        log.debug("InfoElaborazione per idUpload {} prima di chiamare gli esiti : {}", idsUpload, fileElaborazioneList);
        return fileElaborazioneList;
    }

    private List<String> callEsitiCreaLog(List<InfoElaborazione> fileElaborazioneList) {
        List<String> idUploadPerEsito;
        List<String> idUploadPerDownload;
        EsitiDownloadSelezionati esitiDownload = this.selettoreEsitoDownload(fileElaborazioneList);
        idUploadPerEsito = esitiDownload.getEsitoList();
        idUploadPerDownload = esitiDownload.getDownloadList();
        try {
            log.debug("Viene chiamato esito per gli id {}", idUploadPerEsito);
            if (idUploadPerEsito.isEmpty()) {
                return idUploadPerDownload;
            }
            final ResponseEsitoUpload esitoUpload = getEsitoUpload(idUploadPerEsito);
            if (esitoUpload.getErrorCode() != null && !esitoUpload.getErrorCode().equals("")) {
                log.warn("La chiamata ad esito verso il ministero per idupload {} è andata in errore", idUploadPerEsito);
                for (InfoElaborazione info : fileElaborazioneList) {
                    for (String idupload : idUploadPerEsito) {
                        if (info.getIdUpload().equals(idupload)) {
                            info.setCodiceStatoRun("OK");
                            info.setDescrizioneCodiceStatoRun(null);
                            info.setCodiceErroreElaborazioneMds(esitoUpload.getErrorCode());
                            info.setTestoErroreElaborazioneMds(esitoUpload.getErrorText());
                            gestoreLogElaborazione.creaFileLog(info);
                        }
                    }
                }
            } else {
                for (EsitoElaborazioneMds esito : esitoUpload.getEsiti()) {
                    InfoElaborazione infoDaAggiornare = null;
                    for (InfoElaborazione info : fileElaborazioneList) {
                        if (info.getIdUpload().equals(esito.getIdUpload())) {
                            infoDaAggiornare = info;
                            break;
                        }
                    }
                    if (infoDaAggiornare == null) {
                        log.warn("Idupload {} non trovato nella lista", esito.getIdUpload());
                        break;
                    }
                    infoDaAggiornare.setEsitoElaborazioneMds(esito.getEsitoElaborazione().getCodiceErrore());
                    infoDaAggiornare.setDescrizioneEsitoElaborazioneMds(esito.getEsitoElaborazione().getDescrizioneErrore());
                    infoDaAggiornare.setCodiceErroreElaborazioneMds(null);
                    infoDaAggiornare.setTestoErroreElaborazioneMds(null);
                    infoDaAggiornare.setCodiceStatoRun("OK");
                    infoDaAggiornare.setDescrizioneCodiceStatoRun(null);
                    if (esito.getEsitoElaborazione().getCodiceErrore().equals(EsitoElaborazioneEnum.MX21.getCodiceErrore()) || esito.getEsitoElaborazione().getCodiceErrore().equals(EsitoElaborazioneEnum.MX22.getCodiceErrore())) {
                        infoDaAggiornare.setStatoDownloadFus(RICHIESTO);
                        idUploadPerDownload.add(esito.getIdUpload());
                    }
                    if (esito.getNomeFile() != null && !esito.getNomeFile().equals("")) {
                        infoDaAggiornare.setAllegato(esito.getNomeFile());
                    }
                    log.info("InfoElaborazione da scrivere su file {}", infoDaAggiornare);
                    gestoreLogElaborazione.creaFileLog(infoDaAggiornare);
                }
            }

        } catch (Throwable ex) {
            log.error("Impossibile comunicare con MDS chiamata con idupload {}", idUploadPerEsito, ex);
            for (InfoElaborazione info : fileElaborazioneList) {
                for (String idupload : idUploadPerEsito) {
                    if (info.getIdUpload().equals(idupload)) {
                        info.setCodiceStatoRun("KO");
                        info.setDescrizioneCodiceStatoRun(ex.getMessage());
                        gestoreLogElaborazione.creaFileLog(info);
                    }
                }
            }
            return new ArrayList<>();
        }
        return idUploadPerDownload;
    }

    public ResponseEsitoUpload getEsitoUpload(List<String> idUploadPerEsito) throws ConnettoreMdsException {
        return proxyMds.getEsitoUpload(idUploadPerEsito);
    }

    public ResponseDownload getResponseDownload(List<String> idUploadPerDownload) throws ConnettoreMdsException {
        return proxyMds.downloadFus(idUploadPerDownload);
    }

    private void downloadFusCreaLog(List<String> idUploadPerDownload, List<InfoElaborazione> fileElaborazioneList) {
        if (idUploadPerDownload.isEmpty()) {
            log.debug("idUpload {} per il download vuoto, non verrà effettuata nessuna chiamata a downloadFus", idUploadPerDownload);
            return;
        }
        try {
            ResponseDownload responseDownloads = getResponseDownload(idUploadPerDownload);
            if (responseDownloads.getErrorCode() != null && !responseDownloads.getErrorCode().equals("")) {
                log.warn("Errore nella chiamata download FUS per idupload {}", idUploadPerDownload);
                for (InfoElaborazione info : fileElaborazioneList) {
                    for (String idUpload : idUploadPerDownload) {
                        if(Objects.equals(info.getIdUpload(), idUpload)){
//                        if (info.getIdUpload().equals(idUpload)) {
                            info.setCodiceStatoRun("OK");
                            info.setDescrizioneCodiceStatoRun(null);
                            info.setCodiceErroreDownloadFus(responseDownloads.getErrorCode());
                            info.setDescrizioneErroreDownloadFus(responseDownloads.getErrorText());
                            gestoreLogElaborazione.creaFileLog(info);
                        }
                    }
                }
            } else {
                for (EsitoDownloadMds esito : responseDownloads.getEsiti()) {
                    InfoElaborazione infoDaAggiornare = null;
                    for (InfoElaborazione info : fileElaborazioneList) {
                        if(Objects.equals(info.getIdUpload(), esito.getIdUpload())){
                        //if (info.getIdUpload().equals(esito.getIdUpload())) {
                            infoDaAggiornare = info;
                            break;
                        }
                    }
                    if (infoDaAggiornare == null) {
                        log.warn("Idupload {} non trovato nella lista", esito.getIdUpload());
                        break;
                    }
                    infoDaAggiornare.setCodiceStatoRun("OK");
                    infoDaAggiornare.setDescrizioneCodiceStatoRun(null);
                    infoDaAggiornare.setEsitoDownloadFus(esito.getEsitoDownload().getCodiceErrore());
                    infoDaAggiornare.setDescrizioneEsitoDownloadFus(esito.getEsitoDownload().getDescrizioneErrore());
                    if (esito.getEsitoDownload().equals(EsitoDownloadEnum.DF01)) {
                        infoDaAggiornare.setAllegato(esito.getNomeFile());
                        infoDaAggiornare.setStatoDownloadFus(DISPONIBILE);
                    }
                    log.info("InfoElaborazione {} viene salvata su file LOG", infoDaAggiornare);
                    creaFileLog(infoDaAggiornare);
                }
            }

        } catch (Throwable ex) {
            log.error("Impossibile comunicare con MDS per idUpload {}", idUploadPerDownload, ex);
            for (InfoElaborazione info : fileElaborazioneList) {
                for (String idupload : idUploadPerDownload) {
                    if (info.getIdUpload().equals(idupload)) {
                        info.setCodiceStatoRun("KO");
                        info.setDescrizioneCodiceStatoRun(ex.getMessage());
                        gestoreLogElaborazione.creaFileLog(info);
                    }
                }
            }
        }
    }

    public void creaFileLog(InfoElaborazione infoDaAggiornare) {
        gestoreLogElaborazione.creaFileLog(infoDaAggiornare);
    }

    private List<InfoElaborazione> creaInfoVuota(List<String> idUploadList, String nomeFlusso, String idClient) {
        List<InfoElaborazione> infoList = new ArrayList<>();
        for (String idUpload : idUploadList) {
            InfoElaborazione info = InfoElaborazione.builder()
                    .withIdUpload(idUpload)
                    .withNomeFlusso(nomeFlusso)
                    .withIdClient(idClient)
                    .withVersion(getClass().getPackage().getImplementationVersion())
                    .withTimestampElaborazione(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
                    .build();
            infoList.add(info);
        }
        return infoList;
    }

    private boolean esisteFileLog(final String idUpload) {
        try {
            final var logPath = Paths.get(config.getMonitoraggio().getLog(),idUpload + ".log");
            final var path = gestoreFile.leggiFile(logPath.toString());
            File fileEsito = new File(config.getMonitoraggio().getLog(), idUpload + ".log");
            if(!fileEsito.exists()){
                return false;
            }
            return path != null;
        } catch (IOException e) {
            return false;
        }
    }

    protected EsitiDownloadSelezionati selettoreEsitoDownload(List<InfoElaborazione> infoList) {
        List<String> esitiList = new ArrayList<>();
        List<String> downloadList = new ArrayList<>();
        infoList.forEach(i -> {
            String esito = i.getEsitoElaborazioneMds();
            String statoDownload = i.getStatoDownloadFus();
            if (esito == null) {
                log.info("Info {} ha esitoElaborazione null, verrà chiamato esito", i);
                esitiList.add(i.getIdUpload());
            } else {
                switch (esito) {
                    case "MX11":
                        log.info("Info {} è MX11, non conforme a specifica, stato finale non verrà effettuata nessuna " +
                                "operazione", i);
                        break;
                    case "MX20":
                        log.info("Info {} è MX20, non verrà effettuata nessuna operazione", i);
                        break;
                    case "MX21":
                    case "MX22":
                        if (!statoDownload.equals(DISPONIBILE)) {
                            log.info("Info {} è MX21 o MX22 e non è in stato DISPONIBILE, verrà chiamato il download", i);
                            downloadList.add(i.getIdUpload());
                        } else {
                            log.info("Info {} è MX21 o MX22 e è in stato DISPONIBILE, non verrà effettuata alcuna " +
                                            "operazione", i);
                        }
                        break;
                    default:
                        log.info("Info {} è in uno stato per cui viene chiamato esito", i);
                        esitiList.add(i.getIdUpload());
                }
            }
        });
        EsitiDownloadSelezionati esitiDownloadSelezionati = new EsitiDownloadSelezionati(esitiList, downloadList);
        log.debug("La lista degli idUpload su cui chiamare esito e download a partire da {} è:\n {}", infoList, esitiDownloadSelezionati);
        return esitiDownloadSelezionati;
    }

    public void startThread(List<String> idUploadPerDownload, List<InfoElaborazione> fileElaborazioneList)
    {
        Runnable runnable = () -> downloadFusCreaLog(idUploadPerDownload, fileElaborazioneList);
        Thread thread = createThread(runnable);
        thread.start();
    }
    public Thread createThread(Runnable runnable) {
        return new Thread(runnable);
    }
}
