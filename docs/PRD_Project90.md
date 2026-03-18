# Project 90 - Product Requirements Document

This document outlines the design and reasoning behind Project 90, a mobile application that gamifies a 90-day personal development experiment.

The goal of the project is to:
- track identity-based habits over time
- enforce sustainable effort through floor/push mechanics
- provide deterministic feedback based on behavior patterns
- simulate a structured "experiment" rather than a generic habit tracker

This PRD reflects the system as implemented in the current version of the app, with some forward-looking sections for planned features.

## 1. Executive Summary

Project 90 - Identity Engine is a 90-day behavioral reinforcement mobile application designed to help users build identity-based habits through structured effort tracking, reflection, and longitudinal analytics.

The product emphasizes:
- identity reinforcement
- floor-based consistency
- burnout prevention
- data-driven progress visualization

It is designed for personal experimentation while maintaining an architecture that can scale toward a public release.

## 2. Objectives

### 2.1 Primary Objectives

- Enable structured 90-day behavioral experiments
- Reinforce identity-based growth
- Track effort without streak-based punishment
- Provide meaningful longitudinal analytics
- Protect against burnout and over-optimization

### 2.2 Secondary Objectives

- Serve as a portfolio-grade Android project
- Demonstrate clean architecture and behavioral product design
- Support extensibility for future public distribution

## 3. Target Users

### 3.1 Primary User (Phase 1)

High-performing individuals running structured self-improvement experiments, including:
- engineers
- builders
- students
- athletes

### 3.2 Secondary Users (Future Release)

- self-improvement communities
- fitness challenge participants
- corporate performance programs

## 4. Core Product Principles

- No streak punishment
- Floor protection system
- Effort over outcomes
- Built-in recovery mechanisms
- Rolling analytics instead of absolute resets
- Minimal, professional UI

## 5. MVP Feature Requirements

### 5.1 Experiment System

Users must be able to:
- create a new experiment with a default duration of 90 days
- define a start date
- automatically calculate an end date
- view experiment progress over time
- access midpoint and final review screens

Each experiment contains:
- experiment name
- duration
- start date
- end date
- associated identities

### 5.2 Identity System

Users must be able to:
- create 1 to 4 identities per experiment
- select a maximum of one identity per category within an experiment
- choose from a predefined identity library or create a custom identity
- assign each identity:
  - name
  - description
  - category
  - daily floor requirement
  - optional push requirement
  - importance weight from 1 to 3

Supported categories:
- Mind
- Body
- Skill
- Emotional
- Custom

#### Category Constraint Rule

Only one active identity may exist per category within a single experiment.

If a user attempts to add a second identity in the same category, the system must:
- prompt the user to replace the existing identity, or
- require deletion before proceeding

Custom identities count toward the Custom category limit, which is one per experiment unless expanded in a future version.

#### 5.2.1 Identity Creation Onboarding Flow

##### Step 1: Select Identity Type

- Choose from predefined library
- Create custom identity

##### Step 2: Define Behavioral Intent

Prompt: "Who are you becoming in this experiment?"

The user writes a short identity statement, for example:
- "I am a disciplined builder who shows up daily."

##### Step 3: Define Floor

Prompt: "What is the minimum action that proves this identity today?"

Floor guardrails:
- must be realistically achievable daily
- recommended range: 10 to 60 minutes
- cannot exceed 50% of the user's average available discretionary time
- should warn the user if the floor feels intimidating during preview

System validation:
- if the floor is likely to cause burnout, for example more than 90 minutes daily, show a friction warning:
  - "This may reduce long-term consistency. Consider lowering the floor."

##### Step 4: Define Push

Prompt: "What would exceeding the standard look like?"

Push guardrails:
- must be strictly greater than Floor
- cannot exceed 3x Floor duration
- if push ratio exceeds 60% of the week historically, the system should later recommend recovery

##### Step 5: Assign Importance Weight

Weight meanings:
- 1 = Supporting identity
- 2 = Important growth area
- 3 = Core transformation identity

