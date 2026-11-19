package com.performily.flowboard.core.text

import java.text.Collator
import java.util.Locale

/** Locale of every date, number and text shown by the app. */
val AppLocale: Locale = Locale.forLanguageTag("es-PE")

/**
 * Comparator that orders text the way a Spanish reader expects.
 *
 * Accents and letter case do not change the order, so "Cáceres" goes before "Chávez"
 * (a plain string comparison puts every accented letter after "z").
 */
val SpanishTextOrder: Comparator<String> = Collator.getInstance(AppLocale).apply {
    strength = Collator.PRIMARY
}.let { collator -> Comparator { first, second -> collator.compare(first, second) } }

/**
 * Sorts the elements alphabetically in Spanish by the text the selector returns.
 *
 * @param selector text used to order each element.
 * @return a new list in Spanish alphabetical order.
 */
inline fun <T> Iterable<T>.sortedAlphabeticallyBy(crossinline selector: (T) -> String): List<T> =
    sortedWith { first, second -> SpanishTextOrder.compare(selector(first), selector(second)) }
