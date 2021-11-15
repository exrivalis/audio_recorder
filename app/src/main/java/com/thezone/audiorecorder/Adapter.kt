package com.thezone.audiorecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class Adapter(var records : ArrayList<AudioRecord>, var listener: OnItemClickListener) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private var editMode = false

    fun isEditMode(): Boolean { return editMode}
    fun setEditMode(mode: Boolean) {
        if(editMode != mode){
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
        var tvFilename : TextView = itemView.findViewById(R.id.tvFilename)
        var tvMeta : TextView = itemView.findViewById(R.id.tvMeta)
        var checkbox : CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemClickListener(position)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION)
                listener.onItemLongClickListener(position)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION){
            var record = records[position]

            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)

            holder.tvFilename.text = record.filename
            holder.tvMeta.text = "${record.duration } $strDate"

            if(editMode){
                holder.checkbox.visibility = View.VISIBLE
                holder.checkbox.isChecked = record.isChecked
            }else{
                holder.checkbox.visibility = View.GONE
                holder.checkbox.isChecked = false
            }
        }
    }
}