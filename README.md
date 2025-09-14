# OboeApp – Aplicación móvil para la práctica del oboe

## Descripción
OboeApp es una aplicación educativa desarrollada como parte de un **Trabajo Fin de Máster**.  
Su objetivo es facilitar el **estudio técnico autónomo del oboe**, integrando herramientas digitales como:

- **Metrónomo**
- **Rutinas diarias de estudio**
- **Biblioteca de ejercicios en PDF**
- **Sistema de puntos y rachas**
- **Sección social y perfil de usuario**

La app busca complementar la enseñanza tradicional del oboe, fomentando la constancia y la motivación del estudiante.

---

## Funcionalidades principales
- Autenticación de usuarios (Firebase Authentication).
- Pantalla principal (Home) con acceso a todas las secciones.
- Metrónomo integrado con control de tempo.
- Biblioteca de ejercicios técnicos (PDF en Firebase Storage).
- Rutina diaria generada automáticamente y guardada en DataStore.
- Sistema de puntos y rachas sincronizado con Firestore.
- Sección social: añadir amigos por UID y comparar progreso.
- Perfil de usuario con estadísticas básicas.

---

## Tecnologías utilizadas
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose
- **Arquitectura:** MVVM (Model–View–ViewModel)
- **Backend en la nube:** Firebase (Auth, Firestore, Storage)
- **Persistencia local:** DataStore Preferences
- **Control de versiones:** Git + GitHub

---

## Estructura del proyecto
```
app/
 ├── src/main/java/com/example/tfm/
 │    ├── auth/        # Autenticación (Repository, ViewModel, LoginScreen)
 │    ├── nav/         # Navegación con NavGraph
 │    ├── ui/theme/    # Pantallas principales, ViewModels y recursos de UI
 │    └── MainActivity.kt
 ├── src/main/assets/  # biblioteca.json (ejercicios PDF)
 ├── src/main/res/     # Recursos (imágenes, strings, colores)
 └── google-services.json
```

---

## Instalación y ejecución
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/PabloRozalen01/AndroidTFM.git
   ```
2. Abrir el proyecto en **Android Studio**.
3. Configurar un proyecto en **Firebase** y descargar el archivo `google-services.json`.
4. Colocar `google-services.json` en la carpeta `app/`.
5. Ejecutar la app desde Android Studio.

---

## Demostración
 Para validar la aplicación se desarrolló un vídeo demostrativo que podéis encontrar aquí: https://youtu.be/NLjVxrXxQl8.

---

## Evaluación del prototipo
El prototipo de OboeApp fue validado mediante una **encuesta a estudiantes y profesores de oboe**.  
Los resultados muestran:
- Alta valoración en facilidad de uso y utilidad percibida.
- La **rutina diaria** y el **sistema de puntos** fueron las funcionalidades más valoradas.
- El **componente social** requiere mayor desarrollo en futuras versiones.

---

## Limitaciones y futuras mejoras
- Afinador en fase preliminar (requiere integración con librerías FFT/pitch detection).
- Interacción social básica, pendiente de enriquecer con retos y dinámicas colaborativas.
- Ampliación de la biblioteca con nuevos materiales didácticos.
- Integración de recordatorios de práctica y personalización de rutinas.

---

## Autor
Proyecto desarrollado por **Pablo Rozalén Calonge**  
Trabajo Fin de Máster en *Universidad Internacional de La Rioja (UNIR)*.
