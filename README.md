</head><body><article id="13047986-0522-8023-a884-f2e7cb2e2e86" class="page sans"><header><h1 class="page-title">Parcial <strong>Modelamiento de un Perceptrón en NetLogo.</strong></h1><p class="page-description"></p></header><div class="page-body"><p id="13047986-0522-80c0-8b60-cd03ff40f669" class="">
</p><p id="13047986-0522-80fe-8499-eb2fb31a2d50" class="">Código:</p><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism.min.css" integrity="sha512-tN7Ec6zAFaVSG3TpNAKtk4DOHNpSwKHxxrsiw4GHKESGPs5njn/0sMCUMl2svV4wo4BK/rCP7juYz+zx+l6oeQ==" crossorigin="anonymous" referrerPolicy="no-referrer"/><pre id="13047986-0522-80e5-8728-deada535a80a" class="code"><code class="language-JavaScript">globals [
  epoch-error   ;; average error in this epoch
  perceptron    ;; a single output-node
  input-node-1  ;; keep the input nodes in globals so we can refer
  input-node-2  ;; to them directly and distinctly
]

;; A perceptron is modeled by input-node and bias-node agents
;; connected to an output-node agent.

;; Connections from input nodes to output nodes
;; in a perceptron.
links-own [ weight ]

;; all nodes an activation
;; input nodes have a value of 1 or -1
;; bias-nodes are always 1
turtles-own [activation]

breed [ input-nodes input-node ]

;; bias nodes are input-nodes whose activation
;; is always 1.
breed [ bias-nodes bias-node ]

;; output nodes compute the weighted some of their
;; inputs and then set their activation to 1 if
;; the sum is greater than their threshold.  An
;; output node can also be the input-node for another
;; perceptron.
breed [ output-nodes output-node ]
output-nodes-own [threshold]

;;
;; Setup Procedures
;;

to setup
  clear-all

  ;; set our background to something more viewable than black
  ask patches [ set pcolor grey ]

  set-default-shape input-nodes &quot;circle&quot;
  set-default-shape bias-nodes &quot;bias-node&quot;
  set-default-shape output-nodes &quot;output-node&quot;

  create-output-nodes 1 [
    set activation random-activation
    set xcor 6
    set size 2
    set threshold 0
    set perceptron self
  ]

  create-bias-nodes 1 [
    set activation 1
    setxy 3 7
    set size 1.5
    my-create-link-to perceptron

  ]

  create-input-nodes 1 [
    setup-input-node
    setxy -6 5
    set input-node-1 self
  ]

  create-input-nodes 1 [
    setup-input-node
    setxy -6 0
    set input-node-2 self
  ]

  ask perceptron [ compute-activation ]
  reset-ticks
end

to setup-input-node
    set activation random-activation
    set size 1.5
    my-create-link-to perceptron
end

;; links an input or bias node to an output node
to my-create-link-to [ anode ] ;; input or bias node procedure
  create-link-to anode [
    set color red + 1
    ;; links start with a random weight
    set weight random-float 0.1 - 0.05
    set shape &quot;small-arrow-shape&quot;

  ]
end

;;
;; Runtime Procedures
;;

;; train sets the input nodes to a random input
;; it then computes the output
;; it determines the correct answer and back propagates the weight changes
to train ;; observer procedure
  set epoch-error 0
  repeat examples-per-epoch
  [
    ;; set the input nodes randomly
    ask input-nodes
      [ set activation random-activation ]

    ;; distribute error
    ask perceptron [
      compute-activation
      update-weights target-answer
      recolor
    ]
  ]

  ;; plot stats
  set epoch-error epoch-error / examples-per-epoch
  set epoch-error epoch-error * 0.5
  tick
  plot-error
  plot-learned-line
end

;; compute activation by summing the inputs * weights \
;; and run through sign function which determines whether
;; the computed value is above or below the threshold
to compute-activation ;; output-node procedure
  set activation sign sum [ [activation] of end1 * weight ] of my-in-links
  recolor
end

to update-weights [ answer ] ;; output-node procedure
  let output-answer activation

  ;; calculate error for output nodes
  let output-error answer - output-answer

  ;; update the epoch-error
  set epoch-error epoch-error + (answer - sign output-answer) ^ 2

  ;; examine input output edges and set their new weight
  ;; increasing or decreasing it by a value determined by the learning-rate
  ask my-in-links [
    set weight weight + learning-rate * output-error * [activation] of end1
  ]
