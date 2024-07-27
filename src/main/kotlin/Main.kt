import com.varabyte.kotter.foundation.collections.liveListOf
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine

class Question {
    val question = "What is 1 + 2?"
    val options = listOf("42", "3", "5", "1337")
    val correctAnswerIndex = 1
    var selected = 0

}

fun main() =
    session {
        val question = Question()
        var options = liveListOf(question.options)
        var isAnswerCorrect = liveVarOf("")
        section {
            textLine(question.question);
            options.forEachIndexed { index, answer ->
                if (index == question.selected) textLine(">$answer") else textLine(answer)
            }
            input()
            textLine(isAnswerCorrect.value)

        }.runUntilSignal() {
            onKeyPressed {
                when (key) {
                    Keys.UP -> {
                        if (question.selected > 0) {
                            question.selected--
                        }
                    }

                    Keys.DOWN -> {
                        if (question.selected < question.options.size - 1) {
                            question.selected++
                        }
                    }
                }

            }
            onInputEntered {
                if (question.selected == question.correctAnswerIndex) {
                    isAnswerCorrect.value = "YEAH! Correct"
                    signal()
                } else {
                    isAnswerCorrect.value = "Wrong Answer :( "
                }
            }
        }
    }
