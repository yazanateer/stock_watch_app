package com.example.stockwatchapp

data class NewsResponse(
    val data: NewsData
)

data class NewsData(
    val main: NewsMain
)

data class NewsMain(
    val stream: List<NewsItem>
)

data class NewsItem(
    val id: String,
    val content: NewsContent
)

data class NewsContent(
    val id: String,
    val contentType: String,
    val title: String,
    val pubDate: String,
    val tags: List<String>,
    val thumbnail: Thumbnail,
    val clickThroughUrl: ClickThroughUrl,
    val provider: Provider
)

data class Thumbnail(
    val resolutions: List<ThumbnailResolution>
)

data class ThumbnailResolution(
    val url: String,
    val width: Int,
    val height: Int,
    val tag: String
)

data class ClickThroughUrl(
    val url: String
)

data class Provider(
    val displayName: String
)

