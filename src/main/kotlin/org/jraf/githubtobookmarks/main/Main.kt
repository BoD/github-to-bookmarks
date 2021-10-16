/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jraf.githubtobookmarks.main

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.http.withHttpHeader
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jraf.githubtobookmarks.GetRepositoriesQuery
import org.json.JSONObject
import org.slf4j.LoggerFactory

private const val DEFAULT_PORT = 8042

private const val ENV_PORT = "PORT"

private const val PATH_TOKEN = "token"
private const val PATH_GITHUB_USER_NAME = "username"

private const val APP_URL = "https://github-to-bookmarks.herokuapp.com"


private val logger = LoggerFactory.getLogger("org.jraf.githubtobookmarks.main")
private val apolloClient by lazy {
    ApolloClient("https://api.github.com/graphql")
}

suspend fun main() {
    val listenPort = System.getenv(ENV_PORT)?.toInt() ?: DEFAULT_PORT
    embeddedServer(Netty, listenPort) {
        install(DefaultHeaders)

        install(StatusPages) {
            status(HttpStatusCode.NotFound) {
                call.respondText(
                    text = "Usage: $APP_URL/<Auth token>/<GitHub user name>\n\nSee https://github.com/BoD/github-to-bookmarks for more info.",
                    status = it
                )
            }
        }

        routing {
            get("{$PATH_TOKEN}/{$PATH_GITHUB_USER_NAME}") {
                val token = call.parameters[PATH_TOKEN]!!
                val userName = call.parameters[PATH_GITHUB_USER_NAME]!!
                val jsonBookmarks = fetchRepositories(token, userName).asJsonBookmarks()
                val jsonBookmarksWithEnvelope = """{"version": 1, ${jsonBookmarks}}"""
                call.respondText(jsonBookmarksWithEnvelope, ContentType.Application.Json.withCharset(Charsets.UTF_8))
            }
        }
    }.start(wait = true)
}

suspend fun fetchRepositories(token: String, userName: String): List<Bookmark> {
    return apolloClient.query(
        ApolloRequest(
            GetRepositoriesQuery(userLogin = userName)
        ).withHttpHeader("Authorization", "Bearer $token")
    ).dataOrThrow.user!!.repositories.nodes!!.map {
        Bookmark(
            title = it!!.name,
            url = it.url.toString(),
            bookmarks = emptyList()
        )
    }
}

data class Bookmark(
    val title: String,
    val url: String,
    val bookmarks: List<Bookmark>,
)

private fun List<Bookmark>.asJsonBookmarks(): String {
    var res = """
        "bookmarks": [
    """.trimIndent()
    for ((i, bookmark) in this.withIndex()) {
        res += if (bookmark.bookmarks.isEmpty() || bookmark.bookmarks.size == 1) {
            """
                    {
                        "title": ${JSONObject.quote(bookmark.title)},
                        "url": "${bookmark.url}"
                    }${if (i == this.lastIndex) "" else ","}
                """.trimIndent()
        } else {
            """
                    {
                        "title": ${JSONObject.quote(bookmark.title)},
                        ${bookmark.bookmarks.asJsonBookmarks()}
                    }${if (i == this.lastIndex) "" else ","}
                """.trimIndent()
        }
    }
    res += "]"
    return res
}
