# LearnQuiz-FE: Camera Quiz Generation Implementation Guide

## 📋 Implementation Status

### ✅ COMPLETED (Phase 1 - Data Layer)

#### 1. Dependencies Added
**File: `gradle/libs.versions.toml` + `app/build.gradle.kts`**
- ✅ Retrofit 2.11.0 (REST API client)
- ✅ Gson Converter (JSON serialization)
- ✅ OkHttp 4.12.0 + Logging Interceptor
- ✅ Glide 4.16.0 (Image loading & caching)
- ✅ PhotoView 2.3.0 (Pinch-zoom support)
- ✅ JitPack repository configured

**Build Status:** ✅ **BUILD SUCCESSFUL** (38 tasks executed)

---

#### 2. Data Models Created

**Quiz API Models** (`data/model/quiz/`):
```
✅ GenerateQuizRequest.java
   - List<String> images (Base64 encoded, 1-10 images)
   - String language (2-letter code)
   - int questionCount (1-20)
   - String visibility (public/private)
   - int quizExamTimeLimit (0-7200 seconds)
   - String folderId (optional)
   - isValid() validation method

✅ GenerateQuizResponse.java
   - String id, title, description, context
   - List<QuizQuestion> questions
   - Date createdAt
   - String imageSource

✅ QuizQuestion.java
   - String question
   - List<QuizAnswer> answers
   - String explanation
   - getCorrectAnswer() helper

✅ QuizAnswer.java
   - String answer
   - boolean isTrue

✅ ApiResponse<T>.java
   - Generic wrapper for all API responses
   - boolean success
   - String message
   - T data
   - String errorCode
```

**Camera Models** (`data/model/camera/`):
```
✅ ImageRegion.java
   - Rect bounds (selected area coordinates)
   - float scaleX, scaleY (relative to original)
   - int originalImageWidth, originalImageHeight
   - getScaledBounds(), isValid() methods

✅ CapturedImage.java
   - String id (timestamp-based)
   - String base64Data (for API upload)
   - ImageRegion region
   - Bitmap thumbnail (120x120)
   - Uri imageUri (original file)
   - boolean fromCamera
   - hasValidRegion(), isReadyForUpload()

✅ PhotoSession.java
   - List<CapturedImage> images (max 10)
   - addImage(), removeImage(), getImage()
   - getBase64Images() for API payload
   - areAllImagesReady() validation
   - clear() with bitmap cleanup
```

---

#### 3. Network Layer

**API Configuration** (`data/network/`):
```
✅ ApiEndpoints.java
   - BASE_URL constant (TODO: Replace with actual backend)
   - GENERATE_QUIZ = "api/quiz/generate"
   - Timeout configs: 30s connect, 60s read/write

✅ ApiService.java (Retrofit Interface)
   @POST(GENERATE_QUIZ)
   Call<ApiResponse<GenerateQuizResponse>> generateQuiz(@Body GenerateQuizRequest)
   
   @POST(GENERATE_QUIZ)
   Call<...> generateQuizWithAuth(@Header("Authorization") String token, @Body ...)
   
   @GET("api/quiz/{id}")
   Call<...> getQuizDetail(@Path("id") String quizId)

✅ RetrofitClient.java (Singleton)
   - OkHttpClient with interceptors:
     * AuthInterceptor: Auto-adds Bearer token from SharedPreferences
     * HttpLoggingInterceptor: Debug network calls
   - Gson with ISO 8601 date format
   - Methods: getInstance(), getApiService()
   - Token management: setAuthToken(), clearAuthToken()
```

---

#### 4. Repositories

**QuizRepository** (`data/repository/QuizRepository.java`):
```
✅ generateQuiz(request, callback)
   - Validates request with isValid()
   - Async API call with Retrofit
   - Handles HTTP codes: 200, 400, 401, 403, 404, 500, 503
   - Callback interface: onSuccess(response), onError(message, code)

✅ generateQuizWithAuth(request, token, callback)
   - Same as above with Bearer token header

✅ getQuizDetail(quizId, callback)
   - Fetch quiz by ID

Error Handling:
- 400: "Invalid request data"
- 401: "Unauthorized. Please login again"
- 500: "Server error. Try again later"
- Network error: "Network error: {exception}"
```

