# 📱 Ficha Completa para Google Play Console: Sistema Personal

Este documento contiene toda la información necesaria, redactada y optimizada para ser copiada y pegada directamente en la **Google Play Console** para la publicación de la aplicación.

---

## 1. Detalles Principales de la Aplicación

- **Título de la aplicación (máx. 30 caracteres):**
  ```text
  Sistema Personal
  ```
  *(16 caracteres - Cumple con la política de títulos de Google Play).*

- **Descripción corta (máx. 80 caracteres):**
  ```text
  Gestor ejecutivo de hábitos, ciclo diario 24h, metas y finanzas offline-first.
  ```
  *(79 caracteres - Concisa, atractiva y sin palabras prohibidas como "gratis" o "mejor").*

- **Descripción completa (máx. 4000 caracteres):**
  ```text
  Sistema Personal es la plataforma integral de productividad diseñada para profesionales, ejecutivos y personas enfocadas que buscan consistencia diaria sin distracciones ni complicaciones.

  A diferencia de las aplicaciones convencionales que reinician tus tareas a la medianoche interrumpiendo tus jornadas nocturnas, Sistema Personal introduce el motor de Ciclo Diario Personalizado (24 horas continuas de 05:00 a 04:59 o a la hora que tú determines), adaptándose a tu verdadero ritmo biológico y laboral.

  CARACTERÍSTICAS PRINCIPALES:

  1. CICLO DIARIO PERSONALIZADO (DAILY CYCLE)
  • Define la hora exacta en que inicia tu día (04:00 AM, 05:00 AM, 06:00 AM o personalizada).
  • Temporizador dinámico con el tiempo restante para completar tu ciclo.
  • Reseteo automático e inteligente de hábitos y tareas diarias.

  2. HÁBITOS Y RACHAS DE CONSISTENCIA
  • Registro rápido de hábitos con un solo toque.
  • Contador de rachas consecutivas (streaks) para mantener el impulso.
  • Recordatorios locales programables (WorkManager) para no perder tus avances.
  • Filtros por categorías: Salud, Estudio, Enfoque y Organización.

  3. GESTIÓN DE TIEMPO Y ENFOQUE PROFUNDO
  • Temporizador de Enfoque (Focus Timer / Pomodoro) con duraciones de 25m, 50m o personalizadas.
  • Planificación por bloques horarios (Time Blocking).
  • Registro de minutos productivos acumulados en el ciclo.

  4. METAS ESTRATÉGICAS Y SUBMETAS
  • Define objetivos a corto, mediano y largo plazo.
  • Desglose en submetas accionables con cálculo automático de progreso porcentual.
  • Indicador de fecha límite y estado de cumplimiento.

  5. CONTROL FINANCIERO PERSONAL
  • Registro rápido de ingresos y gastos diarios.
  • Balance neto automático por ciclo diario.
  • Metas de ahorro e inversión con seguimiento de fondos.
  • Inventario patrimonial de activos y gestión de deudas.

  6. BIENESTAR DIGITAL
  • Presupuestos diarios de tiempo por aplicación.
  • Modo Estricto para proteger tus bloques de concentración.
  • Monitoreo consciente del tiempo en pantalla.

  7. ASISTENCIA INTELIGENTE OPCIONAL
  • Consejos ejecutivos contextualizados basados en tus métricas de productividad.

  PRIVACIDAD Y ARQUITECTURA OFFLINE-FIRST:
  • Tus datos personales, metas, hábitos y finanzas se almacenan de forma local y privada en tu dispositivo mediante SQLite (Room Database).
  • Funciona 100% sin conexión a internet.
  • No vendemos ni compartimos tus datos con terceros ni utilizamos rastreadores publicitarios invasivos.

  Eleva tu disciplina, optimiza tu tiempo y consolida tus metas con Sistema Personal.
  ```

---

## 2. Categorización y Contacto

