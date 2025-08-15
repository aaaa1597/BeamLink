package com.tks.beamlink

import com.tks.beamlink.MainFragment.Fileinfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class Utils {
    companion object {
        /* Uriからファイル名を取得 */
        fun generateFileinfoFromUri(context: Context, uri: Uri): Fileinfo {
            /* サムネイル画像取得 */
            val bmp = generateThumbnail(context, uri)
            /* mimeType取得 */
            val mimeType = context.contentResolver.getType(uri)
            /* ファイル名取得, ファイルサイズ取得 */
            var name: String? = null
            var size: Long? = null
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (it.moveToFirst()) {
                    if (nameIndex != -1) name = it.getString(nameIndex)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                    else { context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                            size = pfd.statSize
                        }
                    }
                }
            }
            /* ファイル更新日付取得 */
            val updateStr = getLastModifiedDateStr(context, uri)
            /* Fileinfoクラス返却 */
            return Fileinfo(bmp, mimeType, name, size, updateStr)
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

                /* textアイコン */
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

        fun formatFileSize(size: Long?): String {
            if(size == null) return "??? B"
            val sizeInBytes: Long = size
            val kilo = 1024L
            val mega = kilo * 1024
            val giga = mega * 1024
            val tera = giga * 1024

            return when {
                sizeInBytes < kilo -> "$sizeInBytes B"
                sizeInBytes < mega -> String.format(Locale.JAPAN, "%.1f KB", sizeInBytes.toDouble() / kilo)
                sizeInBytes < giga -> String.format(Locale.JAPAN, "%.1f MB", sizeInBytes.toDouble() / mega)
                sizeInBytes < tera -> String.format(Locale.JAPAN, "%.1f GB", sizeInBytes.toDouble() / giga)
                else -> String.format(Locale.JAPAN, "%.1f TB", sizeInBytes.toDouble() / tera)
            }
        }

        fun formatDateTime(timestampms: Long): String {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            return formatter.format(Instant.ofEpochMilli(timestampms))
        }

        fun getLastModifiedDateStr(context: Context, uri: Uri): String {
            val timestampms = getLastModifiedDate(context, uri)
            if(timestampms==null) return "???"

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                             .withZone(ZoneId.systemDefault())
            return formatter.format(Instant.ofEpochMilli(timestampms))
        }
        fun getLastModifiedDate(context: Context, uri: Uri): Long? {
            return when {
                /* MediaStore由来（content://media） */
                uri.authority?.startsWith("media") == true -> {
                    getMediaStoreLastModified(context, uri)
                }

                /* DocumentFile由来（content://com.android.providers...） */
                uri.scheme == "content" || uri.scheme == "file" -> {
                    getDocumentFileLastModified(context, uri)
                }

                else -> null /* 未対応のスキーム */
            }
        }

        fun getMediaStoreLastModified(context: Context, uri: Uri): Long? {
            val projection = arrayOf(MediaStore.MediaColumns.DATE_MODIFIED)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                if (index != -1 && cursor.moveToFirst()) {
                    val seconds = cursor.getLong(index)
                    return seconds * 1000   /* ミリ秒に変換 */
                }
            }
            return null
        }

        fun getDocumentFileLastModified(context: Context, uri: Uri): Long? {
            val docFile = DocumentFile.fromSingleUri(context, uri)
            return docFile?.lastModified()?.takeIf { it > 0 }
        }
    }
}