**ImageRepository** (`data/repository/ImageRepository.java`):
```
✅ processImage(uri, region, fromCamera, callback)
   - Runs on ExecutorService background thread
   - Load → Crop → Resize → Compress → Base64
   - Returns CapturedImage with base64Data and thumbnail
   - Automatic bitmap recycling

✅ processBatch(photoSession, callback)
   - Process multiple images
   - onComplete(successCount, errorCount)

✅ shutdown()
   - Cleanup executor when done
```

---

#### 5. Utilities

**ImageUtils** (`utils/ImageUtils.java`):
```
✅ loadBitmapFromUri(context, uri)
   - Loads with EXIF orientation fix
   - Auto-rotates based on camera metadata

✅ cropBitmap(source, region)
   - Bounds validation to prevent crashes

✅ resizeForUpload(source, maxWidth=1920)
   - Maintains aspect ratio
   - Returns original if already smaller

✅ createThumbnail(source, size=120)
   - Square thumbnail with scaling

✅ compressToBase64(bitmap, quality=85)
   - JPEG compression
   - Returns "data:image/jpeg;base64,{encoded}"

✅ fixOrientation(context, uri, bitmap)
   - Matrix rotation based on EXIF

✅ loadSampledBitmap(context, uri, reqWidth, reqHeight)
   - Memory-efficient loading with inSampleSize
   - Prevents OutOfMemoryError on large images
```

**Constants** (`utils/Constants.java`):
```
✅ Request codes: CAMERA (1001), GALLERY (1002), PREVIEW (1003)
✅ Intent extras keys: IMAGE_URI, FROM_CAMERA, QUIZ_RESPONSE
✅ Image settings: MAX_WIDTH (1920), JPEG_QUALITY (85), THUMBNAIL_SIZE (120)
✅ Session limits: MAX_IMAGES (10), MIN_IMAGES (1)
✅ Quiz defaults: DEFAULT_LANGUAGE ("en"), DEFAULT_QUESTION_COUNT (5)
✅ SharedPreferences keys: AUTH_TOKEN, USER_ID, LANGUAGE
✅ Supported languages: ["en", "vi", "ja", "fr", "es", "de"]
```

---

#### 6. ViewModels

**PhotoSessionViewModel** (`ui/viewmodel/PhotoSessionViewModel.java`):
```
✅ LiveData<List<CapturedImage>> getImages()
✅ LiveData<Integer> getImageCount()
✅ LiveData<Boolean> isFull()
✅ addImage(CapturedImage) → updates LiveData
✅ removeImage(imageId) → cleanup + update
✅ getBase64Images() → List<String> for API
✅ areAllImagesReady() → validation
✅ clearSession() → cleanup all bitmaps
✅ onCleared() → automatic cleanup on destroy
```

**QuizGenerationViewModel** (`ui/viewmodel/QuizGenerationViewModel.java`):
```
✅ LiveData<Resource<GenerateQuizResponse>> getQuizResult()
✅ generateQuiz(request) → async API call
✅ generateQuizWithAuth(request, token)
✅ resetQuizResult() → clear state

Resource<T> wrapper:
- Status: LOADING, SUCCESS, ERROR
- data: T (response object)
- message: String (error message)
- errorCode: int (HTTP status)
```

---

#### 7. Resources

**Strings** (`res/values/strings.xml`):
```
✅ Camera Activity: camera_title, camera_capture, camera_flip, camera_flash, camera_gallery
✅ Photo Preview: preview_title, preview_confirm, preview_retake, preview_instruction
✅ Quiz Generation: generation_title, generation_photos, generation_add_photo, generation_settings
✅ Error messages: error_network, error_server, error_unauthorized, error_invalid_data
✅ Common: loading, retry, cancel, ok, delete, confirm
```

**Permissions** (`AndroidManifest.xml`):
```
✅ CAMERA
✅ READ_EXTERNAL_STORAGE (maxSdkVersion=32)
✅ READ_MEDIA_IMAGES (Android 13+)
✅ INTERNET
✅ ACCESS_NETWORK_STATE
```

---

## 🔄 Data Flow Architecture (Implemented)