end

;; computes the sign function given an input value
to-report sign [input]  ;; output-node procedure
  ifelse input &gt; threshold
  [ report 1 ]
  [ report -1 ]
end

to-report random-activation ;; observer procedure
  ifelse random 2 = 0
  [ report 1 ]
  [ report -1 ]
end

to-report target-answer ;; observer procedure
  let a [activation] of input-node-1 = 1
  let b [activation] of input-node-2 = 1
  report ifelse-value run-result (word &quot;my-&quot; target-function &quot; a b&quot;) [1][-1]
end

to-report my-or [a b];; output-node procedure
  report (a or b)
end

to-report my-xor [a b] ;; output-node procedure
  report (a xor b)
end

to-report my-and [a b] ;; output-node procedure
  report (a and b)
end

to-report my-nor [a b] ;; output-node procedure
  report not (a or b)
end

to-report my-nand [a b] ;; output-node procedure
  report not (a and b)
end

;; test runs one instance and computes the output
to test ;; observer procedure
  ask input-node-1 [ set activation input-1 ]
  ask input-node-2 [ set activation input-2 ]

  ;; compute the correct answer
  let correct-answer target-answer

  ;; color the nodes
  ask perceptron [ compute-activation ]

  ;; compute the answer

  let output-answer [activation] of perceptron

  ;; output the result
  ifelse output-answer = correct-answer
  [
    user-message (word &quot;Output: &quot; output-answer &quot;\nTarget: &quot; correct-answer &quot;\nCorrect Answer!&quot;)
  ]
  [
    user-message (word &quot;Output: &quot; output-answer &quot;\nTarget: &quot; correct-answer &quot;\nIncorrect Answer!&quot;)
  ]
end


;; Sets the color of the perceptron&#x27;s nodes appropriately
;; based on activation
to recolor ;; output, input, or bias node procedure
  ifelse activation = 1
    [ set color white ]
    [ set color black ]
  ask in-link-neighbors [ recolor ]

  resize-recolor-links
end

;; resize and recolor the edges
;; resize to indicate weight
;; recolor to indicate positive or negative
to resize-recolor-links
  ask links [
    ifelse show-weights?
    [ set label precision weight 4 ]
    [ set label &quot;&quot; ]
    set thickness 0.2 * abs weight
    ifelse weight &gt; 0
      [ set color [ 255 0 0 196 ] ]   ; transparent red
      [ set color [ 0 0 255 196 ] ] ; transparent light blue
  ]
end

;;
;; Plotting Procedures
;;

;; plot the error from the training
to plot-error ;; observer procedure
  set-current-plot &quot;Error vs. Epochs&quot;
  plotxy ticks epoch-error
end

;; plot the decision line learned
to plot-learned-line ;; observer procedure
  set-current-plot &quot;Rule Learned&quot;
  clear-plot

  run word &quot;plot-&quot; target-function

  ;; cycle through all the x-values and plot the corresponding x-values
  let edge1 [out-link-to perceptron] of input-node-1
  let edge2 [out-link-to perceptron] of input-node-2

  foreach (range -2 3) [ x1 -&gt;
    ;; calculate w0 (the bias weight)
    let w0 sum [[weight] of out-link-to perceptron] of bias-nodes

    ;; put it all together
    let x2 ( (- w0 - [weight] of edge1 * x1) / [weight] of edge2 )

    ;; plot x1, x2
    set-current-plot-pen &quot;rule&quot;
    plotxy x1 x2
  ]
end

to plot-or
  set-current-plot-pen &quot;positives&quot;
  plotxy -1 1
  plotxy 1 1
  plotxy 1 -1
  set-current-plot-pen &quot;negatives&quot;
  plotxy -1 -1
end

to plot-xor
  set-current-plot-pen &quot;positives&quot;
  plotxy -1 1
  plotxy 1 -1
  set-current-plot-pen &quot;negatives&quot;
  plotxy 1 1
  plotxy -1 -1
end

to plot-and
  set-current-plot-pen &quot;positives&quot;
  plotxy 1 1
  set-current-plot-pen &quot;negatives&quot;
  plotxy 1 -1
  plotxy -1 1
  plotxy -1 -1
