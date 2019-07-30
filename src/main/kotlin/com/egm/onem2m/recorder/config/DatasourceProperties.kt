package com.egm.onem2m.recorder.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.datasource")
class DatasourceProperties {
    lateinit var database: String
    lateinit var host: String
    var port: Int = 5432
    lateinit var user: String
    lateinit var password: String
    lateinit var driver: String
}