```
USER CAPTURES PHOTO
    ↓
Save to app cache → Uri
    ↓
USER SELECTS REGION → ImageRegion (Rect bounds)
    ↓
ImageRepository.processImage()
    ├─> ImageUtils.loadBitmapFromUri() → fix EXIF
    ├─> ImageUtils.cropBitmap(region) → crop selected area
    ├─> ImageUtils.resizeForUpload(1920px) → maintain aspect
    ├─> ImageUtils.compressToBase64(85%) → "data:image/jpeg;base64,..."
    └─> Return CapturedImage { base64Data, thumbnail, metadata }
    ↓
PhotoSessionViewModel.addImage(capturedImage)
    ↓
PhotoSession stores in-memory List<CapturedImage> (max 10)
    ↓
USER CLICKS "GENERATE QUIZ"
    ↓
Build GenerateQuizRequest:
    {
      images: PhotoSession.getBase64Images(),
      language: "en",
      questionCount: 5,
      visibility: "public",
      quizExamTimeLimit: 300
    }
    ↓
QuizGenerationViewModel.generateQuiz(request)
    ↓
QuizRepository.generateQuiz(request, callback)
    ↓
RetrofitClient → POST /api/quiz/generate
    Headers: Authorization: Bearer {token}, Content-Type: application/json
    Body: JSON(GenerateQuizRequest)
    ↓
BACKEND API PROCESSES (External)
    ↓
HTTP Response → ApiResponse<GenerateQuizResponse>
    ↓
QuizRepository handles response:
    ├─> 200 OK: callback.onSuccess(GenerateQuizResponse)
    ├─> 400: callback.onError("Invalid data", 400)
    ├─> 401: callback.onError("Unauthorized", 401)
    ├─> 500: callback.onError("Server error", 500)
    └─> Network Error: callback.onError("Network error: {msg}", -1)
    ↓
ViewModel.quizResultLiveData updates:
    ├─> LOADING: Show ProgressBar
    ├─> SUCCESS: Display quiz questions
    └─> ERROR: Show error dialog with retry
```

---

## 📝 TODO (Phase 2 - UI Layer)

### Required UI Components

#### 1. CameraActivity
**Purpose:** Full-screen camera with capture button

**Layout: `activity_camera.xml`**
```xml
<FrameLayout>
    <androidx.camera.view.PreviewView (full-screen)/>
    <MaterialToolbar (top: title, flip camera button)/>
    <LinearLayout (bottom: gallery, capture, flash buttons)/>
</FrameLayout>
```

**Java: `CameraActivity.java`**
- Initialize CameraX with ImageCapture use case
- Bind camera to lifecycle
- Capture button → save to cache → Intent to PhotoPreviewActivity
- Gallery button → Intent.ACTION_PICK → PhotoPreviewActivity

---

#### 2. PhotoPreviewActivity
**Purpose:** Preview photo with region selection overlay

**Layout: `activity_photo_preview.xml`**
```xml
<ConstraintLayout>
    <com.github.chrisbanes.photoview.PhotoView (zoom support)/>
    <com.example.learnquiz_fe.ui.views.RegionSelectorView (overlay)/>
    <MaterialToolbar (top: back, confirm buttons)/>
    <TextView (bottom: instruction)/>
</ConstraintLayout>
```

**Java: `PhotoPreviewActivity.java`**
- Load image from URI (Intent extra)
- RegionSelectorView handles touch events
- Confirm button:
  1. Get ImageRegion from RegionSelectorView
  2. ImageRepository.processImage(uri, region, fromCamera)
  3. PhotoSessionViewModel.addImage(capturedImage)
  4. Navigate back or to QuizGenerationActivity

**Custom View: `RegionSelectorView.java`**
```java
public class RegionSelectorView extends View {
    private Rect selectedRegion;
    private Paint borderPaint, overlayPaint;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ACTION_DOWN: Start drag
        // ACTION_MOVE: Update region bounds
        // ACTION_UP: Finalize selection
        invalidate(); // Redraw
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw semi-transparent overlay (50% black)
        // Clear selected region (transparent)
        // Draw border around region
        // Draw corner handles for resize
    }
    
    public ImageRegion getSelectedRegion() {
        // Return ImageRegion with bounds and scale factors
    }
}
```

---

#### 3. QuizGenerationActivity
**Purpose:** Gallery of photos + settings + generate button

