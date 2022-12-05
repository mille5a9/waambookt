package waambokt.commands

abstract class Command {
    protected abstract suspend fun respond()
    protected abstract suspend fun execute(): String
}
