package com.tks.beamlink

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.tks.beamlink.databinding.FragmentMainBinding


class MainFragment : Fragment() {
    private lateinit var _binding: FragmentMainBinding
    /* ファイル選択ランチャー */
    private val pickMultipleFilesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        /* ファイルリストの戻り */
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
        /* ファイル選択ボタンをripple effectに */
        val rippleDrawable = RippleDrawable(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.ripple_pink)),
                                                                 ContextCompat.getDrawable(requireContext(), R.drawable.btn_style), null)
        _binding.btnChooser.background = rippleDrawable
        /* ファイル選択ボタンにClickListener設定 */
        _binding.btnChooser.setOnClickListener { v ->
            /* インテント生成 */
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                /* 複数のファイル選択を許可 */
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                /* 選択可能なMIMEタイプを指定 */
                type = "*/*"
                /* EXTRA_INITIAL_URIを設定して、初期表示ディレクトリを指定 */
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            }
            /* 生成Intentで起動 */
            pickMultipleFilesLauncher.launch(intent)
        }
    }

    companion object {
    }
}