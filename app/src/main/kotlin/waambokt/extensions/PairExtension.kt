package waambokt.extensions

object PairExtension {
    fun <A, B> Pair<A, B>.valEq(x: A, y: B) = this.first == x && this.second == y
}