After identities are configured, the system evaluates identity conflict using:
- total weighted floor time per day
- number of weight-3 identities
- category duplication violations

Trigger warnings if:
- total daily floor time exceeds 70% of available discretionary time
- more than two identities are set to weight 3
- combined push ceilings exceed a sustainable threshold
- the user attempts to select more than one identity per category

If category duplication occurs, the system should enforce the rule and suggest:
- "Only one identity per category is allowed per experiment to preserve focus and sustainability. Consider refining your selection."

##### Step 6: Preview Scoring Impact

Show example outcomes:
- Floor Protected -> 1.0 points
- Push Executed -> 1.5 points, with possible resistance bonus

The user then confirms the identity.

#### 5.2.2 Predefined Identity Library

The system provides curated identities with editable defaults.

##### Mind

- Deep Work
  - Description: Focused, uninterrupted cognitive output
  - Suggested Floor: 45 minutes of deep work
  - Suggested Push: 90+ minutes of deep work
- Strategic Planning
  - Description: Structured thinking, decision-making, and long-term direction setting
  - Suggested Floor: 20 minutes of intentional planning
  - Suggested Push: 60+ minutes of roadmap or strategy refinement
- Structured Learning
  - Description: Intentional study and skill acquisition
  - Suggested Floor: 20 minutes of study
  - Suggested Push: 60+ minutes of focused learning

##### Body

- Strength Training
  - Description: Resistance training and muscular development
  - Suggested Floor: 20 minutes of training
  - Suggested Push: Full structured workout
- Personal Care
  - Description: Intentional self-care and hygienic maintenance
  - Suggested Floor: Complete core daily hygiene routine
  - Suggested Push: Extended self-care session
- Recovery
  - Description: Sleep, mobility, and nervous system recovery
  - Suggested Floor: 7+ hours sleep logged or 15 minutes of mobility
  - Suggested Push: Full recovery protocol session

##### Skill

- Skill Practice
  - Description: Deliberate practice in a defined craft
  - Suggested Floor: 20 minutes of focused practice
  - Suggested Push: 60+ minutes of structured progression
- Creative Output
  - Description: Output-driven creative work
  - Suggested Floor: Produce something small
  - Suggested Push: Ship meaningful creative output
- Project Progress
  - Description: Incremental, tangible progress on a defined project
  - Suggested Floor: Complete one clearly defined micro-task
  - Suggested Push: Complete a meaningful milestone or ship a visible increment

##### Emotional

- Emotional Regulation
  - Description: Intentional emotional processing and reflection
  - Suggested Floor: 10 minutes of journaling or reflection
  - Suggested Push: Structured emotional processing session
- Courage Practice
  - Description: Taking uncomfortable but meaningful action
  - Suggested Floor: Confront one avoided task
  - Suggested Push: Execute a major discomfort action
- Relationship Investment
  - Description: Intentional social or relational investment
  - Suggested Floor: One meaningful outreach or interaction
  - Suggested Push: Deep conversation or planned connection event

Each predefined identity includes:
- suggested default Floor
- suggested default Push
- behavioral intent framing

Identity status labels:
- Missed
- Floor Protected
- Push Executed

Identity rules:
- Floor Protected counts as success
- Push Executed increases growth metrics
- Missing a day does not reset experiment progress
- Identity Weight influences Momentum Index and overall experiment completion scoring

### 5.3 Daily Logging System

For each identity per day, users can log:
- effort minutes
- status: Missed, Floor Protected, or Push Executed
- energy level from 1 to 5
- mood from 1 to 5
- resistance level: None, Mild, Moderate, or High
- optional reflection text

System requirements:
- one log per identity per day
- editable entries
- historical log persistence
- high resistance combined with effort contributes to the Resistance Bonus within Daily Identity Points

### 5.4 Analytics Dashboard

The dashboard must display:
- identity strength
- momentum
- cumulative effort
- calendar heatmap

#### 5.4.1 Daily Identity Points

Daily Identity Points are used by both Strength and Momentum calculations.

