package com.tks.beamlink

import android.app.Activity
import android.content.Intent
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

class MainFragment : Fragment() {
    private lateinit var _binding: FragmentMainBinding
    private val pickMultipleFilesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        if(result.data==null) return@registerForActivityResult
        if (result.data!!.clipData != null) {
            /* 複数のファイルが選択された場合 */
            val count = result.data!!.clipData!!.itemCount
            for (idx in 0 until count) {
                val uri = result.data!!.clipData!!.getItemAt(idx).uri
                Log.d("FilePicker", "Selected file URI: $uri")
            }
        }
        else if (result.data!!.data != null) {
            /* 単一のファイルが選択された場合 */
            val uri = result.data!!.data
            Log.d("FilePicker", "Selected file URI: $uri")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.btnTmp.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                /* 複数のファイル選択を許可 */
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                /* 選択可能なMIMEタイプを指定 */
                type = "*/*"
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("image/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("audio/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("video/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("text/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("application/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("*/*")) }
//        _binding.btnTmp.setOnClickListener { v -> pickMultipleDocumentsLauncher.launch(arrayOf("image/*", "audio/*", "video/*", "text/*", "application/*", "*/*")) }
                /* EXTRA_INITIAL_URIを設定して、初期表示ディレクトリを指定 */
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }

            /* Intentを直接起動 */
            pickMultipleFilesLauncher.launch(intent)
        }
    }

    companion object {
    }
}