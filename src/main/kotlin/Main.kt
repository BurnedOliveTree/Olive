import lexer.CodeIterator
import lexer.Lexer
import java.io.FileReader

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")

    FileReader(args[0]).use {
        val lexer = Lexer(CodeIterator(it))

        while (!lexer.isEmpty()) {
            println(lexer.next())
        }
    }
}