end

to plot-nor
  set-current-plot-pen &quot;positives&quot;
  plotxy -1 -1
  set-current-plot-pen &quot;negatives&quot;
  plotxy 1 1
  plotxy 1 -1
  plotxy -1 1
end

to plot-nand
  set-current-plot-pen &quot;positives&quot;
  plotxy -1 -1
  plotxy 1 -1
  plotxy -1 1
  set-current-plot-pen &quot;negatives&quot;
  plotxy 1 1
end</code></pre><p id="13047986-0522-808c-b25b-d51243aaf4a2" class="">
</p><p id="13047986-0522-80a9-88d6-ea3358e72c18" class="">A. <strong>Interfaz gráfica en NetLogo:</strong></p><ul id="13047986-0522-8008-8916-ce1c476d86d8" class="bulleted-list"><li style="list-style-type:disc">Diseña una interfaz con sliders para ajustar los valores de:<ul id="13047986-0522-80da-9401-d8418d880e8a" class="bulleted-list"><li style="list-style-type:circle">Tasa de aprendizaje.</li></ul><figure id="13047986-0522-8007-8ff0-c38e80d5e45e" class="image"><a href="image.png"><img style="width:305px" src="../Recursos/Netlogo/image.png"/></a></figure><ul id="13047986-0522-80d9-a5c0-ff2515e6cfa5" class="bulleted-list"><li style="list-style-type:circle">Número de iteraciones.</li></ul><figure id="13047986-0522-80aa-a532-c5699207e816" class="image"><a href="image%201.png"><img style="width:302px" src="../Recursos/Netlogo/image%201.png"/></a></figure></li></ul><ul id="13047986-0522-8094-a893-e3c501dacb53" class="bulleted-list"><li style="list-style-type:disc">Botones para:<ul id="13047986-0522-80d1-911b-ceec31dff9d3" class="bulleted-list"><li style="list-style-type:circle">Iniciar la simulación del aprendizaje.</li></ul><ul id="13047986-0522-8035-8d02-e09e10f23ce1" class="bulleted-list"><li style="list-style-type:circle">Restablecer la simulación.</li></ul><figure id="13047986-0522-803e-9be0-c455041b66dd" class="image"><a href="image%202.png"><img style="width:309px" src="../Recursos/Netlogo/image%202.png"/></a></figure></li></ul><p id="13047986-0522-806c-aedf-fa713fb5ac4f" class="">B. <strong>Modelo del Perceptrón:</strong></p><ul id="13047986-0522-8077-bb81-da0b78bb1bf1" class="bulleted-list"><li style="list-style-type:disc">Implementa el algoritmo del perceptrón que pueda clasificar puntos en un plano 2D.</li></ul><ul id="13047986-0522-80f0-be53-d5d672af7c2b" class="bulleted-list"><li style="list-style-type:disc">El perceptrón debe tener dos entradas y un valor de sesgo (bias).</li></ul><ul id="13047986-0522-80c1-8d38-d8a486e91e38" class="bulleted-list"><li style="list-style-type:disc">Los pesos de las entradas deben actualizarse utilizando la regla de actualización basada en la tasa de aprendizaje.</li></ul><ul id="13047986-0522-803d-bd8c-d2e26f724c63" class="bulleted-list"><li style="list-style-type:disc">Inicializa los pesos y el sesgo en valores aleatorios.</li></ul><div id="13047986-0522-80c7-b82c-fb89286eb981" class="column-list"><div id="13047986-0522-80e8-8e65-e4c21d1062a7" style="width:100%" class="column"><figure id="13047986-0522-809c-b61c-d8374a5ddf0b" class="image"><a href="image%203.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%203.png"/></a></figure><p id="13047986-0522-8086-b205-d993298912dc" class="">
</p></div><div id="13047986-0522-8072-a017-c9d8c102746d" style="width:100%" class="column"><figure id="13047986-0522-8040-8b5e-c72ccae4fcd4" class="image"><a href="image%204.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%204.png"/></a></figure></div><div id="13047986-0522-809e-91ab-dbd676efd5ad" style="width:100%" class="column"><figure id="13047986-0522-8066-899b-e839eca58b81" class="image"><a href="image%205.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%205.png"/></a></figure></div></div><p id="13047986-0522-80b1-a3b5-f02652bf3796" class="">C. <strong>Entrenamiento:</strong></p><ul id="13047986-0522-8071-b628-f357396baf96" class="bulleted-list"><li style="list-style-type:disc">Genera puntos en el plano 2D (turtles en NetLogo) que representen los datos de entrenamiento. Deben ser linealmente separables.</li></ul><p id="13047986-0522-807c-a582-d46eed647306" class=""> Los puntos en la grafica Rule Learned dependen de la target-function dependiendo de este ya sea xor, or o and, ya que and y or son funciones linealmente separables. El perceptrón debería ser capaz de encontrar una línea de decisión que divida los puntos correctamente entre las clases. Sin embargo xor no es linealmente separable. Un perceptrón simple no puede resolver esta función porque requiere una división no lineal del espacio de entrada.</p><p id="13047986-0522-80ab-b634-d1f1bd00f401" class="">
</p><p id="13047986-0522-809f-865a-d41b6bfed943" class="">Xor: </p><ul id="13047986-0522-80b3-8891-c2d3f37b90ab" class="bulleted-list"><li style="list-style-type:disc">Los puntos (1, 0) y (0, 1) deberían estar en una región clasificada como positiva.</li></ul><ul id="13047986-0522-80df-a818-e9771c2b0fea" class="bulleted-list"><li style="list-style-type:disc">Los puntos (1, 1) y (0, 0) deberían estar en una región clasificada como negativa.</li></ul><p id="13047986-0522-802d-a240-f6a5c0336712" class="">Or:</p><ul id="13047986-0522-8066-b6d3-f3c7fee5b5d2" class="bulleted-list"><li style="list-style-type:disc">Los puntos (1, 1), (1, 0), y (0, 1) deberían estar en una región clasificada como positiva.</li></ul><ul id="13047986-0522-8005-89d8-eda3b96c801f" class="bulleted-list"><li style="list-style-type:disc">Solo el punto (0, 0) estará en la región negativa.</li></ul><p id="13047986-0522-80b6-b7fe-dab08933afcd" class="">And:</p><ul id="13047986-0522-803d-bfc2-dc3d5be231bb" class="bulleted-list"><li style="list-style-type:disc">Solo el punto (1, 1) debe estar en una región clasificada como positiva.</li></ul><ul id="13047986-0522-8021-a7ec-cc8d3f5b38d2" class="bulleted-list"><li style="list-style-type:disc">Los otros tres puntos deben estar en la región negativa.</li></ul><p id="13047986-0522-8055-a330-c5e2313b40d0" class="">
</p><ul id="13047986-0522-80e2-ae77-c3eef2db2ccc" class="bulleted-list"><li style="list-style-type:disc">Asigna etiquetas (1 o -1) a los puntos según su posición respecto a una línea de separación (la frontera de decisión).<figure id="13047986-0522-8080-9a7f-ff10fc0d5587" class="image"><a href="image%206.png"><img style="width:171px" src="../Recursos/Netlogo/image%206.png"/></a></figure></li></ul><ul id="13047986-0522-80c9-bc46-db12b4274aaf" class="bulleted-list"><li style="list-style-type:disc">Durante el entrenamiento, ajusta los pesos del perceptrón para aprender a clasificar correctamente los puntos.</li></ul><p id="13047986-0522-8035-bc26-f74af41427e5" class="">
</p><p id="13047986-0522-80c0-8ebc-f83e342c53ee" class="">D. <strong>Visualización:</strong></p><ul id="13047986-0522-809c-a98c-fccb5d1656ba" class="bulleted-list"><li style="list-style-type:disc">Muestra los puntos y la línea de decisión actualizada en tiempo real durante el entrenamiento.</li></ul><ul id="13047986-0522-8000-8dad-d8d8dcb577d5" class="bulleted-list"><li style="list-style-type:disc">Cambia el color de los puntos correctamente clasificados a verde y los incorrectamente clasificados a rojo.</li></ul><div id="13047986-0522-800e-989c-cc2faa85aed7" class="column-list"><div id="13047986-0522-80cf-a681-c366040cba23" style="width:100%" class="column"><p id="13047986-0522-8044-98d4-e26fb8ce50bc" class="">Xor:</p><figure id="13047986-0522-800a-92d3-fbe7c7f6e72e" class="image"><a href="image%207.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%207.png"/></a></figure><p id="13047986-0522-80b1-8d0b-f5c5e7bc037c" class="">En xor no puede resolverse y separar lo puntos de la forma correcta ya que este perceptrón es simple y no es capaz de aprender y dar una respuesta.</p></div><div id="13047986-0522-80b0-850e-fd24c9035ce2" style="width:100%" class="column"><p id="13047986-0522-8066-8609-c6e9cb67e981" class="">Or:</p><figure id="13047986-0522-802a-a329-dfd2aec614f7" class="image"><a href="image%208.png"><img style="width:205.3181915283203px" src="image%208.png"/></a></figure></div><div id="13047986-0522-8043-84e4-d7d1269fa338" style="width:100%" class="column"><p id="13047986-0522-80c1-a340-dd75439930ea" class="">And:</p><figure id="13047986-0522-80a2-8dda-f0615fefc46b" class="image"><a href="image%209.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%209.png"/></a></figure></div></div><p id="13047986-0522-80e7-9f8e-c0908a099634" class="">
</p><p id="13047986-0522-8067-b3a3-e0dcef2dbc13" class="">
</p><p id="13047986-0522-80f3-a97b-c75a9778bff8" class="">E. <strong>Evaluación:</strong></p><ul id="13047986-0522-8067-bbd0-cdeea12aa604" class="bulleted-list"><li style="list-style-type:disc">Después de que el perceptrón se entrene, verifica su rendimiento clasificando un nuevo conjunto de puntos de prueba.</li></ul><ul id="13047986-0522-80fb-934c-c310eedbec7b" class="bulleted-list"><li style="list-style-type:disc">Muestra el porcentaje de puntos clasificados correctamente.</li></ul><p id="13047986-0522-802e-a474-f84e667fde66" class="">
</p><p id="13047986-0522-8070-8992-fbd1f90ede04" class="">xor:</p><figure id="13047986-0522-8036-bd74-cf41449378af" class="image"><a href="image%2010.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%2010.png"/></a></figure><p id="13047986-0522-80d6-ab5d-d25e50ae4511" class="">
</p><p id="13047986-0522-8014-b97f-f7931d6d2ede" class="">or:</p><figure id="13047986-0522-801f-9c8f-ddd3f8f366bb" class="image"><a href="image%2011.png"><img style="width:707.9886474609375px" src="../Recursos/Netlogo/image%2011.png"/></a></figure><p id="13047986-0522-80a4-9ec7-f74bc4ca06c7" class="">
</p><p id="13047986-0522-80bb-b38b-c04ae7ea986d" class="">and</p><figure id="13047986-0522-806e-9a34-fedbef03d31d" class="image"><a href="image%2012.png"><img style="width:205.3181915283203px" src="../Recursos/Netlogo/image%2012.png"/></a></figure><p id="13047986-0522-8067-9755-d22c48351b80" class="">
</p><h2 id="13047986-0522-8055-9446-cf8ccffe2d5b" class="">Análisis Final:</h2><p id="13047986-0522-80df-a4a1-e34a11a60d86" class="">El perceptrón es un modelo básico de una red neuronal artificial, diseñado originalmente para resolver problemas de clasificación binaria. Su objetivo principal es encontrar una línea de decisión (en problemas bidimensionales) que pueda dividir correctamente dos conjuntos de puntos, cada uno perteneciente a una clase distinta (1 o -1, por ejemplo). El perceptrón ajusta los pesos en función de un parámetro llamado <strong>tasa de aprendizaje</strong>, que controla cuánto cambian los pesos en cada actualización. Durante el entrenamiento, el modelo compara su predicción con la salida esperada y ajusta los pesos para reducir el error en el tiempo.</p><p id="13047986-0522-80db-8660-e9e3f1615051" class="">Durante el análisis del perceptrón, una de las observaciones más interesantes fue cómo este modelo funciona bien con funciones linealmente separables, como OR y AND, pero no logra resolver el caso de XOR. Esto realmente destaca la limitación del perceptrón simple cuando se enfrenta a problemas no linealmente separables.</p><p id="13047986-0522-8032-ac91-e2a5d1c693f3" class="">Además, al manipular la tasa de aprendizaje, se nota el impacto en el tiempo de convergencia y la estabilidad del modelo. Cuando la tasa de aprendizaje era demasiado alta, el modelo tendía a oscilar sin encontrar una solución estable; por otro lado, una tasa demasiado baja hacía que el aprendizaje fuera excesivamente lento. Esta experiencia subrayó lo crucial que es encontrar un equilibrio adecuado en este parámetro para que el aprendizaje sea tanto efectivo como eficiente.</p><p id="13047986-0522-805d-ab5c-d86b2588d69b" class="">La gráfica “Rule Learned” resultó ser especialmente útil. Ver cómo la línea de decisión se movía mientras el perceptrón ajustaba sus pesos permite apreciar visualmente el proceso de aprendizaje del modelo y cómo intentaba encontrar la mejor frontera para clasificar los puntos. </p><p id="13047986-0522-80b9-9908-e19dbbc0019d" class="">En resumen, el análisis del perceptrón revela tanto sus capacidades como sus limitaciones en el ámbito del aprendizaje automático. Aunque es eficaz para problemas linealmente separables, su incapacidad para resolver el caso de XOR destaca la necesidad de modelos más avanzados</p><h3 id="13047986-0522-800a-9e11-f1b649485199" class=""><strong>Resultados Observados para XOR, OR y AND</strong></h3><p id="13047986-0522-8027-b384-c7abbdbb71e3" class="">
</p><ul id="13047986-0522-800c-adc5-d6905f5840b6" class="bulleted-list"><li style="list-style-type:disc"><strong>AND</strong>:<ul id="13047986-0522-80dc-92ce-da4fbb764bf1" class="bulleted-list"><li style="list-style-type:circle">El perceptrón tuvo éxito al aprender esta función. Esto era esperado, ya que AND es linealmente separable; basta con trazar una línea recta para dividir correctamente los puntos. La línea de decisión se estableció de manera que separe la clase (1,1) del resto de las combinaciones (0,0), (1,0), y (0,1), donde sólo la primera cumple la función AND.</li></ul></li></ul><ul id="13047986-0522-8074-8f01-ec06f0a5d292" class="bulleted-list"><li style="list-style-type:disc"><strong>OR</strong>:<ul id="13047986-0522-807f-8e1d-fe0ec5eb01bd" class="bulleted-list"><li style="list-style-type:circle">También fue posible entrenar el perceptrón exitosamente en este caso, ya que OR es otra función linealmente separable. La línea de decisión en OR debía dividir al menos uno de los puntos de clase positiva de los puntos de clase negativa. El perceptrón logra esto sin problemas, ya que puede trazar una línea recta que separe correctamente estas clases.</li></ul></li></ul><ul id="13047986-0522-80ae-bed5-e1acdd9c9134" class="bulleted-list"><li style="list-style-type:disc"><strong>XOR</strong>:<ul id="13047986-0522-809f-8087-d5e8104dfe4b" class="bulleted-list"><li style="list-style-type:circle">Aquí, el modelo falla en aprender la clasificación correcta debido a la naturaleza no lineal de XOR. Los puntos en XOR están dispuestos de manera que se requiere más de una frontera de decisión para separarlos correctamente, lo cual un perceptrón simple no puede hacer. En este caso, aunque el perceptrón intentó ajustar los pesos y sesgos, nunca logró una configuración que separara los puntos correctamente, ya que una sola línea de decisión no es suficiente.</li></ul></li></ul><h3 id="13047986-0522-806d-9bda-e576b4352d89" class="">Capturas de pantalla de la simulación mostrando el entrenamiento y la clasificación de los datos.</h3><p id="13047986-0522-8060-9fe8-c64241f58941" class="">
</p><p id="13047986-0522-808b-a5d5-d8bff4fcf26c" class="">XOR:<br/><br/></p><figure id="13047986-0522-80b5-8c54-dc1bb01411bc" class="image"><a href="image%2013.png"><img style="width:707.977294921875px" src="image%2013.png"/></a></figure><figure id="13047986-0522-803e-b4ed-da7285d18a4f" class="image"><a href="image%2014.png"><img style="width:707.977294921875px" src="../Recursos/Netlogo/image%2014.png"/></a></figure><p id="13047986-0522-8078-b6c8-dc96449f7cb9" class="">
</p><p id="13047986-0522-8082-baf7-dcc2399a1103" class="">OR:<br/><br/><br/></p><figure id="13047986-0522-80b6-a678-d0e5f91e7ff3" class="image"><a href="image%2015.png"><img style="width:707.9886474609375px" src="../Recursos/Netlogo/image%2015.png"/></a></figure><figure id="13047986-0522-8055-80ae-ecbb5d0c7f90" class="image"><a href="image%2016.png"><img style="width:707.9886474609375px" src="../Recursos/Netlogo/image%2016.png"/></a></figure><p id="13047986-0522-8075-aaa9-de2e9b39dafd" class="">
</p><p id="13047986-0522-8098-a15c-d7523d6cd079" class="">AND:<br/><br/><br/></p><figure id="13047986-0522-8072-aa7c-f6288f321da5" class="image"><a href="image%2017.png"><img style="width:707.9886474609375px" src="../Recursos/Netlogo/image%2017.png"/></a></figure><figure id="13047986-0522-80ea-9741-e6f63422966a" class="image"><a href="image%2018.png"><img style="width:707.977294921875px" src="../Recursos/Netlogo/image%2018.png"/></a></figure></div></article><span class="sans" style="font-size:14px;padding-top:2em"></span></body></html>


