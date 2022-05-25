import interpreter.Interpreter
import lexer.CodeIterator
import lexer.Lexer
import parser.LexerIterator
import parser.Parser
import java.io.FileReader

fun main(args: Array<String>) {
    if (args.isEmpty())
        throw IllegalArgumentException("File name must be specified!")

    FileReader(args[0]).use {
        val lexer = Lexer(CodeIterator(it))
        val parser = Parser(LexerIterator(lexer))
        val interpreter = Interpreter()
        interpreter.visit(parser.parse())
    }
}