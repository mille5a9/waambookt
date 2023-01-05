package org.waambokt.service.net.helpers

import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.net.models.NbaNet
import java.util.Date

object RefreshNetsHelper {
    private val logger = KotlinLogging.logger {}

    // Scrapes and updates the nba NET information in the db,
    // then returns a map of relevant values for command execution
    suspend fun refreshNet(db: CoroutineDatabase): List<NbaNet> {
        val dayms = 86400000
        val netsCollection = db.getCollection<NbaNet>("nbaNet")
        val allNets = netsCollection.find().toList()

        // short circuit if they were already updated today
        if (allNets.isNotEmpty() && allNets[0].updated.time.floorDiv(dayms) == Date().time.floorDiv(dayms)) {
            logger.info("using cached net ratings")
            return allNets
        }

        // scrape new nets and update table
        return scrapeNetsAndCommit(netsCollection)
    }

    fun List<NbaNet>.findTeamNet(name: String) =
        this.find { x -> x.teamName == name }?.netValue ?: 0.0

    private suspend fun scrapeNetsAndCommit(
        netsCollection: CoroutineCollection<NbaNet>
    ): List<NbaNet> {
        logger.info("scraping net ratings")
        val allNets =
            skrape(AsyncFetcher) {
                request {
                    url = "https://www.basketball-reference.com/leagues/NBA_2023_ratings.html"
                }
                response {
                    htmlDocument {
                        table("#ratings") {
                            tbody {
                                tr {
                                    findAll {
                                        map {
                                            NbaNet(
                                                it.td {
                                                    withAttribute = "data-stat" to "team_name"
                                                    findFirst {
                                                        this.a {
                                                            findFirst { text }
                                                        }
                                                    }
                                                },
                                                it.td {
                                                    withAttribute = "data-stat" to "net_rtg_adj"
                                                    findFirst { text.toDouble() }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        println(allNets)

        // wipe old nets
        netsCollection.deleteMany()

        // add new nets
        netsCollection.insertMany(allNets)

        return allNets
    }
}
