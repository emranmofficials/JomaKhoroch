package com.example.data.cloud

import androidx.annotation.Keep
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NotebookEntity
import com.example.data.local.entity.PersonalLoanEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionItemEntity
import com.example.domain.accounting.LedgerTransactionType
import com.example.domain.accounting.LoanDirection
import com.example.domain.accounting.PaymentMethod
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

private val ALLOWED_BOOK_TYPES = setOf("PERSONAL", "FAMILY", "BUSINESS", "CUSTOM")
private val ALLOWED_CATEGORY_TYPES = setOf("INCOME", "EXPENSE")
private val ALLOWED_LOAN_DIRECTIONS = LoanDirection.entries.map { it.name }.toSet()
private val ALLOWED_TX_TYPES = LedgerTransactionType.entries.map { it.name }.toSet()
private val ALLOWED_PAYMENT_METHODS = PaymentMethod.entries.map { it.name }.toSet()
private val ALLOWED_NOTE_CATEGORIES = setOf("PERSONAL", "SHOPPING_LIST", "PHONE_ADDRESS", "IMPORTANT")
private val ALLOWED_TASK_TYPES = setOf("DAILY", "BILL", "LOAN_REPAYMENT", "CUSTOM")
private val YEAR_MONTH_REGEX = Regex("^[0-9]{4}-[0-9]{2}$")

private fun String.bounded(maxLen: Int, fallback: String = ""): String =
    trim().take(maxLen).ifEmpty { fallback.take(maxLen) }

