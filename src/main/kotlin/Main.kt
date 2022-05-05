import lexer.CodeIterator
import lexer.Lexer
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")

    val lexer = Lexer(CodeIterator(File(args[0]))) // TODO close file
//    .useLines {
//
//    }

    while (!lexer.isEmpty()) {
        println(lexer.next())
    }
}