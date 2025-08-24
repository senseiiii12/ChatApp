package com.chatapp.chatapp.util.ImageLoader

import android.content.Context
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageLoaderViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {
    val imageLoader = ImageLoader.Builder(context)
        .diskCache { DiskCache.Builder().directory(context.cacheDir.resolve("image_cache")).maxSizeBytes(50L * 1024 * 1024).build() }
        .memoryCache { MemoryCache.Builder(context).maxSizePercent(0.25).build() }
        .build()
}