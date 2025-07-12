
package com.streamin

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.utils.*

class StreamInProvider : MainAPI() {
    override var mainUrl = "https://streamed.su"
    override var name = "StreamIn"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Live)

    override suspend fun getMainPage(): HomePageResponse {
        val sports = app.get("$mainUrl/api/sports").parsed<List<Sport>>()
        val home = sports.map {
            val matches = app.get("$mainUrl/api/matches?sportId=${it.id}").parsed<List<Match>>()
            HomePageList(it.name, matches.map { m -> LiveSearchResponse(m.title, "$mainUrl/match/${m.id}", this.name, TvType.Live) })
        }
        return HomePageResponse(home)
    }

    override suspend fun load(url: String): LoadResponse? {
        val id = url.substringAfterLast("/")
        val streams = app.get("$mainUrl/api/streams?matchId=$id").parsed<List<Stream>>()
        val best = streams.firstOrNull()
        return if (best != null) {
            LiveStreamLoadResponse(best.title, url, this.name, best.url)
        } else null
    }

    data class Sport(val id: String, val name: String)
    data class Match(val id: String, val title: String)
    data class Stream(val title: String, val url: String)
}
