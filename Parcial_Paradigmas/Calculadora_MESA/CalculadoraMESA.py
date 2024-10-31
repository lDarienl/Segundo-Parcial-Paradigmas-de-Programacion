import re
from mesa import Agent, Model
from mesa.time import BaseScheduler

# Agente base para operaciones
class OperationAgent(Agent):
    def __init__(self, unique_id, model, operation_type):
        super().__init__(unique_id, model)
        self.operation_type = operation_type

class SumaAgent(OperationAgent):
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "suma")
    
    def execute(self, a, b):
        return a + b

class RestaAgent(OperationAgent):
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "resta")
    
    def execute(self, a, b):
        return a - b

class MultiplicacionAgent(OperationAgent):
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "multiplicacion")
    
    def execute(self, a, b):
        return a * b

class DivisionAgent(OperationAgent):
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "division")
    
    def execute(self, a, b):
        if b == 0:
            raise ZeroDivisionError("No se puede dividir entre cero.")
        return a / b

class PotenciaAgent(OperationAgent):
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "potencia")
    
    def execute(self, a, b):
        return a ** b

# Agente Entrada/Salida con parsing de expresiones
class EntradaSalidaAgent(OperationAgent):
    def __init__(self, unique_id, model):
        super().__init__(unique_id, model, "entrada_salida")

    def parse_expression(self, expression):
        # Operadores con su precedencia
        ops = {'+': 1, '-': 1, '*': 2, '/': 2, '^': 3}
        output = []
        stack = []
        
        tokens = re.findall(r"\d*\.\d+|\d+|[\+\-\*/\^()]|(?<=\()\-(?=\d)", expression)
        i = 0
        while i < len(tokens):
            token = tokens[i]
            
            # Detecta números, incluyendo negativos
            if re.match(r"^-?\d+(\.\d+)?$", token):
                output.append(float(token))  # Convierte el número a float para incluir decimales
            elif token == '(':
                stack.append(token)
            elif token == ')':
                while stack and stack[-1] != '(':
                    output.append(stack.pop())
                stack.pop()  # Remover el '('
            else:  # Operador
                if token == '-' and (i == 0 or tokens[i - 1] in "()+-*/^"):
                    # Considera el siguiente token como número negativo
                    if i + 1 < len(tokens) and re.match(r"\d+(\.\d+)?", tokens[i + 1]):
                        output.append(-float(tokens[i + 1]))
                        i += 1  # Salta el siguiente token que ya procesamos
                    else:
                        raise ValueError("Número negativo no seguido de dígito.")
                else:
                    # Procesar operadores normales
                    while (stack and stack[-1] != '(' and
                           ops.get(token, 0) <= ops.get(stack[-1], 0)):
                        output.append(stack.pop())
                    stack.append(token)
            
            i += 1

        # Vaciar el stack
        while stack:
            output.append(stack.pop())
        
        return output

    def evaluate_rpn(self, rpn):
        # Evalúa la expresión en notación postfija utilizando los agentes
        stack = []
        agents = {
            '+': self.model.get_agent("suma"),
            '-': self.model.get_agent("resta"),
            '*': self.model.get_agent("multiplicacion"),
            '/': self.model.get_agent("division"),
            '^': self.model.get_agent("potencia")
        }

        for token in rpn:
            if isinstance(token, (int, float)):  # Número entero o decimal
                stack.append(token)
            else:  # Operador
                if len(stack) < 2:
                    raise ValueError("Expresión mal formada.")
                b = stack.pop()
                a = stack.pop()
                agent = agents[token]
                result = agent.execute(a, b)
                stack.append(result)

        return stack[0]

    def resolver(self, expression):
        # Convierte la expresión a RPN y evalúa
        rpn = self.parse_expression(expression)
        result = self.evaluate_rpn(rpn)
        print(f"Resultado de '{expression}': {result}")
        return result

# Modelo de la calculadora
class CalculatorModel(Model):
    def __init__(self):
        super().__init__()
        self.schedule = BaseScheduler(self)
        self.message_queue = []

        # Crear los agentes
        self.agentes = {
            "suma": SumaAgent(1, self),
            "resta": RestaAgent(2, self),
            "multiplicacion": MultiplicacionAgent(3, self),
            "division": DivisionAgent(4, self),
            "potencia": PotenciaAgent(5, self),
            "entrada_salida": EntradaSalidaAgent(6, self)
        }

        for agente in self.agentes.values():
            self.schedule.add(agente)

    def get_agent(self, tipo):
        return self.agentes[tipo]
    
    def step(self):
        self.schedule.step()

# Interfaz
def main():
    model = CalculatorModel()
    entrada_salida = model.get_agent("entrada_salida")
    
    print("Ingrese una expresión matemática o 'salir' para terminar.")
    while True:
        expression = input("Ingrese su expresión: ")
        
        if expression.lower() == "salir":
            print("Saliendo de la calculadora.")
            break
        
        try:
            entrada_salida.resolver(expression)
        except Exception as e:
            print(f"Error en la expresión: {e}")

if __name__ == "__main__":
    main()