package com.example.myapplication

sealed class Route(val r: String) {
    data object Home : Route("home")
    data object Category : Route("category")
    data object Settings : Route("settings")

    data object Theme : Route("settings/theme")
    data object Notifications : Route("settings/notifications")

    object History : Route("history")
    object Recipe : Route("recipe")
}

