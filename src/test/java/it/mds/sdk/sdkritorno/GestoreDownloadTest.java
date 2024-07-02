/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.sdkritorno;

import it.mds.sdk.connettoremds.ConnettoreMds;
import it.mds.sdk.connettoremds.enums.EsitoDownloadEnum;
import it.mds.sdk.connettoremds.enums.EsitoElaborazioneEnum;
import it.mds.sdk.connettoremds.exception.ConnettoreMdsException;
import it.mds.sdk.connettoremds.modelli.EsitoDownloadMds;
import it.mds.sdk.connettoremds.modelli.EsitoElaborazioneMds;
import it.mds.sdk.connettoremds.modelli.ResponseDownload;
import it.mds.sdk.connettoremds.modelli.ResponseEsitoUpload;
import it.mds.sdk.gestoreesiti.GestoreLogElaborazione;
import it.mds.sdk.gestoreesiti.modelli.InfoElaborazione;
import it.mds.sdk.gestoreesiti.modelli.InfoRun;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.sdkritorno.model.EsitiDownloadSelezionati;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@MockitoSettings(strictness = Strictness.LENIENT)
class GestoreDownloadTest {

    @Mock
    private GestoreFile gestoreFileMock;

    @Mock
    private GestoreLogElaborazione gestoreLogElaborazione;

    @Mock
    private ConnettoreMds proxyMds;

    @InjectMocks
    @Spy
    private GestoreDownload gestoreDownload;

    private List<InfoElaborazione> infoList;

    @BeforeEach
    void initInfo() {
        InfoElaborazione infoMX00 = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(EsitoElaborazioneEnum.MX00.getCodiceErrore())
                .withIdUpload("inputMX00")
                .build();
        InfoElaborazione infoMX01 = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(EsitoElaborazioneEnum.MX01.getCodiceErrore())
                .withIdUpload("inputMX01")
                .build();
        InfoElaborazione infoMX20 = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(EsitoElaborazioneEnum.MX20.getCodiceErrore())
                .withIdUpload("inputMX20")
                .build();
        InfoElaborazione infoMX21 = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(EsitoElaborazioneEnum.MX21.getCodiceErrore())
                .withStatoDownloadFus(GestoreDownload.RICHIESTO)
                .withIdUpload("inputMX21")
                .build();
        InfoElaborazione infoMX22 = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(EsitoElaborazioneEnum.MX22.getCodiceErrore())
                .withStatoDownloadFus(GestoreDownload.DISPONIBILE)
                .withIdUpload("inputMX22")
                .build();
        InfoElaborazione infoMX11 = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(EsitoElaborazioneEnum.MX11.getCodiceErrore())
                .withIdUpload("inputMX11")
                .build();
        InfoElaborazione infoNull = InfoElaborazione.builder()
                .withEsitoElaborazioneMds(null)
                .withIdUpload("inputNull")
                .build();
        infoList = new ArrayList<>();
        infoList.add(infoMX00);
        infoList.add(infoMX01);
        infoList.add(infoMX11);
        infoList.add(infoMX20);
        infoList.add(infoMX21);
        infoList.add(infoMX22);
        infoList.add(infoNull);
    }
    @Test
    void selettoreEsitoDownload_OK() {
        List<String> esitoList = new ArrayList<>();
        List<String> downloadList = new ArrayList<>();
        esitoList.add("inputMX00");
        esitoList.add("inputMX01");
        esitoList.add("inputNull");
        downloadList.add("inputMX21");
        EsitiDownloadSelezionati esitiDownloadSelezionatiExp = new EsitiDownloadSelezionati(esitoList, downloadList);

        EsitiDownloadSelezionati esitiDownloadSelezionatiAct = gestoreDownload.selettoreEsitoDownload(infoList);

        Assertions.assertEquals(esitiDownloadSelezionatiExp, esitiDownloadSelezionatiAct);
    }

    @Test
    void getStatoElaborazioneMdsNewTest() throws ConnettoreMdsException {

        List<String> idsUpload = List.of("1");
        InfoElaborazione infoElaborazione = Mockito.mock(InfoElaborazione.class);
        List<InfoElaborazione> infoElaborazioneList = List.of(infoElaborazione);
        Mockito.doReturn(infoElaborazione).when(gestoreLogElaborazione).recuperaInfoElaborazione(any());
        gestoreDownload.getStatoElaborazioneMdsNew(
                idsUpload,
                "nomeFlusso",
                "idClient"
        );
   }

    @Test
    void getStatoElaborazioneMdsNewTest_Else() throws ConnettoreMdsException {
        EsitoElaborazioneMds esitoElaborazioneMds = EsitoElaborazioneMds
                .builder()
                .withEsitoElaborazione(EsitoElaborazioneEnum.MX21)
                .withIdUpload("1")
                .withNomeFile("nomeFile")
                .build();

        ResponseEsitoUpload responseEsitoUpload = new ResponseEsitoUpload(
                null,
                "",
                List.of(esitoElaborazioneMds),
                "p"
                );

        List<String> idsUpload = List.of("1");
        InfoElaborazione infoElaborazione = Mockito.mock(InfoElaborazione.class);
        List<InfoElaborazione> infoElaborazioneList = List.of(infoElaborazione);
        Mockito.doReturn(infoElaborazione).when(gestoreLogElaborazione).recuperaInfoElaborazione(any());
        Mockito.doReturn(responseEsitoUpload).when(gestoreDownload).getEsitoUpload(any());
        Mockito.doReturn(infoElaborazione).when(gestoreLogElaborazione).creaFileLog(any());

        gestoreDownload.getStatoElaborazioneMdsNew(
               idsUpload,
               "nomeFlusso",
               "idClient"
        );
    }

    @Test
    void startThread() throws ConnettoreMdsException {
        InfoElaborazione infoElaborazione = Mockito.mock(InfoElaborazione.class);
        EsitoDownloadMds esitoElaborazioneMds = EsitoDownloadMds.builder()
                .withIdUpload(null)
                .withTipoFile("3")
                .withEsitoDownload(EsitoDownloadEnum.DF01)
                .withNomeFile("nomeFile")
                .build();

        ResponseDownload responseDownload = new ResponseDownload("", "", List.of(esitoElaborazioneMds));
        Mockito.doReturn(responseDownload).when(gestoreDownload).getResponseDownload(any());
        Mockito.doNothing().when(gestoreDownload).creaFileLog(any());

        var list = List.of("1");
        var infoList = List.of(infoElaborazione);
        gestoreDownload.startThread(list,infoList);
    }

    @Test
    void startThread_If() throws ConnettoreMdsException {
        InfoElaborazione infoElaborazione = new InfoElaborazione("1",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);
        EsitoDownloadMds esitoElaborazioneMds = EsitoDownloadMds.builder()
                .withIdUpload("1")
                .withTipoFile("3")
                .withEsitoDownload(EsitoDownloadEnum.DF01)
                .withNomeFile("nomeFile")
                .build();

        ResponseDownload responseDownload = new ResponseDownload("lol", "", List.of(esitoElaborazioneMds));
        Mockito.doReturn(responseDownload).when(gestoreDownload).getResponseDownload(any());
        Mockito.doNothing().when(gestoreDownload).creaFileLog(any());

        var list = List.of("1");
        var infoList = List.of(infoElaborazione);
        gestoreDownload.startThread(list,infoList);
    }

}
