package it.mds.sdk.sdkritorno;

import it.mds.sdk.gestoreesiti.GestoreLogElaborazione;
import it.mds.sdk.gestoreesiti.modelli.InfoElaborazione;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.gestorefile.factory.GestoreFileFactory;
import it.mds.sdk.sdkritorno.service.FlussoRitornoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
@MockitoSettings(strictness = Strictness.LENIENT)
public class FlussoRitornoServiceTest {

    private MockedStatic<GestoreFileFactory> mockedStatic;
    private final GestoreFile gestoreFile = mock(GestoreFile.class);
    private final GestoreDownload gestoreDownload = Mockito.mock(GestoreDownload.class);
    private final GestoreLogElaborazione gle = Mockito.mock(GestoreLogElaborazione.class);

    @InjectMocks
    @Spy
    FlussoRitornoService service;

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
        mockedStatic = mockStatic(GestoreFileFactory.class);
    }
    @Test
    void getStatoElaborazioniTest(){
        var list = List.of(Mockito.mock(InfoElaborazione.class));
        mockedStatic.when(() -> GestoreFileFactory.getGestoreFile("DEFAULT")).thenReturn(gestoreFile);
        doReturn(gle).when(service).getGestoreElaborazioneFromGestoreFile(any());
        doReturn(gestoreDownload).when(service).getGestoreDownload(any(), any(), any());
        doReturn(list).when(gestoreDownload).getStatoElaborazioneMdsNew(any(), any(), any());

        Assertions.assertNotNull(service.getStatoElaborazioni(any(), any(), any()));
        Assertions.assertTrue(service.getStatoElaborazioni(any(), any(), any()).size() > 0);
    }
    @AfterEach
    void closeStaticMocks(){
        mockedStatic.close();
    }
}
