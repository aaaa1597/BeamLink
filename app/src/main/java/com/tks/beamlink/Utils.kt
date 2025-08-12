package com.tks.beamlink

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri


class Utils {
    companion object {
        /* UriからBitmapを生成 */
        fun getResizedBitmapFromUri(context: Context, uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? {
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
        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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