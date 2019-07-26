package com.egm.onem2m.recorder.model

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Subscription(
        @Id val id: UUID = UUID.randomUUID(),
        val entityName: String,
        val creationDate: LocalDateTime = LocalDateTime.now()
)