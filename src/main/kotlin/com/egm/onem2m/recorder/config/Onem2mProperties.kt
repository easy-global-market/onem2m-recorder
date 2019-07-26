package com.egm.onem2m.recorder.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.onem2m")
class Onem2mProperties {
    lateinit var url: String
    lateinit var cseBase: String
    lateinit var origin: String
}