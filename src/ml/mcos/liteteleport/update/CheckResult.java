package ml.mcos.liteteleport.update;

public class CheckResult {

    public enum ResultType {
        SUCCESS, FAILURE
    }

    private final ResultType resultType;
    private final String latestVersion;
    private final boolean majorUpdate;
    private final int responseCode;

    public CheckResult(int responseCode, ResultType type) {
        this(null, false, responseCode, type);
    }

    public CheckResult(String latestVersion, boolean majorUpdate, int responseCode, ResultType type) {
        this.latestVersion = latestVersion;
        this.majorUpdate = majorUpdate;
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
        return latestVersion != null;
    }

    public boolean hasMajorUpdate() {
        return majorUpdate;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
