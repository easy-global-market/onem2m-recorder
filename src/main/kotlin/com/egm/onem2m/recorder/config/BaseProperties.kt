package com.egm.onem2m.recorder.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.base")
class BaseProperties {
    lateinit var url: String
}