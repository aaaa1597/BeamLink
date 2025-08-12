package com.tks.beamlink

import android.app.Activity
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
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tks.beamlink.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    /************/
    /* メンバ定義 */
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

    /**********/
    /* 定型関数 */
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

        /* 送信リストRecyclerViewの初期化 */
        val filesrvw = view.findViewById<RecyclerView>(R.id.ryv_files)
        filesrvw.layoutManager = LinearLayoutManager(requireContext())
        val emptyList = mutableListOf<Fileinfo>()
        emptyList.add(Fileinfo(R.drawable.icon_movie, "a23456789---56789###456789@@@456789$$$456789"))
        emptyList.add(Fileinfo(R.drawable.icon_binary, "c23456789---56789"))
        filesrvw.adapter = FileinfoAdpter(emptyList) {
            /* TODO: アイテム選択時の処理 */
            fileinfo -> Log.d("aaaaa", "fileinfo=(${fileinfo.name}, ${fileinfo.resId})")
        }
        emptyList.add(Fileinfo(R.drawable.icon_text, "d23456789---56789###456789@@@456789$$$456789"))
    }

    /************************/
    /* RecyclerView補助クラス */
    /* Fileinfoクラス */
    data class Fileinfo(@DrawableRes val resId: Int, val name: String)
    /* FilesItemクラスAdpter */
    class FileinfoAdpter(private val files: List<Fileinfo>, private val onItemClick: (Fileinfo) -> Unit):
            RecyclerView.Adapter<FileinfoAdpter.FileinfoViewHolder>() {
        inner class FileinfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val thumbnailImv: ImageView = itemView.findViewById(R.id.imv_file_thumbnail)
            val nameView: TextView = itemView.findViewById(R.id.txt_mime)

            init {
                itemView.setOnClickListener {
                    onItemClick(files[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): FileinfoViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.ryv_files_items, parent, false)
            return FileinfoViewHolder(view)
        }

        override fun onBindViewHolder(holder: FileinfoViewHolder, position: Int) {
            val fileinfo = files[position]
            holder.thumbnailImv.setImageResource(fileinfo.resId)
            holder.nameView.text = fileinfo.name
        }

        override fun getItemCount(): Int = files.size
    }

    companion object{}
}