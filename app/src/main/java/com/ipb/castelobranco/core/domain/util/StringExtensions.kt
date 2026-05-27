package com.ipb.castelobranco.core.domain.util

fun String.normalize(): String =
    java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .replace(Regex("[\\p{Punct}]+"), "")