Base values:
- Missed = 0.0
- Floor Protected = 1.0
- Push Executed = 1.5

Resistance bonus, applied only if status is Floor Protected or Push Executed:
- None = +0.00
- Mild = +0.05
- Moderate = +0.15
- High = +0.25

Rules:
- Daily Identity Points are capped at 1.75
- normalization denominators use 1.5 intentionally, not 1.75
- any normalized value above 100 is clamped to 100

#### 5.4.2 Identity Strength Score

Rolling 14-day weighted consistency score:

```text
Identity Strength Score (14-day) =
  (Sum of Daily Identity Points in last 14 days / (ActiveDays14 x 1.5)) x 100
```

Where:
- `ActiveDays14` = number of days in the last 14 that the identity existed, max 14

This score smooths volatility and avoids streak-based punishment.

#### 5.4.3 Momentum Index

Momentum is a composite score designed to measure sustainable progress.

```text
Strength7 =
  (Sum of Daily Identity Points in last 7 days / (ActiveDays7 x 1.5)) x 100
```

Where:
- `ActiveDays7` = number of days in the last 7 that the identity existed, max 7

```text
PushFreq14 = (PushDays14 / ActiveDays14) x 100
```

Where:
- `PushDays14` = number of days in the last 14 with status = Push Executed

Recovery Balance:

```text
pushRatio14 = PushDays14 / ActiveDays14

If pushRatio14 <= 0.60:
  RecoveryBalance14 = 100
Else:
  RecoveryBalance14 = 100 x (1 - (pushRatio14 - 0.60) / 0.40)
```

Fatigue multipliers:
- if `AvgEnergy7 <= 2.5`, multiply `RecoveryBalance14` by `0.85`
- if `ResistanceTrend5 > 0`, multiply `RecoveryBalance14` by `0.85`
- clamp `RecoveryBalance14` to `0..100`

Per-identity momentum:

```text
Default MomentumIdentity =
  (0.50 x Strength7) + (0.20 x PushFreq14) + (0.30 x RecoveryBalance14)
```

Adaptive burnout weighting:

```text
If Burnout Trigger == true:
  MomentumIdentity =
    (0.40 x Strength7) + (0.20 x PushFreq14) + (0.40 x RecoveryBalance14)
```

This adaptive weighting increases the influence of recovery during periods of elevated strain.

Experiment-level momentum:

```text
MomentumExperiment = Sum(weight_i x MomentumIdentity_i) / Sum(weight_i)
```

#### 5.4.4 Cumulative Effort

Display:
- total minutes per identity
- weekly aggregation
- experiment total

#### 5.4.5 Calendar Heatmap

Display:
- daily effort intensity visualization

### 5.5 90-Day Report Generation

At experiment completion, generate:
- completion rate per identity, weighted by importance
- total effort invested
- growth trend charts
- reflection summary highlights
- behavioral insights, including:
  - trend shifts over time
  - resistance patterns
  - push frequency peaks
  - energy correlations
- exportable PDF report

Completion formulas:

```text
CompletionRateIdentity =
  (Total Daily Identity Points over experiment / (ActiveDaysExperiment x 1.5)) x 100
```

Where:
- `ActiveDaysExperiment` = number of days the identity existed within the experiment duration

```text
OverallExperimentCompletion =
  Sum(weight_i x CompletionRate_i) / Sum(weight_i)
```

## 6. Phase 2 Feature Requirements

### 6.1 Burnout Detection Engine

Trigger when:

```text
PushDays7 >= 5 AND (AvgEnergy7 <= 2.5 OR ResistanceTrend5 > 0)
```

If triggered:
- suggest a floor-only day
- recommend a recovery period
- temporarily adjust Recovery Balance weighting in Momentum

### 6.2 Hypothesis-Based Experiment Mode

Allow users to define:
- a hypothesis statement
- baseline metrics such as confidence, discipline, and energy

After the experiment:
- compare baseline to final metrics
- generate delta analysis
- visualize trend comparisons

### 6.3 AI Reflection Summaries (Optional)

