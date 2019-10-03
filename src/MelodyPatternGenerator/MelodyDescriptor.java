package MelodyPatternGenerator;

public class MelodyDescriptor {
    public static double[][] freqRanges = new double[5 * 12][2];
    public static double[] frequency = new double[5 * 12];
    private static String[] chord = null;

    private static double x_min, x_max, y_min, y_max;

    public static void init() {
        String[] _chord = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        chord = new String[5 * 12];
        for (int n = 2; n <= 6; ++n)
            for (int k = 1; k <= 12; ++k) {
                frequency[(n - 2) * 12 + k - 1] = Math.pow(2, n - 1) * 55.0 *
                        Math.pow(2, (double) (k - 10) / 12.0);
                chord[(n - 2) * 12 + k - 1] = _chord[k - 1] + n;
            }
        for (int i = 0; i < frequency.length; ++i) {
            if (i == 0) freqRanges[0][0] = (61.7354 + frequency[0]) * 0.5;
            else if (i == frequency.length - 1) {
                freqRanges[frequency.length - 1][0] = freqRanges[frequency.length - 2][1] = (frequency[frequency.length - 2] + frequency[frequency.length - 1]) * 0.5;
                freqRanges[frequency.length - 1][1] = (frequency[frequency.length - 1] + 2093.0045) * 0.5;
            } else freqRanges[i - 1][1] = freqRanges[i][0] = (frequency[i - 1] + frequency[i]) * 0.5;
        }
        x_min = frequency[0]; x_max = frequency[frequency.length - 1];
        y_min = 1; y_max = 50;
        for (int i = 0; i < frequency.length; ++i) {
            frequency[i] = normalize(frequency[i]);
            freqRanges[i][0] = normalize(freqRanges[i][0]);
            freqRanges[i][1] = normalize(freqRanges[i][1]);
        }
        System.out.println("frequency initialized.");
    }

    public static double normalize(double x) {
        return normalize(x, x_min, x_max, y_min, y_max);
    }

    public static double normalize(double x, double x_min, double x_max, double y_min, double y_max) {
        if (x == 0.0) return 0.0;
        else {
            double c = (y_max - y_min) / (x_max - x_min);
            double d = y_min - (c * x_min);
            return Math.round((c * x + d) * 10000.0) / 10000.0;
        }
    }

    public static double[] normalize(double[] buffer) {
        for (int i = 0; i < buffer.length; ++i)
            buffer[i] = normalize(buffer[i]);
        return buffer;
    }

    public static double[] normalize(double[] buffer, double x_min, double x_max, double y_min, double y_max) {
        for (int i = 0; i < buffer.length; ++i)
            buffer[i] = normalize(buffer[i], x_min, x_max, y_min, y_max);
        return buffer;
    }

    public static double match(double freq) {
        for (int i = 0; i < freqRanges.length; ++i)
            if (freq >= freqRanges[i][0] && freq < freqRanges[i][1])
                return frequency[i];
        return 0.0;
    }

    public static boolean canPitchUp(double freq) {
        if (pitchUp(freq) != freq) return true;
        else return false;
    }

    public static boolean canPitchDown(double freq) {
        if (pitchDown(freq) != freq) return true;
        else return false;
    }

    public static double getUpPitchValue(double freq) {
        for (int i = 0; i < freqRanges.length; ++i)
            if (freq >= freqRanges[i][0] && freq < freqRanges[i][1]) {
                if (i != freqRanges.length - 1)
                    return frequency[i + 1] - freq;
                else return freqRanges[i][1] - freq;
            }
        return 0.0;
    }

    public static double getDownPitchValue(double freq) {
        for (int i = 0; i < freqRanges.length; ++i)
            if (freq >= freqRanges[i][0] && freq < freqRanges[i][1]) {
                if (i != 0) return freq - frequency[i - 1];
                else return freq - freqRanges[i][0];
            }
        return 0.0;
    }

    public static double pitchUp(double freq) {
        for (int i = 0; i < freqRanges.length; ++i)
            if (freq >= freqRanges[i][0] && freq < freqRanges[i][1]) {
                if (i != freqRanges.length - 1)
                    return Math.round((freq - frequency[i] + frequency[i + 1]) * 10000.0) / 10000.0;
                else return Math.round(freqRanges[i][1] * 10000.0) / 10000.0;
            }

        return freq;
    }

    public static double pitchDown(double freq) {
        for (int i = 0; i < freqRanges.length; ++i)
            if (freq >= freqRanges[i][0] && freq < freqRanges[i][1]) {
                if (i != 0) return Math.round((freq - frequency[i] + frequency[i - 1]) * 10000.0) / 10000.0;
                else return Math.round(freqRanges[i][0] * 10000.0) / 10000.0;
            }
        return freq;
    }

    public static String getChord(double freq) {
        int index = -1;
        for (int i = 0; i < freqRanges.length; ++i)
            if (freq >= freqRanges[i][0] && freq < freqRanges[i][1]) {
                index = i;
                break;
            }
        return index != -1 ? chord[index] : null;
    }
}