**Layout: `activity_quiz_generation.xml`**
```xml
<LinearLayout vertical>
    <MaterialToolbar/>
    
    <!-- Photo Gallery -->
    <TextView "Photos (3)"/>
    <RecyclerView horizontal (photos with delete buttons)/>
    <Button "+ Add Photo"/>
    
    <!-- Settings -->
    <TextView "Settings"/>
    <Spinner "Language" (en, vi, ja, ...)/>
    <TextView "Question Count"/>
    <SeekBar (1-20) + TextView (current value)/>
    <RadioGroup "Visibility" (Public/Private)/>
    <EditText "Time Limit (seconds)" hint="0-7200"/>
    
    <!-- Generate Button -->
    <MaterialButton "Generate Quiz" (full-width)/>
    
    <!-- Loading Overlay -->
    <FrameLayout (invisible initially)>
        <ProgressBar/>
        <TextView "Generating quiz..."/>
    </FrameLayout>
</LinearLayout>
```

**Java: `QuizGenerationActivity.java`**
```java
public class QuizGenerationActivity extends AppCompatActivity {
    private PhotoSessionViewModel sessionViewModel;
    private QuizGenerationViewModel generationViewModel;
    private RecyclerView rvPhotos;
    private PhotoThumbnailAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get ViewModels
        sessionViewModel = new ViewModelProvider(this).get(PhotoSessionViewModel.class);
        generationViewModel = new ViewModelProvider(this).get(QuizGenerationViewModel.class);
        
        // Observe photo session
        sessionViewModel.getImages().observe(this, images -> {
            adapter.setImages(images);
            tvPhotoCount.setText("Photos (" + images.size() + ")");
        });
        
        // Observe quiz generation result
        generationViewModel.getQuizResult().observe(this, resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    hideLoading();
                    navigateToQuizResult(resource.getData());
                    break;
                case ERROR:
                    hideLoading();
                    showErrorDialog(resource.getMessage());
                    break;
            }
        });
        
        // Generate button click
        btnGenerate.setOnClickListener(v -> {
            GenerateQuizRequest request = new GenerateQuizRequest();
            request.setImages(sessionViewModel.getBase64Images());
            request.setLanguage(spinnerLanguage.getSelectedItem().toString());
            request.setQuestionCount(seekBarCount.getProgress());
            request.setVisibility(rgVisibility.getCheckedRadioButtonId() == R.id.rb_public ? "public" : "private");
            request.setQuizExamTimeLimit(Integer.parseInt(etTimeLimit.getText().toString()));
            
            if (!request.isValid()) {
                Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Call API
            String token = RetrofitClient.getInstance(this).getAuthToken();
            if (token != null) {
                generationViewModel.generateQuizWithAuth(request, token);
            } else {
                generationViewModel.generateQuiz(request);
            }
        });
    }
}
```

**Adapter: `PhotoThumbnailAdapter.java`**
```java
public class PhotoThumbnailAdapter extends RecyclerView.Adapter<PhotoThumbnailAdapter.ViewHolder> {
    private List<CapturedImage> images;
    private OnImageClickListener listener;
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CapturedImage image = images.get(position);
        
        // Load thumbnail with Glide
        Glide.with(holder.itemView.getContext())
            .load(image.getThumbnail())
            .centerCrop()
            .into(holder.imageView);
        
        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(image.getId());
            }
        });
    }
    
    interface OnImageClickListener {
        void onDeleteClick(String imageId);
    }
}
```

**Layout: `item_photo_thumbnail.xml`**
```xml
<FrameLayout 120dp x 120dp>
    <ImageView (thumbnail)/>
    <ImageButton (delete icon, top-right corner)/>
</FrameLayout>
```

---

#### 4. Integration with HomeActivity

**Update `HomeActivity.java`:**
```java
// Replace existing camera button click listener
btnTakePhoto.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
    startActivity(intent);
});

// Or keep old functionality and add new button
MaterialButton btnNewCameraFlow = findViewById(R.id.btn_new_camera_flow);
btnNewCameraFlow.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
    startActivity(intent);
});
```

---

#### 5. Register Activities in AndroidManifest.xml

```xml
<application>
    <!-- Existing activities... -->
    
    <!-- Camera Activity -->
    <activity
        android:name=".ui.activities.CameraActivity"
        android:exported="false"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.LearnQuiz_FE.Fullscreen" />
    
    <!-- Photo Preview Activity -->
    <activity
        android:name=".ui.activities.PhotoPreviewActivity"
        android:exported="false"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.LearnQuiz_FE" />
    
    <!-- Quiz Generation Activity -->
    <activity
        android:name=".ui.activities.QuizGenerationActivity"
        android:exported="false"
        android:theme="@style/Theme.LearnQuiz_FE" />
</application>
```

