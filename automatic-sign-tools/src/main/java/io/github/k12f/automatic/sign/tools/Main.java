package io.github.k12f.automatic.sign.tools;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * <a href="https://cv2.pw/">网站签到入口</a>
 */
@Slf4j
public class Main {
    private static final String LOGIN_URL = "https://cv2.pw/auth/login";

    private static final List<String> USER_INFO = List.of(
            "会员时长%s: ",
            "剩余流量%s: ",
            "在线设备数%s: ",
            "钱包余额%s: "
    );

    public static void main(String[] args) {
        log.info(DateUtil.now() + "signing");
        if (ObjectUtil.isEmpty(args) || args.length != 2) {
            log.warn("params could not empty, params:" + Arrays.toString(args));
            return;
        }
        var email = args[0];
        var password = args[1];

        if (!Validator.isEmail(email)) {
            log.warn("email invalid :" + email);
            return;
        }
        log.info("email: " + email + "---" + "password: " + password);

        var main = new Main();
        main.sign(email, password);
    }


    void sign(String email, String password) {
        try (
                Playwright playwright = Playwright.create();
        ) {
            var launchOptions = new BrowserType.LaunchPersistentContextOptions();

            launchOptions.setHeadless(false);
            launchOptions.setAcceptDownloads(true);
            //
            var uuid = IdUtil.simpleUUID();
            var userDataDir = Paths.get("./cookie/" + uuid + "/");

            try (
                    var browserContext = playwright.chromium().launchPersistentContext(userDataDir, launchOptions);
                    var page = browserContext.newPage();
            ) {

                page.navigate(LOGIN_URL);

                var emailSelector = page.waitForSelector("#email");
                var passwordSelector = page.waitForSelector("#password");
                var btnSelector = page.waitForSelector(".login");

                emailSelector.fill(email);
                passwordSelector.fill(password);

                btnSelector.click();

                log.info("thread sleep wait init cookie");
                Thread.sleep(3000);

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
                    log.info(DateUtil.now() + "you has signed");
                    return;
                }
                var signBtnSelector = page.waitForSelector("#checkin-div");
                signBtnSelector.click();
                Thread.sleep(2000);
                // 获取当前 用户数据
                var rowSelector = page.waitForSelector(".row");
                var userRecords = rowSelector.querySelectorAll(".card-body");

                if (userRecords.size() == 4) {
                    IntStream.range(0, USER_INFO.size())
                            .forEach(i -> {
                                var formatStr = String.format(USER_INFO.get(i), userRecords.get(i));
                                log.info(formatStr);
                            });
                }

                log.info("sign success");
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
