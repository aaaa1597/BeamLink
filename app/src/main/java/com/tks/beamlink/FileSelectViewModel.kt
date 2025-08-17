package com.tks.beamlink

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tks.beamlink.FileSelectFragment.Fileinfo
import kotlinx.coroutines.flow.update

class FileSelectViewModel(application: Application) : AndroidViewModel(application) {
    private val _filesFlow = MutableStateFlow<List<Fileinfo>>(mutableListOf())
    val filesFlow = _filesFlow.asStateFlow()

    init {
        /* ToDO デバッグ用の初期データ */
        val initialList = mutableListOf<Fileinfo>()
        initialList.add(Fileinfo(R.drawable.icon_document, getApplication()))
        initialList.add(Fileinfo(R.drawable.icon_binary, getApplication()))
        initialList.add(Fileinfo(R.drawable.icon_text, getApplication()))
        _filesFlow.value = initialList
    }

    fun addItem(item: Fileinfo) {
        _filesFlow.update { currentList ->
            currentList + item  /* 新しいリストを生成して要素を追加 */
        }
    }

    fun removeItem(position: Int) {
        _filesFlow.update { currentList ->
            if (position < currentList.size) {
                currentList.toMutableList().apply { removeAt(position) }
            } else {
                currentList /* 変更がない場合は元のリストを返す */
            }
        }
    }
}