package com.example.learnquiz_fe.data.network;

import com.example.learnquiz_fe.data.model.User;
import com.example.learnquiz_fe.data.model.auth.AuthResponse;
import com.example.learnquiz_fe.data.model.auth.GoogleAuthResponse;
import com.example.learnquiz_fe.data.model.auth.IdTokenRequest;
import com.example.learnquiz_fe.data.dtos.quiz.QuizResponseDTO;
import com.example.learnquiz_fe.data.model.feedback.CreateFeedbackRequest;
import com.example.learnquiz_fe.data.model.feedback.Feedback;
import com.example.learnquiz_fe.data.model.feedback.QuizStats;
import com.example.learnquiz_fe.data.model.feedback.UpdateFeedbackRequest;
import com.example.learnquiz_fe.data.model.payment.request.CreatePaymentRequest;
import com.example.learnquiz_fe.data.model.payment.response.PayOSCreatePaymentResponse;
import com.example.learnquiz_fe.data.model.payment.response.PayOSGetOrderResponse;
import com.example.learnquiz_fe.data.model.auth.LoginRequestDTO;
import com.example.learnquiz_fe.data.model.auth.RegisterRequestDTO;
import com.example.learnquiz_fe.data.model.quiz.ApiResponse;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizRequest;
import com.example.learnquiz_fe.data.model.quiz.GenerateQuizResponse;
import com.example.learnquiz_fe.data.model.quizhistory.QuizHistoryResponseDTO;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizRequestDTO;
import com.example.learnquiz_fe.data.model.quizhistory.SubmitQuizResponseDTO;
import com.example.learnquiz_fe.data.model.user.UserRequestDTO;

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

    // This one differs from getQuizDetail by purpose
    // Used when viewing quiz details for taking the quiz
    @GET(ApiEndpoints.GET_QUIZ_DETAIL)
    Call<ApiResponse<QuizResponseDTO>> getQuizDetailForView(
            @Path("id") String quizId
    );

    /**
     * Submit quiz answers (wrapped in ApiResponse)
     * POST /api/quizhistory/submit
     *
     * @param request Submit quiz request with quizId and answers
     * @return ApiResponse containing SubmitQuizResponseDTO with score and results
     */
    @POST(ApiEndpoints.SUBMIT_QUIZ)
    Call<ApiResponse<SubmitQuizResponseDTO>> submitQuiz(
            @Body SubmitQuizRequestDTO request
    );

    @POST(ApiEndpoints.LOGIN_GOOGLE)
    Call<ApiResponse<GoogleAuthResponse>> loginWithGoogle(@Body IdTokenRequest idTokenRequest);

    @POST(ApiEndpoints.LOGIN)
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequestDTO loginRequest);

    @POST(ApiEndpoints.REGISTER)
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequestDTO registerRequest);
    @PUT(ApiEndpoints.UPDATE_PROFILE)
    Call<ApiResponse<User>> updateProfile(@Body UserRequestDTO userRequestDTO);

    @GET(ApiEndpoints.MY_QUIZ_HISTORY)
    Call<QuizHistoryResponseDTO> getMyQuizHistory();

    @GET(ApiEndpoints.GET_PUBLIC_QUIZ)
    Call<ApiResponse<List<QuizResponseDTO>>> getPublicQuizzies(@Query("query") String query);


    @GET(ApiEndpoints.GET_MY_QUIZZES)
    Call<ApiResponse<List<QuizResponseDTO>>> getMyQuizzies();

    /**
     * Payment endpoints
     */
    @GET(ApiEndpoints.GET_ORDER)
    Call<ApiResponse<PayOSGetOrderResponse>> getOrder(@Path("orderId") int orderId);

    @POST(ApiEndpoints.CREATE_PAYMENT_INTENT)
    Call<ApiResponse<PayOSCreatePaymentResponse>> createPaymentIntent(@Body CreatePaymentRequest paymentRequest);

    @GET(ApiEndpoints.FEEDBACK_STATS)
    Call<ApiResponse<QuizStats>> getQuizRatingStats(
            @Path("quizId") String quizId
    );

    /**
     * Lấy danh sách feedback theo Quiz (Get by Quiz)
     * URL: api/Feedback/quiz/{quizId}
     */
    @GET(ApiEndpoints.FEEDBACK_BY_QUIZ)
    Call<ApiResponse<List<Feedback>>> getFeedbackByQuiz(
            @Path("quizId") String quizId
    );

    /**
     * Lấy feedback của user hiện tại (Get My Feedback)
     * URL: api/Feedback/my-feedback
     */
    @GET(ApiEndpoints.FEEDBACK_MY)
    Call<ApiResponse<List<Feedback>>> getMyFeedback(
    );

    /**
     * Tạo feedback mới (Create)
     * URL: api/Feedback
     */
    @POST(ApiEndpoints.FEEDBACK_BASE)
    Call<ApiResponse<Feedback>> createFeedback(
            @Body CreateFeedbackRequest request
    );

    /**
     * Cập nhật feedback (Update)
     * URL: api/Feedback/{id}
     * Lưu ý: ApiEndpoints.FEEDBACK_BY_ID chứa {id}, nên @Path phải là "id"
     */
    @PUT(ApiEndpoints.FEEDBACK_BY_ID)
    Call<ApiResponse<Feedback>> updateFeedback(
            @Path("id") String feedbackId,
            @Body UpdateFeedbackRequest request
    );

    /**
     * Xóa feedback (Delete)
     * URL: api/Feedback/{id}
     */
    @DELETE(ApiEndpoints.FEEDBACK_BY_ID)
    Call<ApiResponse<Object>> deleteFeedback(
            @Path("id") String feedbackId
    );
}
