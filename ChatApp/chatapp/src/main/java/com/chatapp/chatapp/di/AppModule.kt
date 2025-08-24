package com.chatapp.chatapp.di

import android.app.Application
import android.content.Context
import com.chatapp.chatapp.features.auth.domain.AuthRepository
import com.chatapp.chatapp.features.auth.data.AuthRepositoryImpl
import com.chatapp.chatapp.core.data.FriendRequestRepositoryImpl
import com.chatapp.chatapp.core.data.UsersRepositoryImpl
import com.chatapp.chatapp.features.chat.domain.MessageRepository
import com.chatapp.chatapp.features.chat.data.MessageRepositoryImpl
import com.chatapp.chatapp.features.chat_rooms.data.ChatRoomsRepositoryImpl
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.core.domain.FriendRequestRepository
import com.chatapp.chatapp.core.domain.UsersRepository
import com.chatapp.chatapp.features.my_friends.data.MyFriendsRepositoryImpl
import com.chatapp.chatapp.features.my_friends.domain.MyFriendsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
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
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings { })
            }
        }
    }

    @Provides
    @Singleton
    fun providesAuthRepositoryImpl(
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ) : AuthRepository {
        return AuthRepositoryImpl(firebaseFirestore,firebaseAuth)
    }

    @Provides
    @Singleton
    fun providesRepositoryMessageImpl(firebaseFirestore: FirebaseFirestore) : MessageRepository {
        return MessageRepositoryImpl(firebaseFirestore)
    }
    @Provides
    @Singleton
    fun providesRepositoryChatRoomsImpl(firebaseFirestore: FirebaseFirestore) : ChatRoomsRepository {
        return ChatRoomsRepositoryImpl(firebaseFirestore)
    }

    @Provides
    @Singleton
    fun providesFriendRequestRepositoryImpl(
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ) : FriendRequestRepository {
        return FriendRequestRepositoryImpl(firebaseFirestore,firebaseAuth)
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

    @Provides
    @Singleton
    fun providesMyFriendsRepositoryImpl(
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ) : MyFriendsRepository {
        return MyFriendsRepositoryImpl(firebaseFirestore,firebaseAuth)
    }

}