package com.example.railwayticketmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.railwayticketmanagement.R
import com.example.railwayticketmanagement.models.Train
import com.google.android.material.button.MaterialButton

class TrainAdapter(
    private var trains: List<Train>,
    private var journeyDate: String,
    private val onBookClicked: (Train) -> Unit
) : RecyclerView.Adapter<TrainAdapter.TrainViewHolder>() {

    class TrainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTrainName: TextView = itemView.findViewById(R.id.tvItemTrainName)
        val tvTrainNumber: TextView = itemView.findViewById(R.id.tvItemTrainNumber)
        val tvDepTime: TextView = itemView.findViewById(R.id.tvItemDepTime)
        val tvArrTime: TextView = itemView.findViewById(R.id.tvItemArrTime)
        val tvSource: TextView = itemView.findViewById(R.id.tvItemSource)
        val tvDestination: TextView = itemView.findViewById(R.id.tvItemDestination)
        val tvJourneyDate: TextView = itemView.findViewById(R.id.tvItemJourneyDate)
        val tvFare: TextView = itemView.findViewById(R.id.tvItemFare)
        val tvSeats: TextView = itemView.findViewById(R.id.tvItemSeats)
        val btnBook: MaterialButton = itemView.findViewById(R.id.btnItemBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val train = trains[position]
        holder.tvTrainName.text = train.trainName
        holder.tvTrainNumber.text = "#${train.trainNumber}"
        holder.tvDepTime.text = train.departureTime
        holder.tvArrTime.text = train.arrivalTime
        holder.tvSource.text = train.source
        holder.tvDestination.text = train.destination
        holder.tvJourneyDate.text = if (journeyDate.isNotEmpty()) journeyDate else "Daily"
        holder.tvFare.text = "₹${train.fare.toInt()}"

        if (train.availableSeats > 0) {
            holder.tvSeats.text = "${train.availableSeats} Seats"
            holder.tvSeats.setTextColor(holder.itemView.context.getColor(R.color.primary))
            holder.btnBook.isEnabled = true
            holder.btnBook.text = "Book Ticket"
        } else {
            holder.tvSeats.text = "Sold Out"
            holder.tvSeats.setTextColor(holder.itemView.context.getColor(R.color.accent_red))
            holder.btnBook.isEnabled = false
            holder.btnBook.text = "No Seats"
        }

        holder.btnBook.setOnClickListener {
            onBookClicked(train)
        }
    }

    override fun getItemCount(): Int = trains.size

    fun updateList(newTrains: List<Train>, newJourneyDate: String) {
        this.trains = newTrains
        this.journeyDate = newJourneyDate
        notifyDataSetChanged()
    }
}
