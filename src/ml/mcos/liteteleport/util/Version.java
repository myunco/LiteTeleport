package ml.mcos.liteteleport.util;

public class Version {
    private final int major;
    private final int minor;
    private int patch;

    public Version(String bukkitVersion) {
        // 1.x.x-R0.x-SNAPSHOT
        // YY.x.x-R0.1-SNAPSHOT
        // YY.x.x.build.71-stable
        String[] parts = bukkitVersion.replace('-', '.').split("\\.");
        this.major = Integer.parseInt(parts[0]);
        this.minor = Integer.parseInt(parts[1]);
        try {
            this.patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            this.patch = 0;
        }
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public boolean isLessThan(int minor) {
        return isLessThan(1, minor, 0);
    }

    public boolean isLessThan(int minor, int patch) {
        return isLessThan(1, minor, patch);
    }

    public boolean isLessThan(int major, int minor, int patch) {
        if (this.major != major) {
            return this.major < major;
        }
        if (this.minor != minor) {
            return this.minor < minor;
        }
        return this.patch < patch;
    }

    public boolean isLessOrEqual(int minor) {
        return isLessOrEqual(1, minor, 0);
    }

    public boolean isLessOrEqual(int minor, int patch) {
        return isLessOrEqual(1, minor, patch);
    }

    public boolean isLessOrEqual(int major, int minor, int patch) {
        if (this.major != major) {
            return this.major < major;
        }
        if (this.minor != minor) {
            return this.minor < minor;
        }
        return this.patch <= patch;
    }

    public boolean isGreaterThan(int minor) {
        return isGreaterThan(1, minor, 0);
    }

    public boolean isGreaterThan(int minor, int patch) {
        return isGreaterThan(1, minor, patch);
    }

    public boolean isGreaterThan(int major, int minor, int patch) {
        if (this.major != major) {
            return this.major > major;
        }
        if (this.minor != minor) {
            return this.minor > minor;
        }
        return this.patch > patch;
    }

    public boolean isGreaterOrEqual(int minor) {
        return isGreaterOrEqual(1, minor, 1);
    }

    public boolean isGreaterOrEqual(int minor, int patch) {
        return isGreaterOrEqual(1, minor, patch);
    }

    public boolean isGreaterOrEqual(int major, int minor, int patch) {
        if (this.major != major) {
            return this.major > major;
        }
        if (this.minor != minor) {
            return this.minor > minor;
        }
        return this.patch >= patch;
    }

    public boolean equals(int major, int minor, int patch) {
        return this.major == major && this.minor == minor && this.patch == patch;
    }

    @Override
    public String toString() {
        // return "1" + minor + patch;
        return major + "." + minor + "." + patch;
    }
}
