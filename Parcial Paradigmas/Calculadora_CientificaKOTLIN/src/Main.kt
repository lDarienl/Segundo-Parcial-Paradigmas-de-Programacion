import java.util.Stack

/*
 * Convierte una expresión en notación infija a notación postfija usando el algoritmo Shunting Yard.
 * @param expresion La expresión en formato de cadena.
 * @param calculadora Instancia de CalculadoraCientifica para evaluar constantes.
 * @return Lista de tokens en notación postfija.
 */

fun shuntingYard(expresion: String, calculadora: CalculadoraCientifica): List<String> {
    val output = mutableListOf<String>()
    val operadores = Stack<Any>()
    val precedencia = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2, '^' to 3, "sqrt" to 3, "sin" to 4, "cos" to 4, "tan" to 4, "log" to 4, "ln" to 4)

    var numero = ""
    var i = 0

    while (i < expresion.length) {
        val c = expresion[i]

        when {
            c.isDigit() || c == '.' -> numero += c
            c == '-' && (i == 0 || expresion[i - 1] in precedencia.keys || expresion[i - 1] == '(') -> {
                numero += '-'
            }
            c in precedencia.keys -> {
                if (numero.isNotEmpty()) {
                    output.add(numero)
                    numero = ""
                }
                while (operadores.isNotEmpty() && operadores.peek() != "(" &&
                    (precedencia[operadores.peek()] ?: 0) >= ((precedencia[c]) ?: 0)
                ) {
                    output.add(operadores.pop().toString())
                }
                operadores.push(c.toString())
            }
            c == '(' -> {
                if (numero.isNotEmpty()) {
                    output.add(numero)
                    numero = ""
                }
                operadores.push(c.toString())
            }
            c == ')' -> {
                if (numero.isNotEmpty()) {
                    output.add(numero)
                    numero = ""
                }
                while (operadores.isNotEmpty() && operadores.peek() != "(") {
                    output.add(operadores.pop().toString())
                }
                operadores.pop()
                if (operadores.isNotEmpty() && operadores.peek() in listOf("sin", "cos", "tan", "log", "ln", "sqrt")) {
                    output.add(operadores.pop().toString())
                }
            }
            c.isLetter() -> {
                var function = ""
                while (i < expresion.length && expresion[i].isLetter()) {
                    function += expresion[i]
                    i++
                }
                i-- // Para no saltar el último carácter
                when (function) {
                    "sin", "cos", "tan", "log", "ln", "sqrt" -> operadores.push(function)
                    "MR" -> output.add(calculadora.recuperarMemoria().toString())
                    "e" -> output.add("e")  // Agregar constante e como string
                    "pi" -> output.add("pi")  // Agregar constante pi como string
                    else -> throw IllegalArgumentException("Función desconocida o símbolo no reconocido: $function")
                }
            }
            else -> {
                if (numero.isNotEmpty()) output.add(numero)
                numero = ""
                throw IllegalArgumentException("Símbolo no reconocido: $c")
            }
        }
        i++
    }

    if (numero.isNotEmpty()) output.add(numero)

    while (operadores.isNotEmpty()) {
        val op = operadores.pop()
        if (op == "(") throw IllegalArgumentException("Paréntesis Incorrectos")
        output.add(op.toString())
    }

    return output
}

/*
 * Evalúa una expresión en notación postfija.
 * @param postfix Lista de tokens en notación postfija.
 * @param unidad Tipo de unidad para funciones trigonométricas (radianes o grados).
 * @return Resultado de la evaluación de la expresión.
 */

