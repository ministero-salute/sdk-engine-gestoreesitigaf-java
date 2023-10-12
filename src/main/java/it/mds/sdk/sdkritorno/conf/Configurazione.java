package it.mds.sdk.sdkritorno.conf;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Getter
public class Configurazione {
    private final Monitoraggio monitoraggio;

    public Configurazione() {
        this(leggiConfigurazione("config.properties"));
    }

    public Configurazione(final Properties conf) {
        log.debug("Properties salvate\n");
        this.monitoraggio = Monitoraggio.builder()
                .withFus(conf.getProperty("monitoraggio.fus", ""))
                .withLog(conf.getProperty("monitoraggio.log", ""))
                .build();
    }

    @Value
    @Builder(setterPrefix = "with")
    public static class Monitoraggio {
        String fus;
        String log;
    }

    private static Properties leggiConfigurazione(final String nomeFile) {
        final Properties prop = new Properties();
        if(Configurazione.class.getClassLoader() == null){
            log.trace("{}.getClassLoader() is null", Configurazione.class);
            throw new NullPointerException();
        }
        try (final InputStream is = Configurazione.class.getClassLoader().getResourceAsStream(nomeFile)) {
            prop.load(is);
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }
        return prop;
    }

}
