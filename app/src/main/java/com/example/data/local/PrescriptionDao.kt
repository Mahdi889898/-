package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY timestamp DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Delete
    suspend fun deletePrescription(prescription: PrescriptionEntity)

    @Query("DELETE FROM prescriptions")
    suspend fun clearAll()
}
