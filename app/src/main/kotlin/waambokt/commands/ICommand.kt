package waambokt.commands

interface ICommand<T, A> {
    suspend fun invoke(event: T): Void?
    suspend fun List<A>.execute(): String
}
