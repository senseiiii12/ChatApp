package com.chatapp.chatapp.di

import android.content.Context
import com.chatapp.chatapp.domain.AuthRepository
import com.chatapp.chatapp.data.AuthRepositoryImpl
import com.chatapp.chatapp.data.FriendRequestRepositoryImpl
import com.chatapp.chatapp.data.UsersRepositoryImpl
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.data.MessageRepositoryImpl
import com.chatapp.chatapp.domain.FriendRequestRepository
import com.chatapp.chatapp.domain.UsersRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun providesAuthRepositoryImpl(firebaseAuth: FirebaseAuth) : AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun providesRepositoryMessageImpl(firebaseFirestore: FirebaseFirestore) : MessageRepository {
        return MessageRepositoryImpl(firebaseFirestore)
    }
    @Provides
    @Singleton
    fun providesFriendRequestRepositoryImpl() : FriendRequestRepository {
        return FriendRequestRepositoryImpl()
    }

    @Provides
    @Singleton
    fun providesUsersRepositoryImpl(
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        @ApplicationContext context: Context
    ) : UsersRepository {
        return UsersRepositoryImpl(firebaseFirestore,firebaseAuth,context)
    }

}