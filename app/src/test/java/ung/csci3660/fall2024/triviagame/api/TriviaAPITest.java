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

/*
This is just for testing TriviaAPI somewhat manually to see if it worked or had any bugs
Should not be referenced for TriviaAPI usage as the way it's done here is synchronous rather than async
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TriviaAPITest {

    private static TriviaAPI api;

    @BeforeClass
    public static void setUp() throws Exception {
        api = TriviaAPI.initializeAPI(true, null, false);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (api instanceof APIOnline oAPI) {
            oAPI.shutdown(true);
        }
    }

    @Test
    public void A_testGetCategoriesBeforeInit() {
        assertNull(api.getCategories());
    }

    @Test
    public void B_testInitializeCategories() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Response<?>> future = new CompletableFuture<>();
        api.initializeCategories(new Callback<>() {
            @Override
            public void onSuccess(Response<Map<String, Integer>> response) {
                future.complete(response);
            }

            @Override
            public void onError(Response<Void> response, IOException e) {
                if (e != null) {
                    future.completeExceptionally(e);
                } else future.complete(response);
            }
        }, false);
        // Wait for result
        Response<?> response = future.get(15, TimeUnit.SECONDS);
        assertEquals(Response.Type.SUCCESS, response.type);
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
        CompletableFuture<Response<?>> future = new CompletableFuture<>();
        api.getQuestions(new Callback<>() {

            @Override
            public void onSuccess(Response<List<TriviaQuestion>> response) {
                future.complete(response);
            }

            @Override
            public void onError(Response<Void> response, IOException e) {
                if (e != null) {
                    future.completeExceptionally(e);
                } else future.complete(response);
            }
        }, -1, null, null, null);
        Response<?> response = future.join();
        assertEquals(Response.Type.SUCCESS, response.type);
        assertTrue(response.data instanceof List);
        List<?> questions = (List<?>) response.data;
        assertFalse(questions.isEmpty());
        questions.forEach(question -> {
            assertTrue(question instanceof TriviaQuestion);
            TriviaQuestion q = (TriviaQuestion) question;
            System.out.printf("Q: %s | A: %s | D: %s | T: %s | C: %s | I: %s\n",
                    q.question(), q.correctAnswer(), q.difficulty(),
                    q.type(), q.categoryString(), Arrays.toString(q.incorrectAnswers()));
        });
    }
}