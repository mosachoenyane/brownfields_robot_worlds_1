# 🤖 Robot World Simulator 🚀

Welcome to the **Robot World Simulator**, where robots battle, explore, and survive in a dangerous world filled with obstacles!

## 🔥 Features

- **Robot Commands**: Move (`forward`, `back`), turn (`left`, `right`), `fire`, `look`, `reload`, and `repair`!
- **Dangerous World**: Navigate around mountains, lakes, and deadly pits!
- **Multiplayer**: Connect multiple clients and compete (or cooperate?).
- **Server Admin**: Use the server console to monitor robots and world state.

## 🎮 How to Play

### 1. Launch a Robot

```
launch [make] [name]
```

*(e.g., `launch Sniper Robo-1`)*

### 2. Explore & Fight

- `look` – Scan your surroundings.
- `forward` – Move forward by n steps.
- `back` – Move backend by n steps.
- `fire` – Shoot lasers! (But ammo is limited.)
- `reload` – Restock your weapons.

### 3. Don't Die

- Watch your **shields** (they block damage).
- Avoid **pits** (instant death!).

## 🛠️ Server Commands (Admin Only)

- `dump` – View the entire world state.
- `robots` – List all active robots.
- `quit` – Shut down the server.

## 🚀 Quick Start

```bash
# Run the server
java -jar RobotWorldServer.jar

# Run a client
java -jar RobotWorldClient.jar
```
## Install eodsql
mvn install:install-file -Dfile=.libs/eodsql.jar -DgroupId=net.lemnik -DartifactId=eodsql -Dversion=2.2 -Dpackaging=jar

## 📜 Fun Fact

The `Pit` obstacle instantly destroys any robot that steps on it. **No second chances!**

---

Made with ❤️ (and maybe too much coffee) by your friendly neighborhood robot overlords. 🤖☕