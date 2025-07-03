package com.ritesh.cashiro.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.ritesh.cashiro.data.currency.repository.CurrencyRepository
import com.ritesh.cashiro.data.data_management.repository.DataManagementRepository
import com.ritesh.cashiro.data.local.TransactionDatabase
import com.ritesh.cashiro.data.local.dao.AccountDao
import com.ritesh.cashiro.data.local.dao.CategoryDao
import com.ritesh.cashiro.data.local.dao.SubCategoryDao
import com.ritesh.cashiro.data.local.dao.TransactionDao
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.ActivityLogRepository
import com.ritesh.cashiro.domain.repository.ActivityLogRepositoryImpl
import com.ritesh.cashiro.domain.repository.CategoryRepository
import com.ritesh.cashiro.domain.repository.ProfileRepository
import com.ritesh.cashiro.domain.repository.SettingsRepository
import com.ritesh.cashiro.domain.repository.SubCategoryRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingViewModel
import com.ritesh.cashiro.widgets.WidgetThemeHelper
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val DATASTORE_FILE_NAME = "settings.preferences_pb"

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) }
        )
    }

    @Provides
    @Singleton
    fun provideOnBoardingViewModel(
        profileRepository: ProfileRepository,
        accountRepository: AccountRepository,
        currencyRepository: CurrencyRepository,
        categoryRepository: CategoryRepository,
        subCategoryRepository: SubCategoryRepository,
        dataStore: DataStore<Preferences>
    ): OnBoardingViewModel {
        return OnBoardingViewModel(
            profileRepository,
            accountRepository,
            currencyRepository,
            categoryRepository,
            subCategoryRepository,
            dataStore
        )
    }

    // FIXED: Remove WidgetUpdateUtil dependency to break circular dependency
    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>, activityLogUtils: ActivityLogUtils): SettingsRepository {
        return SettingsRepository(dataStore, activityLogUtils )
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        dataStore: DataStore<Preferences>,
        @ApplicationContext context: Context
    ): ProfileRepository {
        return ProfileRepository(dataStore, context)
    }

    @Provides
    @Singleton
    fun provideTransactionDatabase(application: Application): TransactionDatabase {
        return Room.databaseBuilder(
            application,
            TransactionDatabase::class.java,
            "transaction_database"
        ).build()
    }

    @Provides
    fun provideTransactionDao(transactionDatabase: TransactionDatabase): TransactionDao {
        return transactionDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    fun provideCategoryDao(database: TransactionDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepository(categoryDao)
    }

    @Provides
    fun provideSubCategoryDao(database: TransactionDatabase): SubCategoryDao {
        return database.subCategoryDao()
    }

    @Provides
    @Singleton
    fun provideSubCategoryRepository(subCategoryDao: SubCategoryDao): SubCategoryRepository {
        return SubCategoryRepository(subCategoryDao)
    }

    @Provides
    fun provideAccountDao(database: TransactionDatabase): AccountDao {
        return database.accountDao()
    }

    // FIXED: Keep WidgetUpdateUtil but it will be injected via events, not constructor
    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        transactionRepository: TransactionRepository
    ): AccountRepository {
        return AccountRepository(accountDao, transactionRepository)
    }

    @Provides
    @Singleton
    fun provideDataManagementRepository(
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        subCategoryDao: SubCategoryDao,
        transactionDao: TransactionDao,
        database: TransactionDatabase,
        settingsRepository: SettingsRepository,
        profileRepository: ProfileRepository,
        @ApplicationContext context: Context
    ): DataManagementRepository {
        return DataManagementRepository(
            accountDao,
            categoryDao,
            subCategoryDao,
            transactionDao,
            database,
            settingsRepository,
            profileRepository,
            context
        )
    }

    // Add WidgetUpdateUtil as a separate provider
    @Provides
    @Singleton
    fun provideWidgetUpdateUtil(
        @ApplicationContext context: Context,
        widgetThemeHelper: WidgetThemeHelper // Direct injection now works!
    ): WidgetUpdateUtil {
        return WidgetUpdateUtil(context, widgetThemeHelper)
    }

    // WidgetThemeHelper provider remains the same
    @Provides
    @Singleton
    fun provideWidgetThemeHelper(
        settingsRepository: SettingsRepository,
        @ApplicationContext context: Context
    ): WidgetThemeHelper {
        return WidgetThemeHelper(settingsRepository, context)
    }

    @Provides
    @Singleton
    fun provideActivityLogRepository(
        database: TransactionDatabase
    ): ActivityLogRepository {
        return ActivityLogRepositoryImpl(database.activityLogDao())
    }

    @Provides
    @Singleton
    fun provideActivityLogUtils(
        activityLogRepository: ActivityLogRepository
    ): ActivityLogUtils {
        return ActivityLogUtils(activityLogRepository)
    }

}