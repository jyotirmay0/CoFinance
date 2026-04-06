package com.finance.app.di

import com.finance.app.data.repository.BillRepositoryImpl
import com.finance.app.data.repository.GoalRepositoryImpl
import com.finance.app.data.repository.TransactionRepositoryImpl
import com.finance.app.domain.repository.BillRepository
import com.finance.app.domain.repository.GoalRepository
import com.finance.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl
    ): GoalRepository

    @Binds
    @Singleton
    abstract fun bindBillRepository(
        impl: BillRepositoryImpl
    ): BillRepository
}