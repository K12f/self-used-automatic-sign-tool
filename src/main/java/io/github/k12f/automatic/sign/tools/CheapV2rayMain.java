package io.github.k12f.automatic.sign.tools;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.github.k12f.automatic.robot.feishu.service.FeiShuNotifyRobot;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 网站签到入口
 */
@Slf4j
public class CheapV2rayMain {
    private static final String LOGIN_URL = "https://cv2.store/auth/login";
    private static final String USER_URL = "https://cv2.store/user";

    private static final List<String> USER_INFO = List.of(
            "会员时长: %s",
            "剩余流量: %s",
            "在线设备数: %s",
            "钱包余额: %s"
    );

    public static void main(String[] args) {
        log.info("signing at: " + DateUtil.now());
        if (ObjectUtil.isEmpty(args) || args.length != 3) {
            log.warn("params could not empty, params:" + Arrays.toString(args));
            return;
        }
        var email = args[0];
        var password = args[1];

        var feishuHookUrl = args[2];


        if (!Validator.isEmail(email)) {
            log.warn("email invalid :" + email);
            return;
        }

        if (!Validator.isUrl(feishuHookUrl)) {
            log.warn("webhook url invalid :" + feishuHookUrl);
            return;
        }
        log.info("email: " + email + "---" + "password: " + password);

        var feishuNotifyRobot = new FeiShuNotifyRobot(feishuHookUrl);
        var main = new CheapV2rayMain();
        main.sign(email, password, feishuNotifyRobot);
    }


    private void sign(String email, String password, FeiShuNotifyRobot feishuNotifyRobot) {
        try (
                Playwright playwright = Playwright.create()
        ) {
            var launchOptions = new BrowserType.LaunchPersistentContextOptions();

            launchOptions.setHeadless(true);
            launchOptions.setAcceptDownloads(true);
            //
            var uuid = IdUtil.simpleUUID();
            var userDataDir = Paths.get("./cookie/" + uuid + "/");

            try (
                    var browserContext = playwright.chromium().launchPersistentContext(userDataDir, launchOptions);
                    var page = browserContext.newPage()
            ) {

                page.navigate(LOGIN_URL);
                page.waitForURL(LOGIN_URL);

                var emailSelector = page.waitForSelector("#email");
                var passwordSelector = page.waitForSelector("#password");
                var btnSelector = page.waitForSelector(".login");

                emailSelector.fill(email);
                passwordSelector.fill(password);

                btnSelector.click();

                log.info("thread sleep wait init cookie");

                page.waitForURL(USER_URL);

                log.info("init end");
                var cookies = browserContext.cookies();

                var cookieStr = new StringBuilder();
                for (var cookie : cookies) {
                    cookieStr.append(cookie.name).append("=").append(cookie.value).append(";");
                }

                log.info("cookie: " + cookieStr);

                //
                var signStateText = getSignStateText(page);

                log.info("current sign state: " + signStateText);

                if (signStateText.equals("明日再来")) {
                    var text = DateUtil.now() + "you has signed";
                    log.info(text);
                    feishuNotifyRobot.send(text);
                    return;
                }
                var signBtnSelector = page.waitForSelector("#checkin-div");
                signBtnSelector.click();
                // 获取当前 用户数据
                var rowSelector = page.waitForSelector(".row");
                var userRecords = rowSelector.querySelectorAll(".card-body");

                StringBuilder formatStr = new StringBuilder();
                if (userRecords.size() == 4) {
                    for (int i = 0; i < USER_INFO.size(); i++) {
                        formatStr.append(String.format(USER_INFO.get(i), userRecords.get(i).innerText() + System.lineSeparator()));
                        log.info(formatStr.toString());
                    }
                }
                var text = "sign success";
                log.info(text);
                feishuNotifyRobot.send(text + System.lineSeparator() + formatStr);
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                FileUtil.del(userDataDir);
            }
        }
    }

    /**
     * get current sign state text
     *
     * @param page page
     * @return text
     */
    private String getSignStateText(Page page) {
        return page.waitForSelector("#checkin-div>a").innerText().trim();
    }
}
