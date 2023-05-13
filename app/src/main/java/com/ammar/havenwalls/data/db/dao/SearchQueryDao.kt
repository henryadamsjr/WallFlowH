package com.ammar.havenwalls.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ammar.havenwalls.data.db.entity.SearchQueryEntity

@Dao
interface SearchQueryDao {
    @Query("SELECT * FROM search_query WHERE id = :id")
    suspend fun getById(id: Long): SearchQueryEntity?

    @Query("SELECT * FROM search_query")
    suspend fun getAll(): List<SearchQueryEntity>

    @Query(
        """
        SELECT *
        FROM search_query
        WHERE query_string = :queryString
        """
    )
    suspend fun getBySearchQuery(queryString: String): SearchQueryEntity?

    @Query("SELECT COUNT(1) FROM search_query")
    suspend fun count(): Int

    @Query("DELETE FROM search_query")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsert(vararg searchQuery: SearchQueryEntity): List<Long>

    @Upsert
    suspend fun upsert(searchQueries: Collection<SearchQueryEntity>): List<Long>
}
