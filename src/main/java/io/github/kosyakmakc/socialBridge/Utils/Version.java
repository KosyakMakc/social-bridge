package io.github.kosyakmakc.socialBridge.Utils;

public class Version {
    private final int major;
    private final int minor;
    private final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String rawVersion) {
        var tokens = rawVersion.split("\\.");
        if (tokens.length == 0) {
            throw new RuntimeException("Bad version string - " + rawVersion);
        }

        this.major = Integer.parseInt(tokens[0]);
        this.minor = tokens.length > 1 ? Integer.parseInt(tokens[1]) : 0;
        this.patch = tokens.length > 2 ? Integer.parseInt(tokens[2]) : 0;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public boolean isCompatible(Version compabilityVersion) {
        if (getMajor() == 0) {
            // for alpha versioning minor token is MAJOR token and patch token is MINOR token :/
            return getMajor() == compabilityVersion.getMajor()
                    && getMinor() == compabilityVersion.getMinor()
                    && getPatch() >= compabilityVersion.getPatch();
        }
        else {
            // major MUST be equal, because its marker for breaking changes
            // minor MUST be greater or equal, because module can use new functionality
            // if major and minor are equal patch MUST be greater or equal
            return getMajor() == compabilityVersion.getMajor()
                    && (getMinor() == compabilityVersion.getMinor()
                        ? getPatch() >= compabilityVersion.getPatch()
                        : getMinor() > compabilityVersion.getMinor());
        }
    }

    @Override
    public String toString() {
        return Integer.toString(major) + '.' + Integer.toString(minor) + '.' + Integer.toString(patch);
    }
}
