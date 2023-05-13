package com.ammar.havenwalls.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import androidx.room.withTransaction
import com.ammar.havenwalls.IoDispatcher
import com.ammar.havenwalls.data.common.Purity
import com.ammar.havenwalls.data.common.SearchQuery
import com.ammar.havenwalls.data.db.database.AppDatabase
import com.ammar.havenwalls.data.db.entity.LastUpdatedCategory
import com.ammar.havenwalls.data.db.entity.LastUpdatedEntity
import com.ammar.havenwalls.data.db.entity.PopularTagEntity
import com.ammar.havenwalls.data.db.entity.TagEntity
import com.ammar.havenwalls.data.db.entity.WallpaperTagsEntity
import com.ammar.havenwalls.data.db.entity.WallpaperWithUploaderAndTags
import com.ammar.havenwalls.data.db.entity.asTag
import com.ammar.havenwalls.data.db.entity.asWallpaper
import com.ammar.havenwalls.data.network.WallHavenNetworkDataSource
import com.ammar.havenwalls.data.network.model.NetworkTag
import com.ammar.havenwalls.data.network.model.NetworkUploader
import com.ammar.havenwalls.data.network.model.NetworkWallpaper
import com.ammar.havenwalls.data.network.model.asTagEntity
import com.ammar.havenwalls.data.network.model.asUploaderEntity
import com.ammar.havenwalls.data.network.model.asWallpaperEntity
import com.ammar.havenwalls.data.repository.utils.NetworkBoundResource
import com.ammar.havenwalls.data.repository.utils.Resource
import com.ammar.havenwalls.data.repository.utils.TagsDocumentParser.parsePopularTags
import com.ammar.havenwalls.model.Tag
import com.ammar.havenwalls.model.Wallpaper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class DefaultWallHavenRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val wallHavenNetwork: WallHavenNetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WallHavenRepository {
    private val popularTagsDao = appDatabase.popularTagsDao()
    private val lastUpdatedDao = appDatabase.lastUpdatedDao()
    private val wallpapersDao = appDatabase.wallpapersDao()
    private val tagsDao = appDatabase.tagsDao()
    private val uploadersDao = appDatabase.uploadersDao()

    private val popularTagNetworkResource =
        object : NetworkBoundResource<List<TagEntity>, List<Tag>, List<NetworkTag>>(
            initialValue = emptyList(),
            ioDispatcher = ioDispatcher
        ) {
            override suspend fun loadFromDb(): List<TagEntity> = popularTagsDao
                .getAllWithDetails()
                .map { it.tagEntity }

            override suspend fun shouldFetchData(dbData: List<TagEntity>): Boolean {
                return if (dbData.isEmpty()) {
                    true
                } else {
                    val lastUpdated = lastUpdatedDao.getByKey(
                        LastUpdatedCategory.POPULAR_TAGS.key
                    ) ?: return true
                    (lastUpdated.lastUpdatedOn - Clock.System.now()).absoluteValue.inWholeMinutes / 60f > 3
                }
            }

            override suspend fun fetchFromNetwork(dbData: List<TagEntity>): List<NetworkTag> {
                val doc = wallHavenNetwork.popularTags()
                return doc?.let(::parsePopularTags) ?: emptyList()
            }

            override suspend fun saveFetchResult(fetchResult: List<NetworkTag>) {
                appDatabase.withTransaction {
                    popularTagsDao.deleteAll()
                    // insert non-existing tags first
                    val tagIds = insertTags(fetchResult, false).map { it.id }
                    // create and insert PopularTagEntities
                    popularTagsDao.insert(tagIds.map { PopularTagEntity(0, it) })
                    upsertLastUpdated(LastUpdatedCategory.POPULAR_TAGS)
                }
            }

            override fun entityConverter(dbData: List<TagEntity>) = dbData.map { it.asTag() }

            override fun onFetchFailed(throwable: Throwable) {}

        }

    private suspend fun insertTags(
        tags: List<NetworkTag>,
        shouldUpdate: Boolean,
    ): List<TagEntity> {
        val existingTags = tagsDao.getByWallhavenIds(tags.map { it.id })

        if (shouldUpdate) {
            // update existing tags
            val tagWallhavenIdMap = tags.associateBy { it.id }
            val updatedTags = existingTags.map {
                val networkTag = tagWallhavenIdMap[it.wallhavenId] ?: return@map null
                it.copy(
                    name = networkTag.name,
                    alias = networkTag.alias,
                    categoryId = networkTag.category_id,
                    category = networkTag.category,
                    purity = Purity.fromName(networkTag.purity),
                    createdAt = networkTag.created_at,
                )
            }.filterNotNull()
            tagsDao.update(updatedTags)
        }

        // insert new tags
        val existingTagWallhavenIds = existingTags.map { it.wallhavenId }
        val newTags = tags.filter { it.id !in existingTagWallhavenIds }.map { it.asTagEntity() }
        tagsDao.insert(newTags)

        return tagsDao.getByNames(tags.map { it.name })
    }

    private fun getWallpaperNetworkResource(wallpaperWallhavenId: String) =
        object : NetworkBoundResource<WallpaperWithUploaderAndTags?, Wallpaper?, NetworkWallpaper>(
            initialValue = null,
            ioDispatcher = ioDispatcher
        ) {
            override suspend fun loadFromDb() =
                wallpapersDao.getWithUploaderAndTagsByWallhavenId(wallpaperWallhavenId)

            override suspend fun shouldFetchData(dbData: WallpaperWithUploaderAndTags?) =
                dbData?.uploader == null

            override suspend fun fetchFromNetwork(dbData: WallpaperWithUploaderAndTags?) =
                wallHavenNetwork.wallpaper(wallpaperWallhavenId).data

            override suspend fun saveFetchResult(fetchResult: NetworkWallpaper) {
                appDatabase.withTransaction {
                    var uploaderId: Long? = null
                    if (fetchResult.uploader != null) {
                        // create uploader if not exists in db
                        uploaderId = insertUploader(fetchResult.uploader)
                    }
                    var tagsIds: List<Long>? = null
                    if (fetchResult.tags != null) {
                        // create tags if not exists in db
                        tagsIds = insertTags(fetchResult.tags, true).map { it.id }
                    }
                    // insert or update wallpaper in db
                    val existingWallpaper = wallpapersDao.getByWallhavenId(wallpaperWallhavenId)
                    val wallpaperDbId: Long
                    if (existingWallpaper != null) {
                        wallpaperDbId = existingWallpaper.id
                        wallpapersDao.update(
                            fetchResult.asWallpaperEntity(
                                id = wallpaperDbId,
                                uploaderId = uploaderId,
                            )
                        )
                    } else {
                        wallpaperDbId = wallpapersDao.insert(
                            fetchResult.asWallpaperEntity(uploaderId = uploaderId)
                        ).first()
                    }
                    if (existingWallpaper != null) {
                        // delete existing wallpaper tag mappings
                        wallpapersDao.deleteWallpaperTagMappings(existingWallpaper.id)
                    }
                    if (tagsIds != null) {
                        // insert new wallpaper tag mappings
                        wallpapersDao.insertWallpaperTagMappings(tagsIds.map {
                            WallpaperTagsEntity(
                                wallpaperId = wallpaperDbId,
                                tagId = it,
                            )
                        })
                    }
                }
            }

            override fun entityConverter(dbData: WallpaperWithUploaderAndTags?) =
                dbData?.let {
                    val (wallpaper, uploader, tags) = it
                    wallpaper.asWallpaper(uploader, tags)
                }

            override fun onFetchFailed(throwable: Throwable) {}

        }

    private suspend fun insertUploader(uploader: NetworkUploader): Long {
        val existingUploader = uploadersDao.getByUsername(uploader.username)
        if (existingUploader != null) {
            return existingUploader.id
        }
        return uploadersDao.insert(uploader.asUploaderEntity()).first()
    }

    // override fun topWallpapers(topRange: TopRange) = Pager(config = PagingConfig(pageSize = 24)) {
    //     TopWallpapersPageKeyedPagingSource(wallHavenNetwork, topRange)
    // }.flow

    override fun wallpapersPager(searchQuery: SearchQuery) = Pager(
        config = PagingConfig(pageSize = 24),
        remoteMediator = WallpapersRemoteMediator(
            searchQuery,
            appDatabase,
            wallHavenNetwork,
        ),
        pagingSourceFactory = {
            wallpapersDao.pagingSource(queryString = searchQuery.toQueryString())
        }
    ).flow.map {
        it.map { entity ->
            entity.asWallpaper()
        }
    }.flowOn(ioDispatcher)

    override fun popularTags() = flow {
        withContext(ioDispatcher) {
            popularTagNetworkResource.init()
        }
        emitAll(popularTagNetworkResource.data)
    }

    override suspend fun refreshPopularTags() {
        popularTagNetworkResource.refresh()
    }


    override fun wallpaper(wallpaperWallhavenId: String): Flow<Resource<Wallpaper?>> {
        val resource = getWallpaperNetworkResource(wallpaperWallhavenId)
        return flow {
            withContext(ioDispatcher) {
                resource.init()
            }
            emitAll(resource.data)
        }
    }

    private suspend fun upsertLastUpdated(
        category: LastUpdatedCategory,
        lastUpdatedOn: Instant = Clock.System.now(),
    ) {
        val lastUpdated = lastUpdatedDao.getByKey(category.key)
            ?.copy(lastUpdatedOn = lastUpdatedOn)
            ?: LastUpdatedEntity(
                id = 0,
                key = category.key,
                lastUpdatedOn = lastUpdatedOn,
            )
        lastUpdatedDao.upsert(lastUpdated)
    }
}
