package ml.mcos.liteteleport.update;

import ml.mcos.liteteleport.LiteTeleport;

public class CheckResult {
    public static String currentVersion = LiteTeleport.plugin.getDescription().getVersion();
    public static String[] current = currentVersion.split("\\.");

    public enum ResultType {
        SUCCESS, FAILURE
    }

    private final ResultType resultType;
    private final String latestVersion;
    private final boolean majorUpdate;
    private final boolean newVersion;
    private final int responseCode;

    public CheckResult(int responseCode, ResultType type) {
        this(null, responseCode, type);
    }

    public CheckResult(String latestVersion, int responseCode, ResultType type) {
        this.latestVersion = latestVersion;
        if (currentVersion.equals(latestVersion)) {
            majorUpdate = false;
            newVersion = false;
        } else {
            String[] latest = latestVersion.split("\\.");
            if (Integer.parseInt(latest[0]) > Integer.parseInt(current[0])) {
                newVersion = true;
                majorUpdate = true;
            } else if (Integer.parseInt(latest[1]) > Integer.parseInt(current[1])) {
                newVersion = true;
                majorUpdate = true;
            } else {
                newVersion = Integer.parseInt(latest[2]) > Integer.parseInt(current[2]);
                majorUpdate = false;
            }
        }
        this.responseCode = responseCode;
        this.resultType = type;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean hasNewVersion() {
        return newVersion;
    }

    public boolean hasMajorUpdate() {
        return majorUpdate;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
