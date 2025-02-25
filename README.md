# Character TTS

Character TTS is a simple yet powerful plugin that integrates Azure Text-to-Speech capabilities into your project. It converts dialogue into spoken audio using a variety of Azure voices.

## Features

- **Azure Integration:**  
  Configure your API key and region to connect to Azure’s Text-to-Speech services.
- **Voice Selection:**  
  - Choose from all available Azure voices.
  - Assign a dedicated voice for the player character.
  - Select a different voice for all non-player characters (NPCs).
- **Dialogue Playback:**  
  Currently, only dialogue is spoken.
- **Planned Enhancements:**  
  Future updates will support unique voices for named NPCs, adding more personality and depth to in-game interactions.

## Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/yourusername/character-tts.git
   ```
2. **Open in IntelliJ:**
   - Open the project in IntelliJ IDEA.
   - Enable Git in your project by going to **VCS > Enable Version Control Integration…** and selecting **Git**.
3. **Build the Plugin:**
   - Follow your usual build process in IntelliJ to compile the plugin.

## Configuration

Before using the plugin, set up your Azure credentials:

1. **API Key and Region:**
   - In the plugin settings, enter your Azure API key and region.
2. **Voice Settings:**
   - Select the desired Azure voices from the provided list.
   - Assign one voice for the player and another for all NPCs.

## Usage

After installation and configuration:

1. Launch your application with the plugin enabled.
2. The plugin will convert in-game dialogue into speech using the selected voices.
3. Enjoy the enhanced audio experience.

## Future Development

- **Unique NPC Voices:**  
  Future updates will allow for assigning unique voices to named NPCs for a more immersive experience.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request if you have suggestions, bug reports, or improvements.

## License

This project is licensed under the MIT License.
