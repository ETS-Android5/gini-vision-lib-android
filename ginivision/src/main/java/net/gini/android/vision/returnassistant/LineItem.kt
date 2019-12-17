package net.gini.android.vision.returnassistant

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.util.*

/**
 * Created by Alpar Szotyori on 11.12.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */
@Parcelize
class LineItem(
        val id: String,
        val description: String,
        val quantity: Int,
        val rawAmount: String
) : Parcelable {

    @IgnoredOnParcel
    val amount: BigDecimal
    @IgnoredOnParcel
    val totalAmount: BigDecimal
    @IgnoredOnParcel
    val currency: Currency?

    init {
        rawAmount.split(":").let {
            check(it.size == 2) {
                "Invalid amount format. Expected <Amount>:<Currency Code>, but got: $rawAmount"
            }
            amount = BigDecimal(it[0])
            totalAmount = amount.times(BigDecimal(quantity))
            currency = try {
                Currency.getInstance(it[1])
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun toString() = "LineItem(id=$id, description=$description, quantity=$quantity, rawAmount=$rawAmount, amount=$amount, totalAmount=$totalAmount, currency=$currency)"

    override fun equals(other: Any?) = other is LineItem
            && id == other.id
            && description == other.description
            && quantity == other.quantity
            && amount == other.amount
            && totalAmount == other.totalAmount
            && rawAmount == other.rawAmount
            && currency == other.currency

    override fun hashCode() = Objects.hash(id, description, quantity, rawAmount, amount,
            totalAmount, currency)

    fun copy() = LineItem(id, description, quantity, rawAmount)

}