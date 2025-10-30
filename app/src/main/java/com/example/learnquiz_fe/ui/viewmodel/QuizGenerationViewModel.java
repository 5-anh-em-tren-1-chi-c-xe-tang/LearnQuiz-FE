package com.example.learnquiz_fe.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.repository.QuizRepository;

/**
 * ViewModel for quiz generation process
 * Manages API state, loading, errors, and response data
 */
public class QuizGenerationViewModel extends AndroidViewModel {
    
    private final QuizRepository quizRepository;
    private final MutableLiveData<Resource<GenerateQuizResponse>> quizResultLiveData;
    
    public QuizGenerationViewModel(@NonNull Application application) {
        super(application);
        this.quizRepository = new QuizRepository(application);
        this.quizResultLiveData = new MutableLiveData<>();
    }
    
    /**
     * Get quiz generation result LiveData
     */
    public LiveData<Resource<GenerateQuizResponse>> getQuizResult() {
        return quizResultLiveData;
    }
    
    /**
     * Generate quiz from request
     */
    public void generateQuiz(GenerateQuizRequest request) {
        // Set loading state
        quizResultLiveData.setValue(Resource.loading(null));
        
        // Make API call
        quizRepository.generateQuiz(request, new QuizRepository.QuizCallback() {
            @Override
            public void onSuccess(GenerateQuizResponse response) {
                quizResultLiveData.postValue(Resource.success(response));
            }
            
            @Override
            public void onError(String message, int errorCode) {
                quizResultLiveData.postValue(Resource.error(message, errorCode, null));
            }
        });
    }
    
    /**
     * Generate quiz with authentication
     */
    public void generateQuizWithAuth(GenerateQuizRequest request, String token) {
        quizResultLiveData.setValue(Resource.loading(null));
        
        quizRepository.generateQuizWithAuth(request, token, new QuizRepository.QuizCallback() {
            @Override
            public void onSuccess(GenerateQuizResponse response) {
                quizResultLiveData.postValue(Resource.success(response));
            }
            
            @Override
            public void onError(String message, int errorCode) {
                quizResultLiveData.postValue(Resource.error(message, errorCode, null));
            }
        });
    }
    
    /**
     * Reset quiz result (clear state)
     */
    public void resetQuizResult() {
        quizResultLiveData.setValue(null);
    }
    
    /**
     * Resource class for wrapping data with status
     */
    public static class Resource<T> {
        public enum Status {
            LOADING,
            SUCCESS,
            ERROR
        }
        
        private final Status status;
        private final T data;
        private final String message;
        private final int errorCode;
        
        private Resource(Status status, T data, String message, int errorCode) {
            this.status = status;
            this.data = data;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static <T> Resource<T> loading(T data) {
            return new Resource<>(Status.LOADING, data, null, 0);
        }
        
        public static <T> Resource<T> success(T data) {
            return new Resource<>(Status.SUCCESS, data, null, 0);
        }
        
        public static <T> Resource<T> error(String message, int errorCode, T data) {
            return new Resource<>(Status.ERROR, data, message, errorCode);
        }
        
        public Status getStatus() {
            return status;
        }
        
        public T getData() {
            return data;
        }
        
        public String getMessage() {
            return message;
        }
        
        public int getErrorCode() {
            return errorCode;
        }
    }
}
