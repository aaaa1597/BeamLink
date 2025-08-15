package com.tks.beamlink

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap


class Utils {
    companion object {
        /* Uriからファイル名を取得 */
        fun getPropertyFromUri(context: Context, uri: Uri): Triple<String?, String?, Long?> {
            val mimeType = context.contentResolver.getType(uri)
            var name: String? = null
            var size: Long? = null
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst()) {
                    if (nameIndex != -1) name = it.getString(nameIndex)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                }
            }
            return Triple(mimeType, name, size)
        }

        /* UriからBitmapを生成 */
        fun getResizedBitmapFromDrawableRes(context: Context, @DrawableRes drawableResId: Int, reqWidth: Int = 128, reqHeight: Int = 128): Bitmap? {
            val drawable: Drawable? = ContextCompat.getDrawable(context, drawableResId)
            drawable ?: return null

            /* 元のサイズ */
            val srcWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
            val srcHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

            val ratio = minOf(reqWidth.toFloat() / srcWidth, reqHeight.toFloat() / srcHeight)
            val finalWidth = (srcWidth * ratio).toInt().coerceAtLeast(1)
            val finalHeight = (srcHeight* ratio).toInt().coerceAtLeast(1)

            /* 描画用Bitmap作成 */
            val bitmap = createBitmap(finalWidth, finalHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, finalWidth, finalHeight)
            drawable.draw(canvas)

            return bitmap
        }

        /* UriからBitmapを生成 */
        fun getResizedBitmapFromUri(context: Context, uri: Uri, targetWidth: Int = 128, targetHeight: Int = 128): Bitmap? {
            var inputStream = context.contentResolver.openInputStream(uri) ?: return null

            /* 1. 画像のサイズを読み込む (inJustDecodeBoundsをtrueに設定) */
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)

            /* 2. ダウンサンプルサイズを計算 */
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)

            /* 3. 再度ストリームを開き、ダウンサンプリングして画像を読み込む */
            inputStream.close()
            inputStream = context.contentResolver.openInputStream(uri) ?: return null
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)

            inputStream.close()
            return bitmap
        }

        /* サンプルサイズを計算するヘルパーメソッド */
        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (width < reqWidth || height < reqHeight)
                return inSampleSize /* 幅/高さのどちらかが要求より小さければ要求通り */

            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2
            }
            return inSampleSize
        }
    }
}