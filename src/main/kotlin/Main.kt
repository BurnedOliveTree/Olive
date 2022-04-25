import lexer.Lexer
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")

    val lexer = Lexer(File(args[0]))

    while (!lexer.isEmpty()) {
        println(lexer.next())
    }
}