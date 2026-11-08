package com.hasbi.database.seed

import com.hasbi.database.entity.CategoryEntity
import com.hasbi.database.entity.CategoryGroup

/**
 * Default category seed data.
 *
 * 10 expense categories + 5 income categories — covers typical Pakistani household use cases.
 *
 * `keywords` field enables Smart Entry parsing:
 *   "500 chai" → matches "chai" keyword → Food category
 *   "2500 petrol bank" → matches "petrol" + "bank" → Transport category, Bank wallet
 *
 * Users can customize keywords in Settings (Phase 1.1).
 */
object CategorySeeder {

    fun getSeedCategories(now: Long): List<CategoryEntity> = buildList {
        // === Expense categories (10) ===
        addExpense(1, "Food", "🍔", "#FF9800", "chai,nashta,khana,food,lunch,dinner,snack,breakfast", now)
        addExpense(2, "Transport", "🚗", "#2196F3", "petrol,gas,fuel,transport,bike,car,uber,careem,bus,train,taxi", now)
        addExpense(3, "Grocery", "🛒", "#4CAF80", "grocery,sabzi,atta,chawal,store,market,dal,ghee,oil,rice", now)
        addExpense(4, "Bills", "💡", "#9C27B0", "bill,electricity,gas,water,internet,wifi,ptcl,phone,mobile", now)
        addExpense(5, "Entertainment", "🎬", "#E91E63", "movie,film,netflix,youtube,fun,game,concert,show", now)
        addExpense(6, "Shopping", "👕", "#FF5722", "shopping,kapray,clothes,shoes,store,buy,purchase", now)
        addExpense(7, "Health", "💊", "#F44336", "doctor,medicine,dawa,hospital,clinic,pharmacy,checkup", now)
        addExpense(8, "Education", "📚", "#3F51B5", "books,fees,school,college,course,tuition,exam,uniform", now)
        addExpense(9, "Personal Care", "🧴", "#00BCD4", "hair,salon,beauty,soap,shampoo,barber,grooming", now)
        addExpense(10, "Charity", "🤲", "#8BC34A", "sadqa,zakat,charity,donation,khairat,fitrana", now)

        // === Income categories (5) ===
        addIncome(11, "Salary", "💼", "#4CAF80", "salary,tankhuwa,pay,wage,month", now)
        addIncome(12, "Freelance", "💻", "#2196F3", "freelance,project,client,work,gig", now)
        addIncome(13, "Business", "📈", "#FF9800", "business,profit,sale,revenue,shop,dukaan", now)
        addIncome(14, "Investment", "📊", "#9C27B0", "dividend,interest,profit,investment,stocks,bond", now)
        addIncome(15, "Gift", "🎁", "#E91E63", "gift,tohfa,eid,prize,bonus,reward", now)
    }

    private fun MutableList<CategoryEntity>.addExpense(
        sortOrder: Int,
        name: String,
        icon: String,
        colorHex: String,
        keywords: String,
        now: Long
    ) {
        add(
            CategoryEntity(
                name = name,
                icon = icon,
                colorHex = colorHex,
                group = CategoryGroup.EXPENSE,
                sortOrder = sortOrder,
                keywords = keywords,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun MutableList<CategoryEntity>.addIncome(
        sortOrder: Int,
        name: String,
        icon: String,
        colorHex: String,
        keywords: String,
        now: Long
    ) {
        add(
            CategoryEntity(
                name = name,
                icon = icon,
                colorHex = colorHex,
                group = CategoryGroup.INCOME,
                sortOrder = sortOrder,
                keywords = keywords,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
