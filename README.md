# Project 90 - Identity Engine

A behavior-focused Android app designed to turn a 90-day self-improvement experiment into a structured, trackable system.

---

## Overview

Project 90 is built around a simple idea: consistency matters more than intensity.

Instead of chasing streaks or relying on motivation, the app is designed to reinforce identity-based habits through daily minimum effort and controlled overperformance.

Each experiment runs for 90 days and focuses on building a small number of identities that represent the kind of person you are trying to become. Progress is tracked through actual behavior, not intentions.

The system emphasizes:

* sustainable daily effort ("Floor")
* optional overperformance ("Push")
* burnout prevention
* measurable, data-driven feedback

This is less about doing more and more about showing up consistently.

---

## Core Features

* Create and manage 90-day experiments
* Define up to 4 identity-based habits (1 per category)
* Daily logging with:

  * effort (minutes)
  * energy
  * mood
  * resistance level
* Floor vs Push system to balance consistency and intensity
* Rolling analytics:

  * Strength (consistency and execution)
  * Momentum (trend and behavioral direction)
* Guardrails to prevent unrealistic planning and burnout

---

## Tech Stack

* Kotlin
* Jetpack Compose (Material 3)
* MVVM architecture
* Room (local database)
* DataStore (user preferences)
* Coroutines + Flow
* Navigation Compose

---

## Architecture

The app follows a clean, local-first MVVM structure:

* **UI layer**
  Compose screens and ViewModels that expose immutable UI state

* **Domain layer**
  Business logic, identity rules, and scoring calculations (Strength, Momentum)

* **Data layer**
  Room database for experiments, identities, and logs
  DataStore for lightweight user configuration

Analytics are computed from stored data rather than persisted directly, ensuring consistency and making the system easier to reason about.

---

## Documentation

* [Product Requirements Document](docs/PRD_Project90.md)

---

## Product Thinking

This project was designed from a structured product requirements document before implementation. The PRD captures the behavioral system, guardrails, analytics model, and planned product direction that shaped the current app: [Project 90 PRD](docs/PRD_Project90.md).

---

## Current Status

This project currently includes a working vertical slice:

* Onboarding flow (time budget setup)
* Experiment creation
* Identity creation with validation and guardrails
* Daily logging flow
* Real-time Strength and Momentum calculations
* Local persistence across app restarts

Planned improvements:

* Predefined identity library for faster onboarding
* Expanded analytics views
* Improved UX for the daily logging loop
* Additional guardrail feedback and recommendations

---

## Why I Built This

I built Project 90 as part of a personal 90-day experiment to improve consistency and discipline while also creating something I'd actually use.

At the same time, I wanted to build a portfolio project that shows more than just technical ability. This app reflects how I think about systems, behavior, and long-term progress.

Most habit apps focus on streaks or outcomes. This one focuses on repeatable behavior and realistic execution.

---

## Notes

This project is still in active development. The current version focuses on correctness of the core system and daily loop rather than full feature completeness or polish.
