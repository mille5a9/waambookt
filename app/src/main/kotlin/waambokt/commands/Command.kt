package waambokt.commands

abstract class Command {
    protected abstract suspend fun respond()
    internal abstract suspend fun execute(): String
}
