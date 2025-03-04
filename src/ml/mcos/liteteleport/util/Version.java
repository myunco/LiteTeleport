package ml.mcos.liteteleport.util;

public class Version {
    private final int minor;
    private int patch;

    public Version(String bukkitVersion) {
        // 1.x.x-R0.x-SNAPSHOT
        String[] parts = bukkitVersion.replace('-', '.').split("\\.");
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
        return this.minor < minor;
    }

    public boolean isLessThan(int minor, int patch) {
        if (this.minor != minor) {
            return this.minor < minor;
        }
        return this.patch < patch;
    }

    public boolean isLessThanOrEqualTo(int minor) {
        return this.minor <= minor;
    }

    public boolean isLessThanOrEqualTo(int minor, int patch) {
        return this.isLessThan(minor, patch) || this.equals(minor, patch);
    }

    public boolean isGreaterThan(int minor) {
        return this.minor > minor;
    }

    public boolean isGreaterThan(int minor, int patch) {
        if (this.minor != minor) {
            return this.minor > minor;
        }
        return this.patch > patch;
    }

    public boolean isGreaterThanOrEqualTo(int minor) {
        return this.minor >= minor;
    }

    public boolean isGreaterThanOrEqualTo(int minor, int patch) {
        return this.isGreaterThan(minor, patch) || this.equals(minor, patch);
    }

    public boolean equals(int minor) {
        return this.minor == minor;
    }

    public boolean equals(int minor, int patch) {
        return this.minor == minor && this.patch == patch;
    }

    @Override
    public String toString() {
        return "1" + minor + patch;
    }
}
