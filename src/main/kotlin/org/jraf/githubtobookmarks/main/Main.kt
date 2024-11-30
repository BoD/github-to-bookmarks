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

import com.apollographql.apollo.ApolloClient
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.jraf.githubtobookmarks.GetRepositoriesQuery
import org.slf4j.simple.SimpleLogger

private const val DEFAULT_PORT = 8080

private const val ENV_PORT = "PORT"

private const val PATH_TOKEN = "token"
private const val PATH_GITHUB_USER_NAME = "username"

private val apolloClient = ApolloClient.Builder().serverUrl("https://api.github.com/graphql").build()
private val json = Json { prettyPrint = true }

suspend fun main() {
  // This must be done before any logger is initialized
  System.setProperty(SimpleLogger.LOG_FILE_KEY, "System.out")
  System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "trace")
  System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true")
  System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd HH:mm:ss")

  val listenPort = System.getenv(ENV_PORT)?.toInt() ?: DEFAULT_PORT
  embeddedServer(Netty, listenPort) {
    install(DefaultHeaders)

    install(StatusPages) {
      status(HttpStatusCode.NotFound) { call, status ->
        call.respondText(
          text = "Usage: ${call.request.local.scheme}://${call.request.local.serverHost}:${call.request.local.serverPort}/<Auth token>/<GitHub user name>\n\nSee https://github.com/BoD/github-to-bookmarks for more info.",
          status = status,
        )
      }
    }

    routing {
      get("{$PATH_TOKEN}/{$PATH_GITHUB_USER_NAME}") {
        val token = call.parameters[PATH_TOKEN]!!
        val userName = call.parameters[PATH_GITHUB_USER_NAME]!!
        val jsonBookmarks = fetchRepositories(token, userName).asJsonBookmarks()
        call.respondText(jsonBookmarks, ContentType.Application.Json.withCharset(Charsets.UTF_8))
      }
    }
  }.start(wait = true)
}

suspend fun fetchRepositories(token: String, userName: String): List<Bookmark> {
  return apolloClient.query(GetRepositoriesQuery(userLogin = userName))
    .addHttpHeader("Authorization", "Bearer $token")
    .execute()
    .dataAssertNoErrors.user!!.repositories.nodes!!.map {
      Bookmark(
        title = it!!.name,
        url = it.url.toString(),
        bookmarks = emptyList(),
      )
    }
}

data class Bookmark(
  val title: String,
  val url: String,
  val bookmarks: List<Bookmark>,
)

private fun List<Bookmark>.asJsonBookmarks(): String {
  val jsonObject = buildJsonObject {
    put("version", 1)
    putJsonArray("bookmarks") {
      for (bookmark in this@asJsonBookmarks) {
        add(
          buildJsonObject {
            put("title", bookmark.title)
            put("url", bookmark.url)
          },
        )
      }
    }
  }
  return json.encodeToString(jsonObject)
}