Weekly:
- extract themes from reflections
- identify patterns in mood and energy
- summarize behavioral shifts

## 7. Non-Functional Requirements

### 7.1 Performance

- offline-first architecture
- local database persistence
- fast UI response, under 100ms interaction latency target

### 7.2 Security

- local data encryption
- optional biometric lock

### 7.3 Scalability

- modular architecture
- future cloud sync capability
- multi-experiment support

## 8. Technical Architecture

### 8.1 Recommended Stack

- Kotlin
- Jetpack Compose
- MVVM architecture
- Room for local data
- Hilt for dependency injection
- DataStore for preferences
- Firebase as optional future analytics or cloud sync

### 8.2 High-Level Layers

#### Presentation

- Compose screens
- reusable components
- navigation
- ViewModels for state and UI events

#### Domain

- use cases
- scoring calculators
- burnout detection evaluators

#### Data

- repositories as source of truth
- Room DAOs and entities
- optional future remote sync hooks

### 8.3 Recommended Module Boundaries

Recommended long-term structure:
- `:app` for navigation and DI wiring
- `:core-ui` for design tokens and shared components
- `:domain` for models, use cases, scoring, and burnout evaluation
- `:data` for repositories, Room, DataStore, and mappers

Modules may be collapsed initially, but package boundaries should remain consistent.

### 8.4 Top-Level Navigation

Bottom navigation destinations:
- Today
- Analytics
- Experiment

Additional routes:
- Onboarding / Welcome
- Onboarding / TimeBudget
- Onboarding / ExperimentSetup
- Identity / Add
- Identity / Summary
- Analytics / IdentityDetail
- Reports / Final
- Reports / Insights
- Settings
- Debug / ComponentCatalog

### 8.5 ViewModel Ownership

- `OnboardingViewModel`
  - State: discretionary time, experiment draft
  - Events: set time budget, create experiment
- `IdentityWizardViewModel`
  - State: selected template, intent text, floor, push, weight, category
  - Derived state: guardrail level, time budget percentage, conflicts
  - Events: validate floor, validate push, confirm identity
- `TodayViewModel`
  - State: today identities, daily completion, selected date
  - Events: set status, open log, save log
- `LogViewModel`
  - State: draft effort, energy, mood, resistance, reflection
  - Events: update field, save
- `AnalyticsViewModel`
  - State: summary metrics and identity analytics tiles
  - Events: select identity detail
- `IdentityAnalyticsViewModel`
  - State: Strength14, Strength7, Push Frequency, Recovery Balance, Resistance Trend
- `ExperimentViewModel`
  - State: timeline, midpoint status, completion status
  - Events: open midpoint review, open final report
- `SettingsViewModel`
  - State: preferences such as time budget and lock settings
- `CatalogViewModel`
  - State: component selection and prop controls

Guideline:
- ViewModels own screen state and dispatch domain use cases
- ViewModels do not implement scoring math directly

### 8.6 Domain Services and Use Cases

Pure engines:
- `MetricsCalculator`
  - `computeDailyPoints(log)`
  - `computeStrength(window)`
  - `computePushFrequency(window)`
  - `computeRecoveryBalance(window)`
  - `computeMomentum(window)`
- `BurnoutEvaluator`
  - `evaluate(window) -> BurnoutSignal?`

Use cases:
- `CreateExperiment`
- `AddOrReplaceIdentity`
- `UpsertDailyLog`
- `RecalculateMetrics`
- `GenerateReport`

### 8.7 Data Flow

Single source of truth flow:
1. UI event, such as Save Log
2. ViewModel calls a use case
3. Repository writes to Room
4. Repository exposes streams of identities and logs
5. Domain recalculates derived metrics
6. UI observes state and re-renders

MVP recommendation:
- store raw logs and identities as source of truth
- compute Strength and Momentum on demand
- add persisted metrics snapshots later only if performance requires it

### 8.8 Enforcement Points

- one identity per category: enforced in `AddOrReplaceIdentity` and reflected in UI conflict messaging
- floor and push guardrails: validated in the identity flow and enforced before save
- burnout alerts: computed from recent windows and surfaced through inline sustainability feedback

