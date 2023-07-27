package pers.zjw.daguerre;

import okhttp3.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * okhttp test
 *
 * @author zhangjw
 * @date 2022/05/25 0025 17:44
 */
@RunWith(SpringRunner.class)
public class OkhttpTest {
    private static final String BASE_URL = "http://www.baidu.com";

    private static OkHttpClient client() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
        return client;
    }

    @Test
    public void whenGetRequest_thenCorrect() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/date")
                .build();

        Call call = client().newCall(request);
        Response response = call.execute();

        Assert.assertThat(response.code(), Matchers.equalTo(200));
    }

    @Test
    public void whenAsynchronousGetRequest_thenCorrect() {
        Request request = new Request.Builder()
                .url(BASE_URL + "/date")
                .build();

        Call call = client().newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response)
                    throws IOException {
                // ...
            }

            public void onFailure(Call call, IOException e) {
                // ...
            }
        });
    }

    @Test
    public void whenGetRequestWithQueryParameter_thenCorrect()
            throws IOException {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/ex/bars").newBuilder();
        urlBuilder.addQueryParameter("id", "1");

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client().newCall(request);
        Response response = call.execute();

        Assert.assertThat(response.code(), Matchers.equalTo(200));
    }

    @Test
    public void whenSendPostRequest_thenCorrect()
            throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("username", "test")
                .add("password", "test")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/users")
                .post(formBody)
                .build();

        Call call = client().newCall(request);
        Response response = call.execute();

        Assert.assertThat(response.code(), Matchers.equalTo(200));
    }

    @Test
    public void whenUploadFile_thenCorrect() throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "file.txt",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("src/test/resources/test.txt")))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/users/upload")
                .post(requestBody)
                .build();

        Call call = client().newCall(request);
        Response response = call.execute();

        Assert.assertThat(response.code(), Matchers.equalTo(200));
    }

    @Test
    public void whenSetHeader_thenCorrect() throws IOException {
        Request request = new Request.Builder()
                .url("http://cn.bing.com")
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client().newCall(request);
        Response response = call.execute();
        response.close();
    }

    @Test
    public void whenSetDefaultHeader_thenCorrect()
            throws IOException {
        String contentType = "application/json";
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request requestWithUserAgent = originalRequest
                            .newBuilder()
                            .header("Content-Type", contentType)
                            .build();

                    return chain.proceed(requestWithUserAgent);
                })
                .build();

        Request request = new Request.Builder()
                .url("http://cn.bing.com")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        response.close();
    }
}
