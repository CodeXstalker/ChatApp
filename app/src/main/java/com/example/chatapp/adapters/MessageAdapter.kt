package com.example.chatapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.modalClasses.MessageModal
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import org.json.JSONObject

class MessageAdapter(
    val context: Context,
    val messageList: ArrayList<MessageModal>,
    private val chatRecyclerView: RecyclerView
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2
    val ITEM_SEND_PAY = 3
    val ITEM_RECEIVE_PAY = 4
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)

        return when (viewType) {
            ITEM_RECEIVE -> {
                val view = inflater.inflate(R.layout.recieve_layout, parent, false)
                ReceiveViewHolder(view)
            }

            ITEM_SENT -> {
                val view = inflater.inflate(R.layout.send_layout, parent, false)
                SentViewHolder(view)
            }

            ITEM_SEND_PAY -> {
                val view = inflater.inflate(R.layout.send_pay_layout, parent, false)
                SendPayViewHolder(view)
            }

            ITEM_RECEIVE_PAY -> {
                val view = inflater.inflate(R.layout.receive_pay_layout, parent, false)
                ReceivePayViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }


    }


    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        return if (currentMessage.isPayment) {
            if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
                ITEM_SEND_PAY
            } else {
                ITEM_RECEIVE_PAY
            }
        } else {
            if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId) {
                ITEM_SENT
            } else {
                ITEM_RECEIVE
            }
        }


    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        when (holder.itemViewType) {
            ITEM_SENT -> {
                val viewHolder = holder as SentViewHolder
                viewHolder.thisIsSentMessage.text = currentMessage.message
            }

            ITEM_RECEIVE -> {
                val viewHolder = holder as ReceiveViewHolder
                viewHolder.thisIsReceiveMessage.text = currentMessage.message
            }

            ITEM_SEND_PAY -> {
                val viewHolder = holder as SendPayViewHolder
                viewHolder.thisIsPurpose.text = currentMessage.purpose
                viewHolder.thisIsAmount.text = currentMessage.amount.toString()

                if (currentMessage.payFlag == true) {

                    viewHolder.review.isEnabled = false
                    viewHolder.review.isClickable = false
                    viewHolder.review.text = currentMessage.paymentId
                    viewHolder.thisIsAmount.text = viewHolder.thisIsAmount.text as String + " " + "received"

                }else{

                    viewHolder.review.setOnClickListener {
                        val alertDialogBuilder = AlertDialog.Builder(context)
                        alertDialogBuilder.setTitle("Review Details")
                        alertDialogBuilder.setMessage("Purpose: ${currentMessage.purpose}\nAmount: ${currentMessage.amount}")
                        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }

                }



            }


            ITEM_RECEIVE_PAY -> {
                val viewHolder = holder as ReceivePayViewHolder
                viewHolder.thisIsPurpose.text = currentMessage.purpose
                viewHolder.thisIsAmount.text = currentMessage.amount.toString()

                Log.d("6625", currentMessage.payFlag.toString())

                if (currentMessage.payFlag == true) {

                    viewHolder.reviewAndPay.isEnabled = false
                    viewHolder.reviewAndPay.isClickable = false
                    viewHolder.reviewAndPay.text = currentMessage.paymentId
                    viewHolder.thisIsAmount.text =
                        viewHolder.thisIsAmount.text as String + " " + "sent"

                }else{

                    viewHolder.reviewAndPay.setOnClickListener {
                        // Handle review and pay action here
                        Toast.makeText(context, "Review and pay is working", Toast.LENGTH_SHORT).show()
                        val alertDialogBuilder = AlertDialog.Builder(context)
                        alertDialogBuilder.setTitle("Review and Pay request")
                        alertDialogBuilder.setMessage("Purpose: ${currentMessage.purpose}\nAmount: ${currentMessage.amount}")
                        alertDialogBuilder.setPositiveButton("Pay") { dialog, _ ->
                            allowUserToPayViaRazorPay(currentMessage)
                            dialog.dismiss()
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }

                }


            }
        }

        if (position == messageList.size - 1 && holder.itemViewType != ITEM_SEND_PAY) {
            chatRecyclerView.post {
                chatRecyclerView.smoothScrollToPosition(position)
            }
        }
    }


    private fun allowUserToPayViaRazorPay(currentMessage: MessageModal) {
        try {
            Toast.makeText(context, "Pay is Working", Toast.LENGTH_SHORT).show()
            val checkout = Checkout()
            checkout.setImage(R.drawable.img)
            val activity: Activity = context as Activity
            val options = JSONObject()
            options.put("name", "ChatApp") // Replace "ChapApp" with "ChatApp"
            options.put("description", "Test payment") // Add a description for the payment
            options.put("send_sms_hash", true)
            options.put("allow_rotation", false)
            options.put("currency", "INR")

            // Set the amount (replace 100 with the actual amount in paise)
            val amount = currentMessage.amount
            if (amount != null) {
                options.put("amount", amount * 100)
            }

            val preFill = JSONObject()
            preFill.put("contact", "9876543210") // Replace with the user's contact number
            options.put("prefill", preFill)
            checkout.setKeyID("rzp_test_btyUaBohkgFiPq")
            checkout.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thisIsSentMessage: TextView = itemView.findViewById(R.id.ThisIsSentMessage)
    }


    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thisIsReceiveMessage: TextView = itemView.findViewById(R.id.ThisIsReceivedMessage)
    }


    class SendPayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thisIsPurpose: TextView = itemView.findViewById(R.id.ThisIsPurpose)
        val thisIsAmount: TextView = itemView.findViewById(R.id.ThisIsAmount)
        val review: Button = itemView.findViewById(R.id.Review)
    }


    class ReceivePayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thisIsPurpose: TextView = itemView.findViewById(R.id.ThisIsPurpose)
        val thisIsAmount: TextView = itemView.findViewById(R.id.ThisIsAmount)
        val reviewAndPay: Button = itemView.findViewById(R.id.ReviewAndPay)
    }
}



