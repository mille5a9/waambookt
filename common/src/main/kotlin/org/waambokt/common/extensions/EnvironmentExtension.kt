package org.waambokt.common.extensions

import dev.kord.common.entity.Snowflake
import org.waambokt.common.constants.Environment

object EnvironmentExtension {
    fun Environment.bool(key: String) = this[key].toBoolean()

    fun Environment.sf(key: String) = Snowflake(this[key])
}