fun evaluarPosfijos(postfix: List<String>, unidad: String): Double {
    val calculadoraCientifica = CalculadoraCientifica()
    val stack = Stack<Double>()

    for (token in postfix) {
        when {
            token.toDoubleOrNull() != null -> stack.push(token.toDouble())
            token in listOf("sin", "cos", "tan") -> {
                if (stack.isEmpty()) throw IllegalArgumentException("Expresión inválida")
                val a = stack.pop() // Extraer el valor de la pila
                // Convertir a radianes si es necesario
                val anguloEnRadianes = if (unidad == "g") {
                    calculadoraCientifica.gradosARadianes(a) // Convertir de grados a radianes
                } else {
                    a // Si ya está en radianes
                }
                stack.push(
                    when (token) {
                        "sin" -> calculadoraCientifica.seno(anguloEnRadianes)
                        "cos" -> calculadoraCientifica.coseno(anguloEnRadianes)
                        "tan" -> calculadoraCientifica.tangente(anguloEnRadianes)
                        else -> throw IllegalArgumentException("Función desconocida: $token")
                    }
                )
            }
            token == "sqrt" -> {
                if (stack.isEmpty()) throw IllegalArgumentException("Expresión inválida")
                val a = stack.pop()
                stack.push(calculadoraCientifica.raizCuadrada(a))
            }
            token == "ln" -> {
                if (stack.isEmpty()) throw IllegalArgumentException("Expresión inválida")
                val a = stack.pop()
                stack.push(calculadoraCientifica.logaritmoNatural(a))
            }
            token == "log" -> {
                if (stack.isEmpty()) throw IllegalArgumentException("Expresión inválida")
                val a = stack.pop()
                stack.push(calculadoraCientifica.logaritmoBase10(a))
            }
            token == "e" -> stack.push(calculadoraCientifica.e) // Agregar constante e
            token == "pi" -> stack.push(calculadoraCientifica.pi) // Agregar constante pi

            else -> {
                if (stack.size < 2) throw IllegalArgumentException("Expresión inválida")
                val b = stack.pop()
                val a = stack.pop()

                stack.push(
                    when (token) {
                        "+" -> calculadoraCientifica.suma(a, b)
                        "-" -> calculadoraCientifica.resta(a, b)
                        "*" -> calculadoraCientifica.multiplicacion(a, b)
                        "/" -> calculadoraCientifica.division(a, b)
                        "^" -> calculadoraCientifica.potencia(a, b)
                        else -> throw IllegalArgumentException("Operador desconocido: $token")
                    }
                )
            }
        }
    }
    if (stack.size != 1) throw IllegalArgumentException("Expresión inválida")
    return stack.pop()
}

fun main() {
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    val memoria = CalculadoraCientifica()

    println("¿Quieres usar radianes o grados? (r/g): ")
    val tipoUnidad = readLine()?.toLowerCase() ?: "g"

    if (tipoUnidad != "r" && tipoUnidad != "g") {
        println("Unidad no válida. Se usará radianes por defecto.")
    }

    var unidadActual = tipoUnidad

    while (true) {
        println("Ingresa tu operacion: ")
        val expresion = readLine()?.replace(" ", "") ?: ""

        try {
            val postfix = shuntingYard(expresion, memoria)
            val resultado = evaluarPosfijos(postfix, unidadActual)

            println("Resultado: $resultado")

            println("Quiere guardar el resultado en la memoria? (s/n)")
            if (readLine() == "s") {
                memoria.sumarMemoria(resultado)
            }
            println("Quiere realizar otra operación? (s/n)")
            if (readLine() == "n") {
                break
            }

            // Preguntar y realizar operación con memoria (M+, M-, MR, MC)
            println("¿Quieres realizar una operación con la memoria? (M+/M-/MR/MC/s/n)")
            val memoriaOpcion = readLine()

            when (memoriaOpcion) {
                "M+" -> {
                    println("Ingresa el valor a sumar a la memoria: ")
                    val valor = readLine()?.toDoubleOrNull() ?: 0.0
                    memoria.sumarMemoria(valor)
                }
                "M-" -> {
                    println("Ingresa el valor a restar de la memoria: ")
                    val valor = readLine()?.toDoubleOrNull() ?: 0.0
                    memoria.restarMemoria(valor)
                }
                "MR" -> println("Memoria: ${memoria.recuperarMemoria()}")
                "MC" -> {
                    memoria.limpiarMemoria()
                    println("Memoria borrada.")
                }
                "s" -> continue
                "n" -> break
                else -> println("Opción no válida.")
            }
            // Guardar resultado automático
            println("Guardar el último resultado en memoria (s/n)?")
            if (readLine() == "s") memoria.sumarMemoria(resultado)

        } catch (e: Exception) {
            println("Ocurrió un error: ${e.message}")
        }
    }
}