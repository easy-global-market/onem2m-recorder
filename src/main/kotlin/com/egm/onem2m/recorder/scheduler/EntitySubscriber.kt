package com.egm.onem2m.recorder.scheduler

import com.egm.onem2m.recorder.config.Onem2mProperties
import com.egm.onem2m.recorder.model.Subscription
import com.egm.onem2m.recorder.repository.Onem2mClient
import com.egm.onem2m.recorder.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EntitySubscriber(
        private val onem2mClient: Onem2mClient,
        private val onem2mProperties: Onem2mProperties,
        private val subscriptionRepository: SubscriptionRepository
) {

    private val logger = LoggerFactory.getLogger(EntitySubscriber::class.java)

    @Scheduled(fixedDelay = 60000)
    fun subscribeToNewAE() {
        logger.debug("Looking at AEs list")
        onem2mClient.listEntities().subscribe { wrapper ->
            logger.debug("Received ${wrapper.entities}")
            wrapper.entities.forEach { entityName ->
                subscriptionRepository.findByEntityName(entityName).ifPresentOrElse(
                        { logger.debug("Subscription already exists") },
                        {
                            onem2mClient.subscribeToEntity(entityName, "$entityName-Sub",
                                    "3", onem2mProperties.url + "/sgn/cnt").map {
                                subscriptionRepository.save(Subscription(entityName = entityName))
                            }.subscribe {
                                logger.debug("Subscribed to new entity : ${it.entityName} on ${it.creationDate}")
                            }
                        }
                )
            }
        }
    }
}