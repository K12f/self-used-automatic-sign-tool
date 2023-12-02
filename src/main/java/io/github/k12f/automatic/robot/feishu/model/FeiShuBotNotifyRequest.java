// FeiShuBot.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package io.github.k12f.automatic.robot.feishu.model;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class FeiShuBotNotifyRequest {
    @JSONField(name = "msg_type")
    private String msgType;
    private Content content;


    @Data
    @AllArgsConstructor
    public class Content {
        private String text;
    }
}

// Content.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation
