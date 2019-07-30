package com.egm.onem2m.recorder.scheduler

import com.egm.onem2m.recorder.model.Subscription
import com.egm.onem2m.recorder.repository.Onem2mClient
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class EntitySubscriber(
        private val onem2mClient: Onem2mClient,
        private val databaseClient: DatabaseClient
) {

    private val logger = LoggerFactory.getLogger(EntitySubscriber::class.java)

    @Scheduled(fixedDelay = 60000)
    fun subscribeToNewAE() {
        logger.debug("Looking at AEs list")
        onem2mClient.listEntities()
                .map { it.entities }
                .flatMapMany { Flux.fromIterable(it) }
                .flatMap { entityName ->
                    databaseClient.execute()
                            .sql("SELECT count(*) as count FROM subscription WHERE entity_name = :entityName")
                            .bind("entityName", entityName)
                            .fetch()
                            .one()
                            .map { resultMap -> Pair(resultMap["count"] as Long, entityName) } }
                .onErrorResume { t ->
                    logger.error("Got an error :", t)
                    Mono.empty()
                }
                .filter { it.first == 0L }
                .flatMap {
                    logger.debug("Subscription does not exist, creating it")
                    onem2mClient.subscribeToEntity(it.second, "3", "/api/sgn/cnt") }
                .flatMap {
                    databaseClient.insert()
                            .into(Subscription::class.java)
                            .using(Subscription(entityName = it))
                            .fetch()
                            .rowsUpdated() }
                .subscribe {
                    logger.debug("Subscribed a new entity")
                }
    }
}
