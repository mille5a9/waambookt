package waambokt.commands

abstract class Command {
    abstract suspend fun respond()
    internal abstract suspend fun execute(): String
}
