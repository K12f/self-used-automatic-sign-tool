// FeiShuBot.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package io.github.k12f.automatic.robot.feishu.model;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class FeiShuBotNotifyResponse {
    private String msg;
    private long code;
    private FeiShuBotNotifyResponseData data;
    @JSONField(name = "StatusCode")
    private long statusCode;
    @JSONField(name = "StatusMessage")
    private String statusMessage;

    @Data
    public class FeiShuBotNotifyResponseData {
    }
}
