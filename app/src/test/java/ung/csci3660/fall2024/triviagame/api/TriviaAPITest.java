package ung.csci3660.fall2024.triviagame.api;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TriviaAPITest {

    private static TriviaAPI api;

    @BeforeClass
    public static void setUp() throws Exception {
        api = TriviaAPI.initializeAPI(true, null, false);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (api instanceof TriviaAPIOnline oAPI) {
            oAPI.shutdown(true);
        }
    }

    @Test
    public void A_testGetCategoriesBeforeInit() {
        assertNull(api.getCategories());
    }

    @Test
    public void B_testInitializeCategories() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<TriviaResponse<?>> future = new CompletableFuture<>();
        api.initializeCategories(new TriviaCallback<>() {
            @Override
            public void onSuccess(TriviaResponse<Map<String, Integer>> response) {
                future.complete(response);
            }

            @Override
            public void onError(TriviaResponse<Void> response, IOException e) {
                if (e != null) {
                    future.completeExceptionally(e);
                } else future.complete(response);
            }
        }, false);
        // Wait for result
        TriviaResponse<?> response = future.get(15, TimeUnit.SECONDS);
        assertEquals(TriviaResponse.Type.SUCCESS, response.type);
        assertTrue(response.data instanceof Map);
        Map<?, ?> categories = (Map<?, ?>) response.data;
        assertFalse(categories.isEmpty());
        categories.forEach((key, value) -> {
            assertTrue(key instanceof String);
            assertTrue(value instanceof Integer);
            System.out.printf("%s : %s\n", key, value);
        });
    }

    @Test
    public void C_testGetCategoriesAfterInit() {
        assertNotNull(api.getCategories());
    }

    @Test
    public void D_testGetQuestions() {
        CompletableFuture<TriviaResponse<?>> future = new CompletableFuture<>();
        api.getQuestions(new TriviaCallback<>() {

            @Override
            public void onSuccess(TriviaResponse<List<TriviaQuestion>> response) {
                future.complete(response);
            }

            @Override
            public void onError(TriviaResponse<Void> response, IOException e) {
                if (e != null) {
                    future.completeExceptionally(e);
                } else future.complete(response);
            }
        }, -1, null, null, null);
        TriviaResponse<?> response = future.join();
        assertEquals(TriviaResponse.Type.SUCCESS, response.type);
        assertTrue(response.data instanceof List);
        List<?> questions = (List<?>) response.data;
        assertFalse(questions.isEmpty());
        questions.forEach(question -> {
            assertTrue(question instanceof TriviaQuestion);
            TriviaQuestion q = (TriviaQuestion) question;
            System.out.printf("Q: %s | A: %s | D: %s | T: %s | C: %s | I: %s\n",
                    q.getQuestion(), q.getCorrectAnswer(), q.getDifficulty(),
                    q.getType(), q.getCategoryString(), Arrays.toString(q.getIncorrectAnswers()));
        });
    }
}