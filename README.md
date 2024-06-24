# **1. Introduzione**

## ***1.1 Obiettivi del documento***

Il Ministero della Salute (MdS) metterà a disposizione degli Enti, da cui riceve dati, applicazioni SDK specifiche per flusso logico e tecnologie applicative (Java, PHP e C#) per verifica preventiva (in casa Ente) della qualità del dato prodotto.

![](img/Aspose.Words.aeaba196-b5f9-465a-a66e-53817f224f99.002.png)

Nel presente documento sono fornite la struttura e la sintassi dei tracciati previsti dalla soluzione SDK per avviare il proprio processo elaborativo.

Gli obiettivi del documento sono:

- fornire una descrizione funzionale chiara e consistente dei tracciati gestiti da SDK
- fornire una descrizione dei processi gestiti dall’ SDK e la loro sequenza logica
- fornire una descrizione degli output scambiati fra le componenti del processo

In generale, la soluzione SDK abilita

- l’interoperabilità con il contesto tecnologico dell’Ente in cui la soluzione sarà installata
- l’acquisizione del dato da MdS

## ***1.2 Acronimi***

Nella tabella riportata di seguito sono elencati tutti gli acronimi e le definizioni adottati nel presente documento.


|**#**|**Acronimo / Riferimento**|**Definizione**|
| - | - | - |
|1|SDK|Software Development Kit|
|2|FUS|File Unico Scarti|
|3|GAF|Gestione Accoglienza Flussi|


# **2. Architettura SDK**
## ***2.1 Architettura funzionale***

Di seguito una rappresentazione del processo di monitoraggio e scaricamento del FUS dall’ente verso l’area MdS attraverso l’utilizzo dell’applicativo SDK.

![](img/Aspose.Words.aeaba196-b5f9-465a-a66e-53817f224f99.003.png)

Il processo prevede:

1.	L’applicazione sorgente (ente) invia una richiesta di verifica stato elaborazione MdS a SDK, passando una stringa di ID_UPLOAD, relativi ad una o più elaborazioni dati precedentemente richieste al MdS (tramite SDK e/o tramite processo manuale).
1.	A fronte della richiesta, l’SDK:

  a)	Per ciascun ID_UPLOAD indicato in input, verifica all’interno del proprio logging se esiste un file {ID_UPLOAD}.log e procede in base al valore del campo ESITO ELABORAZIONE MDS riscontrato. Di seguito i possibili scenari:
  -	File di logging non presente oppure presente con valore “Mx00”, “MX01” oppure “Mx99” nel campo ESITO ELABORAZIONE MDS. In tal caso passare al punto b);

  - File di logging presente e valore diverso da “Mx00”, “Mx01” e “Mx99”. In tal caso l’esito MdS è stato già ottenuto, dunque passare al punto c).

 b)	L’SDK invoca la procedura monEsiUploadXML, riceve la risposta contenente l’esito della chiamata ed aggiorna il proprio log interno valorizzando i campi previsti (per la struttura del file fare riferimento al Paragrafo 2.4). Solo nel caso in cui l’esito di monEsiUploadXML sia uguale a:

  - “Mx11”: in allegato alla response sarà presente un file con il dettaglio dell’errore sintattico della request effettuata, l’SDK copierà tale file nella cartella degli Scarti MdS (es. SDK_MONITORAGGIO ELABORAZIONE MDS/SCARTI MDS/) aggiornando inoltre il file di logging;

  - “Mx21” oppure “Mx22”: invocherà la procedura asincrona dwnldAnmSctUpload specificando il valore X come tipoFile per scaricare il FUS, e imposterà sul file di logging STATO DOWNLOAD FUS = RICHIESTO.

 c)	Risponde all’applicazione sorgente (Ente) fornendo il set di dati contenuti nel proprio log per ognuno degli ID_UPLOAD forniti in input. Nel caso in cui il campo ESITO ELABORAZIONE MDS sia uguale a “Mx21” oppure “Mx22”, sono possibili i seguenti scenari:

  - STATO DOWNLOAD FUS = RICHIESTO: il download è ancora in corso, e una volta terminato verrà copiato il file FUS all’interno della cartella dedicata (es. /SDK_MONITORAGGIO ELABORAZIONE MDS/FUS), e aggiornato il campo STATO DOWNLOAD FUS con il valore DISPONIBILE. Da notare che al termine del download, l’SDK non restituisce alcuna risposta sincrona all’ente. Sarà necessario interrogare nuovamente l’SDK con il particolare ID UPLOAD al fine di verificare che il valore del campo STATO DOWNLOAD FUS sia diventato DISPONIBILE;

  - 	STATO DOWNLOAD FUS = DISPONIBILE: il download è terminato, dunque il file FUS è già disponibile nella cartella dedicata (es. /SDK_MONITORAGGIO ELABORAZIONE MDS/FUS).



