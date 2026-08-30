# 🌸Bloombot🌸


## 🌱 About the app
<img width="5025" height="3350" alt="bloombot-mockup" src="https://github.uio.no/user-attachments/assets/8f6c68a6-81e4-4cf7-baa8-3e2392e7b05a" />

Bloombot is a gardening app and plant diary made for hobby gardeners in Norway. The app helps you keep track of all your gardens and the individual plants within them, including logging watering and fertilizing each plant. The app uses weather data from the Meterological Institue Norway.

### Features
**Location and plant recommendations**
- Save a garden location via live location GPS or via address search.
- Get plant recommendations for a location based on [Norwegian hardiness zones](https://www.hageselskapet.no/praktisk/klimasonekart/106772)


**Garden and plant managment**
- Manage multiple saved gardens with plants
- Log watering and fertilizing per plant

**Weather forecast**
- Get a weather forecast for each saved location with data from the MET API
- Recieve weather alerts (MetAlerts) when there are severe conditions at a location

**AI advice**
- Tap the AI button to get personalized gardening tips for your plants at a given location
- The AI gives your advice based on your location, your plants and live weather data from LocationForecast and MetAlerts from MET's MCP server.


<br>

## 📦 How to run the app

1. **Download Android Studio and make sure it is Panda 4 release** - Android Studio needs to be in the Panda 4 version (because the AGP version in the project is 9.2.1, [link](https://developer.android.com/studio/releases). [Guide for installation](https://developer.android.com/studio/install)
2. **Clone the project to Android studio and open it** - clone the project via GitHub [Guide](https://www.geeksforgeeks.org/git/how-to-clone-android-project-from-github-in-android-studio/)


3. **Set up API keys** - This app needs a few API keys to run (Mapbox and Claude AI). These are kept out of the repo for security, so you'll need to add your own:

    - In the project root (same folder as `settings.gradle.kts`), copy `local.properties.example` and rename the copy to `local.properties`
    - Get your Mapbox tokens at [account.mapbox.com/access-tokens](https://account.mapbox.com/access-tokens/) (free account required):
        - **Public token**: copy the existing "Default public token" (starts with `pk.`)
        - **Secret token**: click "Create a token", check the **Downloads:Read** scope under Secret scopes, and create it (starts with `sk.`) — this is required to download the Mapbox SDK itself
    - Get a Claude API key at [console.anthropic.com](https://console.anthropic.com/) → Settings → API Keys (an Anthropic account is required)
    - Open `local.properties` and fill in your keys:
    -  MAPBOX_DOWNLOADS_TOKEN=sk.your_secret_token_here
       MAPBOX_ACCESS_TOKEN=pk.your_public_token_here
       CLAUDE_API_KEY=sk-ant-your_claude_key_here

4. **Sync and rebuild** - Click **Sync Now** if prompted, then go to **Build → Rebuild Project** to make sure the keys are picked up correctly.

5. **Permissions** - The app needs location if the user wants to save locations based on live location. The app will ask for location permissions, so make sure that your emulator or physical device has live location enabled.

6. **Run the app**  - Use Android Studio's built in emulator or use a physical Android device (minSDK 33 / Android 13). The target SDK is 36, the latest version. [Guide](https://developer.android.com/studio/run/emulator) - **Make sure that you have internet connection, the app requires it to search up addresses to save a location!**

7. **Troubleshooting:** If an error occurs, make sure that you're running the latest Android Studio version. Run **Gradle sync**, then **Build -> Clean project.**


<br><br>

## 📚 Libaries

### 📍 Map and location
- **Mapbox Maps SDK** - Interactive map in the app.

- **Mapbox Maps Compose** - Compose integration for MapBox map, so the map can be used as a Composable.

- **Mapbox Search SDK** - Fetches place suggestions based on text search

- **Accompanist Permissions** - Runtime permissions for Jetpack Compose, used for requesting live location permission from the user and save it

### 🌐 Networks
- **Retrofit** - HTTP-client for REST API calls.

- **Retrofit Kotlinx Serialization Converter** - Connects Retrofit to Kotlinx Serialization for automatic JSON parsing.

- **OkHttp** - HTTP client that is used by retrofit

- **OkHttp SSE** - Enables the client to listen to a continuous stream of data from the server for live updates.

- **Ktor** - HTTP client to build asynchronous servers and clients in Kotlin

- **Kotlinx Serialization** - JSON-serializing and deserializing of data classes


### 🤖 AI / MCP
- **MCP Kotlin SDK Client** - Client for MCP, allows AI-models to communicate with MET's MCP server to fetch data

### 🗃️ Database
- **Room** - Local SQLite-database for saving plants and places in the app

### 📱 UI 
- **Jetpack Compose + Material3** - UI-framework.

- **Material Icons Extended** - Icon library for Compose

- **Coil** - For loading and showing images in Compose, used for loading SVG weather icons in this project

- **Navigation Compose** - Navigation between screens in Compose

### ⚙️ Architecture
- **Lifecycle** - Part of Jetpack Compose, allows components to observe and respond to state in the app.

- **Kotlinx Coroutines** - For asynchronous operations.

### 🪛 Testing
- **JUnit** - Library that is used for writing unit tests and testing them.

- **MockK** - For creating mock objects in unit tests, for example mock repositories that does not affect the real repositories.

- **Kotlinx Coroutines Test** - For testing code that uses coroutines.

<br>

## Sources for plant data and climate zones
We got permission from **Det norske hageselskap** to use data about their climate zones from their website https://klimasonekart.no/

For collecting data about plants, we used these websites:
- Rhs plants is the UK’s leading gardening charity: https://www.rhs.org.uk/plants

- Planteportalen, which is a website containting extensive information about over 1900 plants https://planteportalen.no/
- Plantasjen is one of the leading retailers of plants in the nordics, and have a lot of information about plant care: https://plantasjen.no/no


## Picture sources

Weather icons taken from Norwegian Meteorological Institute GitHub repository.
- metno. (2021). *Weather icons* [SVG icons]. GitHub. <https://github.com/metno/weathericons/tree/main/weather/svg>

<br>

Plant pictures taken from [Pixabay](https://pixabay.com/). Their images are free to use under the CC0 license, as written under their [Terms of Service](https://pixabay.com/service/terms/)
- Alexei. (2019). *Cucumbers vegetables food healthy*. [Photograph]. Pixabay. https://pixabay.com/photos/cucumbers-vegetables-food-healthy-4698527/
- Andrews Andrews, V. (2021). *Sunflowers sunflower field*. [Photograph]. Pixabay. https://pixabay.com/photos/sunflowers-sunflower-field-6607530/
- Angela. (2015). *Cherries sour cherries morello*. [Photograph]. Pixabay. https://pixabay.com/photos/cherries-sour-cherries-morello-598170/
- Böckel, M. (2020). *Chives herbs plant garden nature*. [Photograph]. Pixabay. https://pixabay.com/photos/chives-herbs-plant-garden-nature-5022271/
- Böckel, M. (2020). *Rosemary seasoning herbs plant*. [Photograph]. Pixabay. https://pixabay.com/photos/rosemary-seasoning-herbs-plant-4978895/
- Bruno. (2016). *Bleeding heart flowers plant*. [Photograph]. Pixabay. https://pixabay.com/photos/bleeding-heart-flowers-plant-1425870/
- Couleur. (2019). *Lilac Lilac Blossom Bloom Violet*. [Photograph]. Pixabay. https://pixabay.com/photos/lilac-lilac-blossom-bloom-violet-3373924/
- Daniel Dan outsideclick f. (2020). *The planting of the lettuce*. [Photograph]. Pixabay. https://pixabay.com/photos/the-planting-of-the-lettuce-5325655/
- Etienne GONTIER. (2018). *Tulips red tulips tulip*. [Photograph]. Pixabay. https://pixabay.com/photos/tulips-red-tulips-tulip-3335850/
- Hans. (2013). *Geranium Red Blossom Bloom Flower*. [Photograph]. Pixabay. https://pixabay.com/photos/geranium-red-blossom-bloom-flower-141553/
- Hans. (2016). *Lavender flowers field bloom*. [Photograph]. Pixabay. https://pixabay.com/photos/lavender-flowers-field-bloom-1595581/
- Hirst, S. (2012). *Strawberries Strawberry*. [Photograph]. Pixabay. https://pixabay.com/photos/strawberries-strawberry-56995/
- Jai79. (2015). *Potato Agriculture Food Meal Earth*. [Photograph]. Pixabay. https://pixabay.com/photos/potato-agriculture-food-meal-earth-983788/
- Katharina N. (2020). *Dill cucumber umbelliferae*. [Photograph]. Pixabay. https://pixabay.com/photos/dill-cucumber-umbelliferae-4741813/
- Katharina N. (2021). *Parsley leaves plant herb*. [Photograph]. Pixabay. https://pixabay.com/photos/parsley-leaves-plant-herb-6395051/
- LoggaWiggler. (2013). *Leek spring onion food market*. [Photograph]. Pixabay. https://pixabay.com/photos/leek-spring-onion-food-market-65277/
- jacqueline macou Macou, J. (2015). *Sheets basil aromatic plant*. [Photograph]. Pixabay. https://pixabay.com/photos/sheets-basil-aromatic-plant-739286/
- NARAYANAN MADESHAN. (2022). *Bell pepper fruit plant capsicum*. [Photograph]. Pixabay. https://pixabay.com/photos/bell-pepper-fruit-plant-capsicum-6936732/
- NoName_13. (2017). *Apple red red apple*. [Photograph]. Pixabay. https://pixabay.com/photos/apple-red-red-apple-2788616/
- Pixabay. (2018). *Rhododendron Purple Garden Blossom*. [Photograph]. Pixabay. https://pixabay.com/photos/rhododendron-purple-garden-blossom-3418387/
- Ralph. (2018). *Dahlia dahlias bud flower bud*. [Photograph]. Pixabay. https://pixabay.com/photos/dahlia-dahlias-bud-flower-bud-3540835/
- Ralph. (2018). *Rose flower red rose rose bloom*. [Photograph]. Pixabay. https://pixabay.com/photos/rose-flower-red-rose-rose-bloom-3506327/
- Staab, D., . (2020). *Daisies flower garden nature*. [Photograph]. Pixabay. https://pixabay.com/photos/daisies-flower-garden-nature-5212890/
- Stefan. (2018). *Pansy pansy flower viola tricolor*. [Photograph]. Pixabay. https://pixabay.com/photos/pansy-pansy-flower-viola-tricolor-2096071/
- svklimkin. (2016). *Carrot Growth Vegetables*. [Photograph]. Pixabay. https://pixabay.com/photos/carrot-growth-vegetables-1565597/
- Wilstermann-Hildebrand, M. (2019). *Beef tomato tomato tomatoes*. [Photograph]. Pixabay. https://pixabay.com/photos/beef-tomato-tomato-tomatoes-4062505/
