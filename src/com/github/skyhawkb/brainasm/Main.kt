package com.github.skyhawkb.brainasm

import java.io.File
import java.io.InputStreamReader


class Main {
    companion object {
        var currentIndex: Short = 0
        fun getCurrentCell(): Short {
            return currentIndex
        }
    }
}

fun main(args: Array<String>) {
    try {
        var res = ""
        val tokens = tokenize(InputStreamReader(File(args[0]).inputStream()))
        for (token in tokens) {
            res += token.compile(Main.currentIndex) + '\n'
        }
        val outputFile = File(args[0].split('.')[0] + ".bf")
        outputFile.writeText(res)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}