@Keep
@IgnoreExtraProperties
data class CloudUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val deviceId: String = "",
    val syncRevision: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = buildMap {
        put("userId", uid)
        put("displayName", displayName.bounded(80, "User"))
        email?.trim()?.take(200)?.takeIf { it.isNotEmpty() }?.let { put("email", it) }
        put("deviceId", deviceId.bounded(128, "android"))
        put("syncRevision", syncRevision.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("displayName", displayName.bounded(80, "User"))
        email?.trim()?.take(200)?.takeIf { it.isNotEmpty() }?.let { put("email", it) }
        put("deviceId", deviceId.bounded(128, "android"))
        put("syncRevision", syncRevision.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class CloudNotebook(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val name: String = "",
    val type: String = "CUSTOM",
    val iconKey: String = "wallet",
    val openingBalancePoisha: Long = 0L,
    val archived: Boolean = false,
    val clientCreatedAtMs: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "localId" to localId.coerceAtLeast(1L),
        "name" to name.bounded(100, "Book"),
        "type" to (type.takeIf { it in ALLOWED_BOOK_TYPES } ?: "CUSTOM"),
        "iconKey" to iconKey.bounded(40, "wallet"),
        "openingBalancePoisha" to openingBalancePoisha.coerceAtLeast(0L),
        "archived" to archived,
        "clientCreatedAtMs" to clientCreatedAtMs.coerceAtLeast(0L),
        "clientUpdatedAtMs" to clientUpdatedAtMs.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "name" to name.bounded(100, "Book"),
        "type" to (type.takeIf { it in ALLOWED_BOOK_TYPES } ?: "CUSTOM"),
        "iconKey" to iconKey.bounded(40, "wallet"),
        "openingBalancePoisha" to openingBalancePoisha.coerceAtLeast(0L),
        "archived" to archived,
        "clientCreatedAtMs" to clientCreatedAtMs.coerceAtLeast(0L),
        "clientUpdatedAtMs" to clientUpdatedAtMs.coerceAtLeast(0L),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toEntity(): NotebookEntity = NotebookEntity(
        id = localId,
        name = name.ifBlank { "Book" },
        type = type.takeIf { it in ALLOWED_BOOK_TYPES } ?: "CUSTOM",
        iconKey = iconKey.ifBlank { "wallet" },
        openingBalancePoisha = openingBalancePoisha.coerceAtLeast(0L),
        isArchived = archived,
        createdAt = clientCreatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

    companion object {
        fun fromEntity(uid: String, entity: NotebookEntity, nowMs: Long): CloudNotebook = CloudNotebook(
            userId = uid,
            localId = entity.id,
            name = entity.name,
            type = entity.type,
            iconKey = entity.iconKey,
            openingBalancePoisha = entity.openingBalancePoisha,
            archived = entity.isArchived,
            clientCreatedAtMs = entity.createdAt,
            clientUpdatedAtMs = nowMs
        )
    }
}

@Keep
@IgnoreExtraProperties
data class CloudCategory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val nameBn: String = "",
    val nameEn: String = "",
    val type: String = "EXPENSE",
    val iconKey: String = "category",
    val colorHex: String = "#176B45",
    val defaultCategory: Boolean = false,
    val active: Boolean = true,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "localId" to localId.coerceAtLeast(1L),
        "notebookId" to notebookId.coerceAtLeast(1L),
        "nameBn" to nameBn.bounded(80, "অন্যান্য"),
        "nameEn" to nameEn.bounded(80, "Other"),
        "type" to (type.takeIf { it in ALLOWED_CATEGORY_TYPES } ?: "EXPENSE"),
        "iconKey" to iconKey.bounded(40, "category"),
        "colorHex" to colorHex.bounded(16, "#176B45").let { if (it.length >= 4) it else "#176B45" },
        "defaultCategory" to defaultCategory,
        "active" to active,
        "clientUpdatedAtMs" to clientUpdatedAtMs.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "notebookId" to notebookId.coerceAtLeast(1L),
        "nameBn" to nameBn.bounded(80, "অন্যান্য"),
        "nameEn" to nameEn.bounded(80, "Other"),
        "type" to (type.takeIf { it in ALLOWED_CATEGORY_TYPES } ?: "EXPENSE"),
        "iconKey" to iconKey.bounded(40, "category"),
        "colorHex" to colorHex.bounded(16, "#176B45").let { if (it.length >= 4) it else "#176B45" },
        "defaultCategory" to defaultCategory,
        "active" to active,
        "clientUpdatedAtMs" to clientUpdatedAtMs.coerceAtLeast(0L),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toEntity(): CategoryEntity = CategoryEntity(
        id = localId,
        notebookId = notebookId,
        nameBn = nameBn.ifBlank { "অন্যান্য" },
        nameEn = nameEn.ifBlank { "Other" },
        type = type.takeIf { it in ALLOWED_CATEGORY_TYPES } ?: "EXPENSE",
        iconKey = iconKey.ifBlank { "category" },
        colorHex = colorHex.ifBlank { "#176B45" },
        isDefault = defaultCategory,
        isActive = active
    )

    companion object {
        fun fromEntity(uid: String, entity: CategoryEntity, nowMs: Long): CloudCategory = CloudCategory(
            userId = uid,
            localId = entity.id,
            notebookId = entity.notebookId,
            nameBn = entity.nameBn,
            nameEn = entity.nameEn,
            type = entity.type,
            iconKey = entity.iconKey,
            colorHex = entity.colorHex,
            defaultCategory = entity.isDefault,
            active = entity.isActive,
            clientUpdatedAtMs = nowMs
        )
    }
}

@Keep
@IgnoreExtraProperties
data class CloudShop(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val shopCategory: String = "",
    val openingBalancePoisha: Long = 0L,
    val notes: String = "",
    val archived: Boolean = false,
    val clientCreatedAtMs: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "localId" to localId.coerceAtLeast(1L),
        "notebookId" to notebookId.coerceAtLeast(1L),
        "name" to name.bounded(120, "Shop"),
        "ownerName" to ownerName.take(120),
        "phone" to phone.take(40),
        "address" to address.take(250),
        "shopCategory" to shopCategory.take(80),
        "openingBalancePoisha" to openingBalancePoisha.coerceAtLeast(0L),
        "notes" to notes.take(500),
        "archived" to archived,
        "clientCreatedAtMs" to clientCreatedAtMs.coerceAtLeast(0L),
        "clientUpdatedAtMs" to clientUpdatedAtMs.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "notebookId" to notebookId.coerceAtLeast(1L),
        "name" to name.bounded(120, "Shop"),
        "ownerName" to ownerName.take(120),
        "phone" to phone.take(40),
        "address" to address.take(250),
        "shopCategory" to shopCategory.take(80),
        "openingBalancePoisha" to openingBalancePoisha.coerceAtLeast(0L),
        "notes" to notes.take(500),
        "archived" to archived,
        "clientCreatedAtMs" to clientCreatedAtMs.coerceAtLeast(0L),
        "clientUpdatedAtMs" to clientUpdatedAtMs.coerceAtLeast(0L),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toEntity(): ShopEntity = ShopEntity(
        id = localId,
        notebookId = notebookId,
        name = name.ifBlank { "Shop" },
        ownerName = ownerName,
        phone = phone,
        address = address,
        shopCategory = shopCategory,
        openingBalancePoisha = openingBalancePoisha.coerceAtLeast(0L),
        notes = notes,
        isArchived = archived,
        createdAt = clientCreatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

    companion object {
        fun fromEntity(uid: String, entity: ShopEntity, nowMs: Long): CloudShop = CloudShop(
            userId = uid,
            localId = entity.id,
            notebookId = entity.notebookId,
            name = entity.name,
            ownerName = entity.ownerName,
            phone = entity.phone,
            address = entity.address,
            shopCategory = entity.shopCategory,
            openingBalancePoisha = entity.openingBalancePoisha,
            notes = entity.notes,
            archived = entity.isArchived,
            clientCreatedAtMs = entity.createdAt,
            clientUpdatedAtMs = nowMs
        )
    }
}

@Keep
@IgnoreExtraProperties
data class CloudPersonalLoan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val direction: String = "GIVEN",
    val personName: String = "",
    val phone: String = "",
    val principalPoisha: Long = 0L,
    val loanDate: Long = 0L,
    val dueDate: Long? = null,
    val description: String = "",
    val settled: Boolean = false,
    val clientCreatedAtMs: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = buildMap {
        put("userId", uid)
        put("localId", localId.coerceAtLeast(1L))
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("direction", direction.takeIf { it in ALLOWED_LOAN_DIRECTIONS } ?: LoanDirection.GIVEN.name)
        put("personName", personName.bounded(120, "Person"))
        put("phone", phone.take(40))
        put("principalPoisha", principalPoisha.coerceAtLeast(0L))
        put("loanDate", loanDate.coerceAtLeast(0L))
        dueDate?.let { put("dueDate", it) }
        put("description", description.take(500))
        put("settled", settled)
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("direction", direction.takeIf { it in ALLOWED_LOAN_DIRECTIONS } ?: LoanDirection.GIVEN.name)
        put("personName", personName.bounded(120, "Person"))
        put("phone", phone.take(40))
        put("principalPoisha", principalPoisha.coerceAtLeast(0L))
        put("loanDate", loanDate.coerceAtLeast(0L))
        if (dueDate != null) put("dueDate", dueDate) else put("dueDate", FieldValue.delete())
        put("description", description.take(500))
        put("settled", settled)
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toEntity(): PersonalLoanEntity = PersonalLoanEntity(
        id = localId,
        notebookId = notebookId,
        direction = direction.takeIf { it in ALLOWED_LOAN_DIRECTIONS } ?: LoanDirection.GIVEN.name,
        personName = personName.ifBlank { "Person" },
        phone = phone,
        principalPoisha = principalPoisha.coerceAtLeast(0L),
        loanDate = loanDate.takeIf { it > 0L } ?: System.currentTimeMillis(),
        dueDate = dueDate,
        description = description,
        isSettled = settled,
        createdAt = clientCreatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

    companion object {
        fun fromEntity(uid: String, entity: PersonalLoanEntity, nowMs: Long): CloudPersonalLoan =
            CloudPersonalLoan(
                userId = uid,
                localId = entity.id,
                notebookId = entity.notebookId,
                direction = entity.direction,
                personName = entity.personName,
                phone = entity.phone,
                principalPoisha = entity.principalPoisha,
                loanDate = entity.loanDate,
                dueDate = entity.dueDate,
                description = entity.description,
                settled = entity.isSettled,
                clientCreatedAtMs = entity.createdAt,
                clientUpdatedAtMs = nowMs
            )
    }
}

@Keep
@IgnoreExtraProperties
data class CloudTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val type: String = "EXPENSE",
    val totalAmountPoisha: Long = 0L,
    val paidAmountPoisha: Long = 0L,
    val dueAmountPoisha: Long = 0L,
    val categoryId: Long? = null,
    val shopId: Long? = null,
    val loanId: Long? = null,
    val paymentMethod: String = "CASH",
    val note: String = "",
    val transactionDate: Long = 0L,
    val deleted: Boolean = false,
    val items: List<Map<String, Any>>? = null,
    val clientCreatedAtMs: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = buildMap {
        put("userId", uid)
        put("localId", localId.coerceAtLeast(1L))
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("type", type.takeIf { it in ALLOWED_TX_TYPES } ?: LedgerTransactionType.EXPENSE.name)
        put("totalAmountPoisha", totalAmountPoisha.coerceAtLeast(0L))
        put("paidAmountPoisha", paidAmountPoisha.coerceAtLeast(0L))
        put("dueAmountPoisha", dueAmountPoisha.coerceAtLeast(0L))
        categoryId?.let { put("categoryId", it) }
        shopId?.let { put("shopId", it) }
        loanId?.let { put("loanId", it) }
        put("paymentMethod", paymentMethod.takeIf { it in ALLOWED_PAYMENT_METHODS } ?: PaymentMethod.CASH.name)
        put("note", note.take(500))
        put("transactionDate", transactionDate.coerceAtLeast(0L))
        put("deleted", deleted)
        items?.take(50)?.takeIf { it.isNotEmpty() }?.let { put("items", it) }
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("type", type.takeIf { it in ALLOWED_TX_TYPES } ?: LedgerTransactionType.EXPENSE.name)
        put("totalAmountPoisha", totalAmountPoisha.coerceAtLeast(0L))
        put("paidAmountPoisha", paidAmountPoisha.coerceAtLeast(0L))
        put("dueAmountPoisha", dueAmountPoisha.coerceAtLeast(0L))
        if (categoryId != null) put("categoryId", categoryId) else put("categoryId", FieldValue.delete())
        if (shopId != null) put("shopId", shopId) else put("shopId", FieldValue.delete())
        if (loanId != null) put("loanId", loanId) else put("loanId", FieldValue.delete())
        put("paymentMethod", paymentMethod.takeIf { it in ALLOWED_PAYMENT_METHODS } ?: PaymentMethod.CASH.name)
        put("note", note.take(500))
        put("transactionDate", transactionDate.coerceAtLeast(0L))
        put("deleted", deleted)
        val boundedItems = items?.take(50).orEmpty()
        if (boundedItems.isNotEmpty()) put("items", boundedItems) else put("items", FieldValue.delete())
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toEntity(): LedgerTransactionEntity = LedgerTransactionEntity(
        id = localId,
        notebookId = notebookId,
        type = type.takeIf { it in ALLOWED_TX_TYPES } ?: LedgerTransactionType.EXPENSE.name,
        totalAmountPoisha = totalAmountPoisha.coerceAtLeast(0L),
        paidAmountPoisha = paidAmountPoisha.coerceAtLeast(0L),
        dueAmountPoisha = dueAmountPoisha.coerceAtLeast(0L),
        categoryId = categoryId,
        shopId = shopId,
        loanId = loanId,
        paymentMethod = paymentMethod.takeIf { it in ALLOWED_PAYMENT_METHODS } ?: PaymentMethod.CASH.name,
        note = note,
        transactionDate = transactionDate.takeIf { it > 0L } ?: System.currentTimeMillis(),
        isDeleted = deleted,
        createdAt = clientCreatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis(),
        updatedAt = clientUpdatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

    fun toLineItemEntities(): List<TransactionItemEntity> = items.orEmpty().map { m ->
        TransactionItemEntity(
            id = (m["localId"] as? Number)?.toLong() ?: 0L,
            transactionId = localId,
            productName = (m["productName"] as? String).orEmpty(),
            quantityMillis = (m["quantityMillis"] as? Number)?.toLong() ?: 1000L,
            unit = (m["unit"] as? String).orEmpty().ifBlank { "pcs" },
            unitPricePoisha = (m["unitPricePoisha"] as? Number)?.toLong() ?: 0L,
            lineTotalPoisha = (m["lineTotalPoisha"] as? Number)?.toLong() ?: 0L
        )
    }

    companion object {
        fun fromEntity(
            uid: String,
            entity: LedgerTransactionEntity,
            lineItems: List<TransactionItemEntity>
        ): CloudTransaction = CloudTransaction(
            userId = uid,
            localId = entity.id,
            notebookId = entity.notebookId,
            type = entity.type,
            totalAmountPoisha = entity.totalAmountPoisha,
            paidAmountPoisha = entity.paidAmountPoisha,
            dueAmountPoisha = entity.dueAmountPoisha,
            categoryId = entity.categoryId,
            shopId = entity.shopId,
            loanId = entity.loanId,
            paymentMethod = entity.paymentMethod,
            note = entity.note,
            transactionDate = entity.transactionDate,
            deleted = entity.isDeleted,
            items = lineItems.take(50).map { item ->
                mapOf(
                    "localId" to item.id,
                    "productName" to item.productName.take(120),
                    "quantityMillis" to item.quantityMillis,
                    "unit" to item.unit.take(24),
                    "unitPricePoisha" to item.unitPricePoisha,
                    "lineTotalPoisha" to item.lineTotalPoisha
                )
            }.takeIf { it.isNotEmpty() },
            clientCreatedAtMs = entity.createdAt,
            clientUpdatedAtMs = entity.updatedAt
        )
    }
}

@Keep
@IgnoreExtraProperties
data class CloudBudget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val yearMonth: String = "2026-01",
    val categoryId: Long? = null,
    val limitPoisha: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = buildMap {
        put("userId", uid)
        put("localId", localId.coerceAtLeast(1L))
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("yearMonth", yearMonth.takeIf { YEAR_MONTH_REGEX.matches(it) } ?: "2026-01")
        categoryId?.let { put("categoryId", it) }
        put("limitPoisha", limitPoisha.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("yearMonth", yearMonth.takeIf { YEAR_MONTH_REGEX.matches(it) } ?: "2026-01")
        if (categoryId != null) put("categoryId", categoryId) else put("categoryId", FieldValue.delete())
        put("limitPoisha", limitPoisha.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toEntity(): BudgetEntity = BudgetEntity(
        id = localId,
        notebookId = notebookId,
        yearMonth = yearMonth.takeIf { YEAR_MONTH_REGEX.matches(it) } ?: "2026-01",
        categoryId = categoryId,
        limitPoisha = limitPoisha.coerceAtLeast(0L),
        updatedAt = clientUpdatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

    companion object {
        fun fromEntity(uid: String, entity: BudgetEntity): CloudBudget = CloudBudget(
            userId = uid,
            localId = entity.id,
            notebookId = entity.notebookId,
            yearMonth = entity.yearMonth,
            categoryId = entity.categoryId,
            limitPoisha = entity.limitPoisha,
            clientUpdatedAtMs = entity.updatedAt
        )
    }
}

@Keep
@IgnoreExtraProperties
data class CloudNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val category: String = "PERSONAL",
    val checklistItems: List<Map<String, Any>>? = null,
    val contactPhone: String = "",
    val contactAddress: String = "",
    val pinned: Boolean = false,
    val clientCreatedAtMs: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = buildMap {
        put("userId", uid)
        put("localId", localId.coerceAtLeast(1L))
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("title", title.take(200))
        put("content", content.take(5000))
        put("category", category.takeIf { it in ALLOWED_NOTE_CATEGORIES } ?: "PERSONAL")
        checklistItems?.take(100)?.takeIf { it.isNotEmpty() }?.let { put("checklistItems", it) }
        put("contactPhone", contactPhone.take(40))
        put("contactAddress", contactAddress.take(300))
        put("pinned", pinned)
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("title", title.take(200))
        put("content", content.take(5000))
        put("category", category.takeIf { it in ALLOWED_NOTE_CATEGORIES } ?: "PERSONAL")
        val boundedList = checklistItems?.take(100).orEmpty()
        if (boundedList.isNotEmpty()) put("checklistItems", boundedList) else put("checklistItems", FieldValue.delete())
        put("contactPhone", contactPhone.take(40))
        put("contactAddress", contactAddress.take(300))
        put("pinned", pinned)
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toEntity(): NoteEntity {
        val arr = JSONArray()
        checklistItems.orEmpty().forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("text", (item["text"] as? String).orEmpty())
                    put("done", (item["checked"] as? Boolean) ?: false)
                }
            )
        }
        return NoteEntity(
            id = localId,
            notebookId = notebookId,
            title = title,
            content = content,
            category = category.takeIf { it in ALLOWED_NOTE_CATEGORIES } ?: "PERSONAL",
            checklistJson = arr.toString(),
            contactPhone = contactPhone,
            contactAddress = contactAddress,
            isPinned = pinned,
            createdAt = clientCreatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis(),
            updatedAt = clientUpdatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromEntity(uid: String, entity: NoteEntity): CloudNote {
            val nativeList = parseChecklistJsonToNativeMaps(entity.checklistJson)
            return CloudNote(
                userId = uid,
                localId = entity.id,
                notebookId = entity.notebookId,
                title = entity.title,
                content = entity.content,
                category = entity.category,
                checklistItems = nativeList.takeIf { it.isNotEmpty() },
                contactPhone = entity.contactPhone,
                contactAddress = entity.contactAddress,
                pinned = entity.isPinned,
                clientCreatedAtMs = entity.createdAt,
                clientUpdatedAtMs = entity.updatedAt
            )
        }

        private fun parseChecklistJsonToNativeMaps(raw: String): List<Map<String, Any>> {
            return runCatching {
                val arr = JSONArray(raw.ifBlank { "[]" })
                List(arr.length().coerceAtMost(100)) { idx ->
                    val o = arr.getJSONObject(idx)
                    mapOf(
                        "text" to o.optString("text", "").take(200),
                        "checked" to (o.optBoolean("done", false) || o.optBoolean("checked", false))
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}

@Keep
@IgnoreExtraProperties
data class CloudTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val notebookId: Long = 0L,
    val title: String = "",
    val description: String = "",
    val taskType: String = "DAILY",
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val completed: Boolean = false,
    val linkedLoanId: Long? = null,
    val clientCreatedAtMs: Long = 0L,
    val clientUpdatedAtMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = buildMap {
        put("userId", uid)
        put("localId", localId.coerceAtLeast(1L))
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("title", title.bounded(200, "Task"))
        put("description", description.take(500))
        put("taskType", taskType.takeIf { it in ALLOWED_TASK_TYPES } ?: "DAILY")
        dueAt?.let { put("dueAt", it) }
        reminderAt?.let { put("reminderAt", it) }
        put("completed", completed)
        linkedLoanId?.let { put("linkedLoanId", it) }
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toUpdateMap(): Map<String, Any> = buildMap {
        put("notebookId", notebookId.coerceAtLeast(1L))
        put("title", title.bounded(200, "Task"))
        put("description", description.take(500))
        put("taskType", taskType.takeIf { it in ALLOWED_TASK_TYPES } ?: "DAILY")
        if (dueAt != null) put("dueAt", dueAt) else put("dueAt", FieldValue.delete())
        if (reminderAt != null) put("reminderAt", reminderAt) else put("reminderAt", FieldValue.delete())
        put("completed", completed)
        if (linkedLoanId != null) put("linkedLoanId", linkedLoanId) else put("linkedLoanId", FieldValue.delete())
        put("clientCreatedAtMs", clientCreatedAtMs.coerceAtLeast(0L))
        put("clientUpdatedAtMs", clientUpdatedAtMs.coerceAtLeast(0L))
        put("updatedAt", FieldValue.serverTimestamp())
    }

    fun toEntity(): TaskEntity = TaskEntity(
        id = localId,
        notebookId = notebookId,
        title = title.ifBlank { "Task" },
        description = description,
        taskType = taskType.takeIf { it in ALLOWED_TASK_TYPES } ?: "DAILY",
        dueAt = dueAt,
        reminderAt = reminderAt,
        isCompleted = completed,
        linkedLoanId = linkedLoanId,
        createdAt = clientCreatedAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

    companion object {
        fun fromEntity(uid: String, entity: TaskEntity, nowMs: Long): CloudTask = CloudTask(
            userId = uid,
            localId = entity.id,
            notebookId = entity.notebookId,
            title = entity.title,
            description = entity.description,
            taskType = entity.taskType,
            dueAt = entity.dueAt,
            reminderAt = entity.reminderAt,
            completed = entity.isCompleted,
            linkedLoanId = entity.linkedLoanId,
            clientCreatedAtMs = entity.createdAt,
            clientUpdatedAtMs = nowMs
        )
    }
}
