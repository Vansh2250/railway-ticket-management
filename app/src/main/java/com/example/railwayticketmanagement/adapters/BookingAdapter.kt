package com.example.railwayticketmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.railwayticketmanagement.R
import com.example.railwayticketmanagement.models.Booking
import com.google.android.material.button.MaterialButton

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onCancelClicked: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingCardId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvBookingCardStatus)
        val tvTrainTitle: TextView = itemView.findViewById(R.id.tvBookingTrainTitle)
        val tvRoute: TextView = itemView.findViewById(R.id.tvBookingRoute)
        val tvJourneyDate: TextView = itemView.findViewById(R.id.tvBookingJourneyDate)
        val tvPassengerCount: TextView = itemView.findViewById(R.id.tvBookingPassengerCount)
        val tvPassengersSummary: TextView = itemView.findViewById(R.id.tvBookingPassengersSummary)
        val tvTotalFare: TextView = itemView.findViewById(R.id.tvBookingTotalFare)
        val btnCancel: MaterialButton = itemView.findViewById(R.id.btnCancelBooking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val context = holder.itemView.context

        holder.tvBookingId.text = "Booking #${booking.bookingId}"
        holder.tvTrainTitle.text = "${booking.trainName} (#${booking.trainNumber})"
        holder.tvRoute.text = "${booking.source} ➔ ${booking.destination}"
        holder.tvJourneyDate.text = "Date: ${booking.journeyDate}"
        holder.tvPassengerCount.text = "${booking.passengerCount} Passenger(s)"
        holder.tvTotalFare.text = "₹${String.format("%.2f", booking.totalFare)}"

        // Summary of passengers
        if (booking.passengers.isNotEmpty()) {
            val names = booking.passengers.joinToString(", ") { "${it.passengerName} (${it.age})" }
            holder.tvPassengersSummary.text = "Passengers: $names"
            holder.tvPassengersSummary.visibility = View.VISIBLE
        } else {
            holder.tvPassengersSummary.visibility = View.GONE
        }

        // Status badge and cancel button
        if (booking.bookingStatus.equals("CONFIRMED", ignoreCase = true)) {
            holder.tvStatus.text = "CONFIRMED"
            holder.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_confirmed)
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
            holder.btnCancel.visibility = View.VISIBLE
            holder.btnCancel.isEnabled = true
            holder.btnCancel.setOnClickListener {
                onCancelClicked(booking)
            }
        } else {
            holder.tvStatus.text = "CANCELLED"
            holder.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_cancelled)
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_red))
            holder.btnCancel.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateList(newBookings: List<Booking>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
}