## ***2.2 Monitoraggio dello stato elaborazione MdS***

Il processo in oggetto usa la procedura denominata **monEsiUploadXML**,** la quale consente di verificare l’esito dell’elaborazione su MdS di un pacchetto di file individuati attraverso l’indicazione di un insieme di ID\_UPLOAD. Dall’immagine seguente si può osservare l’interazione tra l’SDK e il MdS:


![](img/Aspose.Words.aeaba196-b5f9-465a-a66e-53817f224f99.004.png)


L' informazione necessarie alla corretta invocazione della procedura è la seguente:

- Un insieme di **idUpload**, uno per ogni file di cui si intende monitorare lo stato di elaborazione

La procedura restituisce quanto segue:

1. **errorCode**: stringa alfanumerica di 256 caratteri rappresentante il codice identificativo dell’errore, relativo al solo servizio invocato, eventualmente riscontrato
1. **errorText**: stringa alfanumerica di 256 caratteri rappresentante il descrittivo dell’errore, relativo al solo servizio invocato,  eventualmente riscontrato
1. Un insieme delle seguenti coppie, una per ogni tipologia di file richiesto:

  a. **idUpload**: stringa numerica indicante l’identificativo univoco di un file inviato in precedenza al MdS

  b. **esitoElaborazione**: stringa alfanumerica di 4 caratteri rappresentante l’esito dell’operazione come riportato di seguito:


|**ESITO ELABORAZIONE**|**DESCRIZIONE**|
| :- | :- |
|Mx00|Upload non elaborato|
|Mx01|Upload o fornitura richiesta non esistente|
|Mx11|Struttura del file XML non conforme alle specifiche|
|Mx20|Operazione completata senza scarti e senza anomalie|
|Mx21|Operazione completata con anomali|
|Mx22|Operazione completata con scarti|
|Mx23|Incongruenza con le informazioni di upload|
|Mx99|Errore generico dell’operation|

Per ogni ID\_UPLOAD verrà effettuato l’aggiornamento dei file di log con tutti i dettagli della response. Inoltre, per gli ID\_UPLOAD il cui esito è valorizzato con Mx11 sarà presente in allegato alla response un file (IDUPLOAD\_validazione.txt) da scaricare nell’apposita cartella (SDK\_MONITORAGGIO ELABORAZIONE MDS/SCARTI MDS/).

## ***2.3 Download FUS***

Il processo in oggetto usa la procedura denominata **dwnldAnmSctUpload**, la quale consente di richiedere i file degli scarti o anomali, generati dal MdS, associati ad un insieme di ID\_UPLOAD.

Nella figura seguente è descritta l’interazione tra l’SDK e il MdS:

![](img/Aspose.Words.aeaba196-b5f9-465a-a66e-53817f224f99.005.png)

Le informazioni necessarie alla corretta invocazione della procedura sono le seguenti:

- Una serie di coppie, una per ogni ID\_UPLOAD da cui si vuole estrarre il FUS

 a. **idUpload**: stringa numerica che identifica univocamente uno degli invii effettuati al MdS (ID UPLOAD)

 b. **tipoFile**: un singolo carattere che identifica il tipo di file da richiedere, per ottenere il FUS sarà necessario valorizzare questo parametro con il carattere ‘X’. Di seguito l’insieme dei possibili valori del parametro:


|**TIPO FILE**|**DESCRIZIONE**|
| :- | :- |
|S|Scarti - File contenente i dati scartati|
|A|Scarti - File contenete i dati anomali|
|T|Tutti - Equivale a richiedere in un unico file compresso i tipi “S” e “A”|
|X|File Unico degli Scarti|

L’operation restituisce quanto segue.

