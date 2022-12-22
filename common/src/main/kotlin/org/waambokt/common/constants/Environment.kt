package org.waambokt.common.constants

class Environment(
    vararg envars: Env
) {
    private val vars = envars.associate {
        Pair(
            it.name,
            when (it) {
                Env.TESTGUILD -> System.getenv("TESTGUILD")
                Env.PRODGUILD -> System.getenv("PRODGUILD")
                Env.CLEARCOMMANDS -> System.getenv("CLEARCOMMANDS")
                Env.PROD -> System.getenv("PROD")
                Env.TOKEN -> System.getenv("TOKEN")
                Env.MONGO_CONNECTION_STRING -> System.getenv("MONGO_CONNECTION_STRING")
                Env.PORT -> System.getenv("PORT")
            }
        )
    }

    operator fun get(key: String) = vars[key]

    operator fun get(keyToBool: String, ifTrue: String, ifFalse: String) =
        if (vars[keyToBool].toBoolean()) ifTrue else ifFalse
}

enum class Env {
    TESTGUILD,
    PRODGUILD,
    CLEARCOMMANDS,
    PROD,
    TOKEN,
    MONGO_CONNECTION_STRING,
    PORT
}
