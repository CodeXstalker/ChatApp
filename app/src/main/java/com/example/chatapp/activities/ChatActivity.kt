package com.example.chatapp.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MessageAdapter
import com.example.chatapp.R
import com.example.chatapp.modalClasses.MessageModal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener


class ChatActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var payBtn: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<MessageModal>
    private lateinit var firebaseReference: DatabaseReference


    // Use to create a unique room for sender and receiver pair
    private var receiverRoom: String? = null
    private var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar = findViewById(R.id.Toolbar)
        chatRecyclerView = findViewById(R.id.ChatRecyclerview)
        messageBox = findViewById(R.id.MessageBox)
        sendBtn = findViewById(R.id.SendBtn)
        payBtn = findViewById(R.id.PayBtn)

        /**
         * Razor pay integration
         */
        Checkout.preload(applicationContext)
        val co = Checkout()
        co.setKeyID("rzp_test_btyUaBohkgFiPq")

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        firebaseReference = FirebaseDatabase.getInstance().reference

        val intent: Intent = intent
        val name: String? = intent.getStringExtra("name")
        val receiverUid: String? = intent.getStringExtra("UID")
        val senderUid: String? = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = "$receiverUid$senderUid"
        receiverRoom = "$senderUid$receiverUid"

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, chatRecyclerView)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        firebaseReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(MessageModal::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }
                    messageAdapter.notifyDataSetChanged()

                    if (messageList.isNotEmpty()) {
                        chatRecyclerView.post {
                            chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the onCancelled event if needed
                }
            })

        sendBtn.setOnClickListener {
            allowUserToSendMessages()
        }

        payBtn.setOnClickListener {
            allowUserToPay()
        }

        supportActionBar?.title = name
    }

    private fun allowUserToSendMessages() {
        val message: String = messageBox.text.toString().trim() // Trim any leading/trailing spaces
        if (message.isNotEmpty()) {
            val senderUid = FirebaseAuth.getInstance().currentUser?.uid
            val receiverUid = intent.getStringExtra("UID")
            senderRoom = "$receiverUid$senderUid"
            receiverRoom = "$senderUid$receiverUid"

            val messageObject = MessageModal(message, senderUid)

            firebaseReference.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    firebaseReference.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }

            messageBox.setText("")
        }
    }

    private fun allowUserToPay() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Inflate the custom layout for the dialog
        val dialogView = layoutInflater.inflate(R.layout.payment_dialog, null)
        alertDialogBuilder.setView(dialogView)

        val purpose = dialogView.findViewById<EditText>(R.id.Purpose)
        val amount = dialogView.findViewById<EditText>(R.id.Amount)

        alertDialogBuilder.setPositiveButton("Send Request") { _, _ ->
            val getPurpose = purpose.text.toString()
            val getAmount = amount.text.toString().toDouble()

            val paymentMessage = MessageModal(
                getPurpose,
                getAmount,
                FirebaseAuth.getInstance().currentUser?.uid,
                isPayment = true,
                payFlag = false,
                paymentId = "null"
            )

            // Save the payment message to Firebase for both sender and receiver rooms
            val senderMessageRef =
                firebaseReference.child("chats").child(senderRoom!!).child("messages").push()
            val receiverMessageRef =
                firebaseReference.child("chats").child(receiverRoom!!).child("messages").push()

            paymentMessage.senderMessageId = senderMessageRef.key
            paymentMessage.receiverMessageId = receiverMessageRef.key

            senderMessageRef.setValue(paymentMessage).addOnSuccessListener {
                receiverMessageRef.setValue(paymentMessage).addOnSuccessListener {
                    Log.d("8820", paymentMessage.senderMessageId.toString())
                    Log.d("8821", paymentMessage.receiverMessageId.toString())
                }.addOnFailureListener {
                    // Handle the failure if needed
                }
            }.addOnFailureListener {
                // Handle the failure if needed
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onPaymentSuccess(p0: String?) {
        try {
            val lastPaymentMessage = messageList.lastOrNull { it.isPayment }

            updatePayFlagAndPaymentIdInDBAndDisableButton(p0)

        } catch (e: Exception) {
            Toast.makeText(this, "pay error", Toast.LENGTH_SHORT).show()
            Log.e("PaymentError", "An error occurred during payment success handling: ${e.message}")
        }
    }

    private fun updatePayFlagAndPaymentIdInDBAndDisableButton(p0: String?) {
        firebaseReference.child("chats").child(receiverRoom.toString()).child("messages")
            .child(messageList.last().senderMessageId.toString()).child("payFlag").setValue(true)
        firebaseReference.child("chats").child(receiverRoom.toString()).child("messages")
            .child(messageList.last().senderMessageId.toString()).child("paymentId").setValue(p0)
        firebaseReference.child("chats").child(senderRoom.toString()).child("messages")
            .child(messageList.last().receiverMessageId.toString()).child("payFlag").setValue(true)
        firebaseReference.child("chats").child(senderRoom.toString()).child("messages")
            .child(messageList.last().receiverMessageId.toString()).child("paymentId").setValue(p0)
        Toast.makeText(this, "Payment Successful. Payment ID: $p0", Toast.LENGTH_SHORT).show()

    }


    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this, "Payment Error: $p1", Toast.LENGTH_SHORT).show()
    }


}


