package com.egm.onem2m.recorder.model.onem2m

import com.fasterxml.jackson.annotation.JsonProperty

data class EntitiesWrapper(
        @JsonProperty("m2m:uril") val entities: List<String>
)