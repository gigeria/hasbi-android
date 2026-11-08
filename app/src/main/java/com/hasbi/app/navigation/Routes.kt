package com.hasbi.app.navigation

/**
 * All navigation routes for Hasbi.
 *
 * Convention: route = lowercase, kebab-case, no params.
 * Args: pass via typed NavRoute classes (Week 4+).
 */
object Routes {

    // === Onboarding ===
    const val ONBOARDING_WELCOME = "onboarding/welcome"
    const val ONBOARDING_WALLET = "onboarding/wallet"
    const val ONBOARDING_BUDGET = "onboarding/budget"

    // === Main app (bottom nav) ===
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val WALLETS = "wallets"
    const val MORE = "more"

    // === Detail / Form screens ===
    const val TRANSACTION_DETAIL = "transaction/detail"
    const val WALLET_DETAIL = "wallet/detail"
    const val ADD_WALLET = "wallet/add"
    const val ADD_BUDGET = "budget/add"
    const val ADD_DEBT = "debt/add"
    const val DEBT_DETAIL = "debt/detail"
    const val CATEGORIES = "categories"
    const val RECENTLY_DELETED = "recently-deleted"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"

    // === Top-level navigation graph ===
    const val ROOT_GRAPH = "root_graph"
    const val ONBOARDING_GRAPH = "onboarding_graph"
    const val MAIN_GRAPH = "main_graph"
}
