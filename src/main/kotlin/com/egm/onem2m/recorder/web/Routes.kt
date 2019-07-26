package com.egm.onem2m.recorder.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(
        private val sgnHandler: SgnHandler
) {

    @Bean
    fun router() = router {
        (accept(MediaType.APPLICATION_JSON) and "/api").nest {
            "/sgn".nest {
                POST("/", sgnHandler::genericAck)
                POST("/cnt", sgnHandler::notifyNewCnt)
                POST("/cin", sgnHandler::notifyNewCin)
            }
        }
    }
}