## 9. Data Model Overview

### Entities

#### User

Future multi-user support:
- `user_id`

#### Experiment

- `experiment_id`
- `user_id` for future use
- `name`
- `start_date`
- `end_date`
- `created_at`
- `is_archived`

#### Identity

- `identity_id`
- `experiment_id`
- `name`
- `description`
- `intent_statement`
- `category`
- `floor_requirement`
- `push_requirement`
- `weight`
- `created_at`
- `is_active`

#### DailyLog

- `log_id`
- `identity_id`
- `date`
- `effort_minutes`
- `status`
- `energy`
- `mood`
- `resistance_level`
- `reflection_text`
- `updated_at`

#### MetricsSnapshot

Optional future optimization:
- `snapshot_id`
- `experiment_id`
- `date`
- `identity_strength_score`
- `momentum_index`

## 10. UX Guidelines

### 10.1 Product-Level UX Principles

- dark mode default
- minimal visual noise
- no exaggerated animations
- neutral-tone feedback
- data-forward design

Example feedback language:
- "You showed up."
- "Data recorded."
- "Floor protected."

### 10.2 Screen-by-Screen UX Flow

#### First Launch

1. Welcome
   - title: "Run a 90-Day Experiment"
   - short explanation of identity-based growth
   - CTA: "Start Experiment"
2. Available Time Setup
   - prompt for realistic discretionary time per day
   - slider input from 30 minutes to 8 hours
   - used for floor guardrail calculations
   - CTA: "Continue"
3. Experiment Setup
   - experiment name
   - duration selector, default 90 days
   - start date picker
   - CTA: "Create Experiment"

#### Identity Creation Flow

4. Identity Overview
   - "Add up to 4 identities"
   - button: "Add Identity"
   - show projected total floor time
   - show conflict warnings in real time
5. Choose Identity Type
   - grid of predefined identities by category
   - option: "Create Custom"
6. Behavioral Intent
   - prompt: "Who are you becoming?"
   - short text field
   - example shown below
7. Define Floor
   - numeric input
   - guardrail indicator:
     - Green = sustainable
     - Yellow = aggressive
     - Red = likely unsustainable
   - show percent of available time consumed
8. Define Push
   - numeric input
   - show ratio versus Floor
   - warn if above 3x Floor
9. Assign Importance Weight
   - 1 / 2 / 3 selector
   - show impact preview on experiment score
10. Identity Summary
   - display Floor, Push, Weight, and estimated daily time impact
   - CTA: "Confirm Identity"
   - after confirmation, return to Identity Overview and recalculate conflicts

#### Home Dashboard

11. Today View
   - display each identity as a card
   - show Floor target and Push target
   - show quick status options:
     - Missed
     - Floor Protected
     - Push Executed
   - show progress toward daily completion
   - CTA per identity: "Log Details"
12. Log Detail Modal
   - effort minutes
   - energy selector
   - mood selector
   - resistance selector
   - optional reflection
   - save button
   - on save:
     - show neutral confirmation
     - update Momentum preview

#### Analytics Flow

13. Analytics Overview
   - tabs:
     - Strength
     - Momentum
     - Effort
     - Heatmap
   - displays:
     - MomentumExperiment
     - identity comparison bars
14. Identity Detail Analytics
   - Strength14 graph
   - Strength7 graph
   - push frequency chart
   - recovery balance indicator
   - resistance trend visualization

#### Burnout Alert Flow

15. Sustainability Alert
   - triggered conditionally
   - message: "Intensity is trending higher than recovery. Consider a Floor-only day."
   - options:
     - Accept recommendation
     - Dismiss

#### Midpoint Review

16. Midpoint Reflection
   - metrics summary
   - prompts:
     - "What has changed?"
     - "What feels sustainable?"
     - "What needs adjustment?"
   - allow identity adjustments with guardrail reevaluation

#### Experiment Completion

