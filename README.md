# Olive

This document is about Olive, a strongly typed language that was created for the purpose of the project from TKOM (pol. *Techniki Kompilacji*, en. *Compilation Techniques*).

## Functionality

- Strong, static typing
    - Type system:
        - Unit
        - Number
            - Integer
            - Float
        - String
        - Boolean
- Variable and function names can be created from letters and underscore characters
    - Variables and functions can't be named the same as keywords
    - Variables and functions can have the same name, but functions themselves can't have the same names
- Function f declared as ```fun f(a: Int, b: Int) {}``` can be called by:
    - f(a, b)
    - a.f(b)
- All variables are references to mutable objects
    - That's why there is an operator copying the value of the object "=", and an operator copying only the reference "&="
- All statements must be put inside a function

## Specification

### Lexis

#### Keywords

`var` - variable definition
\
`if` - condition
\
`else` - condition
\
`while` - loop with condition
\
`return` - return value of a function
\
`not` - negates value (boolean)
\
`and` - logical and (boolean)
\
`or` - logical or (boolean)
\
`is` - `a is Int` means "is a an Int?"
\
`as` - cast operator

#### Operators

- Arithmetic
    - infix + (addition)
    - infix - (subtraction)
    - prefix - (additive inverse)
    - infix * (multiplication)
    - infix ^ (power)
    - infix / (division)
    - infix | (root)
    - infix % (modulo)
- Comparison
    - infix == (objects (values) are equal)
    - infix &== (object references (addresses) are equal)
    - infix > (greater)
    - infix < (lesser)
    - infix >= (greater or equal)
    - infix <= (lesser or equal)
- Assignment
    - infix = (assignment, value copy)
    - infix &= (assignment, reference copy)
    - (all the arithmetic operators combined with =)
- Other
    - () (function call, expression prioritisation)
    - infix . (function call with preceding expression as first argument)
    - infix ,

### Syntax

Variable declaration
```
var newVariable: Int = 6;
```
Arithmetic operations
```
newVariable = (6 + 12 * 3 - 4) / 2;
```
Conditions
```
if (newVariable > 100) {
    newVariable = 100;
}
```
Loops
```
while (newVariable > 100 and oldVariable < -100) {
    newVariable = newVariable - 1;
    oldVariable = oldVariable + 1;
}
```
Function declaration
```
add (first: Int, second: Int): Int {
    return first + second;
    var thisCodeWontExecute: Int;
}
```
Inline comments
```
# This is an inline comment
```

Complex example
```
isPerfectNumber(number: Int): Bool {
    var count: Int = 0;
    var iterator: Int = 1;
    while (iterator < number) {
        if (number % iterator == 0) {
            count += iterator;
        }
        iterator += 1;
    }
    return count == number;
}

isPrime(number: Int): Bool {
    var root: Int = (number | 2) as Int;
    if (number == 1) { return false; }
    if (number == 2) { return true; }
    if (number % 2 == 0) { return false; }
    var iterator: Int = 3;
    while (iterator <= root) {
        if (number % iterator == 0) { return false; }
        iterator += 2;
    }
    return true;
}

add(first: Int, second: Int): Int {
    return first + second;
}

subtract(first: Int, second: Int): Int {
    return first - second;
}

multiply(first: Int, second: Int): Int {
    return first * second;
}

main(): Unit {
    var number: Float = 6.5;
    isPrime((number as Int).add(2).multiply(7).subtract(1));
    isPerfectNumber(number as Int);
}
```

## Functionality analysis

Variables and functions have to have their type explicitly stated. 

Variables are automatically destroyed when all their references are deleted.

Language has no classes, so there is no access modificators (const, public, private, etc.)

There must be a user-defined `main` function, and all other user functions have to be defined above or below it (there are no nested functions).

## Implementation method

Interpreter has a map of function names to function definitions, as well as an instance of Environment class.

Functions map is instanced upon visiting of Program object (a product of Parser.parse()). Visiting any other parser object directly (statements, expressions, etc.) will result in undefined behaviour.

All values are kept in the Environment object, which has two attributes: stack, that holds all recently calculated values in a Stack, and a functionCallStack, that is a stack of CallContext objects, which hold a stack of Scopes, which finally hold the variables. Variable values are searched for in the latest CallContext (since there are no global variables), starting from the latest Scope. After the interpreter leaves the scope, so does the variables declared in this scope.

// TODO
Upon variable / function declaration, the name will be checked if it is already present in the Environment. If it is, the declaration will be declared as an error to the user.

On file execution, interpreter is looking for a function named `main`, and visit it.

## Error handling

Since the only non-structural operations in Kotlin are return, break, continue and throw, I will make my own classes inheriting from Exception, and simply use the preexisting `throw` in Kotlin. I've wrapped the preexisting exceptions that may occur (like the error that occurs on division by 0) into these classes. // TODO

## How to run

Since Kotlin by default compiles to Java bytecode, I have packed the lexer, parser and interpreter into a single jar file, that is runnable with the `java -jar` command. To work, this // TODO

## Testing

There are unit tests for each keyword and operator, checking if they work as intended, and there is also a few integration tests with some sort of algorithm, for example: is "number" a perfect number, checking if all the components are working as intended.
