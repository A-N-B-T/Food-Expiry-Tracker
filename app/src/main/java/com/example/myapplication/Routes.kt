package com.example.myapplication

sealed class Route(val r: String) {
    data object Home : Route("home")
    data object Category : Route("category")
    data object Profile : Route("profile")
    data object Account : Route("profile/account")
    data object Settings : Route("profile/settings")
    data object About : Route("profile/about")
    data object Help : Route("profile/help")
    data object Privacy : Route("profile/privacy")

    data object Theme : Route("profile/settings/theme")
    data object CountdownFormat : Route("profile/settings/countdown-format")
    data object Notifications : Route("profile/settings/notifications")
    data object AutoDelete : Route("profile/settings/auto-delete")

    object History : Route("history")
    object Recipe : Route("recipe")
}