17. Final Report Overview
   - weighted completion score
   - identity completion breakdown
   - total effort invested
   - CTA: "View Insights"
18. Behavioral Insights
   - auto-generated insights
   - energy versus performance graph
   - resistance heatmap
   - push frequency over time
   - CTA: "Export PDF"

#### Settings

19. Settings
   - adjust available discretionary time
   - edit identities
   - archive experiment
   - enable biometric lock
   - enable AI summaries in Phase 2

### 10.3 Visual Component System

The interface should remain minimal, professional, and reusable.

#### Design Tokens

Color and theme:
- dark mode default, light mode optional
- neutral surfaces
- typography-forward layout
- subdued status colors
- status should be distinguishable through color, icon, and label

Spacing and layout:
- 8dp spacing grid
- cards use consistent padding and rounded corners
- touch targets should be at least 48dp

Typography:
- use Material 3 baseline roles for hero numbers, titles, body, labels, and helper text

Iconography:
- simple line icons
- use icons for status and warnings, not decoration

#### Core Components

- `AppScaffold`
  - top bar
  - optional actions
  - bottom navigation
  - snackbar host
- `IdentityCard`
  - daily interaction unit with identity summary, status controls, metrics, and detail CTA
- `StatusSegmentedControl`
  - three-state selector for Missed, Floor Protected, and Push Executed
- `GuardrailIndicator`
  - sustainability rating plus explanation
- `ConflictBanner`
  - conflict warning with cause and suggested fix
- `MetricTile`
  - big number plus label and optional delta
- `ProgressRing`
  - accessible at-a-glance completion display
- `HeatmapCalendar`
  - month grid showing daily intensity
- `ChartContainer`
  - chart wrapper with title, helper text, and empty states
- `LogBottomSheet`
  - daily detail logging surface
- `InsightCard`
  - final report and insight summary component

#### Input Components

- `RatingSelector`
  - used for Energy and Mood
- `ResistanceSelector`
  - None / Mild / Moderate / High
- `DurationInput`
  - minutes input with stepper and manual entry
- `WeightSelector`
  - 1 / 2 / 3 selector with explanation

#### System Feedback Patterns

- neutral confirmations via snackbar, such as "Saved"
- warnings via banners and inline helper text
- no red failure screens for missed days

#### Accessibility Requirements

- all interactive elements meet 48dp touch targets
- color is never the only indicator
- support dynamic type scaling
- support screen reader labels for charts and progress indicators

### 10.4 Design Token Definitions

Semantic colors:
- AppBg
- Surface
- SurfaceAlt
- TextPrimary
- TextSecondary
- Border
- Divider
- StatusMissed
- StatusFloor
- StatusPush
- IntentWarning
- IntentInfo
- GuardrailSustainable
- GuardrailAggressive
- GuardrailUnsustainable

Shape and elevation:
- CornerRadiusSm = 12dp
- CornerRadiusMd = 16dp
- CardElevation = Material 3 tonal elevation defaults

Spacing:
- Space1 = 4dp
- Space2 = 8dp
- Space3 = 12dp
- Space4 = 16dp
- Space5 = 24dp
- Space6 = 32dp

Typography aliases:
- TypeHeroNumber
- TypeScreenTitle
- TypeCardTitle
- TypeBody
- TypeLabel
- TypeHelper

Shared style groups:
- AppCardDefaults
- AppChipDefaults
- AppButtonDefaults

### 10.5 Component Catalog Screen

Purpose:
- provide a built-in playground for component QA and iteration

Availability:
- debug builds only
- accessible through a hidden Settings entry

Catalog capabilities:
- search by component name
- filter by category
- open detail preview for individual components
- inspect preview states such as Default, Completed, Warning, Empty, and Disabled
- review accessibility checklist items
- preview tokens for colors, typography, spacing, shapes, and elevation

Example catalog entries:
- IdentityCard
- StatusSegmentedControl
- GuardrailIndicator
- ConflictBanner
- MetricTile
- ProgressRing
- LogBottomSheet
- HeatmapCalendar
- InsightCard

## End of PRD v1
