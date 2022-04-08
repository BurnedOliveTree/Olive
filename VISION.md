# Vision of Felis

This document is about vision of Felis, a strongly typed language that is being created for the purpose of the project from TKOM.

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
    - Variables and functions can't have the same name, but functions themselves can have the same name, if they differ in terms of their argument and return types
- Function f declared as ```fun f(a: Int, b: Int) {}``` can be called by:
    - f(a, b)
    - a.f(b)
- All variables are references to mutable objects
    - That's why there is an operator copying the value of the object "=", and an operator copying only the reference "&="
- All expressions must be put inside a function

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
    - infix . (function call, method call, attribute)
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
    count: Int = 0;
    iterator: Int = 1;
    while (iterator < number) {
        if (number % iterator == 0) {
            count += iterator;
        }
        iterator += 1;
    }
    return count == number;
}

main(): Unit {
    var number: Float = 6.5;
    isPerfectNumber(number as Int);
}
```

## Functionality analysis

Variables and functions have to have their type explicitly stated. 

Variables are automatically destroyed when all their references are deleted.

Language has no classes, so there will be no access modificators (const, public, private, etc.)

There will be a `main` function, and all the user functions will have to be defined above it (there will be no nested functions).

## Implementation method

There will be a mutable map of function names (plus argument and return types) to function code. I'm thinking about simply joining these together as Strings. For example, `fun f(a: Int, b: Float): String` as a "fStringIntFloat".

Variables similarly will be kept in a map of variable names to variable objects. These objects will hold it's value, type, and a set of names of cells.

Upon variable / function declaration, the name will be checked if it is already present in map keys. If it is, the declaration will be declared as an error to the user.

Since there is no inheritance, a function `fun f(x: Number): Number`, given that in a call the `x` is an Int, will firstly be searched under "fIntNumber" and if that fails, than it will be searched under "fNumberNumber".

On file execution, interpreter will look for a function named `main`, and execute the code present inside. All the user defined functions will have to be declared above `main`. In case of our spreadsheet application, `main` will contain all cell declarations and assignments, and user will be able to declare his/her functions in a special area besides the table.

## Error handling

Since the only non-structural operations in Kotlin are return, break, continue and throw, I will make my own classes inheriting from Exception, and simply use the preexisiting `throw` in Kotlin. I will wrap the pre-existing exceptions that may occur (like the error that occurs on division by 0) into these classes.

## How to run

Since Kotlin by default compiles to Java bytecode, I will pack the lexer, parser and interpreter into a single jar file, that will be runnable with the `java -jar` command. To work, this

## Testing

There will be unit tests for each keyword and operator, checking if they work as intented, and there also will be integration tests: one with a happy-path, with some sort of algorithm, for example a is a number a perfect number, checking if all the components will work as intended, and a few bad-path, which will have improper syntax written in them, and check that our compiler will also correctly respond to that situation.
