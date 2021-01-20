package ua.com.expertsolution.chesva.service.sync;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.com.expertsolution.chesva.BuildConfig;
import ua.com.expertsolution.chesva.utils.SharedStorage;

import static ua.com.expertsolution.chesva.common.Consts.APP_CASH_TOKEN_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.CONNECT_SERVER_URL;
import static ua.com.expertsolution.chesva.common.Consts.CONNECT_TIMEOUT_SECONDS_RETROFIT;
import static ua.com.expertsolution.chesva.common.Consts.SERVER;
import static ua.com.expertsolution.chesva.common.Consts.TOKEN;
import static ua.com.expertsolution.chesva.common.Consts.TOKEN_HEADER;
import static ua.com.expertsolution.chesva.common.Consts.TYPE_CONNECTION;
import static ua.com.expertsolution.chesva.common.Consts.TYPE_CONNECTION_HTTP;


public class SyncServiceFactory {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .readTimeout(CONNECT_TIMEOUT_SECONDS_RETROFIT, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS_RETROFIT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true);
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    private static JsonSerializer<Date> dateJsonSerializer = new JsonSerializer<Date>() {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext
                context) {
            return src == null ? null : new JsonPrimitive(src.getTime());
        }
    };

    private static JsonDeserializer<Date> dateJsonDeserializer = new JsonDeserializer<Date>() {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            return json == null ? null : new Date(json.getAsLong());
        }
    };


    private static Retrofit.Builder getBuilder(String url) {

        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(new NullOnEmptyConverterFactory())
//                .addConverterFactory(JsonConverterFactory.create(new GsonBuilder()
//                        .registerTypeAdapter(Date.class, dateJsonSerializer)
//                        .registerTypeAdapter(Date.class, dateJsonDeserializer)
//                        .create()))
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
//                                .registerTypeAdapter(Date.class, dateJsonSerializer)
//                                .registerTypeAdapter(Date.class, dateJsonDeserializer)
                                .setLenient()
                                .create()));
    }

    public static <S> S createService(Class<S> serviceClass, Context context) {
        return buildService(serviceClass, context, 0);
    }

    public static <S> S createService(Class<S> serviceClass, Context context, int timeOut) {
        return buildService(serviceClass, context, timeOut);
    }


    private static <S> S buildService(Class<S> serviceClass, Context context, int timeOut) {

        String token = SharedStorage.getString(context, APP_CASH_TOKEN_PREFS, TOKEN, "");

        if(BuildConfig.DEBUG){
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        }else{
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        if(timeOut>0){
            httpClient.readTimeout(timeOut, TimeUnit.SECONDS)
                    .connectTimeout(timeOut, TimeUnit.SECONDS);
        }

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .method(original.method(), original.body());

            Request request = requestBuilder.build();
            return chain.proceed(request);
            });

        httpClient.addInterceptor(logging);

        OkHttpClient client = httpClient.build();

        Retrofit retrofit;

        try {
            retrofit = getBuilder(
                    SharedStorage.getString(context,
                            APP_SETTINGS_PREFS, TYPE_CONNECTION, TYPE_CONNECTION_HTTP) + "://" +
                    SharedStorage.getString(context, APP_SETTINGS_PREFS, SERVER, CONNECT_SERVER_URL)+"/")
                    .client(client)
                    .build();
        }catch (IllegalArgumentException e) {
            retrofit = getBuilder(TYPE_CONNECTION+"localhost/")
                    .client(client)
                    .build();
        }

        return retrofit.create(serviceClass);
    }

    private static class NullOnEmptyConverterFactory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException {
                    if (body.contentLength() == 0) return null;
                    return delegate.convert(body);                }
            };
        }
    }
}
