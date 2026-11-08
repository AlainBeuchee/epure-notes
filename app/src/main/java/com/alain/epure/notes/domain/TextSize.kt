package com.alain.epure.notes.domain

// Tailles du texte de l'éditeur, en sp (SPEC F-8.1). Un toucher sur le réglage passe à la valeur suivante.
val TEXT_SIZES = listOf(12, 14, 16, 18, 20, 22, 24)
const val DEFAULT_TEXT_SIZE = 14

fun nextTextSize(current: Int): Int {
    val index = TEXT_SIZES.indexOf(current)
    return if (index < 0) DEFAULT_TEXT_SIZE else TEXT_SIZES[(index + 1) % TEXT_SIZES.size]
}
