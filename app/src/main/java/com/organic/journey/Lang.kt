package com.organic.journey

import java.util.Locale

enum class Lang(val label: String, val locale: Locale) {
    ENGLISH("English", Locale.ENGLISH),
    HINDI("Hindi", Locale.forLanguageTag("hi-IN")),
    PUNJABI("Punjabi", Locale.forLanguageTag("pa-IN")),
    SPANISH("Spanish", Locale.forLanguageTag("es-ES")),
    FRENCH("French", Locale.FRENCH),
    GERMAN("German", Locale.GERMAN),
    ARABIC("Arabic", Locale.forLanguageTag("ar")),
    JAPANESE("Japanese", Locale.JAPANESE),
    CHINESE("Chinese (Simplified)", Locale.SIMPLIFIED_CHINESE);

    override fun toString() = label   // shown in the spinner
}