package com.egm.onem2m.recorder.web

import com.egm.onem2m.recorder.model.Measure
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import java.time.ZonedDateTime

@Component
class TsHandler(
        private val databaseClient: DatabaseClient
) {

    private val logger = LoggerFactory.getLogger(TsHandler::class.java)

    fun add(req: ServerRequest): Mono<ServerResponse> {
        val content = req.pathVariable("content")
        logger.debug("Adding value $content")
        return databaseClient.insert()
                .into(Measure::class.java)
                .using(Measure(time = ZonedDateTime.now(), container = "Test Cnt",
                        contentInfo = "Temp", content = content.toFloat()))
                .fetch()
                .rowsUpdated()
                .flatMap { ok().build() }
    }

    fun last(req: ServerRequest): Mono<ServerResponse> {
        val count = req.pathVariable("count")
        logger.debug("Searching for the last $count measures")
        return databaseClient.select()
                .from(Measure::class.java)
                .fetch()
                .first()
                .flatMap { measure -> ok().body(BodyInserters.fromObject(measure)) }
    }
}