## Implementación de una Calculadora Científica usando el Paradigma de Objetos en Kotlin

Este proyecto implementa una calculadora científica en Kotlin que cumple con varios requisitos de programación orientada a objetos, como el uso de clases, herencia, polimorfismo y manejo de excepciones. La calculadora permite realizar operaciones aritméticas básicas y avanzadas, incluyendo operaciones científicas como trigonometría, logaritmos, potencias, funciones exponenciales y manejo de memoria. La entrada de expresiones complejas es soportada mediante el uso del algoritmo de Shunting Yard, y el proyecto está diseñado para ejecutarse en la terminal.

### Requisitos del Proyecto

#### A. Clases y Objetos

La clase base `Calculadora` contiene las operaciones aritméticas básicas, organizadas y encapsuladas de forma que faciliten su uso:

- **Operaciones Básicas**: La clase `Calculadora` tiene métodos para suma, resta, multiplicación y división, y cada uno maneja posibles excepciones, como la división por cero. Estas operaciones se aplican de manera segura para evitar errores inesperados en tiempo de ejecución.

```kotlin
class Calculadora {
    fun sumar(a: Double, b: Double) = a + b
    fun restar(a: Double, b: Double) = a - b
    fun multiplicar(a: Double, b: Double) = a * b
    fun dividir(a: Double, b: Double): Double {
        if (b == 0.0) throw IllegalArgumentException("No se puede dividir entre cero")
        return a / b
    }
}
```

