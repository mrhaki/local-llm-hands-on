# OpenAI Hello World

Een minimale Spring Boot-applicatie met Maven en Java 21 of hoger.
`GET /hello` stuurt een vaste hello-world-prompt naar de OpenAI-compatible Responses API en geeft uitsluitend de antwoordtekst terug.

## Configuratie

Pas `src/main/resources/application.properties` aan:

```properties
openai.responses-url=http://127.0.0.1:1234/v1/responses
openai.model=local-model
```

Vervang `local-model` door de model-ID van je geladen LM Studio-model.
Start de lokale server in LM Studio op poort 1234. 
Deze moet `/v1/responses`ondersteunen. (Zie [LM Studio API](https://docs.lmstudio.ai/docs/api) voor details.)
Als authenticatie aanstaat, stel dan `OPENAI_API_KEY` in.


## Starten

Voer vanuit de projectmap uit:

```sh
mvn spring-boot:run
```

Roep in een tweede terminal aan:

```sh
curl http://127.0.0.1:8080/hello
```

Of bouw en start de jar:

```sh
mvn clean package
java -jar target/openai-hello-world-0.0.1-SNAPSHOT.jar
```

Er is geen AI-framework of OpenAI SDK nodig: de HTTP-aanroep gebruikt Spring`RestClient`. 
De code leest de `output_text`-onderdelen uit `output`, zodat een eventueel reasoning-item vóór het antwoord niet als antwoord wordt behandeld.
Een onbereikbare backend, een HTTP-fout of een antwoord zonder tekst resulteert in HTTP 502.
