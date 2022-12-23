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
                Env.ISPROD -> System.getenv("ISPROD")
                Env.TOKEN -> System.getenv("TOKEN")
                Env.MONGO_CONNECTION_STRING -> System.getenv("MONGO_CONNECTION_STRING")
                Env.PORT -> System.getenv("PORT")
            }
        )
    }

    operator fun get(key: String) = vars[key]!! // Missing an EV is worthy of a NullPointerException
}

enum class Env {
    TESTGUILD,
    PRODGUILD,
    CLEARCOMMANDS,
    ISPROD,
    TOKEN,
    MONGO_CONNECTION_STRING,
    PORT
}
