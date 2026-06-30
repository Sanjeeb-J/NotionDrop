<div align="center">
  <img src="assets/logo.png" alt="NotionDrop Logo" width="200" height="200"/>
  <h1>NotionDrop</h1>
  <p><strong>A minimalist, AI-powered voice note app that structures your thoughts and drops them directly into Notion.</strong></p>

  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform: Android" />
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/API-Notion-000000?style=flat-square&logo=notion&logoColor=white" alt="Notion API" />
  <img src="https://img.shields.io/badge/AI-OpenAI%20%7C%20Gemini-412991?style=flat-square&logo=openai&logoColor=white" alt="AI Powered" />
</div>

<br/>

## 🚀 Overview

**NotionDrop** captures your raw thoughts—either by voice or text—and uses the power of AI (OpenAI, NVIDIA NIM, or Google Gemini) to automatically structure them into beautiful, organized Notion blocks before dropping them right into your database. 

Designed with a sleek, pure black-and-white minimalist UI, NotionDrop stays out of your way so you can focus entirely on capturing ideas.

---

## ✨ Features

- **🎙️ Voice-to-Text Native**: Instantly capture ideas using built-in Android voice recognition.
- **🧠 AI Structuring**: Your raw braindumps are automatically converted into perfectly structured Markdown (titles, tags, headers, to-do lists, and dividers).
- **🔌 Multi-Provider AI**: Support for OpenAI, Google AI Studio (Gemini), and NVIDIA NIM. 
- **📓 Direct Notion Sync**: Seamlessly pushes formatted content to any of your configured Notion databases.
- **🎨 Minimalist UI**: Distraction-free, fully responsive Light and Dark themes built with Jetpack Compose.
- **🕒 Local History**: Keeps a local record of everything you've dropped.

---

## 🛠️ Setup & Installation

### 1. Requirements
- Android Studio (Koala or newer)
- Minimum Android SDK: 26

### 2. Configure Your AI
To use the AI features, you will need an API key from your preferred provider:
- **OpenAI / NVIDIA**: Get an API key from the OpenAI platform or NVIDIA Build and paste it into the *OpenAI API Key* field in the app settings.
- **Google Gemini**: Get an API key from Google AI Studio and paste it into the *Gemini API Key* field.

### 3. Configure Notion
To allow NotionDrop to write to your workspace:
1. Go to [Notion Developers Connections](https://app.notion.com/developers/connections) and create a new internal integration.
2. Copy your **Internal Integration Token** and paste it into the NotionDrop settings.
3. Open your target Notion Database as a full page in your browser. Extract the 32-character **Database ID** from the URL (`https://www.notion.so/workspace/DATABASE_ID?v=...`).
4. **Important**: In Notion, click the `...` menu on your database, go to **Add connections**, and invite your newly created Integration!
5. Finally, add the Database ID and a nickname to NotionDrop.

### 4. Database Schema Requirements
For NotionDrop to categorize your drops correctly, your target Notion database must have the following properties:
- **`Name`**: Title property (`Aa`)
- **`Tags`**: Multi-select property (List icon)
- **`Date`**: Date property (Calendar icon)

*(Property names are case-sensitive!)*

---

## 📸 Screenshots

*(Coming Soon: Add screenshots of the Home Screen, Minimalist Voice Recording, and Settings here!)*

---

## 💻 Tech Stack
- **UI**: Jetpack Compose & Material 3
- **Architecture**: MVVM with Hilt Dependency Injection
- **Network**: Retrofit & Gson (for Notion API & AI endpoints)
- **Local Storage**: Room Database & DataStore Preferences

---

<div align="center">
  <i>Built with ❤️ to keep your brain organized.</i>
</div>
