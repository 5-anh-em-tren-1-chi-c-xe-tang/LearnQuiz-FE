package com.example.learnquiz_fe.data.network;

import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.IdTokenRequest;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit API service interface
 * Defines all API endpoints for the application
 */
public interface ApiService {
    
    /**
     * Generate quiz from images
     * POST /api/quiz/generate
     * 
     * @param request Quiz generation request with images and settings
     * @return ApiResponse containing GenerateQuizResponse
     */
    @POST(ApiEndpoints.GENERATE_QUIZ)
    Call<ApiResponse<GenerateQuizResponse>> generateQuiz(
        @Body GenerateQuizRequest request
    );
    
    /**
     * Generate quiz with authentication token
     * 
     * @param token Authorization Bearer token
     * @param request Quiz generation request
     * @return ApiResponse containing GenerateQuizResponse
     */
    @POST(ApiEndpoints.GENERATE_QUIZ)
    Call<ApiResponse<GenerateQuizResponse>> generateQuizWithAuth(
        @Header("Authorization") String token,
        @Body GenerateQuizRequest request
    );
    
    /**
     * Get quiz detail by ID
     * GET /api/quiz/{id}
     * 
     * @param quizId Quiz ID
     * @return ApiResponse containing GenerateQuizResponse
     */
    @GET(ApiEndpoints.GET_QUIZ_DETAIL)
    Call<ApiResponse<GenerateQuizResponse>> getQuizDetail(
        @Path("id") String quizId
    );


    @POST(ApiEndpoints.LOGIN_GOOGLE)
    Call<ApiResponse<AuthResponse>> loginWithGoogle(@Body IdTokenRequest idTokenRequest);

    @GET(ApiEndpoints.GET_PUBLIC_QUIZ)
    Call<ApiResponse<List<QuizResponseDTO>>> getPublicQuizzies();
}
