package com.appmattus.certificatetransparency.internal.loglist.model.v3

import com.appmattus.certificatetransparency.internal.loglist.deserializer.Rfc3339Deserializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name Name of the log operator
 * @property endDate The time at which this operator stopped operating this log.
 */

@Serializable
internal data class PreviousOperator(
    @Serializable(with = Rfc3339Deserializer::class) @SerialName("end_time") val endDate: Instant,
    @SerialName("name") val name: String
)
