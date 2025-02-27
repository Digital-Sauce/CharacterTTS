# Character TTS

Character TTS is a simple yet powerful plugin that integrates Azure Text-to-Speech capabilities into the RuneLite client. It converts written dialogue into spoken audio using the Azure cloud API.

## Features

- **Azure Integration:**  
  Configure your API key and region (northcentralus) to connect to Azure’s Text-to-Speech services.
- **Voice Selection:**  
  - Choose from all available Azure voices.  
  - Assign a dedicated voice for the player character.  
  - **Gender-Based Voice Assignment:**  
    - The plugin now includes a **GenderService** that determines the gender of NPCs.  
    - GenderService queries the OSRS Wiki API to parse and cache gender data.  
    - If an NPC has a known gender, their voice will match the gender.  
    - If an NPC's gender is unknown, they will share the player’s voice.  
- **Dialogue Playback:**  
  Currently, only dialogue text is spoken.

## Installation

1. **Clone the Repository:**

   git clone https://github.com/yourusername/character-tts.git

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
   - The **GenderService** will automatically assign gender-appropriate voices when possible.

3. **Save All:**  

   - In the plugin settings, enter your Azure API key and region and select Save All to generate the voices list.
   - One genereated, select your voices and adjust pitch and volume as desired.
   - You can test each voice in the TTS seciton of the menu
   - When finished, hit Save All again to apply changes

## Contributing

Contributions are welcome! Please open an issue or submit a pull request if you have suggestions, bug reports, or improvements.  

## License

This project is licensed under the MIT License.
