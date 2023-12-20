package com.salesrep.app.di

import android.content.Context
import androidx.room.Room
import com.salesrep.app.App
import com.salesrep.app.dao.*
//import com.salesrep.app.dao.RemoteKeysDao
//import com.salesrep.app.dao.RoutesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "salesrep.db")
      .fallbackToDestructiveMigration()
      .build()
  }

//  @Provides
//  fun providePostsDao(appDatabase: AppDatabase): RoutesDao {
//    return appDatabase.getRoutesDao()
//  }

//  @Provides
//  fun provideChatDao(appDatabase: AppDatabase): RemoteKeysDao {
//    return appDatabase.remoteKeyDao()
//  }

  @Provides
  fun provideUserTrackDao(appDatabase: AppDatabase): UserTrackDao {
    return appDatabase.userTrackDao()
  }

  @Provides
  fun provideUpdateSurveyDao(appDatabase: AppDatabase): UpdateSurveyDao {
    return appDatabase.getUpdateSurveyDao()
  }

  @Provides
  fun provideUpdateActivityDao(appDatabase: AppDatabase): UpdateActivityDao {
    return appDatabase.getUpdateActivity()
  }

  @Provides
  fun provideUpdateRouteDao(appDatabase: AppDatabase): UpdateRouteDao {
    return appDatabase.getUpdateRoutesDao()
  }

  @Provides
  fun provideRouteDao(appDatabase: AppDatabase): RoutesDao {
    return appDatabase.getRoutesDao()
  }

  @Provides
  fun provideRouteActivityDao(appDatabase: AppDatabase): RouteActivityDao {
    return appDatabase.getRouteActivityDao()
  }

  @Provides
  fun provideCreateOrderDao(appDatabase: AppDatabase): CreateOrderDao {
    return appDatabase.createOrderDao()
  }

  @Provides
  fun provideSaveOrderDao(appDatabase: AppDatabase): SaveOrderDao {
    return appDatabase.saveOrderDao()
  }

  @Provides
  fun provideInventoryDao(appDatabase: AppDatabase): InventoryDao {
    return appDatabase.inventoryDao()
  }

  @Provides
  fun provideCreateMovement(appDatabase: AppDatabase): CreateMovementDao {
    return appDatabase.createMovementDao()
  }

  @Provides
  fun provideChangeMovementDao(appDatabase: AppDatabase): ChangeStatusMovementDao {
    return appDatabase.changeMovementStatusDao()
  }

}