package com.example.chatapp.modalClasses

class MessageModal {

    var message: String? = null
    var senderId: String? = null
    var isPayment: Boolean = false
    var purpose: String? = null
    var amount: Double? = null
    var paymentId: String? = null
    var payFlag: Boolean? = false
    var senderMessageId: String? = null
    var receiverMessageId: String? = null


    constructor()

    constructor(message: String?, senderId: String?) {
        this.message = message
        this.senderId = senderId
    }

    constructor(
        purpose: String?,
        amount: Double?,
        senderId: String?,
        isPayment: Boolean,
        payFlag: Boolean?,
        paymentId: String?
    ) {
        this.purpose = purpose
        this.senderId = senderId
        this.amount = amount
        this.isPayment = isPayment
        this.payFlag = payFlag
        this.paymentId = paymentId

    }
}

