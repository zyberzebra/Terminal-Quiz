import com.varabyte.kotter.foundation.LiveVar
import com.varabyte.kotter.foundation.collections.LiveList
import com.varabyte.kotter.foundation.collections.liveListOf
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.text.StringEscapeUtils.unescapeHtml4


@Serializable
data class ApiResponse(
    val response_code: Int,
    val results: List<TriviaQuestion>
)

@Serializable
data class TriviaQuestion(
    val type: String,
    val difficulty: String,
    val category: String,
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)

@Serializable
data class Question(
    val question: String = "What is the answer to life, the universe, and everything?",
    val options: List<String> = listOf("42", "3", "5", "1337"),
    val correctAnswerIndex: Int = 0,
    var selected: Int = 0
)

suspend fun fetchQuestions(): List<Question> {
    val json = Json {
        ignoreUnknownKeys = true
    }

    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
    }

    val response: String = client.get("https://opentdb.com/api.php?amount=2&category=11")
    client.close()

    val apiResponse = json.decodeFromString<ApiResponse>(response)

    return apiResponse.results.map { triviaQuestion ->
        val question = unescapeHtml4(triviaQuestion.question)
        val allOptions = triviaQuestion.incorrect_answers.map(::unescapeHtml4).toMutableList()
        val correctAnswer = unescapeHtml4(triviaQuestion.correct_answer)
        val indexToInsertCorrectAnswer = (0..allOptions.size).random()
        allOptions.add(indexToInsertCorrectAnswer, correctAnswer)
        Question(
            question = question,
            options = allOptions,
            correctAnswerIndex = allOptions.indexOf(correctAnswer)
        )
    }
}

fun main() {
    val questions: List<Question> = runBlocking {
        fetchQuestions()
    }
    session {
        val question = questions.first()
        val options = liveListOf(question.options)
        val isAnswerCorrect = liveVarOf("")
        section {
            textLine(question.question)
            showAnsweroptions(options, question)
            input()
            textLine(isAnswerCorrect.value)
        }.runUntilSignal {
            onKeyPressed(navigate(question))
            onInputEntered(checkAnswer(question, isAnswerCorrect))
        }
    }
}

private fun MainRenderScope.showAnsweroptions(
    options: LiveList<String>,
    question: Question
) {
    options.forEachIndexed { index, answer ->
        if (index == question.selected) textLine("> | $answer") else textLine("  | $answer")
    }
}

private fun navigate(question: Question): OnKeyPressedScope.() -> Unit = {
    when (key) {
        Keys.UP -> if (question.selected > 0) question.selected--
        Keys.DOWN -> if (question.selected < question.options.size - 1) question.selected++
    }
}

private fun RunScope.checkAnswer(question: Question, isAnswerCorrect: LiveVar<String>): OnInputEnteredScope.() -> Unit {
    val checkAnswer: OnInputEnteredScope.() -> Unit = {
        if (question.selected == question.correctAnswerIndex) {
            isAnswerCorrect.value = "YEAH! Correct"
            signal()
        } else {
            isAnswerCorrect.value = "Wrong Answer :( "
        }
    }
    return checkAnswer
}
