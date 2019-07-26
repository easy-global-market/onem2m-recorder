package com.egm.onem2m.recorder

import com.egm.onem2m.recorder.config.Onem2mProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(Onem2mProperties::class)
class Onem2mRecorderApplication

fun main(args: Array<String>) {
	runApplication<Onem2mRecorderApplication>(*args)
}
