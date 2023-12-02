package io.github.k12f.automatic.robot.feishu.service;

import com.alibaba.fastjson2.JSON;
import io.github.k12f.automatic.robot.feishu.model.FeiShuBotNotifyRequest;
import io.github.k12f.automatic.robot.feishu.model.FeiShuBotNotifyResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@Data
@AllArgsConstructor
@Slf4j
public class FeiShuNotifyRobot {

    private String hookUrl;

    public boolean send(String text) {
        try {
            var client = new OkHttpClient();

            var params = new FeiShuBotNotifyRequest();
            params.setMsgType("text");

            params.setContent(params.new Content(text));

            var reqJson = JSON.toJSONString(params);
            log.info("request data: " + reqJson);
            var reqBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), reqJson);

            var request = new Request.Builder()
                    .url(hookUrl)
                    .post(reqBody).build();

            var response = client.newCall(request).execute();

            System.out.println(response);
            log.info("response data: " + response);
            if (!response.isSuccessful()) {
                log.warn("post failed: " + response.body());
            }

            var responseBody = JSON.parseObject(response.body().bytes(), FeiShuBotNotifyResponse.class);

            if (responseBody.getCode() != 0) {
                log.warn(responseBody.getMsg());
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }
}
