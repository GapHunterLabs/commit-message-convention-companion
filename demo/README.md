# Cómo probar este plugin

Este plugin revisa el mensaje que un programador escribe al guardar
un cambio (a esto se le llama "hacer un commit") y avisa si ese
mensaje no sigue un formato estándar y prolijo.

No hace falta abrir ningún proyecto especial — se prueba en este
mismo proyecto del plugin.

## Qué hacer

1. Abrí cualquier archivo (por ejemplo `README.md`) y escribí
   cualquier letra de más al final, después borrala — así el
   programa detecta que "hay un cambio para guardar".
2. Buscá el botón de "Commit" (usualmente un ícono con un tilde/check
   en la barra lateral izquierda o arriba) y hacé click para abrir
   esa ventana.
3. En el campo donde se escribe el mensaje, probá escribir, uno a la
   vez, estos 3 mensajes (borrando el anterior antes de escribir el
   siguiente):

| Mensaje a escribir | Qué debería pasar |
|---|---|
| `arreglo cosas` | El plugin debería avisar que falta un formato correcto |
| `fix: correct order total calculation` | No debería aparecer ningún aviso — está bien escrito |
| `fix: correct order total calculation.` (con un punto al final) | Debería avisar por el punto final sobrante |

4. **Importante: no hagas click en el botón final de "Commit" con
   ninguno de estos mensajes de prueba** — solo escribí el texto,
   mirá si aparece el aviso, sacá la captura, y después cerrá la
   ventana sin confirmar el commit (o cancelá).

## Si algo no se ve así

Sacá la captura igual, y avisame qué mensaje no coincide con lo de
arriba.
