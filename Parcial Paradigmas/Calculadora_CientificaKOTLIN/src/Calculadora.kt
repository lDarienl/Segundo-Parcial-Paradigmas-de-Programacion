/*
 * Clase base para una calculadora con operaciones aritméticas básicas.
 */
open class Calculadora {

    /*
     * Suma dos números.
     * @param a Primer número.
     * @param b Segundo número.
     * @return El resultado de la suma de a y b.
     */
    fun suma(a: Double, b: Double): Double = a + b

    /*
     * Resta dos números.
     * @param a Minuendo.
     * @param b Sustraendo.
     * @return El resultado de la resta de a y b.
     */
    fun resta(a: Double, b: Double): Double = a - b

    /*
     * Multiplica dos números.
     * @param a Primer número.
     * @param b Segundo número.
     * @return El resultado de la multiplicación de a y b.
     */
    fun multiplicacion(a: Double, b: Double): Double = a * b

    /*
     * Divide dos números.
     * @param a Dividendo.
     * @param b Divisor.
     * @return El resultado de la división de a entre b.
     * @throws IllegalArgumentException Si el divisor es 0.
     */
    fun division(a: Double, b: Double): Double {
        if (b == 0.0) {
            throw IllegalArgumentException("No se puede dividir entre 0")
        }
        return a / b
    }
}