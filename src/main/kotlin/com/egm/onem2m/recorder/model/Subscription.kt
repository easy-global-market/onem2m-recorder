package com.egm.onem2m.recorder.model

import java.time.LocalDateTime
import java.util.*

data class Subscription(
        val id: UUID = UUID.randomUUID(),
        val entityName: String,
        val creationDate: LocalDateTime = LocalDateTime.now()
)