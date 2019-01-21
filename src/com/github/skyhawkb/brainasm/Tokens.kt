package com.github.skyhawkb.brainasm

import java.io.InputStreamReader
import java.lang.Exception


fun tokenize(input: InputStreamReader): List<Tokens.SecondaryToken> {
    val tokens: MutableList<Tokens.SecondaryToken> = mutableListOf()
    for (line in input.readText().split(Regex("\r?\n"))) {
        val k = line.split(" ")[0]
        if (TOKENS.containsKey(k)) {
            tokens.add(TOKENS[k]!!.newInstance().resolve(line.substring(k.length + 1)))
        } else {
            throw Exception()
        }
    }
    return tokens
}
val TOKENS: Map<String, Class<out Tokens.SecondaryToken>> = mapOf(
        "goto" to Tokens.GotoCmd::class.java,
        "set" to Tokens.SetCmd::class.java,
        "inc" to Tokens.IncrementCmd::class.java,
        "dec" to Tokens.DecrementCmd::class.java,
        "mov" to Tokens.MoveCmd::class.java,
        "cpy" to Tokens.CopyCmd::class.java,
        "otp" to Tokens.PrintCmd::class.java,
        "inp" to Tokens.InputCmd::class.java
)
fun goto(currentIndex: Short, destination: Short): String {
    if (currentIndex == destination) return ""

    return if (currentIndex < destination) {
        Main.currentIndex = Main.currentIndex.plus(Math.abs(currentIndex - destination)).toShort()

        ">".repeat(Math.abs(currentIndex - destination))
    } else {
        Main.currentIndex = Main.currentIndex.minus(Math.abs(currentIndex - destination)).toShort()

        "<".repeat(Math.abs(currentIndex - destination))
    }
}
class Tokens {
    abstract class PrimaryToken(private val matcher: Regex) {
        abstract fun resolve(match: MatchResult?, currentIndex: Short): Any
        fun resolve(source: String): Any {
            return resolve(matcher.matchEntire(source), Main.getCurrentCell())
        }
    }
    abstract class SecondaryToken(private val argList: List<PrimaryToken>) {
        protected val args: MutableList<Any> = mutableListOf()

        abstract fun compile(currentIndex: Short): String
        fun resolve(source: String): SecondaryToken {
            println(source)
            val givenArgs = source.split(" ")
            if (givenArgs.size != argList.size) throw Exception("${givenArgs.size} given, ${argList.size} expected")

            for (i: Int in IntRange(0, argList.size - 1)) {
                this.args.add(argList[i].resolve(givenArgs[i]))
            }

            return this
        }
    }

    class CellArg : PrimaryToken(Regex("\\d+|cur")) {
        override fun resolve(match: MatchResult?, currentIndex: Short): Short {
            if (match == null) throw Exception()

            return if (match.value == "cur") { currentIndex } else { Integer.valueOf(match.value).toShort() }
        }
    }
    class CharArg : PrimaryToken(Regex("\\d+|[a-zA-Z]")) {
        override fun resolve(match: MatchResult?, currentIndex: Short): Byte {
            if (match == null) throw Exception()

            return if (match.value.contains(Regex("[a-zA-Z]"))) { match.value.toByte() } else { Integer.parseInt(match.value).toByte() }
        }
    }

    class GotoCmd : SecondaryToken(listOf(CellArg())) {
        override fun compile(currentIndex: Short): String {
            return goto(currentIndex, this.args[0] as Short)
        }
    }
    class SetCmd : SecondaryToken(listOf(CellArg(), CharArg())) {
        override fun compile(currentIndex: Short): String {
            println(this.args[1])
            return goto(currentIndex, this.args[0] as Short) + "[-]" + "+".repeat(Integer.valueOf(this.args[1].toString()))
        }
    }
    class IncrementCmd : SecondaryToken(listOf(CellArg(), CharArg())) {
        override fun compile(currentIndex: Short): String {
            return goto(currentIndex, this.args[0] as Short) + "+".repeat(this.args[1] as Int)
        }
    }
    class DecrementCmd : SecondaryToken(listOf(CellArg(), CharArg())) {
        override fun compile(currentIndex: Short): String {
            return goto(currentIndex, this.args[0] as Short) + "-".repeat(this.args[1] as Int)
        }
    }
    class MoveCmd : SecondaryToken(listOf(CellArg(), CellArg())) {
        override fun compile(currentIndex: Short): String {
            return "TODO"
        }
    }
    class CopyCmd : SecondaryToken(listOf(CellArg(), CellArg())) {
        override fun compile(currentIndex: Short): String {
            return "TODO"
        }
    }
    class PrintCmd : SecondaryToken(listOf(CellArg())) {
        override fun compile(currentIndex: Short): String {
            return goto(currentIndex, this.args[0] as Short) + '.'
        }
    }
    class InputCmd : SecondaryToken(listOf(CellArg())) {
        override fun compile(currentIndex: Short): String {
            return goto(currentIndex, this.args[0] as Short) + ','
        }
    }
}