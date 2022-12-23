package org.waambokt.service.waambokt.commands

sealed class Command {
    abstract suspend fun respond()
    internal abstract suspend fun execute(): String
}