1. **errorCode**: stringa alfanumerica di 256 caratteri rappresentante il codice identificativo dell’errore, relativo al solo servizio invocato, eventualmente riscontrato
1. **errorText**: stringa alfanumerica di 256 caratteri rappresentante il descrittivo dell’errore, relativo al solo servizio invocato,  eventualmente riscontrato
1. Una serie delle seguenti triple, una per ogni tipologia di file richiesto:

 a. **idUpload**: stringa numerica indicante l’identificativo univoco di un file inviato in precedenza al MdS

 b. **esitoElaborazione**: stringa alfanumerica di 4 caratteri rappresentante l’esito dell’operazione come riportato di seguito:

 |**ESITO ELABORAZIONE**|**DESCRIZIONE**|
 | :- | :- |
 |DF01|File restituito|
 |DF02|Non individuato alcun file per l’upload indicato|
 |DF03|Errore durante l’invio del pacchetto|
 |DF99|Errore generico dell’operation|

 c. **tipoFile**: un singolo carattere che identifica il tipo di file richiesto.

1. Un file in formato Zip allegato alla response per ogni ID\_UPLOAD richiesto (*IDUPLOAD*.zip). I file ottenuti verranno salvati nella cartella preposta alla conservazione del FUS (/SDK\_MONITORAGGIO ELABORAZIONE MDS/FUS)

## ***2.4 Logging***

In una cartella (es /sdk/log) verrà generato il file di log detto applicativo, ovvero il log tecnico generato dal codice sorgente del sdk. Il naming del file sarà **SDK\_MONITORAGGIO-FLUSSI.log**.

In un’altra cartella (es. /SDK\_MONITORAGGIO ELABORAZIONE MDS/LOG) verranno creati un insieme di file di log contenente il dettaglio delle elaborazioni del processo in oggetto.

Il naming del file sarà: **IDUPLOAD.log**. Nel caso di più esecuzioni consecutive con stesso id upload, il file verrà sovrascritto.


|**CAMPO**|**DESCRIZIONE**|
| :- | :- |
|ID\_UPLOAD|Identificativo di caricamento fornito da MdS (parametro input obbligatorio)|
|ID CLIENT|Identificativo con il quale viene invocato l’SDK di Ritorno da parte dell’applicazione sorgente.|
|NOME FLUSSO|Nome del flusso associato all’ID\_UPLOAD fornito in input alla request.|
|TIMESTAMP ELABORAZIONE|Timestamp di scrittura sul log|
|VERSION|Versione del SDK|
|CODICE STATO RUN|<p>Codice dell’esito dell’esecuzione dell’ SDK di Ritorno. Il valore del campo viene aggiornato ogni volta che viene lanciato SDK di Ritorno con un particolare Id Upload.</p><p>Possibili valori: </p><p>- OK: Esecuzione completata con successo;</p><p>- KO: Esecuzione fallita.</p>|
|DESCRIZIONE CODICE STATO RUN |Specifica il testo completo dell’errore (Opzionale)|
|ESITO ELABORAZIONE MDS|Esito di elaborazione del dato lato MdS (response monEsiUploadXML) (es: MX22)|
|DESCRIZIONE ESITO ELABORAZIONE MDS|Descrizione dell’esito di elaborazione del dato lato MdS (response monEsiUploadXML) (es: Operazione completata senza scarti e senza anomalie)|
|CODICE ERRORE ELABORAZIONE MDS|ErrorCode della response monEsiUploadXML|
|TESTO ERRORE ELABORAZIONE MDS|ErrorText della response monEsiUploadXML|
|STATO DOWNLOAD FUS|“RICHIESTO” oppure “DISPONIBILE”|
|ESITO DOWNLOAD FUS|Esito download del FUS (response dwnldAsnmSctUpload) (es: DF01) nei casi di esito MX21 e MX22|
|DESCRIZIONE ESITO DOWNLOAD FUS|Descrizione esito download del FUS (response dwnldAsnmSctUpload) (es: File restituito) nei casi di esito MX21 e MX22|
|CODICE ERRORE DOWNLOAD FUS|ErrorCode della response dwnldAsnmSctUpload|
|DESCRIZIONE ERRORE DOWNLOAD FUS|TextCode della response dwnldAsnmSctUpload|
|ALLEGATO|Path assoluto del nome file Validation ottenuto in caso di esito uguale a Mx11 (allegato alla response monEsiUploadXML), oppure del nome file FUS ottenuto in caso di esito uguale a Mx21 o Mx22 (allegato alla response dwnldAnmSctUpload).|

## mantainer:
 Accenture SpA until January 2026
