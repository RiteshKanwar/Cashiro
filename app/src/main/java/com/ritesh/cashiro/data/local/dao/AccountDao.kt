package com.ritesh.cashiro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritesh.cashiro.data.local.entity.AccountEntity

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT * FROM account_entity")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM account_entity WHERE id = :accountId")
    suspend fun getAccountById(accountId: Int): AccountEntity?

    @Query("UPDATE account_entity SET isMainAccount = :isMain WHERE id = :id")
    suspend fun updateMainAccount(id: Int, isMain: Boolean)

    @Update
    suspend fun updateAccounts(accounts: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)
}
