package MelodyPatternGenerator;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PatternGenerator {
    private double[] noteFrequencies = null;
    private int[] noteLengths = null;

    private double[] melodyBuffer = null;
    private double[] labelBuffer = null;
    private List<double[]> melodies = null;

    public PatternGenerator(double[] melodyBuffer, double[] labelBuffer, int dataNums) {
        this.melodyBuffer = melodyBuffer.clone();
        this.labelBuffer = labelBuffer;
        resize();
        generate(upDownFrequency(), dataNums);
    }

    public static Pair<double[], double[]> loadFile(String fileName, double label) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        String line = file.readLine();
        file.close();
        String[] arr = line.split(" ");
        double[] buffer = new double[arr.length], labels = new double[arr.length];
        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = MelodyDescriptor.normalize(Double.parseDouble(arr[i]));
            labels[i] = label;
        }
        return new Pair<>(buffer, labels);
    }

    public double[] get(int index) {
        return this.melodies.get(index);
    }

    public List<double[]> getList() {
        return this.melodies;
    }

    private void resize() {
        int size = this.melodyBuffer.length, begin = 0, end = size - 1;
        System.out.println(Arrays.toString(this.melodyBuffer));
        while (MelodyDescriptor.match(this.melodyBuffer[begin]) == 0.0) ++begin;
        while (MelodyDescriptor.match(this.melodyBuffer[end]) == 0.0) --end;
        double[] melody = new double[end - begin + 1];
        for (int i = begin; i <= end; ++i)
            melody[i - begin] = this.melodyBuffer[i];
        this.melodyBuffer = new double[size];
        for (int i = 0; i < melody.length; ++i)
            for (int j = Math.round((float) i * ((float) size / (float) melody.length));
                 j < Math.round((float) (i + 1) * ((float) size / (float) melody.length)); ++j)
                this.melodyBuffer[j] = melody[i];
        System.out.println("melody buffer : " + Arrays.toString(this.melodyBuffer));
        System.out.println("buffer size : " + this.melodyBuffer.length);
    }

    private List<double[]> upDownFrequency() {
        List<double[]> list = new ArrayList<>(), upList = new ArrayList<>(), downList = new ArrayList<>();
        double maxFrequency = -2.0, minFrequency = 150.0;
        for (int i = 0; i < this.melodyBuffer.length; ++i) {
            maxFrequency = maxFrequency > this.melodyBuffer[i] ? maxFrequency : this.melodyBuffer[i];
            if (this.melodyBuffer[i] >= 0.5) minFrequency = minFrequency < this.melodyBuffer[i] ? minFrequency : this.melodyBuffer[i];
        }
        list.add(this.melodyBuffer.clone());
        while (MelodyDescriptor.canPitchUp(maxFrequency)) {
            double[] buffer = (upList.isEmpty()) ? this.melodyBuffer.clone() : upList.get(upList.size() - 1).clone();
            for (int i = 0; i < buffer.length; ++i)
                buffer[i] = MelodyDescriptor.pitchUp(buffer[i]);
            maxFrequency = MelodyDescriptor.pitchUp(maxFrequency);
            upList.add(buffer);
        }
        while (MelodyDescriptor.canPitchDown(minFrequency)) {
            double[] buffer = downList.isEmpty() ? this.melodyBuffer.clone() : downList.get(downList.size() - 1).clone();
            for (int i = 0; i < buffer.length; ++i)
                buffer[i] = MelodyDescriptor.pitchDown(buffer[i]);
            minFrequency = MelodyDescriptor.pitchDown(minFrequency);
            downList.add(buffer);
        }
        list.addAll(upList);
        list.addAll(downList);
        Collections.shuffle(list);
        return list;
    }

    private void generate(List<double[]> data, int dataNums) {
        this.melodies = new ArrayList<>();
        for (int i = 0; i < data.size(); ++i) {
            List<double[]> list = generate(data.get(i));
            this.melodies.addAll(list);
            Collections.shuffle(this.melodies);
        }
        Random random = new Random();
        if (this.melodies.size() > dataNums) {
            List<double[]> temp = new ArrayList<>();
            temp.addAll(this.melodies.subList(0, dataNums));
            this.melodies.clear();
            this.melodies.addAll(temp);
        } else
            while (this.melodies.size() < dataNums) {
                int index = Math.abs(random.nextInt()) % this.melodies.size();
                double[] temp = this.melodies.get(index).clone();
                getBendRandomFrequencyBuffer(random, temp);
                this.melodies.add(temp);
            }
    }

    private List<double[]> generate(double[] buffer) {
        getNoteLengths(buffer);
        int[] ranges = getLengthsRangeBuffer();
        List<double[]> list = new ArrayList<>();
        Random random = new Random();
        calculate(random, list, new int[ranges.length], ranges, 0);
        return list;
    }

    private void getNoteLengths(double[] buffer) {
        double[] stdFreqBuffer = getStandardFreqMelody(buffer);
        List<Pair<Double, Integer>> descriptor = new ArrayList<>();
        int length = 1;
        for (int i = 1; i < stdFreqBuffer.length; ++i) {
            if (stdFreqBuffer[i - 1] == stdFreqBuffer[i]) ++length;
            else {
                descriptor.add(new Pair<>(stdFreqBuffer[i - 1], length));
                length = 1;
            }
        }
        descriptor.add(new Pair<>(stdFreqBuffer[stdFreqBuffer.length - 1], length));
        this.noteFrequencies = new double[descriptor.size()];
        this.noteLengths = new int[descriptor.size()];
        for (int i = 0; i < descriptor.size(); ++i) {
            Pair<Double, Integer> pair = descriptor.get(i);
            this.noteFrequencies[i] = pair.getKey();
            this.noteLengths[i] = pair.getValue();
        }
    }

    private double[] getStandardFreqMelody(double[] buffer) {
        for (int i = 0; i < buffer.length; ++i)
            buffer[i] = MelodyDescriptor.match(buffer[i]);
        return buffer;
    }

    private int[] getLengthsRangeBuffer() {
        int maxLength = 0;
        long max = 0;
        for (int i = 0; i < this.noteLengths.length; ++i)
            maxLength = maxLength > this.noteLengths[i] ? maxLength : this.noteLengths[i];
        int[] range = new int[this.noteLengths.length], result = null;
        for (int minLength = 1; minLength < maxLength; ++minLength) {
            long total = 1;
            for (int i = 0; i < this.noteLengths.length; ++i) {
                if (this.noteLengths[i] - minLength >= minLength) range[i] = minLength;
                else if (this.noteLengths[i] - minLength < 0) range[i] = 0;
                else range[i] = this.noteLengths[i] - minLength;
                total *= range[i] + 1;
            }
            if (max < total && total < 1000) {
                max = total;
                result = range.clone();
            }
        }
        return result;
    }

    int count = 0;

    private void calculate(Random random, List<double[]> list, int[] buffer, final int[] ranges, int pos) {
        for (int i = 0; i <= ranges[pos]; ++i) {
            buffer[pos] = -i;
            if (pos < buffer.length - 1)
                calculate(random, list, buffer, ranges, pos + 1);
            else {
                int[] lengths = buffer.clone();
                List<double[]> melody = getResizedBuffer(lengths);
                for (int j = 0; j < melody.size(); ++j) {
                    getBendRandomFrequencyBuffer(random, melody.get(j));
                    list.add(melody.get(j));
                }
            }
        }
    }

    private List<double[]> getResizedBuffer(int[] lengths) {
        int size = 0;
        for (int i = 0; i < lengths.length; ++i)
            size += lengths[i] += this.noteLengths[i];
        double[] buffer = new double[size];
        for (int i = 0, j = 1, k = 0; k < buffer.length; ++k) {
            if (j++ == lengths[i]) {
                j = 1;
                ++i;
            } else buffer[k] = this.noteFrequencies[i];
        }
        List<double[]> list = new ArrayList<>();
        for (int i = 0; i < 216 - buffer.length + 1; ++i) {
            double[] vector = new double[216];
            for (int j = 0; j < vector.length; ++j) {
                if (j >= i && j < i + buffer.length)
                    vector[j] = buffer[j - i];
                else vector[j] = 0;
            }
            list.add(vector);
        }
        return list;
    }

    private void getBendRandomFrequencyBuffer(Random random, double[] buffer) {
        for (int i = 0; i < buffer.length; ++i) {
            double rand = Math.abs(random.nextDouble()),
                    upValue = MelodyDescriptor.getUpPitchValue(buffer[i]),
                    downValue = MelodyDescriptor.getDownPitchValue(buffer[i]);
            if (upValue != 0 && downValue != 0) {
                upValue = rand % upValue;
                downValue = -(rand % downValue);
            }
            buffer[i] += random.nextBoolean() ? upValue : downValue;
        }
    }
}
