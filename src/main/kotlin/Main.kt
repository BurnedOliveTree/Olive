import lexer.Lexer
import java.io.File

fun main(args: Array<String>) {
    // TODO add handling of direct source code
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")
    val sourceCode = File(args[0]).readLines().joinToString("\n")
    val lexer = Lexer(sourceCode)

    while (lexer.hasNext()) {
        println(lexer.peek())
        lexer.next()
    }
}