package com.example.core

import androidx.room.*
import java.util.UUID

@Entity(tableName = "system_logs")
data class LogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val type: String,
    val message: String,
    val severity: String = "INFO"
)

@Dao
interface LogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): kotlinx.coroutines.flow.Flow<List<LogEntity>>

    @Insert
    suspend fun insert(log: LogEntity)

    @Query("DELETE FROM system_logs")
    suspend fun deleteAll()
}
