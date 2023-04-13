package com.minivv.pilot.utils;


import com.minivv.pilot.model.AppSettings;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.apache.commons.collections.CollectionUtils;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.theokanning.openai.service.OpenAiService.*;

public class GPTClient {

    public static List<CompletionChoice> callChatGPT(String code, AppSettings settings) {
        try {
            Locale.setDefault(Locale.getDefault());
            if (settings.enableProxy) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(settings.proxyHost, settings.proxyPort));
                OkHttpClient client = defaultClient(settings.gptKey, Duration.ofSeconds(settings.maxWaitSeconds))
                        .newBuilder()
                        .proxy(proxy)
                        .build();
                Retrofit retrofit = defaultRetrofit(client, defaultObjectMapper());
                OpenAiApi api = retrofit.create(OpenAiApi.class);
                OpenAiService service = new OpenAiService(api);
                CompletionRequest completionRequest = CompletionRequest.builder()
                        .prompt(code)
                        .model(settings.gptModel)
                        .maxTokens(settings.gptMaxTokens)
                        .temperature(0.3)
                        .presencePenalty(0.0)
                        .frequencyPenalty(0.0)
                        .bestOf(1)
                        .stream(false)
                        .echo(false)
                        .build();
                return service.createCompletion(completionRequest).getChoices();
            } else {
                OpenAiService service = new OpenAiService(settings.gptKey);
                CompletionRequest completionRequest = CompletionRequest.builder()
                        .prompt(code)
                        .model(settings.gptModel)
                        .echo(true)
                        .build();
                return service.createCompletion(completionRequest).getChoices();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static boolean isSuccessful(List<CompletionChoice> choices) {
        return CollectionUtils.isNotEmpty(choices) && !choices.get(0).getText().isBlank();
    }

    public static String toString(List<CompletionChoice> choices) {
        if (CollectionUtils.isEmpty(choices)) {
            return "ChatGPT response is empty,please check your network or config!";
        }
        return choices.get(0).getText();
    }
}
