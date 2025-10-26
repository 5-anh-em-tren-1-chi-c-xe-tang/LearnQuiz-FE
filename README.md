# AIQuiz-FE
Front end for AIQuiz using Java (Android app)

# Folder structure
```
MyApp/
│
├── app/                      # Main Android app module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/myapp/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/         # Entity classes
│   │   │   │   │   ├── network/       # APIs calls
│   │   │   │   │   ├── repository/    # Data repositories (Data get from network/db)
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── activities/    # Activities (MainActivity, etc.)
│   │   │   │   │   ├── fragments/     # Fragments (HomeFragment, etc.)
│   │   │   │   │   ├── viewmodel/     # ViewModels
│   │   │   │   │   └── adapter/       # RecyclerView adapters
│   │   │   │   │
│   │   │   │   ├── utils/             # Helpers, formatters, constants
│   │   │   │   └── MyApp.java         # Application class
│   │   │   │
│   │   │   ├── res/                   # XML resources
│   │   │   │   ├── layout/            # UI layouts
│   │   │   │   ├── values/            # strings.xml, colors.xml, styles.xml
│   │   │   │   ├── drawable/          # icons, shapes
│   │   │   │   └── mipmap/            # app icons
│   │   │   └── AndroidManifest.xml
│   │   └── test/                      # Unit tests
│   │
│   ├── build.gradle
│   └── proguard-rules.pro
│
├── build.gradle
└── settings.gradle
```
