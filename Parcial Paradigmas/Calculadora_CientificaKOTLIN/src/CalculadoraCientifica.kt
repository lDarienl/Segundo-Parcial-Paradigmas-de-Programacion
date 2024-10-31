import kotlin.math.*

/*
 * Clase extendida que implementa una calculadora científica con funciones avanzadas.
 * Extiende la clase Calculadora para incluir operaciones científicas como funciones
 * trigonométricas, potencias, logaritmos y una memoria para almacenar valores.
 */
class CalculadoraCientifica : Calculadora() {
    var memoria = 0.0  // Variable de memoria para almacenar un valor

    val e: Double = Math.E // Constante de Euler
    val pi: Double = Math.PI // Constante Pi

    // Funciones trigonométricas
    fun seno(a: Double): Double = sin(a)
    fun coseno(a: Double): Double = cos(a)
    fun tangente(a: Double): Double = tan(a)

    /*
     * Calcula la potencia de un número elevado a otro.
     * @param a Base.
     * @param b Exponente.
     * @return El resultado de a elevado a b.
     * @throws IllegalArgumentException Si los parámetros son inválidos (e.g., potencia negativa de base 0).
     */
    fun potencia(a: Double, b: Double): Double {
        if (a < 0 && b % 1 != 0.0) {
            throw IllegalArgumentException("No se puede calcular la potencia de un número negativo con exponente no entero.")
        } else if (a == 0.0 && b < 0) {
            throw IllegalArgumentException("No se puede calcular la potencia de 0 con un exponente negativo.")
        }
        return a.pow(b)
    }

    /*
     * Calcula la raíz cuadrada de un número.
     * @param a Número.
     * @return La raíz cuadrada de a.
     * @throws IllegalArgumentException Si el número es negativo.
     */
    fun raizCuadrada(a: Double): Double {
        if (a < 0) {
            throw IllegalArgumentException("No se puede calcular la raíz cuadrada de un número negativo.")
        }
        return sqrt(a)
    }

    // Logaritmos
    fun logaritmoBase10(a: Double): Double {
        if (a <= 0) {
            throw IllegalArgumentException("El logaritmo no está definido para números menores o iguales a cero.")
        }
        return log10(a)
    }

    fun logaritmoNatural(a: Double): Double {
        if (a <= 0) {
            throw IllegalArgumentException("El logaritmo natural no está definido para números menores o iguales a cero.")
        }
        return ln(a)
    }

    // Conversión de grados a radianes
    fun gradosARadianes(grados: Double): Double = Math.toRadians(grados)

    // Funciones de memoria
    fun sumarMemoria(valor: Double) { memoria += valor }
    fun restarMemoria(valor: Double) { memoria -= valor }
    fun recuperarMemoria(): Double = memoria
    fun limpiarMemoria() { memoria = 0.0 }
}