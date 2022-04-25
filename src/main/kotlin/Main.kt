import lexer.Lexer
import java.io.File

fun parseFile(filename: String): String {
    val file = File(filename)
    return file.readLines().joinToString("\n")
}

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")

    val sourceCode = parseFile(args[0])
    val lexer = Lexer(sourceCode)

    while (lexer.hasNext()) {
        println(lexer.peek())
        lexer.next()
    }
}