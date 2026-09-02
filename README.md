C_Tailor_Android_Dev

Android Application – Project Setup & Initialization Guide

1. Overview
	This document provides the required steps to clone, configure, initialize, build, and run the Android application locally.

The project is developed using:
- Kotlin
- Jetpack Compose
- Gradle
- Android Studio
- Google Services / Firebase

	This guide is intended for developers setting up the project for the first time or configuring the project on a new development environment.

2. Development Environment
Required IDE
Android Studio Panda 4 | 2025.3.4
Required Tools
- Git
- Android SDK
- JDK
- Gradle
- Android Studio

Ensure that the required Android SDK and JDK versions configured for the project are installed before starting the setup.

3. Project Initialization
Follow the steps below in the given order when setting up the project for the first time.
Step 1 – Clone the Repository

Clone the project repository using Git.

```bash
git clone <repository-url>
```

Navigate to the project directory:

```bash
cd <project-directory>
```

Step 2 – Open the Project in Android Studio

- Open Android Studio.
- Select Open.
- Select the cloned project directory.
- Wait for Android Studio to load the project.

 Do not run the application immediately after opening the project.

4. Checkout the Required Git Branch
After opening the project, verify the currently checked-out Git branch.
By default, the project may open on the `main` branch.
To switch to the required developer branch:
- Locate the Git Branch selector at the top of Android Studio.
- Select the required branch.
- Select Checkout.
Example:
```
main
 ↓
nithish
 ↓
Checkout
```
The same process applies when working with another developer's branch.

For example:

```
main
 ↓
feature/customer-module
 ↓
Checkout
```
Always make sure that the correct branch is checked out before starting development.

 5. Gradle Project Synchronization
After checking out the required branch, synchronize the project with Gradle.
In Android Studio:
- Locate the Load Gradle Changes / Sync Project with Gradle Files option.
- Click the button.
- Wait for Gradle synchronization to complete.
Gradle will download and configure the required project dependencies.
Important: Do not proceed until Gradle synchronization completes successfully. If Gradle reports an error, resolve the Gradle or dependency issue before continuing with the setup.

 6. Restart Android Studio
After the initial Gradle synchronization:

- Close Android Studio completely.
- Reopen Android Studio.
- Open the project again.
- Wait for Gradle indexing and project configuration to complete.

The project may take some time to complete the initial Gradle build and indexing. Allow the process to finish before running the application.

 7. Configure Google Services
The project requires the `google-services.json` configuration file.

 File Location

The file must be placed inside the application's `app` directory:

project-root/
│
├── app/
│   ├── google-services.json
│   ├── build.gradle.kts
│   └── src/
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```
 Setup

- Obtain the correct `google-services.json` file from the project owner/team (Firebase console access is restricted to project owners — do not generate or use a config from a different Firebase project).
- Copy the file.
- Paste it inside: `app/google-services.json`
- Verify that the file name is exactly: `google-services.json`
 Security

The `google-services.json` file contains project-specific Firebase configuration.
- Do not replace it with a configuration from another Firebase project unless specifically instructed.
- Follow the team's repository and security policy regarding whether this file should be committed to Git.

 8. Final Gradle Build
After adding `google-services.json`, allow Android Studio to complete Gradle synchronization and project configuration.

Verify that:
- Gradle Sync is successful.
- No dependency errors are present.
- No configuration errors are present.
- The correct Git branch is checked out.
- `google-services.json` exists inside the `app` directory.

 9. Run the Application
Once the project has been successfully initialized:

- Connect an Android device or start an Android Emulator.
- Select the target device from Android Studio.
- Click the **Run ▶** button.
- Android Studio will build and install the application.
- The application will launch on the selected device.

 10. Complete Setup Flow
```
Project Setup
      ↓
Git Clone
      ↓
Open in Android Studio
      ↓
Checkout Required Branch
      ↓
Gradle Sync
      ↓
Restart Android Studio
      ↓
Add google-services.json
      ↓
Gradle Build
      ↓
Select Device
      ↓
Run ▶
      ↓
Application Launch
```
 11. Initial Application Flow
Once the application launches, the general application flow is:
```
Application Launch
        ↓
Application Initialization
        ↓
Authentication / Session Check
        ↓
Login / Existing Session
        ↓
Dashboard
        ↓
Module Navigation
```
The exact screen displayed after application launch depends on the current authentication and session state.

 12. Development Workflow
After successful initialization, developers should follow this workflow:
```
Checkout Required Branch
        ↓
Pull Latest Changes
        ↓
Open Project
        ↓
Gradle Sync
        ↓
Run Application
        ↓
Develop / Modify Feature
        ↓
Test
        ↓
Commit Changes
        ↓
Push Changes
```

Before starting development, ensure that the local branch is up to date with the remote branch.

13. Important Notes
Git Branch
Always verify the active branch before making code changes.

```bash
git branch
```
The currently active branch will be marked with `*`.

Gradle
Do not interrupt the initial Gradle synchronization or build process.

Configuration
Use only the configuration files provided by the project/team.
Credentials and Secrets
Do not hardcode passwords, API tokens, private keys, or other sensitive credentials in the source code.

14. Troubleshooting
Gradle Sync Failed
Try:
```bash
.\gradlew.bat clean
```
Then synchronize the project again from Android Studio.

Google Services Error
Verify that the following file exists:
```
app/google-services.json
```
Also verify that the correct configuration file has been provided by the project/team.

Application Does Not Run
Verify the following:
- Gradle synchronization completed successfully.
- Correct Git branch is checked out.
- `google-services.json` is present.
- Android device/emulator is available.
- Required Android SDK is installed.
- No build errors are present.
- Android Studio has completed indexing.

 15. Setup Complete
The project is considered successfully initialized when:
- The required Git branch is checked out.
- Gradle synchronization completes without errors.
- Required project configuration is available.
- The project builds successfully.
- The application installs successfully.
- The application launches successfully on the target device/emulator.
Once these steps are completed, the developer can proceed with feature development and testing.

____________________________________________________________________________

