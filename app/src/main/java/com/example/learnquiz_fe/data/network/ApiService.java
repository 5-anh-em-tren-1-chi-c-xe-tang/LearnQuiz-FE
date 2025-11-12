package com.example.learnquiz_fe.data.network;

// Import your existing models
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.IdTokenRequest;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.model.payment.response.PayOSCreatePaymentResponse;
import com.example.learnquiz_fe.data.model.payment.response.PayOSGetOrderResponse;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizRequestDTO;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizResponseDTO;

// --- Imports for NEW Feedback models ---
// You will need to create these files in your 'model' package
import com.example.learnquiz_fe.data.model.feedback.Feedback;
import com.example.learnquiz_fe.data.model.feedback.CreateFeedbackRequest;
import com.example.learnquiz_fe.data.model.feedback.UpdateFeedbackRequest;
import com.example.learnquiz_fe.data.model.feedback.QuizStats;
// ----------------------------------------

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API service interface
 * Defines all API endpoints for the application
 */
public interface ApiService {

    /**
     * Generate quiz from images
     * POST /api/quiz/generate
     * * @param request Quiz generation request with images and settings
     * @return ApiResponse containing GenerateQuizResponse
     */
    @POST(ApiEndpoints.GENERATE_QUIZ)
    Call<ApiResponse<GenerateQuizResponse>> generateQuiz(
            @Body GenerateQuizRequest request
    );

    /**
     * Generate quiz with authentication token
     * * @param token Authorization Bearer token
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
     * * @param quizId Quiz ID
     * @return ApiResponse containing GenerateQuizResponse
     */
    @GET(ApiEndpoints.GET_QUIZ_DETAIL)
    Call<ApiResponse<GenerateQuizResponse>> getQuizDetail(
            @Path("id") String quizId
    );

    /**
     * Submit quiz answers (wrapped in ApiResponse)
     * POST /api/quizhistory/submit
     * * @param request Submit quiz request with quizId and answers
     * @return ApiResponse containing SubmitQuizResponseDTO with score and results
     */
    @POST(ApiEndpoints.SUBMIT_QUIZ)
    Call<ApiResponse<SubmitQuizResponseDTO>> submitQuiz(
            @Body SubmitQuizRequestDTO request
    );

    @POST(ApiEndpoints.LOGIN_GOOGLE)
    Call<ApiResponse<AuthResponse>> loginWithGoogle(@Body IdTokenRequest idTokenRequest);

    @GET(ApiEndpoints.GET_PUBLIC_QUIZ)
    Call<ApiResponse<List<QuizResponseDTO>>> getPublicQuizzies(@Query("query") String query);

    /**
     * Payment endpoints
     */
    @GET(ApiEndpoints.GET_ORDER)
    Call<ApiResponse<PayOSGetOrderResponse>> getOrder(@Path("orderId") String orderId);

    @POST(ApiEndpoints.CREATE_PAYMENT_INTENT)
    Call<ApiResponse<PayOSCreatePaymentResponse>> createPaymentIntent(@Body Object paymentRequest);


    // ===============================================
    // ============= NEW FEEDBACK API ================
    // ===============================================

    /**
     * Create new feedback for a quiz
     * POST /api/Feedback
     */
    @POST(ApiEndpoints.FEEDBACK_BASE)
    Call<ApiResponse<Feedback>> createFeedback(
            @Header("Authorization") String token,
            @Body CreateFeedbackRequest request
    );

    /**
     * Get a specific feedback by its ID
     * GET /api/Feedback/{id}
     */
    @GET(ApiEndpoints.FEEDBACK_BY_ID)
    Call<ApiResponse<Feedback>> getFeedbackById(@Path("id") String feedbackId);

    /**
     * Get all feedback for a specific quiz
     * GET /api/Feedback/quiz/{quizId}
     */
    @GET(ApiEndpoints.FEEDBACK_BY_QUIZ)
    Call<ApiResponse<List<Feedback>>> getFeedbackByQuiz(@Path("quizId") String quizId);

    /**
     * Get all feedback submitted by the current user
     * GET /api/Feedback/my-feedback
     */
    @GET(ApiEndpoints.FEEDBACK_MY)
    Call<ApiResponse<List<Feedback>>> getMyFeedback(@Header("Authorization") String token);

    /**
     * Update an existing feedback
     * PUT /api/Feedback/{id}
     */
    @PUT(ApiEndpoints.FEEDBACK_BY_ID)
    Call<ApiResponse<Feedback>> updateFeedback(
            @Header("Authorization") String token,
            @Path("id") String feedbackId,
            @Body UpdateFeedbackRequest request
    );

    /**
     * Delete a feedback
     * DELETE /api/Feedback/{id}
     */
    @DELETE(ApiEndpoints.FEEDBACK_BY_ID)
    Call<ApiResponse<Object>> deleteFeedback(
            @Header("Authorization") String token,
            @Path("id") String feedbackId
    );

    /**
     * Get rating statistics for a quiz
     * GET /api/Feedback/quiz/{quizId}/stats
     */
    @GET(ApiEndpoints.FEEDBACK_STATS)
    Call<ApiResponse<QuizStats>> getQuizRatingStats(@Path("quizId") String quizId);
}