package com.tks.beamlink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.tks.beamlink.databinding.FragmentMainBinding
import androidx.core.net.toUri

class MainFragment : Fragment() {
    private lateinit var _binding: FragmentMainBinding
    private val pickMultipleDocumentsLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        uris: List<Uri> ->
        // ここで選択された複数のファイルURIを処理します
        if (uris.isNotEmpty()) {
            Log.d("FilePicker", "Selected files count=: ${uris.size}")
            for (uri in uris) {
                Log.d("FilePicker", "Selected file URI: $uri")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("image/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("audio/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("video/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("text/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("application/*")) }
        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("*/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("image/*", "audio/*", "video/*", "text/*", "application/*", "*/*")) }

//        _binding.btnTmp.setOnClickListener { v ->
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                // 選択可能なMIMEタイプを指定
//                type = "*/*"
//                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//
//                // DownloadsフォルダのContent URIを指定
//                val initialUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
//
//                // EXTRA_INITIAL_URIを設定
//                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
//            }
//
//            Log.d("aaaaa", "intent.type.toString()=${intent.type.toString()}")
//            pickMultipleDocumentsLauncher.launch(arrayOf(intent.type.toString()))
//        }
    }

    companion object {
    }
}