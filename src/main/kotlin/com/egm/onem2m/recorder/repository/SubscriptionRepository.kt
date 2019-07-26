package com.egm.onem2m.recorder.repository

import com.egm.onem2m.recorder.model.Subscription
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SubscriptionRepository : CrudRepository<Subscription, String> {

    fun findByEntityName(entityName: String): Optional<Subscription>
}