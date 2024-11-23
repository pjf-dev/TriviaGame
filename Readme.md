# Trivia Game
Trivia Game is a game made for Android as a project for the 
Mobile Application Development course at the University of North Georgia.
## Description & Current Features
Play a game of trivia with up to 5 friends in a round-robbin style game
where players answer trivia to receive points and compete for first place amongst themselves.
Players can configure the game parameters (Category, Player Count, Game Mode)
and start the trivia game. During play, players will be met with a question along with
four possible answers, where they must guess the correct answer within 30 seconds 
or it's automatically incorrect.

In the Classic game mode, players will each answer 10 questions where they'll then
receive a score for each question based on their response time and question difficulty.
At the end of the game, each players score is displayed along with their respective ranking.

In the Infinity game mode, players each answer an unlimited number of questions
until they answer 3 questions incorrectly, at which point they'll be eliminated from the game.
The game is over once there is only 1 player remaining, or if in single player, no players remaining.
At the end of the game, each player gets a score based on their number of correct answers along with
the number of strikes they had remaining at the end of the game. Each players score is displayed
along with their respective rankings.

## How to run
Ensure you have the latest version of [Android Studio](https://developer.android.com/studio), 
then clone this project using Git into a directory of your choice.

Import the project directory into Android Studio and allow the gradle import to complete.
If the import doesn't automatically start, press the "Sync Project with Gradle Files" button
in the top bar (Default shortcut `CTRL+SHIFT+O`).

Before the next step, ensure you have an Android test device properly linked to Android Studio 
running API level 33 or later. API levels down to 26 should work, but aren't very tested, so 33 is recommended at a minimum.
This will likely be an emulator, but can also be a physical Android device. 
For more detail, see [this guide to setup an Android emulator](https://developer.android.com/studio/run/emulator).

Once the import is complete, you should automatically have a configuration titled "app"
that will run the application on your default Android test device (likely an emulator). If this configuration is missing,
you can add it by going to `Edit Configurations ... > + > Android App` and specifying
the module as `Trivia_Game.app.main`. Run this configuration and the app will build, install, 
and launch on your test device.

## Other Info
Unit tests in `./app/src/test/` for TriviaAPI were originally created to test
online TriviaAPI implementation before font end / game logic were ready.
After initial confirmation of the API and tests, the tests were then no longer maintained
and don't all pass after some changes to the API code. All subsequent testing
was done via running the app on an Android device emulator.

## Used Libraries (Non-Android)
- [OkHTTP](https://square.github.io/okhttp/)

## Help / References
[Git Branches Guide](./docs/GitBranches.md)