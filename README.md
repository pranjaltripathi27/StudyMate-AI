# StudyMate AI

An AI-powered student utility web application built with Java, Spring Boot, HTML, CSS, JavaScript and the OpenAI Responses API.

## Features
1. Notes Summarizer
2. Quiz Generator
3. Concept Explainer
4. Answer Improver
5. Flashcard Generator
6. AI Study Assistant

## Requirements
- Java 25 or later (latest LTS)
- Maven 3.6.3 or later
- OpenAI API key with API access and available usage/billing

## Run on Windows (PowerShell)
1. Open this folder in VS Code or IntelliJ IDEA.
2. Create an API key from the OpenAI platform dashboard. Do not share it or commit it to Git.
3. In PowerShell, set the key for this terminal session:

   ```powershell
   $env:OPENAI_API_KEY="your_actual_api_key"
   ```

4. Optional: set a model available to your API project:

   ```powershell
   $env:OPENAI_MODEL="gpt-6-astra"
   ```

5. Start the app:

   ```powershell
   mvn spring-boot:run
   ```

6. Open http://localhost:8080 in your browser. Keep the terminal open while using the app.

## API
POST `/api/ai/{feature}`

Supported features: `summarizer`, `quiz`, `explainer`, `improver`, `flashcards`, `assistant`.

Request:
```json
{"text":"Paste study notes or a question here"}
```

Success response:
```json
{"result":"AI-generated response"}
```

## Security
The API key is read by the Java backend from an environment variable. It is never sent to browser JavaScript. Never put your key in HTML, JavaScript, GitHub, or screenshots. API usage may incur charges according to your account's pricing and limits.

## Structure
```text
src/main/java/com/studymate/
  StudyMateApplication.java
  config/HttpClientConfig.java
  controller/AiController.java
  controller/ApiExceptionHandler.java
  dto/AiRequest.java
  service/AiService.java
src/main/resources/
  application.properties
  static/index.html
  static/styles.css
  static/app.js
```
