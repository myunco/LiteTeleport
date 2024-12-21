package ml.mcos.liteteleport.update;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.Language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CheckResult {
    private CheckResult.ResultType resultType;
    private final String currentVersion;
    private String downloadLink;
    private final StringBuilder updateInfo = new StringBuilder();
    private boolean majorUpdate;
    private boolean newVersion;
    private String latestVersion;
    private String errorMessage;

    public enum ResultType {
        SUCCESS, FAILURE
    }

    public CheckResult(String url, String currentVersion) {
        this.currentVersion = currentVersion;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String line = reader.readLine();
                if (line == null || !line.startsWith("ULT ")) { // 第一行必须以ULT 开头 作为标识判断
                    resultType = CheckResult.ResultType.FAILURE;
                    errorMessage = "Error Code: 1";
                } else {
                    downloadLink = line.substring(4); // ULT 后面跟下载地址
                    line = reader.readLine();
                    if (line == null || !line.startsWith("LV ")) { // 第二行必须以LV 开头
                        resultType = CheckResult.ResultType.FAILURE;
                        errorMessage = "Error Code: 2";
                        reader.close();
                        conn.disconnect();
                        return;
                    }
                    latestVersion = line.substring(3); // LV 后面跟最新版本
                    compareVersion();
                    if (newVersion) { // 如果有新版本 获取更新信息
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("v")) {
                                line = line.substring(1);
                                if (line.split(" ")[0].equals(currentVersion)) { //TODO
                                    break;
                                } else if (!majorUpdate && line.endsWith(" 重要更新")) {
                                    majorUpdate = true;
                                }
                                if (line.endsWith("SNAPSHOT") || line.endsWith("BETA")) {
                                    continue;
                                }
                            } else if (line.startsWith("//")) {
                                continue;
                            }
                            updateInfo.append(line).append('\n');
                        }
                    }
                    resultType = CheckResult.ResultType.SUCCESS;
                }
                reader.close();
                conn.disconnect();
            } else {
                resultType = CheckResult.ResultType.FAILURE;
                errorMessage = "HTTP " + code;
            }
        } catch (IOException e) {
            LiteTeleport.plugin.sendMessage(Language.updateCheckException);
            e.printStackTrace();
        }
    }

    private void compareVersion() {
        if (latestVersion != null && !currentVersion.equals(latestVersion)) {
            String[] latest = latestVersion.replace('-', '.').split("\\.");
            String[] current = currentVersion.replace('-', '.').split("\\.");
            if (Integer.parseInt(latest[0]) > Integer.parseInt(current[0])) {
                newVersion = true;
                majorUpdate = true;
                return;
            } else if (Integer.parseInt(latest[0]) == Integer.parseInt(current[0])) {
                if (Integer.parseInt(latest[1]) > Integer.parseInt(current[1])) {
                    newVersion = true;
                    majorUpdate = true;
                    return;
                } else if (Integer.parseInt(latest[1]) == Integer.parseInt(current[1])) {
                    if (Integer.parseInt(latest[2]) > Integer.parseInt(current[2])) {
                        newVersion = true;
                        majorUpdate = false;
                        return;
                    } else if (latest[2].equals(current[2]) && current.length > 3) { //主版本次版本修正号相同 但当前版本不是最终正式版 例如1.0.0-SNAPSHOT
                        newVersion = true;
                        majorUpdate = false;
                        return;
                    }
                }
            }
        }
        majorUpdate = false;
        newVersion = false;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public String getUpdateInfo() {
        return updateInfo.toString();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public boolean hasNewVersion() {
        return newVersion;
    }

    public boolean hasMajorUpdate() {
        return majorUpdate;
    }

    public String getDownloadLink() {
        return downloadLink;
    }
}