#### B. Herencia y Extensión de Funcionalidades

Para extender las capacidades de `Calculadora`, se crea una clase derivada llamada `CalculadoraCientifica` que hereda los métodos básicos y añade funcionalidades científicas avanzadas:

- **Funciones Trigonométricas**: La clase `CalculadoraCientifica` incluye métodos para seno, coseno y tangente. Estas funciones permiten trabajar en grados o radianes, proporcionando flexibilidad para cálculos que dependen de la unidad de ángulo.
- **Potencias y Raíces**: La clase permite realizar potencias y extraer raíces cuadradas, gestionando también posibles entradas no válidas, como intentar calcular la raíz de un número negativo.
- **Logaritmos y Exponenciales**: Incluye funciones para logaritmos en base 10, logaritmos naturales (base `e`) y exponentes, utilizando métodos de la biblioteca matemática de Kotlin.
- **Conversión de Ángulos**: Permite convertir entre grados y radianes para facilitar el uso de las funciones trigonométricas.

```kotlin
class CalculadoraCientifica : Calculadora() {
    fun seno(grados: Double): Double = Math.sin(Math.toRadians(grados))
    fun coseno(grados: Double): Double = Math.cos(Math.toRadians(grados))
    fun tangente(grados: Double): Double = Math.tan(Math.toRadians(grados))

    fun potencia(base: Double, exponente: Double) = Math.pow(base, exponente)
    fun raiz(numero: Double): Double {
        if (numero < 0) throw IllegalArgumentException("No se puede calcular la raíz de un número negativo")
        return Math.sqrt(numero)
    }

    fun logaritmoBase10(numero: Double): Double {
        if (numero <= 0) throw IllegalArgumentException("El logaritmo sólo está definido para números positivos")
        return Math.log10(numero)
    }

    fun logaritmoNatural(numero: Double): Double {
        if (numero <= 0) throw IllegalArgumentException("El logaritmo sólo está definido para números positivos")
        return Math.log(numero)
    }
}
```