- **Tipo de aplicación:** Aplicación
- **Categoría:** Productividad (Productivity)
- **Etiquetas sugeridas en Play Console:**
  - Productividad
  - Gestor de tareas
  - Hábitos
  - Finanzas personales
  - Temporizador de concentración

- **Datos de contacto:**
  - **Correo electrónico de soporte:** enriquepradorojas934@gmail.com
  - **URL de la política de privacidad:** Puede alojarse una versión estática o referenciar el texto de privacidad disponible dentro de la propia app en *Configuración > Privacidad y Transparencia*.

---

## 3. Cuestionario de Clasificación de Contenido (IARC / PEGI)

- **Clasificación esperada:** Para todos / PEGI 3 / Everyone.
- **Respuestas al cuestionario:**
  - ¿La aplicación contiene material violento? **No**
  - ¿Contiene contenido sexual o desnudez? **No**
  - ¿Contiene lenguaje potencialmente ofensivo? **No**
  - ¿Promueve o facilita el uso de sustancias controladas (alcohol, tabaco, drogas)? **No**
  - ¿Permite a los usuarios interactuar o intercambiar mensajes libremente con otros usuarios? **No**
  - ¿Comparte la ubicación física del usuario con otros usuarios? **No**
  - ¿Permite la compra de bienes digitales con dinero real? **No**

---

## 4. Formulario de Seguridad de los Datos (Data Safety)

| Pregunta en Google Play Console | Respuesta | Justificación |
|---|---|---|
| ¿Tu app recopila o comparte datos de los usuarios? | **No** (o Sí únicamente si usas Gemini / Firebase Cloud Sync opcional) | Todos los datos de hábitos, tareas y finanzas se guardan localmente en SQLite (Room). |
| ¿Todos los datos recopilados por la app se cifran en tránsito? | **Sí** (Conexiones HTTPS para consultas de IA o sincronización opcional). |
| ¿Permites que los usuarios soliciten la eliminación de sus datos? | **Sí** (Borrando los datos de la app o desinstalándola se eliminan por completo de forma local). |

### Desglose de Permisos utilizados:
1. `android.permission.INTERNET`: Utilizado exclusivamente para consultas opcionales a la API de IA (Gemini) o sincronización si el usuario lo solicita.
2. `android.permission.RECEIVE_BOOT_COMPLETED`: Permite a WorkManager reprogramar las alertas locales de hábitos tras reiniciar el teléfono.
3. `android.permission.POST_NOTIFICATIONS`: Utilizado para mostrar notificaciones locales de recordatorio de hábitos a la hora definida por el usuario.
4. `android.permission.VIBRATE`: Proporciona respuesta táctil leve al sonar la alarma o recordatorio.

---

## 5. Recursos Gráficos Preparados en el Proyecto

1. **Icono Adaptativo de la Aplicación:**
   - Ubicación: `res/mipmap-anydpi-v26/ic_launcher.xml`
   - Tamaño requerido por Play Store para la ficha: 512 x 512 px (formato PNG de 32 bits con canal alfa).

2. **Gráfico de Funciones (Feature Graphic):**
   - Dimensiones requeridas: 1024 x 500 px.
   - Archivo generado en el proyecto: `app/src/main/res/drawable/img_feature_graphic.jpg`

3. **Banner de Bienvenida / Onboarding:**
   - Archivo generado e integrado: `app/src/main/res/drawable/img_onboarding_hero.jpg`

---

## 6. Generación del Archivo de Producción (AAB / APK)

Para generar el paquete final de publicación para la Google Play Store:

1. **Opción Directa desde AI Studio:**
   - Abre el menú superior de configuración / ajustes y selecciona **Export project** o **Generate AAB/APK**.
2. **Opción por Línea de Comandos (Gradle):**
   - Ejecuta:
     ```bash
     gradle :app:bundleRelease
     ```
   - El archivo `.aab` generado se encontrará en:
     `app/build/outputs/bundle/release/app-release.aab`
