package com.tks.beamlink

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tks.beamlink.databinding.DialogFileinfoBinding
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
                Log.d("aaaaa", "1-Selected file URI: $uri")
                val bmp = Utils.generateThumbnail(requireContext(), uri)
                Log.d("aaaaa", "1-Selected file bmp.size(${bmp?.width},${bmp?.height})")
                val adapter = _binding.ryvFiles.adapter as FileinfoAdpter
                val fileinfo = Utils.generateFileinfoFromUri(requireContext(), uri)
                adapter.addItem(fileinfo)
                adapter.notifyItemInserted(adapter.itemCount - 1)

            }
        }
        else if (result.data!!.data != null) {
            /* 単一のファイルが選択された場合 */
            val uri = result.data!!.data ?: return@registerForActivityResult
            Log.d("aaaaa", "2-Selected file URI: $uri")
            val adapter = _binding.ryvFiles.adapter as FileinfoAdpter
            val fileinfo = Utils.generateFileinfoFromUri(requireContext(), uri)
            adapter.addItem(fileinfo)
            adapter.notifyItemInserted(adapter.itemCount - 1)
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

        val emptyList = mutableListOf<Fileinfo>()
        val adaper = FileinfoAdpter(emptyList) { fileinfo ->
            val dialogView: DialogFileinfoBinding = DialogFileinfoBinding.inflate(LayoutInflater.from(requireContext()))
            dialogView.txtName.text = fileinfo.name
            dialogView.imvFileicon.setImageBitmap(fileinfo.bmp)
            dialogView.txvSize.text = Utils.formatFileSize(fileinfo.size)
            dialogView.txvUpdate.text = fileinfo.update
            AlertDialog.Builder(requireContext())
                .setView(dialogView.root)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            }

        /* 送信リストRecyclerViewの初期化 */
        val filesrvw = view.findViewById<RecyclerView>(R.id.ryv_files)
        filesrvw.layoutManager = LinearLayoutManager(requireContext())
        filesrvw.adapter = adaper

        /* RecyclerViewからのswip削除実装 */
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false /* ドラッグ移動は不要 */
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                adaper.removeItem(position)
            }

            override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float/*スワイプ距離 */, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView

                /* テキスト描画（💥削除！） */
                val textPaint = Paint().apply {
                    color = Color.RED
                    textSize = 48f
                    typeface = Typeface.DEFAULT_BOLD
                }
                val text = "💥${getString(R.string.deletestr)}!!"
                val textX = if (dX > 0) itemView.left + 150f else itemView.right - 300f
                val textY = itemView.top + itemView.height / 2f + 16f
                canvas.drawText(text, textX, textY, textPaint)

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(filesrvw)



		/* TODO: デバッグ用削除予定 */
        emptyList.add(Fileinfo(R.drawable.icon_document, requireContext()))
        emptyList.add(Fileinfo(R.drawable.icon_binary, requireContext()))
        emptyList.add(Fileinfo(R.drawable.icon_text, requireContext()))
        /* デバッグ用削除予定 ここまで */
    }

    /************************/
    /* RecyclerView補助クラス */
    /* Fileinfoクラス */
    class Fileinfo {
        var bmp: Bitmap?
        var mimeType: String?
        var name: String?
        var size: Long?
        var update: String?

        constructor(bmp: Bitmap?, mimeType: String?, name: String?, size: Long?, update: String?) {
            this.bmp = bmp
            this.mimeType = mimeType
            this.name = name
            this.size = size
            this.update = update
        }
        constructor(@DrawableRes resId: Int, context: Context) {
            this.bmp = Utils.getResizedBitmapFromDrawableRes(context, resId )
            this.mimeType = "image/*"
            this.name = context.resources.getResourceEntryName(resId) + ".png" // 拡張子は適宜変更
            this.size = bmp?.byteCount?.toLong()
            this.update = Utils.formatDateTime(context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime)
        }
    }
    /* FilesItemクラスAdpter */
    class FileinfoAdpter(private val files: MutableList<Fileinfo>, private val onItemClick: (Fileinfo) -> Unit):
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
            holder.thumbnailImv.setImageBitmap(fileinfo.bmp)
            holder.nameView.text = fileinfo.name
        }

        override fun getItemCount(): Int = files.size

        fun removeItem(position: Int) {
            files.removeAt(position)
            notifyItemRemoved(position)
        }

        fun addItem(fileinfo: Fileinfo) {
            files.add(fileinfo)
        }
    }

    companion object{}
}