#### C. Polimorfismo

Se implementa polimorfismo a través de la sobrecarga de métodos que permiten realizar cálculos con distintos tipos de datos (números enteros, decimales). Esto facilita que `Calculadora` y `CalculadoraCientifica` manejen operaciones sin preocuparse del tipo exacto de dato ingresado, asegurando una flexibilidad en el uso de los métodos.

#### D. Manejo de Excepciones

Para manejar errores comunes, se incluyen excepciones personalizadas que aseguran que el usuario reciba mensajes claros:

- **División por Cero**: El método `dividir` en `Calculadora` lanza una excepción específica si se intenta dividir entre cero.
- **Operaciones no válidas**: Los métodos en `CalculadoraCientifica` lanzan excepciones cuando se intenta realizar operaciones no válidas, como logaritmos de números no positivos o raíces de números negativos.
- **Entrada de Datos no Válida**: Durante la evaluación de expresiones, se verifican y gestionan los datos no válidos para evitar cálculos incorrectos.

#### E. Funcionalidades Adicionales

1. **Evaluación de Expresiones Complejas**: Mediante el algoritmo de Shunting Yard, el programa soporta la entrada de expresiones matemáticas complejas, como `2 + 3 * sin(45) - log(10)`. Este algoritmo convierte expresiones infijas a notación posfija para procesar operaciones con distintos niveles de prioridad.

