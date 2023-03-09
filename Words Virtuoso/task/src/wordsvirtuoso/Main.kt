package wordsvirtuoso

import java.io.File
import kotlin.system.exitProcess

fun hasDuplicateCharacters(word: String): Boolean {
    val duplicateLetterRegex = Regex("([a-zA-Z])\\1+")
    return duplicateLetterRegex.find(word) != null
}

fun countInvalidCharacters(word: String): Int {
    val invalidCharactersRegex = Regex("[^a-zA-Z]")
    return invalidCharactersRegex.findAll(word).count()
}

fun isWordValid(word: String): Boolean =
    word.length == 5 && countInvalidCharacters(word) == 0 && !hasDuplicateCharacters(word)

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Error: Wrong number of arguments.")
        exitProcess(-1)
    }

    val wordsFile = File(args[0])
    if (!wordsFile.exists()) {
        println("Error: The words file ${args[0]} doesn't exist.")
        exitProcess(-1)
    }

    val candidatesFile = File(args[1])
    if (!candidatesFile.exists()) {
        println("Error: The candidate words file ${args[1]} doesn't exist.")
        exitProcess(-1)
    }

    val words = wordsFile.readLines()
    checkWordsList(words, args[0])

    val candidates = candidatesFile.readLines()
    checkWordsList(candidates, args[1])

    val lowercaseWords = words.map { it.lowercase() }
    val lowercaseCandidates = candidates.map { it.lowercase() }
    if (lowercaseCandidates.any { !lowercaseWords.contains(it) }) {
        val notIncludedCount = lowercaseCandidates.count { !lowercaseWords.contains(it) }
        println("Error: $notIncludedCount candidate words are not included in the ${args[0]} file.")
        exitProcess(-1)
    }

    println("Words Virtuoso")
    val secretWord = candidates.random()
    val secretWordCharacters = secretWord.toList()
    val startTime = System.currentTimeMillis()
    var turns = 0
    val clues = mutableListOf<String>()
    val wrongChars = mutableSetOf<Char>()

    while (true) {
        turns++
        println("Input a 5-letter word:")
        val guess = readln()

        if (guess.lowercase() == "exit") {
            println("The game is over.")
            break
        }
        if (guess.length != 5) {
            println("The input isn't a 5-letter word.")
            continue
        }
        if (countInvalidCharacters(guess) > 0) {
            println("One or more letters of the input aren't valid.")
            continue
        }
        if (hasDuplicateCharacters(guess)) {
            println("The input has duplicate letters.")
            continue
        }
        if (!lowercaseWords.contains(guess.lowercase())) {
            println("The input word isn't included in my words list.")
            continue
        }
        val guessCharacters = guess.toList()
        val clue = guessCharacters.mapIndexed { index, c ->
            if (c.lowercase() == secretWordCharacters[index].lowercase()) {
                "\u001B[48:5:10m${c.uppercase()}\u001B[0m"
            } else if (secretWord.lowercase().contains(c.lowercaseChar())) {
                "\u001B[48:5:11m${c.uppercase()}\u001B[0m"
            } else {
                wrongChars.add(c.uppercaseChar())
                "\u001B[48:5:7m${c.uppercase()}\u001B[0m"
            }
        }.joinToString("")
        clues.add(clue)
        clues.forEach { println(it) }
        if (guess.lowercase() == secretWord.lowercase()) {
            println("Correct!")
            if (turns == 1) {
                println("Amazing luck! The solution was found at once.")
            } else {
                val endTime = System.currentTimeMillis()
                val duration = (endTime - startTime) / 1000
                println("The solution was found after $turns tries in $duration seconds.")
            }
            break
        }

        println("\u001B[48:5:14m${wrongChars.sorted().joinToString("")}\u001B[0m")
    }
}

private fun checkWordsList(words: List<String>, fileName: String) {
    if (words.any { !isWordValid(it) }) {
        val invalidWordsCount = words.count { !isWordValid(it) }
        println("Error: $invalidWordsCount invalid words were found in the $fileName file.")
        exitProcess(-1)
    }
}
