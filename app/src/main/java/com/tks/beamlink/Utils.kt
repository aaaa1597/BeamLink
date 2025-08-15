package com.tks.beamlink

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import java.io.File


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

        /* DrawableResからBitmapを生成 */
        fun getResizedBitmapFromDrawableRes(context: Context, @DrawableRes drawableResId: Int, reqWidth: Int = 200, reqHeight: Int = 200): Bitmap? {
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

        fun generateThumbnail(context: Context, uri: Uri, thumbnailSize: Int = 200): Bitmap? {
            val contentResolver = context.contentResolver

            /* MIMEタイプ取得 */
            val mimeType = contentResolver.getType(uri) ?: return getResizedBitmapFromDrawableRes(context, R.drawable.icon_binary)

            return when {
                /* 画像サムネイル */
                mimeType.startsWith("image/") -> {
                    return contentResolver.openInputStream(uri)?.use { input ->
                                val bitmap = BitmapFactory.decodeStream(input)
                                bitmap?.scale(thumbnailSize, thumbnailSize)
                            }
                }

                /* 動画サムネイル */
                mimeType.startsWith("video/") -> {
                    val tempFile = File.createTempFile("thumb", null, context.cacheDir)
                                        contentResolver.openInputStream(uri)?.use { input ->
                                            tempFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                    return contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        ThumbnailUtils.createVideoThumbnail(
                            tempFile,
                            android.util.Size(thumbnailSize, thumbnailSize),
                            null
                        )
                    }
                }

                /* PDFサムネイル */
                mimeType == "application/pdf" -> {
                    /* PDFサムネイル（1ページ目） */
                    return contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        val pdfRenderer = PdfRenderer(pfd)
                        val page = pdfRenderer.openPage(0)

                        /* サイズ比を維持してサムネイル生成 */
                        val ratio = page.width.toFloat() / page.height
                        val width: Int
                        val height: Int
                        if (ratio > 1) {
                            width = thumbnailSize
                            height = (thumbnailSize / ratio).toInt()
                        } else {
                            height = thumbnailSize
                            width = (thumbnailSize * ratio).toInt()
                        }

                        val bitmap = createBitmap(width, height)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        pdfRenderer.close()
                        return bitmap
                    }
                }

                /* その他アイコン */
                mimeType.startsWith("text/") -> {
                    return getResizedBitmapFromDrawableRes(context, R.drawable.icon_text)
                }

                /* その他アイコン */
                mimeType == "application/octet-stream" -> {
                    return getResizedBitmapFromDrawableRes(context, R.drawable.icon_binary)
                }

                /* ドキュメントアイコン */
                mimeType.startsWith("application/") -> {
                    return getResizedBitmapFromDrawableRes(context, R.drawable.icon_audio)
                }

                /* 音楽アイコン */
                mimeType.startsWith("audio/") -> {
                    return getResizedBitmapFromDrawableRes(context, R.drawable.icon_audio)
                }

                else -> {
                    /* 不明な場合はデフォルトアイコン */
                    getResizedBitmapFromDrawableRes(context, R.drawable.icon_binary)
                }
            }
        }
    }
}