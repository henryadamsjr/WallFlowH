package com.ammar.havenwalls.data.network.model

import com.ammar.havenwalls.data.common.Purity
import com.ammar.havenwalls.data.db.entity.ThumbsEntity
import com.ammar.havenwalls.data.db.entity.WallpaperEntity
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class NetworkWallpaperTest {
    @Test
    fun `convert json to NetworkWallpaper`() {
        // language=JSON
        val json = """
            {
              "id": "85k6eo",
              "url": "https:\/\/wallhaven.cc\/w\/85k6eo",
              "short_url": "https:\/\/whvn.cc\/85k6eo",
              "views": 444,
              "favorites": 43,
              "source": "",
              "purity": "sketchy",
              "category": "people",
              "dimension_x": 1429,
              "dimension_y": 1031,
              "resolution": "1429x1031",
              "ratio": "1.39",
              "file_size": 1159446,
              "file_type": "image\/png",
              "created_at": "2023-04-26 02:41:32",
              "colors": [
                "#996633",
                "#999999",
                "#cc6633",
                "#e7d8b1",
                "#cccccc"
              ],
              "path": "https:\/\/w.wallhaven.cc\/full\/85\/wallhaven-85k6eo.png",
              "thumbs": {
                "large": "https:\/\/th.wallhaven.cc\/lg\/85\/85k6eo.jpg",
                "original": "https:\/\/th.wallhaven.cc\/orig\/85\/85k6eo.jpg",
                "small": "https:\/\/th.wallhaven.cc\/small\/85\/85k6eo.jpg"
              }
            }
        """.trimIndent()
        val networkWallpaper = Json.decodeFromString<NetworkWallpaper>(json)
        assertEquals(
            NetworkWallpaper(
                id = "85k6eo",
                url = "https://wallhaven.cc/w/85k6eo",
                short_url = "https://whvn.cc/85k6eo",
                uploader = null,
                views = 444,
                favorites = 43,
                source = "",
                purity = "sketchy",
                category = "people",
                dimension_x = 1429,
                dimension_y = 1031,
                resolution = "1429x1031",
                ratio = 1.39f,
                file_size = 1159446,
                file_type = "image/png",
                created_at = Instant.parse("2023-04-26T02:41:32Z"),
                colors = listOf("#996633", "#999999", "#cc6633", "#e7d8b1", "#cccccc"),
                path = "https://w.wallhaven.cc/full/85/wallhaven-85k6eo.png",
                thumbs = NetworkThumbs(
                    large = "https://th.wallhaven.cc/lg/85/85k6eo.jpg",
                    original = "https://th.wallhaven.cc/orig/85/85k6eo.jpg",
                    small = "https://th.wallhaven.cc/small/85/85k6eo.jpg"
                ),
                tags = null
            ),
            networkWallpaper,
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `convert NetworkWallpaper to json`() {
        // language=JSON
        val expected = """
            {
              "id": "85k6eo",
              "url": "https://wallhaven.cc/w/85k6eo",
              "short_url": "https://whvn.cc/85k6eo",
              "views": 444,
              "favorites": 43,
              "source": "",
              "purity": "sketchy",
              "category": "people",
              "dimension_x": 1429,
              "dimension_y": 1031,
              "resolution": "1429x1031",
              "ratio": 1.39,
              "file_size": 1159446,
              "file_type": "image/png",
              "created_at": "2023-04-26T02:41:32Z",
              "colors": [
                "#996633",
                "#999999",
                "#cc6633",
                "#e7d8b1",
                "#cccccc"
              ],
              "path": "https://w.wallhaven.cc/full/85/wallhaven-85k6eo.png",
              "thumbs": {
                "large": "https://th.wallhaven.cc/lg/85/85k6eo.jpg",
                "original": "https://th.wallhaven.cc/orig/85/85k6eo.jpg",
                "small": "https://th.wallhaven.cc/small/85/85k6eo.jpg"
              }
            }
        """.trimIndent()
        val networkWallpaper = NetworkWallpaper(
            id = "85k6eo",
            url = "https://wallhaven.cc/w/85k6eo",
            short_url = "https://whvn.cc/85k6eo",
            uploader = null,
            views = 444,
            favorites = 43,
            source = "",
            purity = "sketchy",
            category = "people",
            dimension_x = 1429,
            dimension_y = 1031,
            resolution = "1429x1031",
            ratio = 1.39f,
            file_size = 1159446,
            file_type = "image/png",
            created_at = Instant.parse("2023-04-26T02:41:32Z"),
            colors = listOf("#996633", "#999999", "#cc6633", "#e7d8b1", "#cccccc"),
            path = "https://w.wallhaven.cc/full/85/wallhaven-85k6eo.png",
            thumbs = NetworkThumbs(
                large = "https://th.wallhaven.cc/lg/85/85k6eo.jpg",
                original = "https://th.wallhaven.cc/orig/85/85k6eo.jpg",
                small = "https://th.wallhaven.cc/small/85/85k6eo.jpg"
            ),
            tags = null
        )
        val format = Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }
        val json = format.encodeToString(networkWallpaper)
        assertEquals(
            expected,
            json,
        )
    }

    @Test
    fun `convert NetworkWallpaper to WallpaperEntity`() {
        val networkWallpaper = NetworkWallpaper(
            id = "85k6eo",
            url = "https://wallhaven.cc/w/85k6eo",
            short_url = "https://whvn.cc/85k6eo",
            uploader = null,
            views = 444,
            favorites = 43,
            source = "",
            purity = "sketchy",
            category = "people",
            dimension_x = 1429,
            dimension_y = 1031,
            resolution = "1429x1031",
            ratio = 1.39f,
            file_size = 1159446,
            file_type = "image/png",
            created_at = Instant.parse("2023-04-26T02:41:32Z"),
            colors = listOf("#996633", "#999999", "#cc6633", "#e7d8b1", "#cccccc"),
            path = "https://w.wallhaven.cc/full/85/wallhaven-85k6eo.png",
            thumbs = NetworkThumbs(
                large = "https://th.wallhaven.cc/lg/85/85k6eo.jpg",
                original = "https://th.wallhaven.cc/orig/85/85k6eo.jpg",
                small = "https://th.wallhaven.cc/small/85/85k6eo.jpg"
            ),
            tags = null
        )
        val expected = WallpaperEntity(
            id = 0,
            wallhavenId = "85k6eo",
            url = "https://wallhaven.cc/w/85k6eo",
            shortUrl = "https://whvn.cc/85k6eo",
            uploaderId = null,
            views = 444,
            favorites = 43,
            source = "",
            purity = Purity.SKETCHY,
            category = "people",
            dimensionX = 1429,
            dimensionY = 1031,
            fileSize = 1159446,
            fileType = "image/png",
            createdAt = Instant.parse("2023-04-26T02:41:32Z"),
            colors = listOf("#996633", "#999999", "#cc6633", "#e7d8b1", "#cccccc"),
            path = "https://w.wallhaven.cc/full/85/wallhaven-85k6eo.png",
            thumbs = ThumbsEntity(
                large = "https://th.wallhaven.cc/lg/85/85k6eo.jpg",
                original = "https://th.wallhaven.cc/orig/85/85k6eo.jpg",
                small = "https://th.wallhaven.cc/small/85/85k6eo.jpg"
            ),
        )
        assertEquals(
            expected,
            networkWallpaper.asWallpaperEntity(),
        )
    }
}
