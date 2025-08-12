package com.tks.beamlink

import android.app.Activity
import android.content.Context
import android.content.Intent
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
        /* ファイル種別のリスト */
        val listitemArray = listOf(
            MimetypeItem(R.drawable.icon_image, getString(R.string.field_1_image)),
            MimetypeItem(R.drawable.icon_movie, getString(R.string.field_2_movie)),
            MimetypeItem(R.drawable.icon_audio, getString(R.string.field_3_audio)),
            MimetypeItem(R.drawable.icon_text, getString(R.string.field_4_document)),
            MimetypeItem(R.drawable.icon_binary, getString(R.string.field_5_all)),
        )
        /* ファイル種別のリストを設定 */
        _binding.ltvMime.adapter = MimetypeAdpter(requireContext(), listitemArray)
        /* ItemClickListener設定 */
        _binding.ltvMime.setOnItemClickListener  { parent, view, position, id ->
            /* クリックアイテムを取得 */
            val item = parent.adapter.getItem(position) as MimetypeItem
            val (mimeTypes, initUri) = when(item.mime) {
                getString(R.string.field_1_image) -> Pair(arrayOf("image/*"), MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                getString(R.string.field_2_movie) -> Pair(arrayOf("video/*"), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                getString(R.string.field_3_audio) -> Pair(arrayOf("audio/*"), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                getString(R.string.field_4_document) -> Pair(arrayOf("text/*","application/*"), MediaStore.Downloads.EXTERNAL_CONTENT_URI)
                getString(R.string.field_5_all) -> Pair(arrayOf("*/*"),  MediaStore.Downloads.EXTERNAL_CONTENT_URI)
                else -> Pair(arrayOf("*/*"),  MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }
            Log.d("aaaaa", "mime=${item.mime} resid=${item.resId}")
            /* Intentで起動 */
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                /* 複数のファイル選択を許可 */
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                /* 選択可能なMIMEタイプを指定 */
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                /* EXTRA_INITIAL_URIを設定して、初期表示ディレクトリを指定 */
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
            }
            /* Intentで起動 */
            pickMultipleFilesLauncher.launch(intent)
        }
    }

    /* Mimetypeクラス */
    data class MimetypeItem(@DrawableRes val resId: Int, val mime: String)
    /* MimetypeクラスAdpter */
    class MimetypeAdpter(context: Context, mimetypes: List<MimetypeItem>): ArrayAdapter<MimetypeItem>(context, 0, mimetypes) {
        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val mimetype = getItem(pos)
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.listitem_mime, parent, false)
            view.findViewById<ImageView>(R.id.imv_icon).setImageResource(mimetype?.resId ?: R.drawable.icon_binary)
            view.findViewById< TextView>(R.id.txt_mime).setText(mimetype?.mime)
            return view
        }
    }

    companion object {
    }
}