2. **Manejo de Memoria**: La calculadora incluye funcionalidad de memoria con operaciones `M+`, `M-` y `MR` (recuperar memoria), lo que permite al usuario almacenar resultados y usarlos en cálculos posteriores. Se permite también el uso de memoria en nuevas operaciones (por ejemplo, `MR + 5`), aumentando la flexibilidad y facilidad de uso.

```kotlin
class CalculadoraCientifica : Calculadora() {
    private var memoria: Double = 0.0

    fun sumarMemoria(valor: Double) {
        memoria += valor
    }

    fun restarMemoria(valor: Double) {
        memoria -= valor
    }

    fun recuperarMemoria(): Double = memoria

    fun limpiarMemoria() {
        memoria = 0.0
    }
}
```

### Experiencia y Reflexión

Este proyecto ha sido un excelente ejercicio de aprendizaje práctico de Kotlin y programación orientada a objetos. Algunos desafíos notables incluyeron la implementación del algoritmo Shunting Yard para evaluar expresiones complejas y el manejo de funciones trigonométricas que requieren conversión de grados a radianes. La estructura del proyecto permitió organizar las funcionalidades científicas de manera modular y aprovechar los principios de encapsulamiento, herencia y polimorfismo. 

Mediante el uso de funciones y clases de la biblioteca estándar de Kotlin, se lograron cálculos precisos, aunque con desafíos en el manejo de excepciones para evitar entradas inválidas. Esta experiencia me ha permitido entender mejor la flexibilidad y las capacidades de Kotlin para crear aplicaciones de consola robustas, y espero poder aplicar estos conceptos en futuros proyectos.

---

### Cómo Ejecutar el Proyecto

Para ejecutar esta calculadora científica, puedes clonar el repositorio y compilar el código en IntelliJ IDEA o cualquier entorno compatible con Kotlin. Asegúrate de tener Kotlin configurado en tu sistema.

```bash
# Clona el repositorio
git clone <URL_DEL_REPOSITORIO>

# Navega al directorio del proyecto
cd <NOMBRE_DEL_DIRECTORIO>

# Compila y ejecuta el proyecto en IntelliJ o con el siguiente comando
kotlinc Main.kt -include-runtime -d CalculadoraCientifica.jar
java -jar CalculadoraCientifica.jar
```

Con esta calculadora científica podrás realizar desde cálculos básicos hasta operaciones avanzadas de manera rápida y precisa.
