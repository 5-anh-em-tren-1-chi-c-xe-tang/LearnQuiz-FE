package com.example.learnquiz_fe.utils;

/**
 * Application-wide constants
 * 
 * Note: These are compile-time constants required by Android.
 * For runtime configuration, see AppConfig.java
 */
public class Constants {
    
    /**
     * Request codes
     */
    public static final int REQUEST_CODE_CAMERA = 1001;
    public static final int REQUEST_CODE_GALLERY = 1002;
    public static final int REQUEST_CODE_PREVIEW = 1003;
    
    /**
     * Intent extras keys
     */
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_FROM_CAMERA = "extra_from_camera";
    public static final String EXTRA_QUIZ_RESPONSE = "extra_quiz_response";
    public static final String EXTRA_IMAGE_ID = "extra_image_id";
    public static final String EXTRA_DOCUMENT_URI = "extra_document_uri";
    public static final String EXTRA_MIME_TYPE = "extra_mime_type";
    
    /**
     * Image processing settings
     */
    public static final int MAX_IMAGE_WIDTH = 1920;
    public static final int MAX_IMAGE_HEIGHT = 1920;
    public static final int JPEG_QUALITY = 85;
    public static final int THUMBNAIL_SIZE = 200;
    
    /**
     * PDF processing settings
     */
    public static final long MAX_PDF_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    public static final long PDF_ENCODING_TIMEOUT_MS = 30000; // 30 seconds
    
    /**
     * Photo session limits
     */
    public static final int MAX_IMAGES_PER_SESSION = 10;
    public static final int MIN_IMAGES_FOR_QUIZ = 1;
    
    /**
     * Quiz generation defaults
     */
    public static final String DEFAULT_LANGUAGE = "vi";
    public static final int DEFAULT_QUESTION_COUNT = 10;
    public static final int MIN_QUESTION_COUNT = 1;
    public static final int MAX_QUESTION_COUNT = 20;
    public static final int MAX_TIME_LIMIT = 300;
    
    /**
     * SharedPreferences keys
     */
    public static final String PREF_AUTH_TOKEN = "auth_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_LANGUAGE = "pref_language";
    public static final String PREF_LAST_QUESTION_COUNT = "pref_last_question_count";
    
    /**
     * Language codes
     */
    public static final String[] SUPPORTED_LANGUAGES = {"vi", "en"};
    public static final String[] LANGUAGE_NAMES = {
        "Tiếng Việt", "English"
    };
}
