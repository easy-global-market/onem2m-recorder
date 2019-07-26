package com.egm.onem2m.recorder.web

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class SgnHandler {

    private val logger = LoggerFactory.getLogger(SgnHandler::class.java)

    /**
     * Received payload looks like :
     *
     * {"m2m:sgn":{"vrq":true,"sur":"Mobius/AE-Test/SensorWind6/SubWind6","cr":"Cxxx","rvi":"2a"}}
     */
    fun genericAck(req: ServerRequest): Mono<ServerResponse> {
        val parser: Parser = Parser.default()
        val json = req.bodyToMono<String>().map { parser.parse(it) as JsonObject }.block()

        logger.debug("${json?.obj("m2m")!!.obj("sgn")!!.string("sur")}")
        return ok().build()
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
        val json = req.bodyToMono<String>().map { parser.parse(it) as JsonObject }.block()!!
        if (json.lookup<String>("m2m.sgn.vrq").isEmpty()) {
            logger.debug("Received a simple sub ack")
        } else {
            logger.debug("Received a CNT")
            val subscription = json.obj("m2m")!!.obj("sgn")!!.string("sur")
            val value = json.obj("m2m:sgn")!!.obj("nev")!!.obj("rep")!!.obj("m2m:cin")!!.string("con")
            val creationTime = json.obj("m2m:sgn")!!.obj("nev")!!.obj("rep")!!.obj("m2m:cin")!!.string("ct")
        }

        return ok().build()
    }

    /**
     * Received payload looks like :
     *
     * {"m2m:sgn":{"sur":"Mobius/AE-Test/SensorWind6/SubWind6","nev":{"net":3,
     *    "rep":{"m2m:cin":{"rn":"4-20190719081355100690322","ty":4,"pi":"sSiyamykWp","ri":"LvwgazQ2wR",
     *                      "ct":"20190719T081355","et":"20220719T081355","lt":"20190719T081355","st":3,"cs":2,
     *                      "con":"30","cr":"Cegm"}}},"rvi":"2a"}}
     */
    fun notifyNewCin(req: ServerRequest) = ok().build()
}