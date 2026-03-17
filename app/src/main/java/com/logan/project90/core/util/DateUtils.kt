package com.logan.project90.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

fun LocalDate.toEpochDayLong(): Long = toEpochDay()

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

fun todayLocalDate(): LocalDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()

fun formatDisplayDate(date: LocalDate): String = displayFormatter.format(date)
