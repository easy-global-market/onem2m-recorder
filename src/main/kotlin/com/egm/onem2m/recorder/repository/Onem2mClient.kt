package com.egm.onem2m.recorder.repository

import com.egm.onem2m.recorder.config.BaseProperties
import com.egm.onem2m.recorder.config.Onem2mProperties
import com.egm.onem2m.recorder.model.onem2m.EntitiesWrapper
import com.egm.onem2m.recorder.util.generateRI
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class Onem2mClient(
        private val onem2mProperties: Onem2mProperties,
        private val baseProperties: BaseProperties
) {

    private val logger = LoggerFactory.getLogger(Onem2mClient::class.java)

    private val client = WebClient.builder().baseUrl(onem2mProperties.url).build()

    fun listEntities(): Mono<EntitiesWrapper> {
        return client.get()
                .uri("/${onem2mProperties.cseBase}?ty=2&fu=1")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-M2M-RI", "AE-${generateRI()}")
                .header("X-M2M-Origin", onem2mProperties.origin)
                .retrieve()
                .bodyToMono(EntitiesWrapper::class.java)
    }

    fun subscribeToEntity(entityName: String, subscriptionName: String, subscriptionType: String,
                          subscriptionPath: String): Mono<HttpStatus> {

        logger.debug("Subscribing entity $entityName under name $subscriptionName")

        val payload = """
            {
                "m2m:sub": {
		            "rn": "$subscriptionName",
		            "enc": {
			            "net": ["$subscriptionType"]
		            },
		            "nu": ["${baseProperties.url}$subscriptionPath"],
		            "nct": "1"
		        }
	        }""".trimIndent()

        return client.post()
                .uri("/$entityName")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";ty=23")
                .header("X-M2M-RI", "AE-${generateRI()}")
                .header("X-M2M-Origin", onem2mProperties.origin)
                .body(BodyInserters.fromObject(payload))
                .exchange()
                .flatMap { Mono.just(HttpStatus.CREATED) }
                //.onStatus(HttpStatus::is409Status) { Mono.from(HttpStatus.CONFLICT) }
                //.onStatus(HttpStatus::is2xxSuccessful) { Mono.from(HttpStatus.CREATED) }
    }
}

fun HttpStatus.is409Status() = this.value() == HttpStatus.CONFLICT.value()