---

## 🚀 How to Continue Development

### Step 1: Create CameraActivity
```bash
# Create Java file
# Create XML layout
# Test camera capture → save to cache
```

### Step 2: Create RegionSelectorView
```bash
# Custom View with onTouchEvent
# Draw selection rectangle with Canvas
# Test touch gestures
```

### Step 3: Create PhotoPreviewActivity
```bash
# Load image with PhotoView
# Overlay RegionSelectorView
# Test region selection + crop
```

### Step 4: Create PhotoThumbnailAdapter
```bash
# RecyclerView adapter with Glide
# Test thumbnail display + delete
```

### Step 5: Create QuizGenerationActivity
```bash
# Gallery + Settings UI
# Integrate PhotoSessionViewModel
# Integrate QuizGenerationViewModel
# Test API call with backend
```

### Step 6: Integration Testing
```bash
# Full workflow: Home → Camera → Preview → Generation → API
# Test error cases: network error, invalid data, etc.
# Test edge cases: max 10 images, empty session, etc.
```

---

## 📦 Backend API Integration

**Base URL:** Update in `ApiEndpoints.java`
```java
public static final String BASE_URL = "https://your-actual-backend.com/";
```

**Authentication:**
- If backend requires auth, user must login first
- Token stored in SharedPreferences via RetrofitClient
- Auto-added to requests via AuthInterceptor

**Test API Call:**
```java
// Example test in QuizGenerationActivity
GenerateQuizRequest request = new GenerateQuizRequest();
request.addImage("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/...");
request.setLanguage("en");
request.setQuestionCount(5);

QuizRepository repo = new QuizRepository(this);
repo.generateQuiz(request, new QuizRepository.QuizCallback() {
    @Override
    public void onSuccess(GenerateQuizResponse response) {
        Log.d("TEST", "Quiz ID: " + response.getId());
        Log.d("TEST", "Questions: " + response.getQuestionCount());
    }
    
    @Override
    public void onError(String message, int errorCode) {
        Log.e("TEST", "Error " + errorCode + ": " + message);
    }
});
```

---

## ✅ Verification Checklist

- [x] Build successful (data layer)
- [x] Dependencies installed
- [x] Models created
- [x] Network layer implemented
- [x] Repositories with error handling
- [x] ImageUtils with Base64 encoding
- [x] ViewModels with LiveData
- [x] String resources added
- [x] Permissions configured
- [ ] CameraActivity created
- [ ] PhotoPreviewActivity created
- [ ] RegionSelectorView custom view
- [ ] QuizGenerationActivity created
- [ ] PhotoThumbnailAdapter created
- [ ] Activities registered in manifest
- [ ] Backend URL configured
- [ ] Full workflow tested
- [ ] API integration verified

---

## 📞 Support & Resources

**Documentation:**
- CameraX: https://developer.android.com/training/camerax
- Retrofit: https://square.github.io/retrofit/
- Glide: https://bumptech.github.io/glide/
- PhotoView: https://github.com/Baseflow/PhotoView

**Key Files to Reference:**
- `QuizRepository.java` - API call patterns
- `ImageUtils.java` - Image processing examples
- `PhotoSessionViewModel.java` - LiveData usage
- `GenerateQuizRequest.java` - Request validation

**Build Commands:**
```powershell
# Clean build
.\gradlew clean assembleDebug

# Install on device
.\gradlew installDebug

# Run with logs
adb logcat | Select-String "LearnQuiz"
```

---

## 🎯 Summary

**Phase 1 (COMPLETE):** Full data layer architecture với:
- ✅ 15 Java classes
- ✅ Retrofit API integration
- ✅ Image processing pipeline
- ✅ ViewModels với LiveData
- ✅ Error handling cho HTTP codes
- ✅ Base64 encoding
- ✅ In-memory photo session management

**Phase 2 (TODO):** UI layer với 3 activities + 1 custom view + 1 adapter

**Estimated Time:** 4-6 hours để complete Phase 2 UI implementation

---

**Generated:** October 29, 2025  
**Project:** LearnQuiz-FE  
**Build Status:** ✅ SUCCESSFUL (38 tasks executed)
