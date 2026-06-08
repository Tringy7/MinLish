package com.example.utils

import com.example.data.local.entity.VocabularyWordEntity

object CsvHelper {
    private const val HEADERS = "Word,Pronunciation,Meaning,Example,Note,DescriptionEN,Collocations,RelatedWords"

    fun exportToCsv(words: List<VocabularyWordEntity>): String {
        val sb = StringBuilder()
        sb.appendLine(HEADERS)
        words.forEach { word ->
            val row = listOf(
                word.word,
                word.pronunciation,
                word.meaning,
                word.example,
                word.note,
                word.descriptionEN,
                word.collocations,
                word.relatedWords
            ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
            sb.appendLine(row)
        }
        return sb.toString()
    }

    fun parseCsv(csvData: String, setId: Int, userId: Int): List<VocabularyWordEntity> {
        val lines = csvData.lines()
        if (lines.size <= 1) return emptyList()

        val words = mutableListOf<VocabularyWordEntity>()
        // Skip header
        lines.drop(1).filter { it.isNotBlank() }.forEach { line ->
            try {
                val parts = parseCsvLine(line).map { it.trim() }
                if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                    words.add(
                        VocabularyWordEntity(
                            setId = setId,
                            userId = userId,
                            word = parts.getOrNull(0) ?: "",
                            pronunciation = parts.getOrNull(1) ?: "",
                            meaning = parts.getOrNull(2) ?: "",
                            example = parts.getOrNull(3) ?: "",
                            note = parts.getOrNull(4) ?: "",
                            descriptionEN = parts.getOrNull(5) ?: "",
                            collocations = parts.getOrNull(6) ?: "",
                            relatedWords = parts.getOrNull(7) ?: ""
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip error lines
            }
        }
        return words
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(c)
                }
            } else {
                if (c == '\"') {
                    inQuotes = true
                } else if (c == ',') {
                    result.add(curVal.toString())
                    curVal.setLength(0)
                } else {
                    curVal.append(c)
                }
            }
            i++
        }
        result.add(curVal.toString())
        return result
    }
}
