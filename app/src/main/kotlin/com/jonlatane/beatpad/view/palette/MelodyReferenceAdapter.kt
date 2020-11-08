package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.ViewGroup
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.uiThread
import java.util.*


class MelodyReferenceAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView,
	private val partHolder: PartHolder
) : SmartAdapter<MelodyReferenceHolder>() {
	val partPosition: Int get() = partHolder.adapterPosition
	val part: Part? get() = viewModel.palette.parts.getOrNull(partPosition)
	override fun getItemCount(): Int = part?.melodies?.let { it.size + 1 } ?: 0

	val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.Callback() {
		init {
		}
		override fun getMovementFlags(
			recyclerView: RecyclerView,
			viewHolder: RecyclerView.ViewHolder
		): Int {
			return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
		}

		override fun onMove(
			recyclerView: RecyclerView,
			viewHolder: RecyclerView.ViewHolder,
			target: RecyclerView.ViewHolder
		): Boolean {
			if(viewHolder == null || target == null) return false
			val fromPosition = viewHolder.adapterPosition
			val toPosition = target.adapterPosition
			(viewHolder as? MelodyReferenceHolder)?.editMelodyMenu?.dismiss()
			return part?.let { part ->
				if(toPosition >= part.melodies.size || fromPosition >= part.melodies.size) return false
				if (fromPosition < toPosition) {
					for (i in fromPosition until toPosition) {
						Collections.swap(part.melodies, i, i + 1)
					}
				} else {
					for (i in fromPosition downTo toPosition + 1) {
						Collections.swap(part.melodies, i, i - 1)
					}
				}
				notifyItemMoved(fromPosition, toPosition)
				notifyItemChanged(fromPosition)
				notifyItemChanged(toPosition)
				return true
			} ?: false
		}

		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
			// TODO("not implemented")
		}

		override fun isLongPressDragEnabled(): Boolean = true
	}).also { it.attachToRecyclerView(recyclerView) }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyReferenceHolder {
		return recyclerView.run {
			MelodyReferenceHolder(viewModel, this@MelodyReferenceAdapter)
		}
	}

	override fun onBindViewHolder(holder: MelodyReferenceHolder, position: Int) {
		super.onBindViewHolder(holder, position)
		holder.onPositionChanged()
	}

	fun createAndOpenDrawnMelody(
		newMelody: Melody<*> = PaletteStorage.baseMelody.also {
			// Don't try to conform drum parts to harmony
			if((part?.instrument as? MIDIInstrument)?.drumTrack == true) {
				it.limitedToNotesInHarmony = false
			}
		}
	) {
		while(BeatClockPaletteConsumer.palette?.parts?.flatMap { it.melodies.map { it.id } }?.contains(newMelody.id) == true) {
			newMelody.relatedMelodies.add(newMelody.id)
			newMelody.id = UUID.randomUUID()
		}
		BeatClockPaletteConsumer.section?.melodies?.add(
			Section.MelodyReference(newMelody, 0.5f, Section.PlaybackType.Indefinite)
		)
		part?.apply {
			melodies.add(newMelody)
			notifyItemInserted(melodies.size - 1)
		}
		doAsync {
			Thread.sleep(300L)
			uiThread {
				viewModel.editingMelody = newMelody
			}
		}
	}
}