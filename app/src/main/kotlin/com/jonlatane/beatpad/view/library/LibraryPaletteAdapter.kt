package com.jonlatane.beatpad.view.library

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.storage.Storage
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.recyclerview.v7._RecyclerView

class LibraryPaletteAdapter(
  val viewModel: LibraryViewModel,
  val recyclerView: _RecyclerView
): RecyclerView.Adapter<LibraryPaletteHolder>(), AnkoLogger {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
   = LibraryPaletteHolder(viewModel, recyclerView)

  override fun getItemCount() = Storage.getPalettes(recyclerView.context).size

  override fun onBindViewHolder(holder: LibraryPaletteHolder, position: Int) {
    //TODO("not implemented")
  }

}