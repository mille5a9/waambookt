package waambokt.commands

interface ICommand<T, A> {
    suspend fun invoke(event: T)
    suspend fun List<A>.execute(): String
}
