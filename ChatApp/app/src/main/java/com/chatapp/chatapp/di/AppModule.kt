package com.chatapp.chatapp.di

import com.chatapp.chatapp.domain.AuthRepository
import com.chatapp.chatapp.data.AuthRepositoryImpl
import com.chatapp.chatapp.data.FirebaseDatabaseRepositoryImpl
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.data.MessageRepositoryImpl
import com.chatapp.chatapp.domain.FirebaseDatabaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providesRepositoryImpl(firebaseAuth: FirebaseAuth) : AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun providesRepositoryMessageImpl(firebaseFirestore: FirebaseFirestore) : MessageRepository {
        return MessageRepositoryImpl(firebaseFirestore)
    }

    @Provides
    @Singleton
    fun providesRepositoryFirebaseDatabaseImpl(
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ) : FirebaseDatabaseRepository {
        return FirebaseDatabaseRepositoryImpl(firebaseFirestore,firebaseAuth)
    }

}