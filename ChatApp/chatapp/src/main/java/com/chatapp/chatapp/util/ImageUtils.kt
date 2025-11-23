package com.chatapp.chatapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

object ImageUtils {

    /**
     * Сжимает и изменяет размер изображения для оптимальной загрузки
     *
     * @param context Контекст приложения
     * @param imageUri URI изображения
     * @param quality Качество сжатия (0-100)
     * @param maxSize Максимальный размер стороны в пикселях
     * @return Сжатое изображение в виде ByteArray
     */
    suspend fun compressAndResizeImage(
        context: Context,
        imageUri: Uri,
        quality: Int = 75,
        maxSize: Int = 512
    ): ByteArray = withContext(Dispatchers.Default) {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Получаем размеры изображения без загрузки в память
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Вычисляем sample size для уменьшения памяти
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
            options.inJustDecodeBounds = false

            // Декодируем с уменьшенным размером
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream, null, options)
                    ?: throw IllegalStateException("Не удалось декодировать изображение")

                // Масштабируем до финального размера
                val scaledBitmap = scaleBitmap(bitmap, maxSize)

                // Сжимаем в WebP
                ByteArrayOutputStream().use { outputStream ->
                    scaledBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)

                    // Освобождаем память
                    if (scaledBitmap != bitmap) bitmap.recycle()
                    scaledBitmap.recycle()

                    outputStream.toByteArray()
                }
            } ?: throw IllegalStateException("Не удалось открыть поток изображения")
        } ?: throw IllegalStateException("Не удалось открыть изображение")
    }

    /**
     * Вычисляет оптимальный inSampleSize для загрузки изображения
     *
     * @param options Опции изображения с размерами
     * @param reqWidth Требуемая ширина
     * @param reqHeight Требуемая высота
     * @return Значение inSampleSize (степень двойки)
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Масштабирует bitmap с сохранением пропорций
     *
     * @param bitmap Исходный bitmap
     * @param maxSize Максимальный размер стороны
     * @return Масштабированный bitmap
     */
    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val finalWidth: Int
        val finalHeight: Int

        if (width > height) {
            finalWidth = maxSize
            finalHeight = (maxSize / ratio).toInt()
        } else {
            finalHeight = maxSize
            finalWidth = (maxSize * ratio).toInt()
        }

        return bitmap.scale(finalWidth, finalHeight)
    }

    /**
     * Проверяет, является ли URI изображением
     */
    fun isImageUri(context: Context, uri: Uri): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true
    }

    /**
     * Получает размер изображения без его загрузки в память
     */
    fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                Pair(options.outWidth, options.outHeight)
            }
        } catch (e: Exception) {
            null
        }
    }
}