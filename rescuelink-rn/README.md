# 📱 RescueLink - React Native App

Hey there! 👋 Welcome to the **React Native** portion of RescueLink. While the main app focuses heavily on a native Android experience, this folder contains our cross-platform React Native playground!

This project was bootstrapped using [`@react-native-community/cli`](https://github.com/react-native-community/cli) and is designed to bring the RescueLink experience to both iOS and Android simultaneously.

---

## 🚀 Let's Get Running!

Before you start, make sure your development environment is fully set up. If you haven't yet, check out the official [React Native Environment Setup Guide](https://reactnative.dev/docs/set-up-your-environment).

### Step 1: Fire up Metro

Think of Metro as the engine that bundles all our JavaScript code together. Open a terminal in this `rescuelink-rn` folder and run:

```sh
# If you prefer npm
npm start

# Or if you're a Yarn fan
yarn start
```

### Step 2: Build and launch the app

Leave the Metro terminal running! Open a *new* terminal window (still inside the `rescuelink-rn` folder) and let's get the app on a screen.

#### For Android users 🤖

```sh
# Using npm
npm run android

# Using Yarn
yarn android
```

#### For iOS users 🍎

*Note: You only need to do this step if you're running on a Mac.*

The very first time you clone the project (or whenever we add new native dependencies), you need to install the iOS pods:

```sh
# Install CocoaPods if you haven't already
bundle install

# Install the iOS dependencies
bundle exec pod install
```

Once that's done, launch the iOS simulator:

```sh
# Using npm
npm run ios

# Using Yarn
yarn ios
```

If the stars align (and your setup is correct), you should see RescueLink running right in front of you! 🎉

---

## 🛠️ Making Changes (The Fun Part)

Ready to tinker? 

1. Open up `App.tsx` (or any screen inside `src/`) in your favorite text editor.
2. Make a small change, hit save, and watch the magic! 
3. Thanks to [Fast Refresh](https://reactnative.dev/docs/fast-refresh), your app will update almost instantly.

**Need a hard reset?**
- **Android**: Double-tap the <kbd>R</kbd> key on your keyboard, or open the Dev Menu (<kbd>Cmd ⌘</kbd> + <kbd>M</kbd> on Mac, <kbd>Ctrl</kbd> + <kbd>M</kbd> on Windows) and click **"Reload"**.
- **iOS**: Just press <kbd>R</kbd> while the Simulator is in focus.

---

## 📚 Want to learn more?

If you're new to React Native or just want to brush up on some skills, here are some awesome resources:
- [React Native Official Website](https://reactnative.dev)
- [Learn the Basics](https://reactnative.dev/docs/getting-started)
- [Stuck? Check out Troubleshooting](https://reactnative.dev/docs/troubleshooting)

*Happy coding!* ✨
