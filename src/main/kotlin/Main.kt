import interpreter.Interpreter
import lexer.CodeIterator
import lexer.Lexer
import parser.LexerIterator
import parser.Parser
import java.io.FileReader

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")

    FileReader(args[0]).use { file ->
        try {
            val lexer = Lexer(CodeIterator(file))
            val parser = Parser(LexerIterator(lexer))
            parser.getException().let { if (it.isNotEmpty()) println(it) }
            val interpreter = Interpreter()
            interpreter.visit(parser.parse())
        } catch (e: Exception) {
            println(e.message)
        }
    }
}