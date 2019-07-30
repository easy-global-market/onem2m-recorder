package com.egm.onem2m.recorder.web

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.egm.onem2m.recorder.model.Measure
import com.egm.onem2m.recorder.model.Subscription
import com.egm.onem2m.recorder.repository.Onem2mClient
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
class SgnHandler(
        private val onem2mClient: Onem2mClient,
        private val databaseClient: DatabaseClient
) {

    private val logger = LoggerFactory.getLogger(SgnHandler::class.java)

    /**
     * Received payload looks like :
     *
     * {"m2m:sgn":{"vrq":true,"sur":"Mobius/AE-Test/SensorWind6/SubWind6","cr":"Cxxx","rvi":"2a"}}
     */
    fun genericAck(req: ServerRequest): Mono<ServerResponse> {
        val parser: Parser = Parser.default()
        return req.bodyToMono<String>()
                .map { parser.parse(it.byteInputStream()) as JsonObject }
                .log()
                .flatMap { ok().build() }
    }

    /**
     * Received payload looks like :
     *
     * {"m2m:sgn":{"sur":"Mobius/AE-Test/SubAeTest4","nev":{"net":3,
     *     "rep":{"m2m:cnt":{"rn":"SensorWind6","ty":3,"pi":"1oXrLz_I0x","ri":"sSiyamykWp","ct":"20190719T080123",
     *                       "et":"20220719T080123","lt":"20190719T080123","st":0,"mni":3153600000,"mbs":3153600000,
     *                       "mia":31536000,"cr":"Cegm","cni":0,"cbs":0}}},"rvi":"2a"}}
     */
    fun notifyNewCnt(req: ServerRequest): Mono<ServerResponse> {
        val parser: Parser = Parser.default()
        return req.bodyToMono<String>()
                .map { parser.parse(it.byteInputStream()) as JsonObject }
                .doOnNext { logger.debug("Received CNT notification on ${it.toJsonString(prettyPrint = true)}") }
                // TODO : does not seem to work like expected
                .filter { json -> !json.containsKey("m2m:sgn.vrq") }
                .flatMap { json ->
                    val resourceName = json.obj("m2m:sgn")!!.obj("nev")!!.obj("rep")!!.obj("m2m:cnt")!!.string("rn")!!
                    val triggeringEntityName = json.obj("m2m:sgn")!!.string("sur")!!
                    val entityName = triggeringEntityName.replaceAfterLast("/", "") + resourceName
                    logger.debug("Received a notification on entity $triggeringEntityName (new entity is $resourceName)")
                    databaseClient.insert()
                            .into(Subscription::class.java)
                            .using(Subscription(entityName = entityName))
                            .fetch()
                            .one()
                            .map { resultMap -> resultMap["entity_name"] as String }
                }
                .flatMap {
                    onem2mClient.subscribeToEntity(it, "3", "/api/sgn/cin")
                }
                .flatMap { ok().build() }
    }

    /**
     * Received payload looks like :
     *
     * {"m2m:sgn":{"sur":"Mobius/AE-Test/SensorWind6/SubWind6","nev":{"net":3,
     *    "rep":{"m2m:cin":{"rn":"4-20190719081355100690322","ty":4,"pi":"sSiyamykWp","ri":"LvwgazQ2wR",
     *                      "ct":"20190719T081355","et":"20220719T081355","lt":"20190719T081355","st":3,"cs":2,
     *                      "con":"30","cr":"Cegm"}}},"rvi":"2a"}}
     */
    fun notifyNewCin(req: ServerRequest): Mono<ServerResponse> {
        val parser: Parser = Parser.default()
        // TODO : filter the "subscription ack" call
        return req.bodyToMono<String>()
                .map { parser.parse(it.byteInputStream()) as JsonObject }
                .doOnNext { logger.debug("Received a content instance : ${it.toJsonString(prettyPrint = true)}") }
                .flatMap { json ->
                    val subscriptionName = json.obj("m2m:sgn")!!.string("sur")!!
                    val container = subscriptionName.replaceAfterLast("/", "").removeSuffix("/")
                    val baseCin = json.obj("m2m:sgn")!!.obj("nev")!!.obj("rep")!!.obj("m2m:cin")!!
                    val content = baseCin.string("con")!!
                    val creationTime = baseCin.string("ct")!!
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneId.systemDefault())
                    val measureTime = ZonedDateTime.parse(creationTime, formatter)
                    logger.debug("Extracted value $content on subscription $subscriptionName (cnt : $container) at $measureTime")
                    // TODO : how to get the content info ?
                    databaseClient.insert()
                            .into(Measure::class.java)
                            .using(Measure(time = measureTime, container = container,
                                    contentInfo = "Temperature", content = content.toFloat()))
                            .fetch()
                            .rowsUpdated() }
                .flatMap { ok().build() }
    }
}